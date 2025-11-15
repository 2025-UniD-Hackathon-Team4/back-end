package com.example.oauthsession.repository;

import com.example.oauthsession.entity.Uuid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UuidRepository extends JpaRepository<Uuid, Long> {
}
