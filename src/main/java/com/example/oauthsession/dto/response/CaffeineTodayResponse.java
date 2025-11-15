package com.example.oauthsession.dto.response;

import java.time.LocalDate;

public record CaffeineTodayResponse(
        LocalDate date,
        int totalCaffeineMg
) {}
