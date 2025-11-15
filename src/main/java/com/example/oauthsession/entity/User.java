package com.example.oauthsession.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String role;

    @OneToMany(mappedBy = "user")
    private List<CaffeineIntakes> intakes = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<DaySummaries> summaries = new ArrayList<>();
}
