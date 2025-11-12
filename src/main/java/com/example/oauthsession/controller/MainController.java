package com.example.oauthsession.controller;

import com.example.oauthsession.service.UpstageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class MainController {

    private final UpstageService upstageService;

    @GetMapping("/")
    public String mainPage(){
        return "main";
    }

    @GetMapping("/chat")
    public Mono<String> getResponse(@RequestParam String prompt, HttpSession session){
        String model = "solar-pro2";
        return upstageService.getChatResponseText(model, prompt, session);
    }
}
