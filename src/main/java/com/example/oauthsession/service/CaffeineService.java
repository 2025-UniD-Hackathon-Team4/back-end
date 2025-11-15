package com.example.oauthsession.service;

import com.example.oauthsession.dto.request.CaffeineRequest;
import com.example.oauthsession.dto.response.CaffeineResponse;
import com.example.oauthsession.entity.CaffeineIntakes;
import com.example.oauthsession.entity.DaySummaries;
import com.example.oauthsession.entity.MenuItem;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.repository.CaffeineIntakesRepository;
import com.example.oauthsession.repository.MenuItemRepository;
import com.example.oauthsession.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional
@RequiredArgsConstructor
public class CaffeineService {
    private final CaffeineIntakesRepository caffeineReposiory;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public CaffeineResponse.AddCaffeineResponseDto recordCaffeineIntake(CaffeineRequest.AddCaffeineRequestDto request,
                                                User user){

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

}
