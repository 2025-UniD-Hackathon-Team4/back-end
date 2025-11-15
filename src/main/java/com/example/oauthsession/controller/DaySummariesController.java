package com.example.oauthsession.controller;

import com.example.oauthsession.apiPayload.ApiResponse;
import com.example.oauthsession.dto.request.DaySummariesRequest;
import com.example.oauthsession.dto.response.DaySummaryResponse;
import com.example.oauthsession.dto.response.SleepAverageResponse;
import com.example.oauthsession.entity.DaySummaries;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.repository.UserRepository;
import com.example.oauthsession.service.ConditionService;
import com.example.oauthsession.service.DaySummariesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DaySummariesController {
    private final ConditionService conditionService;
    private final UserRepository userRepository;
    private final DaySummariesService daySummariesService;

    @PostMapping("/api/daily-summary")
    public Mono<DaySummaryResponse> createDailySummary(@RequestBody DaySummariesRequest request,
                                                       HttpSession session) {
        return conditionService.createDailySummary(request, session);
    }
    // ⭐ 프론트엔드에서 LLM 문장만 가져가기 위한 GET API
    @GetMapping("/api/daily-summary/summary")
    public Mono<String> getConditionSummary(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return conditionService.getConditionSummary(userId, date);
    }

    @Operation(
            summary = "목표 취침 시간 등록",
            description = "오늘의 목표 취침 시간을 등록합니다"
    )
    @PostMapping("/api/add/sleapGoal")
    public ApiResponse<String> addSleapGoal(@RequestBody String sleepGoal,
                                            HttpSession session){
        User user = getUser(session);
        DaySummaries daySummaries = daySummariesService.updateTodaySleepGoal(user, sleepGoal);
        return ApiResponse.onSuccess(daySummaries.getSleepGoal());
    }

    @Operation(
            summary = "목표 취침 시간 조회",
            description = "오늘의 목표 취침 시간을 조회합니다"
    )
    @Parameter(
            description = "조회할 날짜 (yyyy-MM-dd 형식)",
            example = "2025-11-15"
    )
    @GetMapping("/api/sleepGoal")
    public ApiResponse<String> getSleepGoal(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, HttpSession session){
        User user = getUser(session);

        return ApiResponse.onSuccess(daySummariesService.getSleepGoal(user,date));
    }

    @Operation(
            summary = "주간 평균 수면 시간 조회",
            description = "주간 평균 수면 시간 조회 시간을 조회합니다"
    )
    @GetMapping("/api/sleepTime/weekly")
    public ApiResponse<SleepAverageResponse> getWeeklySleep(HttpSession session){
        User user = getUser(session);
        SleepAverageResponse weeklySleep = daySummariesService.getWeeklySleep(user);
        return ApiResponse.onSuccess(weeklySleep);
    }

    @Operation(
            summary = "4주간 평균 수면 시간 조회",
            description = "4주간 평균 수면 시간 조회 시간을 조회합니다"
    )
    @GetMapping("/api/sleepTime/fourWeek")
    public ApiResponse<List<SleepAverageResponse>> getFourWeekSleep(HttpSession session){
        User user = getUser(session);
        List<SleepAverageResponse> weeklySleeps = daySummariesService.getFourWeekSleep(user);
        return ApiResponse.onSuccess(weeklySleeps);
    }

    @Operation(
            summary = "월간 평균 수면 시간 조회",
            description = "월간 평균 수면 시간 조회 시간을 조회합니다"
    )
    @GetMapping("/api/sleepTime/monthly")
    public ApiResponse<SleepAverageResponse> getMeeklySleep(HttpSession session){
        User user = getUser(session);
        SleepAverageResponse monthlySleep = daySummariesService.getMonthlySleep(user);
        return ApiResponse.onSuccess(monthlySleep);
    }

    @Operation(
            summary = "4개월간 평균 수면 시간 조회",
            description = "4개월간 평균 수면 시간 조회 시간을 조회합니다"
    )
    @GetMapping("/api/sleepTime/fourMonth")
    public ApiResponse<List<SleepAverageResponse>> getFourMonthSleep(HttpSession session){
        User user = getUser(session);
        List<SleepAverageResponse> mothlySleeps = daySummariesService.getLastFourMonthsSleep(user);
        return ApiResponse.onSuccess(mothlySleeps);
    }

    @Operation(
            summary = "날짜별 컨디션 온도 조회",
            description = "날짜별 컨디션 온도를 조회합니다, 날짜를 입력해주세요"
    )
    @Parameter(
            description = "조회할 날짜 (yyyy-MM-dd 형식)",
            example = "2025-11-15"
    )
    @GetMapping("api/conditionTemp")
    public ApiResponse<Integer> getConditionTemp(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, HttpSession session){
        User user = getUser(session);
        return ApiResponse.onSuccess(daySummariesService.getCondtionTemp(user,date));
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
