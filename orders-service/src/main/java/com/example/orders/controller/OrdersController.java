package com.example.orders.controller;

import com.example.orders.model.CartItem;
import com.example.orders.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @PostMapping("/cart")
    public void addToCart(@RequestParam String email, @RequestParam Long bookId, @RequestParam int quantity) {
        ordersService.addToCart(email, bookId, quantity);
    }

    @GetMapping("/cart")
    public List<CartItem> getCartItems(@RequestParam String email) {
        return ordersService.getCartItems(email);
    }

    @PostMapping("/checkout")
    public void checkout(@RequestParam String email) {
        ordersService.checkout(email);
    }
}