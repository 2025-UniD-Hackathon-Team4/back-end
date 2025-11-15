package com.example.oauthsession.dto.response;

public record DaySummaryResponse(
        Long daySummaryId,
        int conditionScore,
        String conditionSummary
) {}

