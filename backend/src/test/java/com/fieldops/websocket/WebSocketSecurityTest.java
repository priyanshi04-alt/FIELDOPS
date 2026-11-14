package com.fieldops.websocket;

import com.fieldops.auth.CustomUserDetailsService;
import com.fieldops.auth.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketSecurityTest {

    private JwtTokenProvider tokenProvider;
    private CustomUserDetailsService userDetailsService;
    private WebSocketConfig webSocketConfig;
    private ChannelInterceptor interceptor;
    private MessageChannel mockChannel;

    @BeforeEach
    void setUp() throws Exception {
        tokenProvider = new JwtTokenProvider();
        Field secretField = JwtTokenProvider.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(tokenProvider, "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");

        Field expField = JwtTokenProvider.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.set(tokenProvider, 86400000L);

        userDetailsService = new CustomUserDetailsService(null) {
            @Override
            public UserDetails loadUserByUsername(String email) {
                if ("dispatcher@fieldops.demo".equalsIgnoreCase(email)) {
                    return new User("dispatcher@fieldops.demo", "hashed",
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_DISPATCHER")));
                } else if ("customer@fieldops.demo".equalsIgnoreCase(email)) {
                    return new User("customer@fieldops.demo", "hashed",
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
                } else if ("technician@fieldops.demo".equalsIgnoreCase(email)) {
                    return new User("technician@fieldops.demo", "hashed",
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_TECHNICIAN")));
                }
                throw new UsernameNotFoundException("User not found: " + email);
            }
        };

        webSocketConfig = new WebSocketConfig(tokenProvider, userDetailsService);
        mockChannel = (message, timeout) -> true;

        // Extract the ChannelInterceptor registered by configureClientInboundChannel
        org.springframework.messaging.simp.config.ChannelRegistration registration =
                new org.springframework.messaging.simp.config.ChannelRegistration();
        webSocketConfig.configureClientInboundChannel(registration);

        Field interceptorsField = org.springframework.messaging.simp.config.ChannelRegistration.class.getDeclaredField("interceptors");
        interceptorsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ChannelInterceptor> list = (List<ChannelInterceptor>) interceptorsField.get(registration);
        assertNotNull(list);
        assertFalse(list.isEmpty());
        interceptor = list.get(0);
    }

    @Test
    @DisplayName("STOMP CONNECT without Authorization header must throw MessageDeliveryException")
    void stompConnect_MissingAuthHeader_ShouldThrowException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        MessageDeliveryException ex = assertThrows(MessageDeliveryException.class, () ->
                interceptor.preSend(message, mockChannel)
        );
        assertTrue(ex.getMessage().contains("Missing or malformed Authorization header"));
    }

    @Test
    @DisplayName("STOMP CONNECT with invalid/expired JWT must throw MessageDeliveryException")
    void stompConnect_InvalidToken_ShouldThrowException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        MessageDeliveryException ex = assertThrows(MessageDeliveryException.class, () ->
                interceptor.preSend(message, mockChannel)
        );
        assertTrue(ex.getMessage().contains("Invalid or expired JWT token"));
    }

    @Test
    @DisplayName("STOMP CONNECT with real signed JWT must authenticate principal correctly")
    void stompConnect_ValidToken_ShouldAuthenticatePrincipal() {
        String validToken = tokenProvider.generateToken("dispatcher@fieldops.demo");

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer " + validToken);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, mockChannel);
        assertNotNull(result);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertNotNull(resultAccessor.getUser());
        assertEquals("dispatcher@fieldops.demo", resultAccessor.getUser().getName());
    }

    @Test
    @DisplayName("STOMP SUBSCRIBE to /topic/operations without authentication must be rejected")
    void stompSubscribe_Unauthenticated_ShouldThrowException() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/operations");
        accessor.setSubscriptionId("sub-0");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        MessageDeliveryException ex = assertThrows(MessageDeliveryException.class, () ->
                interceptor.preSend(message, mockChannel)
        );
        assertTrue(ex.getMessage().contains("Unauthenticated subscription attempt"));
    }

    @Test
    @DisplayName("STOMP SUBSCRIBE to /topic/operations by CUSTOMER role must be rejected")
    void stompSubscribe_CustomerRole_ShouldBeRejected() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/operations");
        accessor.setSubscriptionId("sub-0");

        UserDetails customerDetails = new User("customer@fieldops.demo", "hashed",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(customerDetails, null, customerDetails.getAuthorities());
        accessor.setUser(auth);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        MessageDeliveryException ex = assertThrows(MessageDeliveryException.class, () ->
                interceptor.preSend(message, mockChannel)
        );
        assertTrue(ex.getMessage().contains("Access denied: You do not have permission to subscribe to /topic/operations"));
    }

    @Test
    @DisplayName("STOMP SUBSCRIBE to /topic/operations by DISPATCHER must be permitted")
    void stompSubscribe_DispatcherRole_ShouldBePermitted() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/operations");
        accessor.setSubscriptionId("sub-0");

        UserDetails dispatcherDetails = new User("dispatcher@fieldops.demo", "hashed",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_DISPATCHER")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(dispatcherDetails, null, dispatcherDetails.getAuthorities());
        accessor.setUser(auth);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, mockChannel);
        assertNotNull(result);
    }

    @Test
    @DisplayName("STOMP SUBSCRIBE to /topic/operations by TECHNICIAN must be permitted")
    void stompSubscribe_TechnicianRole_ShouldBePermitted() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/operations");
        accessor.setSubscriptionId("sub-0");

        UserDetails techDetails = new User("technician@fieldops.demo", "hashed",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_TECHNICIAN")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(techDetails, null, techDetails.getAuthorities());
        accessor.setUser(auth);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, mockChannel);
        assertNotNull(result);
    }
}
