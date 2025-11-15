package com.example.oauthsession.controller;

import com.example.oauthsession.apiPayload.ApiResponse;
import com.example.oauthsession.dto.request.CaffeineRequest;
import com.example.oauthsession.dto.response.CaffeinePeriodResponse;
import com.example.oauthsession.dto.response.CaffeineResponse;
import com.example.oauthsession.dto.response.CaffeineTodayResponse;
import com.example.oauthsession.dto.response.SleepAverageResponse;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.repository.UserRepository;
import com.example.oauthsession.service.CaffeineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequiredArgsConstructor
public class CaffeineController {

    private final CaffeineService caffeineService;
    private final UserRepository userRepository;

    @PostMapping("/caffeine/add")
    @Operation(
            summary = "카페인 섭취 기록 추가",
            description = "사용자가 특정 시간, 메뉴, 매장 정보를 포함하여 카페인 섭취 기록을 등록 후 값 반환"
    )
    public ApiResponse<CaffeineResponse.AddCaffeineResponseDto> addCaffeineIntake(@RequestBody CaffeineRequest.AddCaffeineRequestDto request,
                                                                                  HttpSession session){
        User loginUser = (User) session.getAttribute("LOGIN_USER"); // 세션에 저장한 유저 정보
        log.info("loginUser : {}", loginUser);

        if(loginUser == null){
            loginUser = userRepository.findById(1L).orElseThrow();
        }

        CaffeineResponse.AddCaffeineResponseDto addCaffeineResponseDto = caffeineService.recordCaffeineIntake(request, loginUser);
        return ApiResponse.onSuccess(addCaffeineResponseDto);
    }

    @Operation(
            summary = "날짜별 카페인 섭취량 조회",
            description = "날짜별 카페인 섭취량을 조회합니다, 날짜를 입력해주세요"
    )
    @Parameter(
            description = "조회할 날짜 (yyyy-MM-dd 형식)",
            example = "2025-11-15"
    )
    @GetMapping("/caffeine/caffeine/byDay")
    public ApiResponse<CaffeineResponse.CaffeineByDayResponseDto> getCaffeineByDay(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                                   HttpSession session){
        User user = getUser(session);
        return ApiResponse.onSuccess(caffeineService.getCaffeineByDay(user, date));
    }

    /**
     * 오늘 총 카페인 섭취량
     * GET /caffeine/today?userId=1
     */
    @GetMapping("/caffeine/today")
    public CaffeineTodayResponse getToday(@RequestParam Long userId) {
        return caffeineService.getTodayCaffeine(userId);
    }

    /**
     * 최근 7일(오늘 포함) 평균/총량
     * GET /api/caffeine/weekly?userId=1
     */
    @GetMapping("/caffeine/weekly")
    public CaffeinePeriodResponse getWeekly(@RequestParam Long userId) {
        return caffeineService.getWeeklyCaffeine(userId);
    }

    /**
     * 이번 달 평균/총량
     * GET /api/caffeine/monthly?userId=1
     */
    @GetMapping("/caffeine/monthly")
    public CaffeinePeriodResponse getMonthly(@RequestParam Long userId) {
        return caffeineService.getMonthlyCaffeine(userId);
    }

    @Operation(
            summary = "4개월간 평균 카페인량 조회",
            description = "4개월간 평균 카페인량을 조회합니다"
    )
    @GetMapping("/caffeine/fourMonths")
    public ApiResponse<List<CaffeinePeriodResponse>> getFourMonthSleep(HttpSession session){
        User user = getUser(session);
        List<CaffeinePeriodResponse> mothlyCaffeine = caffeineService.getLastFourMonthsCaffeine(user);
        return ApiResponse.onSuccess(mothlyCaffeine);
    }

    @Operation(
            summary = "4주간 평균 카페인량 조회",
            description = "4주간 평균 카페인량을 조회합니다"
    )
    @GetMapping("/caffeine/fourWeeks")
    public ApiResponse<List<CaffeinePeriodResponse>> getFourWeekSleep(HttpSession session){
        User user = getUser(session);
        List<CaffeinePeriodResponse> weeklyCaffeine = caffeineService.getLastFourWeeksCaffeine(user);
        return ApiResponse.onSuccess(weeklyCaffeine);
    }

    private User getUser(HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER"); // 세션에 저장한 유저 정보
        log.info("loginUser : {}", loginUser);

        if(loginUser == null){
            loginUser = userRepository.findById(1L).orElseThrow();
        }
        return loginUser;
    }
}
