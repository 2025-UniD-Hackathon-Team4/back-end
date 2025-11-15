package com.example.oauthsession.controller;

import com.example.oauthsession.apiPayload.ApiResponse;
import com.example.oauthsession.dto.request.CaffeineRequest;
import com.example.oauthsession.dto.response.CaffeinePeriodResponse;
import com.example.oauthsession.dto.response.CaffeineResponse;
import com.example.oauthsession.dto.response.CaffeineTodayResponse;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.repository.UserRepository;
import com.example.oauthsession.service.CaffeineService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequiredArgsConstructor
public class CaffeineController {

    private final CaffeineService caffeineService;
    private final UserRepository userRepository;

    @PostMapping("/caffeine/add")
    public ApiResponse<CaffeineResponse.AddCaffeineResponseDto> addCaffeineIntake(CaffeineRequest.AddCaffeineRequestDto request,
                                                                                  HttpSession session){
        User loginUser = (User) session.getAttribute("LOGIN_USER"); // 세션에 저장한 유저 정보
        log.info("loginUser : {}", loginUser);

        if(loginUser == null){
            loginUser = userRepository.findById(1L).orElseThrow();
        }

        CaffeineResponse.AddCaffeineResponseDto addCaffeineResponseDto = caffeineService.recordCaffeineIntake(request, loginUser);
        return ApiResponse.onSuccess(addCaffeineResponseDto);
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
}
