package com.example.oauthsession.controller;

import com.example.oauthsession.dto.request.DaySummarysRequest;
import com.example.oauthsession.dto.response.DaySummaryResponse;
import com.example.oauthsession.service.ConditionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DaySummarysController {
    private final ConditionService conditionService;

    @PostMapping("/api/daily-summary")
    public Mono<DaySummaryResponse> createDailySummary(@RequestBody DaySummarysRequest request,
                                                       HttpSession session) {
        return conditionService.createDailySummary(request, session);
    }
}
