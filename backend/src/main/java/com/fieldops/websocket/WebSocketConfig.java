package com.fieldops.websocket;

import com.fieldops.auth.CustomUserDetailsService;
import com.fieldops.auth.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost:8080,http://localhost:8082,http://127.0.0.1:5173,http://127.0.0.1:3000,http://127.0.0.1:8080,http://127.0.0.1:8082}")
    private String allowedOrigins = "http://localhost:5173,http://localhost:3000,http://localhost:8080,http://localhost:8082,http://127.0.0.1:5173,http://127.0.0.1:3000,http://127.0.0.1:8080,http://127.0.0.1:8082";

    protected WebSocketConfig() {
        this.tokenProvider = null;
        this.userDetailsService = null;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public WebSocketConfig(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    public WebSocketConfig(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService, String allowedOrigins) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins)
                .withSockJS();

        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null) {
                    StompCommand command = accessor.getCommand();

                    // 1. Mandatory Authentication on STOMP CONNECT
                    if (StompCommand.CONNECT.equals(command)) {
                        String authHeader = accessor.getFirstNativeHeader("Authorization");
                        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                            log.warn("STOMP CONNECT rejected: missing or malformed Authorization header");
                            throw new MessageDeliveryException("Unauthorized: Missing or malformed Authorization header");
                        }

                        String token = authHeader.substring(7).trim();
                        if (!tokenProvider.validateToken(token)) {
                            log.warn("STOMP CONNECT rejected: invalid or expired JWT");
                            throw new MessageDeliveryException("Unauthorized: Invalid or expired JWT token");
                        }

                        try {
                            String username = tokenProvider.getUsernameFromJwt(token);
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            if (accessor.isMutable()) {
                                accessor.setUser(auth);
                            } else {
                                StompHeaderAccessor mutable = StompHeaderAccessor.wrap(message);
                                mutable.setUser(auth);
                                message = org.springframework.messaging.support.MessageBuilder
                                        .createMessage(message.getPayload(), mutable.getMessageHeaders());
                            }
                            log.debug("WebSocket authenticated user: {}", username);
                        } catch (Exception ex) {
                            log.warn("STOMP CONNECT rejected: could not load user details: {}", ex.getMessage());
                            throw new MessageDeliveryException("Unauthorized: User not found or inactive");
                        }
                    }

                    // 2. Subscription Authorization on STOMP SUBSCRIBE
                    if (StompCommand.SUBSCRIBE.equals(command)) {
                        Principal user = accessor.getUser();
                        if (user == null) {
                            log.warn("STOMP SUBSCRIBE rejected: unauthenticated session");
                            throw new MessageDeliveryException("Unauthorized: Unauthenticated subscription attempt");
                        }

                        String destination = accessor.getDestination();
                        if (destination != null) {
                            // Staff-only destinations: /topic/operations and /topic/alerts
                            if (destination.startsWith("/topic/operations") || destination.startsWith("/topic/alerts")) {
                                if (user instanceof UsernamePasswordAuthenticationToken authToken) {
                                    boolean isStaff = authToken.getAuthorities().stream()
                                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                                    || a.getAuthority().equals("ROLE_DISPATCHER")
                                                    || a.getAuthority().equals("ROLE_TECHNICIAN"));
                                    if (!isStaff) {
                                        log.warn("STOMP SUBSCRIBE rejected for user {} on destination {}: insufficient privileges",
                                                user.getName(), destination);
                                        throw new MessageDeliveryException("Access denied: You do not have permission to subscribe to " + destination);
                                    }
                                }
                            }
                        }
                    }

                    // 3. Message Send Protection on STOMP SEND
                    if (StompCommand.SEND.equals(command)) {
                        Principal user = accessor.getUser();
                        if (user == null) {
                            log.warn("STOMP SEND rejected: unauthenticated session");
                            throw new MessageDeliveryException("Unauthorized: Unauthenticated send attempt");
                        }
                    }
                }
                return message;
            }
        });
    }
}
