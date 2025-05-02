To add the new features to your bookshop microservices application, we need to make several modifications and additions. Here's a comprehensive guide for implementing the `orders-service`, `notification-service`, and `rabbitmq-client`.

### Step 1: Update Parent POM

We'll add the new modules to the parent `pom.xml`.

```xml
<modules>
    <module>api-gateway</module>
    <module>book-service</module>
    <module>eureka-server</module>
    <module>orders-service</module> <!-- Added -->
    <module>notification-service</module> <!-- Added -->
    <module>rabbitmq-client</module> <!-- Added -->
</modules>
```

### Step 2: Create RabbitMQ Client Module

Create a new module named `rabbitmq-client`.

#### `rabbitmq-client/pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>book_shop</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>rabbitmq-client</artifactId>
</project>
```

#### `rabbitmq-client/src/main/java/com/example/rabbitmq/RabbitMQConfig.java`

```java
package com.example.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for RabbitMQ settings
 */
@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "notificationQueue";

    /**
     * Creates a durable queue for notification messages
     * @return Queue instance
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }
}
```

#### `rabbitmq-client/src/main/java/com/example/rabbitmq/RabbitMQProducer.java`

```java
package com.example.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for sending messages to RabbitMQ queue
 */
@Service
public class RabbitMQProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Sends a message to the notification queue
     * @param message The message to send
     */
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
    }
}
```

### Step 3: Create Orders Service

Create a new module named `orders-service`.

#### `orders-service/pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>book_shop</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>orders-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>rabbitmq-client</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
```

#### `orders-service/src/main/resources/application.yml`

```yaml
server:
  port: 8083

spring:
  application:
    name: orders-service
  datasource:
    url: jdbc:postgresql://localhost:5432/bookshop
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false
  rabbitmq:
    host: localhost
    port: 5672

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

#### `orders-service/src/main/java/com/example/orders/OrdersServiceApplication.java`

```java
package com.example.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the Orders Service
 * Scans both the orders and rabbitmq packages for components
 */
@SpringBootApplication(
        scanBasePackages = {
                "com.example.rabbitmq",
                "com.example.orders"
        }
)
@EnableDiscoveryClient
public class OrdersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersServiceApplication.class, args);
    }
}
```

#### `orders-service/src/main/java/com/example/orders/model/CartItem.java`

```java
package com.example.orders.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity representing an item in a user's shopping cart
 */
@Entity
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private Long bookId;
    private int quantity;
}
```

#### `orders-service/src/main/java/com/example/orders/model/Order.java`

```java
package com.example.orders.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * Entity representing a customer order
 */
@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @ElementCollection
    private List<Long> bookIds;
}
```

#### `orders-service/src/main/java/com/example/orders/repository/CartItemRepository.java`

```java
package com.example.orders.repository;

import com.example.orders.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for managing cart items in the database
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Finds all cart items for a specific user
     * @param email User identifier
     * @return List of cart items
     */
    List<CartItem> findByEmail(String email);

    /**
     * Removes all cart items for a specific user
     * @param email User identifier
     */
    void deleteByEmail(String email);
}
```

#### `orders-service/src/main/java/com/example/orders/repository/OrderRepository.java`

```java
package com.example.orders.repository;

import com.example.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for managing orders in the database
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
```

#### `orders-service/src/main/java/com/example/orders/service/OrdersService.java`

```java
package com.example.orders.service;

import com.example.orders.model.CartItem;
import com.example.orders.model.Order;
import com.example.orders.repository.CartItemRepository;
import com.example.orders.repository.OrderRepository;
import com.example.rabbitmq.RabbitMQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * Adds a book to the user's cart
     *
     * @param email User identifier
     * @param bookId Book to add
     * @param quantity Number of copies
     */
    public void addToCart(String email, Long bookId, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setEmail(email);
        cartItem.setBookId(bookId);
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    /**
     * Retrieves all items in the user's cart
     *
     * @param email User identifier
     * @return List of cart items
     */
    public List<CartItem> getCartItems(String email) {
        return cartItemRepository.findByEmail(email);
    }

    /**
     * Creates an order from cart items and sends notification
     *
     * @param email User identifier
     */
    @Transactional
    public void checkout(String email) {
        List<CartItem> cartItems = cartItemRepository.findByEmail(email);
        List<Long> bookIds = cartItems.stream().map(CartItem::getBookId).collect(Collectors.toList());

        // Create and save the order
        Order order = new Order();
        order.setEmail(email);
        order.setBookIds(bookIds);
        orderRepository.save(order);

        // Clear the cart
        cartItemRepository.deleteByEmail(email);

        // Send notification via RabbitMQ
        String message = "Order placed successfully for user: " + email;
        rabbitMQProducer.sendMessage(message);
    }
}
```

