package com.fieldops.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class OperationsWebSocketController {

    @MessageMapping("/ping")
    @SendTo("/topic/operations")
    public OperationsEvent handlePing(Map<String, Object> message) {
        return OperationsEvent.builder()
                .eventType(OperationsEventType.ANOMALY_DETECTED)
                .severity("INFO")
                .message("WebSocket heartbeat OK")
                .payload(message)
                .build();
    }
}
