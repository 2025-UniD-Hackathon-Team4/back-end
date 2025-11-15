package com.example.oauthsession.repository;

import com.example.oauthsession.entity.DaySummaries;
import com.example.oauthsession.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DaySummariesRepository extends JpaRepository<DaySummaries, Long> {
    Optional<DaySummaries> findByUserAndDate(User user, LocalDate date);
}