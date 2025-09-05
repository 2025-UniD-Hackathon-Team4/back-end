package org.example.security.controller;

import lombok.RequiredArgsConstructor;
import org.example.security.dto.JoinDto;
import org.example.security.service.JoinService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @GetMapping("/join")
    public String join() {
        return "join";
    }

    @PostMapping("/joinProc")
    public String joinProc(JoinDto joinDto) {
        joinService.join(joinDto);
        return "redirect:/login";
    }
}
