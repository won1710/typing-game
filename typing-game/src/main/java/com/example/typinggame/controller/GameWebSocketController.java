package com.example.typinggame.controller;

import com.example.typinggame.model.GameMessage;
import com.example.typinggame.service.GameService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class GameWebSocketController {

    private final SimpMessagingTemplate messaging;
    private final GameService gameService;

    public GameWebSocketController(SimpMessagingTemplate messaging, GameService gameService) {
        this.messaging = messaging;
        this.gameService = gameService;
    }

    @MessageMapping("/join")
    public void join(@Payload GameMessage message, SimpMessageHeaderAccessor accessor) {
        String nickname = message.getNickname();
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs != null) attrs.put("nickname", nickname);
        gameService.addUser(nickname);

        messaging.convertAndSend("/topic/chat",
                new GameMessage("SYSTEM",
                        "[" + nickname + "] 님이 입장하셨습니다.",
                        gameService.getUserCount()));
    }

    @MessageMapping("/submit")
    public void submit(@Payload GameMessage message) {
        gameService.submitAnswer(message.getNickname(), message.getText());
    }
}
