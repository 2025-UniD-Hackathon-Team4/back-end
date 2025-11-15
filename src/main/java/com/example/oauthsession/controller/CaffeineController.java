package com.example.oauthsession.controller;

import com.example.oauthsession.apiPayload.ApiResponse;
import com.example.oauthsession.dto.request.CaffeineRequest;
import com.example.oauthsession.dto.response.CaffeineResponse;
import com.example.oauthsession.entity.CaffeineIntakes;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.service.CaffeineService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.UserPrincipal;

@RestController
@RequiredArgsConstructor
public class CaffeineController {

    private final CaffeineService caffeineService;

    @PostMapping("/caffeine/add")
    public ApiResponse<CaffeineResponse.AddCaffeineResponseDto> addCaffeineIntake(CaffeineRequest.AddCaffeineRequestDto request,
                                                                                  HttpSession session){
        User loginUser = (User) session.getAttribute("LOGIN_USER"); // 세션에 저장한 유저 정보
        CaffeineResponse.AddCaffeineResponseDto addCaffeineResponseDto = caffeineService.recordCaffeineIntake(request, loginUser);
        return ApiResponse.onSuccess(addCaffeineResponseDto);
    }
}
