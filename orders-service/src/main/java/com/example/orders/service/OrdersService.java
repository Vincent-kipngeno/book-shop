package com.example.orders.service;

import com.example.orders.model.CartItem;
import com.example.orders.model.Order;
import com.example.orders.repository.CartItemRepository;
import com.example.orders.repository.OrderRepository;
import com.example.rabbitmq.RabbitMQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrdersService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RabbitMQProducer rabbitMQProducer;

    public void addToCart(String email, Long bookId, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setEmail(email);
        cartItem.setBookId(bookId);
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    public List<CartItem> getCartItems(String email) {
        return cartItemRepository.findByEmail(email);
    }

    @Transactional
    public void checkout(String email) {
        List<CartItem> cartItems = cartItemRepository.findByEmail(email);
        List<Long> bookIds = cartItems.stream().map(CartItem::getBookId).collect(Collectors.toList());

        Order order = new Order();
        order.setEmail(email);
        order.setBookIds(bookIds);
        orderRepository.save(order);

        cartItemRepository.deleteByEmail(email);

        String message = "Order placed successfully for user: " + email;
        rabbitMQProducer.sendMessage(message);
    }
}