#### `orders-service/src/main/java/com/example/orders/controller/OrdersController.java`

```java
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

    /**
     * Endpoint to add a book to cart
     *
     * @param email User identifier
     * @param bookId Book to add
     * @param quantity Number of copies
     */
    @PostMapping("/cart")
    public void addToCart(@RequestParam String email, @RequestParam Long bookId, @RequestParam int quantity) {
        ordersService.addToCart(email, bookId, quantity);
    }

    /**
     * Endpoint to get all items in a user's cart
     *
     * @param email User identifier
     * @return List of cart items
     */
    @GetMapping("/cart")
    public List<CartItem> getCartItems(@RequestParam String email) {
        return ordersService.getCartItems(email);
    }

    /**
     * Endpoint to convert cart to an order
     *
     * @param email User identifier
     */
    @PostMapping("/checkout")
    public void checkout(@RequestParam String email) {
        ordersService.checkout(email);
    }
}
```

### Step 4: Create Notification Service

Create a new module named `notification-service`.

#### `notification-service/pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>book_shop</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>notification-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>rabbitmq-client</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
```

#### `notification-service/src/main/resources/application.yml`

```yaml
server:
  port: 8084

spring:
  application:
    name: notification-service
  datasource:
    url: jdbc:postgresql://localhost:5432/bookshop
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false
  rabbitmq:
    host: localhost
    port: 5672

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

#### `notification-service/src/main/java/com/example/notification/NotificationServiceApplication.java`

```java
package com.example.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the Notification Service
 * Scans both the notification and rabbitmq packages for components
 */
@SpringBootApplication(
        scanBasePackages = {
                "com.example.rabbitmq",
                "com.example.notification"
        }
)
@EnableDiscoveryClient
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
```

#### `notification-service/src/main/java/com/example/notification/model/Notification.java`

```java
package com.example.notification.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity for storing notification messages
 */
@Entity
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
}
```

#### `notification-service/src/main/java/com/example/notification/repository/NotificationRepository.java`

```java
package com.example.notification.repository;

import com.example.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for managing notifications in the database
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
```

#### `notification-service/src/main/java/com/example/notification/service/NotificationService.java`

```java
package com.example.notification.service;

import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Receives a message, saves it to the database, and logs it
     *
     * @param message The notification message
     */
    public void receiveMessage(String message) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notificationRepository.save(notification);

        System.out.println("Notification received and saved: " + message);
    }
}
```

#### `notification-service/src/main/java/com/example/notification/listener/RabbitMQListener.java`

```java
package com.example.notification.listener;

import com.example.notification.service.NotificationService;
import com.example.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component that listens for messages from RabbitMQ queue
 */
@Component
public class RabbitMQListener {

    @Autowired
    private NotificationService notificationService;

    /**
     * Listens for messages on the notification queue
     *
     * @param message The received message
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void listen(String message) {
        notificationService.receiveMessage(message);
    }
}
```

### Step 5: Update API Gateway

Update the `application.yml` in the `api-gateway` module to include routes for the new services.

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: book_service
          uri: lb://BOOK-SERVICE
          predicates:
            - Path=/books/**
        - id: orders_service
          uri: lb://ORDERS-SERVICE
          predicates:
            - Path=/orders/**
        - id: notification_service
          uri: lb://NOTIFICATION-SERVICE
          predicates:
            - Path=/notifications/**
```

This setup will give you a fully functional orders and notification system integrated with your existing bookshop microservices. Make sure to adjust any configurations specific to your environment.