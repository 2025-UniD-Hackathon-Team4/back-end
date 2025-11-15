package com.example.oauthsession.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class DaySummaries {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private LocalDateTime sleepStartAt;

    private LocalDateTime sleepEndAt;

    private String sleepGoal;

    private int freshness;

    private int prevDayCaffeine;

    private int prevDayIntakeCount;

    private int conditionScore;

    private String conditionSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
