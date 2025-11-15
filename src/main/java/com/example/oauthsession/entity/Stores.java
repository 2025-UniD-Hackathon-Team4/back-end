package com.example.oauthsession.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "stores")
public class Stores{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeName;

    @OneToMany(mappedBy = "stores", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menuItems = new ArrayList<>();
}
