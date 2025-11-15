package com.example.oauthsession.repository;

import com.example.oauthsession.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findByUsername(String username);
    public Optional<User> findByEmail(String email);
}
