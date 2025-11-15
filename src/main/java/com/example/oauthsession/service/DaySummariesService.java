package com.example.oauthsession.service;

import com.example.oauthsession.dto.response.SleepAverageResponse;
import com.example.oauthsession.entity.DaySummaries;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.repository.DaySummariesRepository;
import com.example.oauthsession.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;



@Service
@Transactional
@RequiredArgsConstructor
public class DaySummariesService {

    private final DaySummariesRepository daySummariesRepository;
    private final UserRepository userRepository;

    public DaySummaries updateTodaySleepGoal(User user, String sleepGoal) {

        LocalDate today = LocalDate.now();

        // 오늘 날짜 데이터 조회
        DaySummaries todaySummary = daySummariesRepository
                .findByUserAndDate(user, today)
                .orElseGet(() -> {
                    // 없으면 생성
                    DaySummaries newSummary = new DaySummaries();
                    newSummary.setUser(user);
                    newSummary.setDate(today);
                    return newSummary;
                });

        // 🔥 sleepGoal만 수정
        todaySummary.setSleepGoal(sleepGoal);

        // 저장 후 반환
        return daySummariesRepository.save(todaySummary);
    }

    public String getSleepGoal(User user) {

        LocalDate today = LocalDate.now();

        // 오늘 날짜 데이터 조회
        DaySummaries todaySummary = daySummariesRepository
                .findByUserAndDate(user, today)
                .orElseThrow();
        return todaySummary.getSleepGoal();
    }


    /**
     * 월간 평균 (이번 달)
     */
    public SleepAverageResponse getMonthlySleep(User user) {


        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate firstDayNextMonth = firstDay.plusMonths(1);
        LocalDate lastDay = firstDayNextMonth.minusDays(1);

        List<DaySummaries> list =
                daySummariesRepository.findByUserAndDateBetween(user, firstDay, lastDay);

        long totalMinutes = list.stream()
                .mapToLong(this::getSleepMinutes)
                .sum();

        long daysWithData = list.size(); // 실제 기록이 있는 날 수
        double avgHours = daysWithData > 0 ? (totalMinutes / 60.0) / daysWithData : 0.0;
        String formatted = formatHours(avgHours);

        return new SleepAverageResponse(formatted, firstDay, lastDay);
    }

    /**
     * 주간 평균 (최근 7일)
     */
    public SleepAverageResponse getWeeklySleep(User user) {

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);   // 7일
        LocalDate endDate = today;

        List<DaySummaries> list =
                daySummariesRepository.findByUserAndDateBetween(user, startDate, endDate);

        long totalMinutes = list.stream()
                .mapToLong(this::getSleepMinutes)
                .sum();

        long daysWithData = list.size(); // 실제 기록이 있는 날 수
        double avgHours = daysWithData > 0 ? (totalMinutes / 60.0) / daysWithData : 0.0;
        String formatted = formatHours(avgHours);

        return new SleepAverageResponse(formatted, startDate, endDate);
    }

    private long getSleepMinutes(DaySummaries s) {
        if (s.getSleepStartAt() == null || s.getSleepEndAt() == null) return 0;
        return Duration.between(s.getSleepStartAt(), s.getSleepEndAt()).toMinutes();
    }

    private String formatHours(double hoursDecimal) {
        int hours = (int) Math.floor(hoursDecimal);
        int minutes = (int) Math.round((hoursDecimal - hours) * 60);
        return hours + "시간 " + minutes + "분";
    }

}
