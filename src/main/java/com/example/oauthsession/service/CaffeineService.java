package com.example.oauthsession.service;

import com.example.oauthsession.dto.request.CaffeineRequest;
import com.example.oauthsession.dto.response.CaffeinePeriodResponse;
import com.example.oauthsession.dto.response.CaffeineResponse;
import com.example.oauthsession.dto.response.CaffeineTodayResponse;
import com.example.oauthsession.entity.CaffeineIntakes;
import com.example.oauthsession.entity.MenuItem;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.repository.CaffeineIntakesRepository;
import com.example.oauthsession.repository.MenuItemRepository;
import com.example.oauthsession.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CaffeineService {
    private final CaffeineIntakesRepository caffeineReposiory;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public CaffeineResponse.AddCaffeineResponseDto recordCaffeineIntake(CaffeineRequest.AddCaffeineRequestDto request,
                                                User user){

        log.info("request : size={}, dateTime={}, menuName={}, storeName={}",
                request.getSize(),
                request.getDateTime(),
                request.getMenuName(),
                request.getStoreName()
        );

        MenuItem menuItem = menuItemRepository
                .findByStores_StoreNameAndMenuNameAndSize(
                        request.getStoreName(),
                        request.getMenuName(),
                        request.getSize()
                ).orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 없습니다."));

        // DB에 저장
        CaffeineIntakes intake = CaffeineIntakes.builder()
                .user(user)
                .menuItem(menuItem)
                .dateTime(request.getDateTime())
                .caffeineMg(menuItem.getCaffeineMg())
                .build();

        caffeineReposiory.save(intake);

        // 응답 DTO 생성
        return CaffeineResponse.AddCaffeineResponseDto.builder()
                .dateTime(request.getDateTime())
                .storeName(request.getStoreName())
                .size(request.getSize())
                .menuName(request.getMenuName())
                .caffeineMg(menuItem.getCaffeineMg())
                .build();
    }

    private final CaffeineIntakesRepository caffeineIntakesRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    /**
     * 오늘 하루 총 카페인 섭취량
     */
    public CaffeineTodayResponse getTodayCaffeine(Long userId) {
        User user = getUserOrThrow(userId);

        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        int total = caffeineIntakesRepository
                .sumCaffeineMgByUserAndPeriod(user, start, end);

        return new CaffeineTodayResponse(today, total);
    }

    /**
     * 최근 7일(오늘 포함) 기준 주간 통계
     * 예) 오늘이 15일이면 9~15일 데이터 기준
     */
    public CaffeinePeriodResponse getWeeklyCaffeine(Long userId) {
        User user = getUserOrThrow(userId);

        LocalDate today = LocalDate.now(KST);
        LocalDate startDate = today.minusDays(6);   // 7일 치 (today 포함)
        LocalDate endDate = today;

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        int total = caffeineIntakesRepository
                .sumCaffeineMgByUserAndPeriod(user, start, end);

        long days = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)); // 7일
        double avgPerDay = days > 0 ? (double) total / days : 0.0;

        return new CaffeinePeriodResponse(startDate, endDate, total, avgPerDay);
    }

    /**
     * 이번 달 기준 월간 통계
     * 예) 11월이면 11/1 ~ 11/30 or 11/31 까지
     */
    public CaffeinePeriodResponse getMonthlyCaffeine(Long userId) {
        User user = getUserOrThrow(userId);

        LocalDate today = LocalDate.now(KST);
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate firstDayNextMonth = firstDay.plusMonths(1);
        LocalDate lastDay = firstDayNextMonth.minusDays(1);

        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = firstDayNextMonth.atStartOfDay();

        int total = caffeineIntakesRepository
                .sumCaffeineMgByUserAndPeriod(user, start, end);

        long days = ChronoUnit.DAYS.between(firstDay, firstDayNextMonth); // 이번 달 날짜 수
        double avgPerDay = days > 0 ? (double) total / days : 0.0;

        return new CaffeinePeriodResponse(firstDay, lastDay, total, avgPerDay);
    }

    public CaffeineResponse.CaffeineByDayResponseDto getCaffeineByDay(User user, LocalDate date) {
        LocalDate today = LocalDate.now(KST);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<CaffeineIntakes> intakes = caffeineIntakesRepository.findAllByUserAndDateTimeBetween(user, startOfDay, endOfDay);

        List<CaffeineResponse.AddCaffeineResponseDto> caffeineDtos = intakes.stream()
                .map(intake -> {
                    // RDS UTC 기준 LocalDateTime을 KST로 변환
                    LocalDateTime kstDateTime = intake.getDateTime().plusHours(9);

                    return CaffeineResponse.AddCaffeineResponseDto.builder()
                            .dateTime(kstDateTime)
                            .storeName(intake.getMenuItem().getStores().getStoreName())
                            .menuName(intake.getMenuItem().getMenuName())
                            .size(intake.getMenuItem().getSize())
                            .caffeineMg(intake.getCaffeineMg())
                            .build();
                })
                .collect(Collectors.toList());

        int totalCaffeine = caffeineDtos.stream()
                .mapToInt(CaffeineResponse.AddCaffeineResponseDto::getCaffeineMg)
                .sum();

        return CaffeineResponse.CaffeineByDayResponseDto.builder()
                .caffeineIntakes(caffeineDtos)
                .totalCaffeineMg(totalCaffeine)
                .build();
    }
}
