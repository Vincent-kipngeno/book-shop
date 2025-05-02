package com.example.orders.repository;

import com.example.orders.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByEmail(String email);
    void deleteByEmail(String email);
}