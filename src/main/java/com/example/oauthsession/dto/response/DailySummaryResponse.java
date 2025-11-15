package com.example.oauthsession.dto.response;

public record DailySummaryResponse(
        Long daySummaryId,
        int conditionScore,
        String conditionSummary
) {}

