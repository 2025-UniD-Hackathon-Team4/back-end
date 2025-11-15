package com.example.oauthsession.dto.response;

import java.time.LocalDate;

public record CaffeinePeriodResponse(
        LocalDate startDate,
        LocalDate endDate,      // endDate는 "마지막 날짜" (포함) 기준으로 보내줄게
        int totalCaffeineMg,
        double averagePerDayMg  // 기간 동안 하루 평균 섭취량
) {}
