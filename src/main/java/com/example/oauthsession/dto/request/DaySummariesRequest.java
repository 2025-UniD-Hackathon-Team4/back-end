package com.example.oauthsession.dto.request;

import java.time.LocalDateTime;

public record DaySummariesRequest(
        Long userId,
        LocalDateTime sleepStartAt,
        LocalDateTime sleepEndAt,
        int freshness
) {}
