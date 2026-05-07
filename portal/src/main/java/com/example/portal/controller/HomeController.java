package com.example.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        List<Map<String, String>> applications = List.of(
                Map.of("name", "Slack Scheduler", "description", "매일 오전 7시 30분에 Slack 메시지 발송", "url", "/slack-scheduler/"),
                Map.of("name", "Typing Game", "description", "닉네임으로 입장한 뒤 20초 동안 입력 순위 측정", "url", "/typing-game/")
        );
        model.addAttribute("applications", applications);
        return "index";
    }
}
