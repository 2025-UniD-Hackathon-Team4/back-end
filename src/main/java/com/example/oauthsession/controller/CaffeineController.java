package com.example.oauthsession.controller;

import com.example.oauthsession.apiPayload.ApiResponse;
import com.example.oauthsession.dto.request.CaffeineRequest;
import com.example.oauthsession.dto.response.CaffeineResponse;
import com.example.oauthsession.entity.CaffeineIntakes;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.repository.UserRepository;
import com.example.oauthsession.service.CaffeineService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.UserPrincipal;

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
}
