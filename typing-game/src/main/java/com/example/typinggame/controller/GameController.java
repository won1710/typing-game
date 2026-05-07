package com.example.typinggame.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GameController {

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String nickname, HttpSession session) {
        session.setAttribute("nickname", nickname.trim());
        return "redirect:/game";
    }

    @GetMapping("/game")
    public String gamePage(HttpSession session, Model model) {
        Object nickname = session.getAttribute("nickname");
        if (nickname == null) {
            return "redirect:/";
        }
        model.addAttribute("nickname", nickname);
        return "game";
    }
}
