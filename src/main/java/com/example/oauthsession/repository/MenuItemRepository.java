package com.example.oauthsession.repository;

import com.example.oauthsession.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Optional<MenuItem> findByStores_StoreNameAndMenuNameAndSize(String storeName, String menuName, String size);
}
