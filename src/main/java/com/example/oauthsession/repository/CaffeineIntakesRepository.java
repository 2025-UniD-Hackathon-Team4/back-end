package com.example.oauthsession.repository;


import com.example.oauthsession.entity.CaffeineIntakes;
import com.example.oauthsession.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CaffeineIntakesRepository extends JpaRepository<CaffeineIntakes, Long> {

    // 특정 유저의 기간 내 섭취 기록
    List<CaffeineIntakes> findByUserAndDateTimeBetween(User user,
                                                       LocalDateTime start,
                                                       LocalDateTime end);

    @Query("""
           select coalesce(sum(c.caffeineMg), 0)
           from CaffeineIntakes c
           where c.user = :user
             and c.dateTime >= :start
             and c.dateTime < :end
           """)
    Integer sumCaffeineMgByUserAndPeriod(@Param("user") User user,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    List<CaffeineIntakes> findAllByUserAndDateTimeBetween(User user, LocalDateTime start, LocalDateTime end);

}
