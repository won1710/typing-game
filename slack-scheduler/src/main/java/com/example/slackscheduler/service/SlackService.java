package com.example.slackscheduler.service;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SlackService {
    private static final Logger logger = LoggerFactory.getLogger(SlackService.class);

    private final String botToken;
    private final String channel;

    public SlackService(
            @Value("${slack.bot-token}") String botToken,
            @Value("${slack.channel}") String channel) {
        this.botToken = botToken;
        this.channel = channel;
    }

    public void sendMessage(String message) {
        logger.info("Slack 메시지를 전송합니다: {}", message);
        try {
            MethodsClient methods = Slack.getInstance().methods(botToken);
            ChatPostMessageResponse response = methods.chatPostMessage(r -> r
                    .channel(channel)
                    .text(message));
            if (!response.isOk()) {
                logger.error("Slack 메시지 전송 실패: {}", response.getError());
            }
        } catch (Exception ex) {
            logger.error("Slack 메시지 전송 실패", ex);
        }
    }
}
