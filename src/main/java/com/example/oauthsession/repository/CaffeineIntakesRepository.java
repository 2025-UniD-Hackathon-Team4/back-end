package com.example.oauthsession.repository;


import com.example.oauthsession.entity.CaffeineIntakes;
import com.example.oauthsession.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CaffeineIntakesRepository extends JpaRepository<CaffeineIntakes, Long> {

    // 특정 유저의 기간 내 섭취 기록
    List<CaffeineIntakes> findByUserAndDateTimeBetween(User user,
                                                       LocalDateTime start,
                                                       LocalDateTime end);
}
