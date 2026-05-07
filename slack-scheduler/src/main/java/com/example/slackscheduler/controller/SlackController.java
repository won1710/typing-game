package com.example.slackscheduler.controller;

import com.example.slackscheduler.service.SlackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SlackController {

    private final SlackService slackService;

    public SlackController(SlackService slackService) {
        this.slackService = slackService;
    }

    @GetMapping("/send-test")
    public ResponseEntity<String> sendTestMessage() {
        slackService.sendMessage("[AI가 전달합니다.]\nㅋㅍㅇ!~\nhttps://myc2102-hue.github.io/omock/coffee.html");
        return ResponseEntity.ok("테스트 메시지 전송 요청을 보냈습니다.");
    }
}
