package com.example.oauthsession.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class MenuItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String menuName;

    private String size;

    private Integer price;

    private Integer caffeine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stores_id")
    private Stores stores;

    @OneToMany(mappedBy = "menuItem")
    private List<CaffeineIntakes> intakes = new ArrayList<>();
}
