package com.example.oauthsession.service;

import com.example.oauthsession.dto.response.SleepAverageResponse;
import com.example.oauthsession.entity.DaySummaries;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.repository.DaySummariesRepository;
import com.example.oauthsession.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DaySummariesService {

    private final DaySummariesRepository daySummariesRepository;
    private final UserRepository userRepository;

    public DaySummaries updateTodaySleepGoal(User user, String sleepGoal) {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        log.info("time:{}",today);

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

    public String getSleepGoal(User user, LocalDate date) {

        // 오늘 날짜 데이터 조회
        DaySummaries todaySummary = daySummariesRepository
                .findByUserAndDate(user, date)
                .orElseThrow();
        return todaySummary.getSleepGoal();
    }

    public Integer getCondtionTemp(User user, LocalDate date) {
        DaySummaries daySummaries = daySummariesRepository
                .findByUserAndDate(user, date)
                .orElseThrow();
        return daySummaries.getConditionScore();
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

    public List<SleepAverageResponse> getLastFourMonthsSleep(User user) {

        List<SleepAverageResponse> fourMonthsData = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul")); // 현재 시간 기준

        // 현재 달(i=0)부터 4개월 전(i=3)까지 반복
        for (int i = 0; i < 4; i++) {

            // 해당 월의 첫째 날과 마지막 날 계산
            LocalDate targetMonth = today.minusMonths(i);
            LocalDate firstDay = targetMonth.withDayOfMonth(1);

            // 다음 달 1일에서 하루를 빼서 마지막 날을 구함
            LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);

            // 데이터베이스에서 해당 월의 데이터 조회
            List<DaySummaries> list =
                    daySummariesRepository.findByUserAndDateBetween(user, firstDay, lastDay);

            // 😴 수면 시간 평균 계산
            long totalMinutes = list.stream()
                    .mapToLong(this::getSleepMinutes)
                    .sum();

            long daysWithData = list.size(); // 실제 기록이 있는 날 수
            double avgHours = daysWithData > 0 ? (totalMinutes / 60.0) / daysWithData : 0.0;
            String formattedAvgSleep = formatHours(avgHours);

            SleepAverageResponse response = new SleepAverageResponse(
                    formattedAvgSleep,
                    firstDay,
                    lastDay
            );

            // 가장 최근 월(i=0)이 리스트의 가장 앞에 오도록 추가
            fourMonthsData.add(i, response);
        }

        // [4개월 전, 3개월 전, 2개월 전, 이번 달] 순서로 정렬되어 반환됩니다.
        return fourMonthsData;
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

    public List<SleepAverageResponse> getFourWeekSleep(User user) {
        List<SleepAverageResponse> fourWeeksData = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        for (int i = 0; i < 4; i++) {
            LocalDate endDate = today.minusDays(i * 7);
            LocalDate startDate = endDate.minusDays(6);

            List<DaySummaries> weekList =
                    daySummariesRepository.findByUserAndDateBetween(user, startDate, endDate);

            long totalMinutes = weekList.stream()
                    .mapToLong(this::getSleepMinutes)
                    .sum();

            long daysWithData = weekList.size();
            double avgHours = daysWithData > 0 ? (totalMinutes / 60.0) / daysWithData : 0.0;
            String formattedAvgSleep = formatHours(avgHours);

            SleepAverageResponse response = new SleepAverageResponse(
                    formattedAvgSleep,
                    startDate,
                    endDate
            );
            fourWeeksData.add(i, response);
        }
        return fourWeeksData;
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
