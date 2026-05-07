package com.example.typinggame.config;

import com.example.typinggame.model.GameMessage;
import com.example.typinggame.service.GameService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, GameService gameService) {
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String nickname = (String) accessor.getSessionAttributes().get("nickname");
        if (nickname != null) {
            gameService.removeUser(nickname);
            GameMessage msg = new GameMessage("SYSTEM",
                    "[" + nickname + "] 님이 퇴장하셨습니다.", gameService.getUserCount());
            messagingTemplate.convertAndSend("/topic/chat", msg);
        }
    }
}
