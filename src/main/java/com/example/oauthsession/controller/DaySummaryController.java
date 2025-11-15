package com.example.oauthsession.controller;

import com.example.oauthsession.dto.request.DailySummaryRequest;
import com.example.oauthsession.dto.response.DailySummaryResponse;
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
public class DaySummaryController {
    private final ConditionService conditionService;

    @PostMapping("/api/daily-summary")
    public Mono<DailySummaryResponse> createDailySummary(@RequestBody DailySummaryRequest request,
                                                         HttpSession session) {
        return conditionService.createDailySummary(request, session);
    }
}
