# QuickBite — MEMBER 6: Payment, Driver & Review Management — COMPLETE CODE

*Backend (Java / Spring Boot) + Frontend (HTML + CSS) of this module in one document.*

---

## PART A — BACKEND (Java)

### `src/main/java/com/example/fooddelivery/model/Payment.java`
```java
package com.example.fooddelivery.model;

import com.example.fooddelivery.common.BaseEntity;
import com.example.fooddelivery.model.enums.PaymentMethod;
import com.example.fooddelivery.model.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MEMBER 6 - PAYMENT MODULE
 * One payment per order. Status changes are done through business methods
 * (markPaid / refund) instead of setting fields directly -> ENCAPSULATION.
 */
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method = PaymentMethod.CASH_ON_DELIVERY;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(unique = true)
    private String transactionRef;

    private LocalDateTime paidAt;

    public Payment() {
    }

    public Payment(Order order, PaymentMethod method) {
        this.order = order;
        this.amount = order.getTotalAmount();
        this.method = method;
        this.transactionRef = "TXN-" + System.currentTimeMillis() % 10000000;
    }

    @Override
    public String getDisplayName() {
        return "Payment " + transactionRef;
    }

    /** Business rule: a payment can only be paid once, and never after a refund. */
    public void markPaid() {
        if (status == PaymentStatus.PAID) {
            throw new IllegalStateException("This payment is already paid.");
        }
        if (status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("A refunded payment cannot be paid again - create a new payment instead.");
        }
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    /** Business rule: only paid payments can be refunded. */
    public void refund() {
        if (status != PaymentStatus.PAID) {
            throw new IllegalStateException("Only a paid payment can be refunded.");
        }
        this.status = PaymentStatus.REFUNDED;
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    // ---------- Getters / Setters ----------

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}

```

### `src/main/java/com/example/fooddelivery/model/Driver.java`
```java
package com.example.fooddelivery.model;

import com.example.fooddelivery.common.Person;
import com.example.fooddelivery.model.enums.DriverStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * MEMBER 6 - DELIVERY MODULE
 * OOP: INHERITANCE (BaseEntity -> Person -> Driver), POLYMORPHISM (getRole),
 *      ENCAPSULATION.
 */
@Entity
@Table(name = "drivers")
public class Driver extends Person {

    private String vehicleType;    // Bike, Scooter, Car...

    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    private DriverStatus status = DriverStatus.AVAILABLE;

    public Driver() {
    }

    public Driver(String name, String email, String phone, String vehicleType, String licenseNumber) {
        setName(name);
        setEmail(email);
        setPhone(phone);
        this.vehicleType = vehicleType;
        this.licenseNumber = licenseNumber;
    }

    /** POLYMORPHISM: same method name as Customer.getRole(), different value. */
    @Override
    public String getRole() {
        return "DRIVER";
    }

    @Override
    public String getDisplayName() {
        return getName() + " (" + vehicleType + ")";
    }

    /** A driver is only free when his status says so (ENCAPSULATION of the rule). */
    public boolean isAvailableForDelivery() {
        return status == DriverStatus.AVAILABLE;
    }

    // ---------- Getters / Setters ----------

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }
}

```

### `src/main/java/com/example/fooddelivery/model/Review.java`
```java
package com.example.fooddelivery.model;

import com.example.fooddelivery.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * MEMBER 6 - REVIEW MODULE (customer feedback)
 */
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    /** Optional link to the order that was delivered. */
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    /** 1 to 5 stars. */
    private int rating = 5;

    @Column(length = 500)
    private String comment;

    public Review() {
    }

    public Review(Customer customer, Restaurant restaurant, int rating, String comment) {
        this.customer = customer;
        this.restaurant = restaurant;
        this.rating = rating;
        this.comment = comment;
    }

    @Override
    public String getDisplayName() {
        return "Review by " + (customer != null ? customer.getName() : "guest");
    }

    /** Formats "★★★★☆" for the frontend. */
    public String getRatingStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= rating ? "\u2605" : "\u2606");
        }
        return stars.toString();
    }

    public String getSentiment() {
        if (rating >= 4) {
            return "Positive";
        }
        if (rating == 3) {
            return "Neutral";
        }
        return "Negative";
    }

    // ---------- Getters / Setters ----------

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

```

### `src/main/java/com/example/fooddelivery/model/enums/PaymentMethod.java`
```java
package com.example.fooddelivery.model.enums;

public enum PaymentMethod {
    CASH_ON_DELIVERY("Cash on delivery"),
    CREDIT_CARD("Credit card"),
    DEBIT_CARD("Debit card"),
    ONLINE_WALLET("Online wallet");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

```

### `src/main/java/com/example/fooddelivery/model/enums/PaymentStatus.java`
```java
package com.example.fooddelivery.model.enums;

public enum PaymentStatus {
    PENDING("Pending"),
    PAID("Paid"),
    FAILED("Failed"),
    REFUNDED("Refunded");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return "status-" + name().toLowerCase();
    }
}

```

### `src/main/java/com/example/fooddelivery/model/enums/DriverStatus.java`
```java
package com.example.fooddelivery.model.enums;

public enum DriverStatus {
    AVAILABLE("Available"),
    ON_DELIVERY("On delivery"),
    OFFLINE("Offline");

    private final String label;

    DriverStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return "status-" + name().toLowerCase().replace('_', '-');
    }
}

```

### `src/main/java/com/example/fooddelivery/repository/PaymentRepository.java`
```java
package com.example.fooddelivery.repository;

import com.example.fooddelivery.model.Payment;
import com.example.fooddelivery.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** MEMBER 6 - data access for payments. */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

    /** All payments that belong to one customer (payment portal). */
    List<Payment> findByOrder_Customer_IdOrderByIdDesc(Long customerId);

    List<Payment> findAllByOrderByIdDesc();

    long countByStatus(PaymentStatus status);
}

```

### `src/main/java/com/example/fooddelivery/repository/DriverRepository.java`
```java
package com.example.fooddelivery.repository;

import com.example.fooddelivery.model.Driver;
import com.example.fooddelivery.model.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** MEMBER 6 - data access for delivery drivers. */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByStatus(DriverStatus status);

    List<Driver> findByNameContainingIgnoreCase(String name);

    boolean existsByLicenseNumber(String licenseNumber);
}

```

### `src/main/java/com/example/fooddelivery/repository/ReviewRepository.java`
```java
package com.example.fooddelivery.repository;

import com.example.fooddelivery.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** MEMBER 6 - data access for reviews. */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurantIdOrderByIdDesc(Long restaurantId);

    List<Review> findByCustomerId(Long customerId);

    List<Review> findAllByOrderByIdDesc();

    long countByRestaurantId(Long restaurantId);
}

```

### `src/main/java/com/example/fooddelivery/service/PaymentService.java`
```java
package com.example.fooddelivery.service;

import com.example.fooddelivery.model.Order;
import com.example.fooddelivery.model.Payment;
import com.example.fooddelivery.model.enums.PaymentMethod;
import com.example.fooddelivery.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentService {

    List<Payment> findAll();

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByCustomer(Long customerId);

    Payment findById(Long id);

    Optional<Payment> findByOrder(Long orderId);

    Payment createForOrder(Order order, PaymentMethod method);

    Payment save(Payment payment);

    Payment markPaid(Long paymentId);

    Payment markFailed(Long paymentId);

    Payment refund(Long paymentId);

    void deleteById(Long id);

    long count();

    long countByStatus(PaymentStatus status);

    BigDecimal totalCollected();
}

```

### `src/main/java/com/example/fooddelivery/service/DriverService.java`
```java
package com.example.fooddelivery.service;

import com.example.fooddelivery.model.Driver;
import com.example.fooddelivery.model.enums.DriverStatus;

import java.util.List;

public interface DriverService {

    List<Driver> findAll();

    List<Driver> findAvailable();

    List<Driver> search(String keyword);

    Driver findById(Long id);

    Driver save(Driver driver);

    void deleteById(Long id);

    void changeStatus(Long id, DriverStatus status);

    long count();
}

```

### `src/main/java/com/example/fooddelivery/service/ReviewService.java`
```java
package com.example.fooddelivery.service;

import com.example.fooddelivery.model.Review;

import java.util.List;

public interface ReviewService {

    List<Review> findAll();

    List<Review> findByRestaurant(Long restaurantId);

    List<Review> findByCustomer(Long customerId);

    Review findById(Long id);

    Review save(Review review);

    void deleteById(Long id);

    /** Used to update the restaurant's average rating automatically. */
    double averageRating(Long restaurantId);

    long countForRestaurant(Long restaurantId);

    long count();
}

```

### `src/main/java/com/example/fooddelivery/service/impl/PaymentServiceImpl.java`
```java
package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.common.ResourceNotFoundException;
import com.example.fooddelivery.model.Order;
import com.example.fooddelivery.model.Payment;
import com.example.fooddelivery.model.enums.PaymentMethod;
import com.example.fooddelivery.model.enums.PaymentStatus;
import com.example.fooddelivery.repository.PaymentRepository;
import com.example.fooddelivery.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** MEMBER 6 - business logic for payments (also used by the customer payment portal). */
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    public List<Payment> findByCustomer(Long customerId) {
        return paymentRepository.findByOrder_Customer_IdOrderByIdDesc(customerId);
    }

    @Override
    public Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", id));
    }

    @Override
    public Optional<Payment> findByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    /** Called right after an order is placed. */
    @Override
    public Payment createForOrder(Order order, PaymentMethod method) {
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new IllegalStateException("This order already has a payment.");
        }
        Payment payment = new Payment(order, method);
        if (method != PaymentMethod.CASH_ON_DELIVERY) {
            // Online payments are treated as paid immediately in this demo.
            payment.markPaid();
        }
        return paymentRepository.save(payment);
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment markPaid(Long paymentId) {
        Payment payment = findById(paymentId);
        payment.markPaid();
        return paymentRepository.save(payment);
    }

    @Override
    public Payment markFailed(Long paymentId) {
        Payment payment = findById(paymentId);
        payment.markFailed();
        return paymentRepository.save(payment);
    }

    @Override
    public Payment refund(Long paymentId) {
        Payment payment = findById(paymentId);
        payment.refund();
        return paymentRepository.save(payment);
    }

    @Override
    public void deleteById(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Payment", id);
        }
        paymentRepository.deleteById(id);
    }

    @Override
    public long count() {
        return paymentRepository.count();
    }

    @Override
    public long countByStatus(PaymentStatus status) {
        return paymentRepository.countByStatus(status);
    }

    @Override
    public BigDecimal totalCollected() {
        BigDecimal total = BigDecimal.ZERO;
        for (Payment payment : paymentRepository.findByStatus(PaymentStatus.PAID)) {
            total = total.add(payment.getAmount());
        }
        return total;
    }
}

```

### `src/main/java/com/example/fooddelivery/service/impl/DriverServiceImpl.java`
```java
package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.common.ResourceNotFoundException;
import com.example.fooddelivery.model.Driver;
import com.example.fooddelivery.model.enums.DriverStatus;
import com.example.fooddelivery.repository.DriverRepository;
import com.example.fooddelivery.service.DriverService;
import org.springframework.stereotype.Service;

import java.util.List;

/** MEMBER 6 - business logic for delivery drivers. */
@Service
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;

    public DriverServiceImpl(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    @Override
    public List<Driver> findAvailable() {
        return driverRepository.findByStatus(DriverStatus.AVAILABLE);
    }

    @Override
    public List<Driver> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return driverRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public Driver findById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Driver", id));
    }

    @Override
    public Driver save(Driver driver) {
        return driverRepository.save(driver);
    }

    @Override
    public void deleteById(Long id) {
        if (!driverRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Driver", id);
        }
        driverRepository.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, DriverStatus status) {
        Driver driver = findById(id);
        driver.setStatus(status);
        driverRepository.save(driver);
    }

    @Override
    public long count() {
        return driverRepository.count();
    }
}

```

### `src/main/java/com/example/fooddelivery/service/impl/ReviewServiceImpl.java`
```java
package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.common.ResourceNotFoundException;
import com.example.fooddelivery.model.Restaurant;
import com.example.fooddelivery.model.Review;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.example.fooddelivery.repository.ReviewRepository;
import com.example.fooddelivery.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

/** MEMBER 6 - business logic for reviews. */
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             RestaurantRepository restaurantRepository) {
        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public List<Review> findAll() {
        return reviewRepository.findAllByOrderByIdDesc();
    }

    @Override
    public List<Review> findByRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByIdDesc(restaurantId);
    }

    @Override
    public List<Review> findByCustomer(Long customerId) {
        return reviewRepository.findByCustomerId(customerId);
    }

    @Override
    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Review", id));
    }

    /** After saving a review, the restaurant's average rating is refreshed. */
    @Override
    public Review save(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalStateException("Rating must be between 1 and 5 stars.");
        }
        Review saved = reviewRepository.save(review);
        refreshRestaurantRating(saved.getRestaurant().getId());
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        Review review = findById(id);
        Long restaurantId = review.getRestaurant().getId();
        reviewRepository.delete(review);
        refreshRestaurantRating(restaurantId);
    }

    @Override
    public double averageRating(Long restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantIdOrderByIdDesc(restaurantId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        int sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return Math.round((sum * 10.0) / reviews.size()) / 10.0;
    }

    @Override
    public long countForRestaurant(Long restaurantId) {
        return reviewRepository.countByRestaurantId(restaurantId);
    }

    @Override
    public long count() {
        return reviewRepository.count();
    }

    private void refreshRestaurantRating(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
        if (restaurant == null) {
            return;
        }
        double average = averageRating(restaurantId);
        if (average > 0) {
            restaurant.setRating(average);
            restaurantRepository.save(restaurant);
        }
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/PaymentController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.model.Payment;
import com.example.fooddelivery.model.enums.PaymentMethod;
import com.example.fooddelivery.model.enums.PaymentStatus;
import com.example.fooddelivery.service.OrderService;
import com.example.fooddelivery.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MEMBER 6 - PAYMENT MODULE
 * CREATE -> GET /admin/payments/new + POST /payments
 * READ   -> GET /admin/payments (+ filter), GET /admin/payments/{id}
 * UPDATE -> GET /admin/payments/{id}/edit + POST /payments/{id}
 *           (+ business actions: mark as paid / failed / refunded)
 * DELETE -> POST /payments/{id}/delete
 */
@Controller
@RequestMapping("/admin/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) PaymentStatus status, Model model) {
        if (status != null) {
            model.addAttribute("payments", paymentService.findByStatus(status));
        } else {
            model.addAttribute("payments", paymentService.findAll());
        }
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("totalCollected", paymentService.totalCollected());
        return "payments/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("payment", paymentService.findById(id));
        return "payments/view";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("orders", orderService.findAll());
        model.addAttribute("methods", PaymentMethod.values());
        return "payments/form";
    }

    @PostMapping
    public String create(@RequestParam Long orderId,
                         @RequestParam PaymentMethod method,
                         RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paymentService.createForOrder(orderService.findById(orderId), method);
            redirectAttributes.addFlashAttribute("success",
                    "Payment " + payment.getTransactionRef() + " created for order "
                            + payment.getOrder().getOrderNumber() + ".");
            return "redirect:/admin/payments/" + payment.getId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/payments/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("payment", paymentService.findById(id));
        model.addAttribute("methods", PaymentMethod.values());
        return "payments/form-edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("payment") Payment payment,
                         @RequestParam(required = false) PaymentMethod method,
                         RedirectAttributes redirectAttributes) {
        Payment existing = paymentService.findById(id);
        if (payment.getAmount() != null) {
            existing.setAmount(payment.getAmount());
        }
        if (method != null) {
            existing.setMethod(method);
        }
        paymentService.save(existing);
        redirectAttributes.addFlashAttribute("success", "Payment updated.");
        return "redirect:/admin/payments/" + id;
    }

    // ---------------- business actions ----------------
    @PostMapping("/{id}/pay")
    public String markPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            paymentService.markPaid(id);
            redirectAttributes.addFlashAttribute("success", "Payment marked as PAID.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/payments/" + id;
    }

    @PostMapping("/{id}/failed")
    public String markFailed(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        paymentService.markFailed(id);
        redirectAttributes.addFlashAttribute("success", "Payment marked as FAILED.");
        return "redirect:/admin/payments/" + id;
    }

    @PostMapping("/{id}/refund")
    public String refund(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            paymentService.refund(id);
            redirectAttributes.addFlashAttribute("success", "Payment refunded.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/payments/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        paymentService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Payment deleted.");
        return "redirect:/admin/payments";
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/DriverController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.model.Driver;
import com.example.fooddelivery.model.enums.DriverStatus;
import com.example.fooddelivery.service.DriverService;
import com.example.fooddelivery.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MEMBER 6 - DELIVERY DRIVER MODULE
 * CREATE -> GET /admin/drivers/new + POST /drivers
 * READ   -> GET /admin/drivers (+ search), GET /admin/drivers/{id}
 * UPDATE -> GET /admin/drivers/{id}/edit + POST /drivers/{id}
 * DELETE -> POST /drivers/{id}/delete
 */
@Controller
@RequestMapping("/admin/drivers")
public class DriverController {

    private final DriverService driverService;
    private final OrderService orderService;

    public DriverController(DriverService driverService, OrderService orderService) {
        this.driverService = driverService;
        this.orderService = orderService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("drivers", driverService.search(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", DriverStatus.values());
        return "drivers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("driver", new Driver());
        model.addAttribute("statuses", DriverStatus.values());
        return "drivers/form";
    }

    @PostMapping
    public String create(@ModelAttribute("driver") Driver driver,
                         RedirectAttributes redirectAttributes) {
        if (driver.getName() == null || driver.getName().isBlank()
                || driver.getEmail() == null || driver.getEmail().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Driver name and e-mail are required.");
            return "redirect:/admin/drivers/new";
        }
        driverService.save(driver);
        redirectAttributes.addFlashAttribute("success", "Driver \"" + driver.getName() + "\" added to the fleet.");
        return "redirect:/admin/drivers";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("driver", driverService.findById(id));
        model.addAttribute("orders", orderService.findByDriver(id));
        model.addAttribute("statuses", DriverStatus.values());
        return "drivers/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("driver", driverService.findById(id));
        model.addAttribute("statuses", DriverStatus.values());
        return "drivers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("driver") Driver driver,
                         @RequestParam(required = false) DriverStatus status,
                         RedirectAttributes redirectAttributes) {
        Driver existing = driverService.findById(id);
        existing.setName(driver.getName());
        existing.setEmail(driver.getEmail());
        existing.setPhone(driver.getPhone());
        existing.setAddress(driver.getAddress());
        existing.setVehicleType(driver.getVehicleType());
        existing.setLicenseNumber(driver.getLicenseNumber());
        if (status != null) {
            existing.setStatus(status);
        }
        driverService.save(existing);
        redirectAttributes.addFlashAttribute("success", "Driver updated.");
        return "redirect:/admin/drivers/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            driverService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Driver removed.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error",
                    "This driver is linked to existing orders, so they cannot be removed. Set the status to OFFLINE instead.");
        }
        return "redirect:/admin/drivers";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam DriverStatus status,
                               RedirectAttributes redirectAttributes) {
        driverService.changeStatus(id, status);
        redirectAttributes.addFlashAttribute("success", "Driver status changed to " + status.getLabel() + ".");
        return "redirect:/admin/drivers/" + id;
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/ReviewController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.model.Review;
import com.example.fooddelivery.service.CustomerService;
import com.example.fooddelivery.service.OrderService;
import com.example.fooddelivery.service.RestaurantService;
import com.example.fooddelivery.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MEMBER 6 - REVIEW MODULE
 * CREATE -> GET /admin/reviews/new + POST /reviews
 * READ   -> GET /admin/reviews (+ filter by restaurant)
 * UPDATE -> GET /admin/reviews/{id}/edit + POST /reviews/{id}
 * DELETE -> POST /reviews/{id}/delete
 */
@Controller
@RequestMapping("/admin/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final RestaurantService restaurantService;
    private final CustomerService customerService;
    private final OrderService orderService;

    public ReviewController(ReviewService reviewService,
                            RestaurantService restaurantService,
                            CustomerService customerService,
                            OrderService orderService) {
        this.reviewService = reviewService;
        this.restaurantService = restaurantService;
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long restaurantId, Model model) {
        if (restaurantId != null) {
            model.addAttribute("reviews", reviewService.findByRestaurant(restaurantId));
            model.addAttribute("filterRestaurant", restaurantService.findById(restaurantId));
        } else {
            model.addAttribute("reviews", reviewService.findAll());
        }
        model.addAttribute("restaurants", restaurantService.findAll());
        model.addAttribute("restaurantId", restaurantId);
        return "reviews/list";
    }

    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Long restaurantId, Model model) {
        model.addAttribute("restaurants", restaurantService.findAll());
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("restaurantId", restaurantId);
        return "reviews/form";
    }

    @PostMapping
    public String create(@RequestParam Long restaurantId,
                         @RequestParam Long customerId,
                         @RequestParam int rating,
                         @RequestParam(required = false) String comment,
                         RedirectAttributes redirectAttributes) {
        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Rating must be between 1 and 5.");
            return "redirect:/admin/reviews/new";
        }
        Review review = new Review();
        review.setRestaurant(restaurantService.findById(restaurantId));
        review.setCustomer(customerService.findById(customerId));
        review.setRating(rating);
        review.setComment(comment);
        reviewService.save(review);
        redirectAttributes.addFlashAttribute("success",
                "Thank you! Your review was saved and the restaurant rating was recalculated.");
        return "redirect:/admin/reviews";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("review", reviewService.findById(id));
        model.addAttribute("restaurants", restaurantService.findAll());
        model.addAttribute("customers", customerService.findAll());
        return "reviews/form-edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam int rating,
                         @RequestParam(required = false) String comment,
                         RedirectAttributes redirectAttributes) {
        Review existing = reviewService.findById(id);
        existing.setRating(rating);
        existing.setComment(comment);
        reviewService.save(existing);
        redirectAttributes.addFlashAttribute("success", "Review updated.");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Review deleted.");
        return "redirect:/admin/reviews";
    }

}

```

### `src/main/java/com/example/fooddelivery/controller/CustomerPaymentController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.common.SessionHelper;
import com.example.fooddelivery.model.Payment;
import com.example.fooddelivery.model.enums.PaymentStatus;
import com.example.fooddelivery.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * CUSTOMER PAYMENT PORTAL   (Member 6)   -   /customer/payments/**
 * ------------------------------------------------------------
 * The customer's own payment pages (protected, CUSTOMER role only):
 *
 *   GET  /customer/payments          -> all my payments + how many are still pending (READ)
 *   POST /customer/payments/{id}/pay -> PAY NOW: marks my pending payment as PAID (UPDATE)
 *
 * The rules live in PaymentServiceImpl: a payment can only be paid once, and a refunded or
 * failed payment can never be paid again. Ownership is checked before anything happens.
 */
@Controller
@RequestMapping("/customer")
public class CustomerPaymentController {

    private final PaymentService paymentService;

    public CustomerPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    private Long customerId(HttpSession session) {
        return SessionHelper.getUserId(session);
    }

    // ---------------------------------------------------- PAYMENT PORTAL
    @GetMapping("/payments")
    public String myPayments(HttpSession session, Model model) {
        Long id = customerId(session);
        List<Payment> payments = paymentService.findByCustomer(id);
        model.addAttribute("payments", payments);
        model.addAttribute("pendingCount", payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING).count());
        return "customer/payments";
    }

    @PostMapping("/payments/{id}/pay")
    public String payNow(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Payment payment = paymentService.findById(id);
        if (!payment.getOrder().getCustomer().getId().equals(customerId(session))) {
            throw new IllegalStateException("This payment does not belong to your account.");
        }
        try {
            paymentService.markPaid(id);
            redirectAttributes.addFlashAttribute("success",
                    "Payment " + payment.getTransactionRef() + " completed. Thank you!");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/payments";
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/CustomerReviewController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.common.SessionHelper;
import com.example.fooddelivery.model.Review;
import com.example.fooddelivery.service.CustomerService;
import com.example.fooddelivery.service.RestaurantService;
import com.example.fooddelivery.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MY REVIEWS   (Member 6)   -   /customer/reviews/**
 * ------------------------------------------------
 * The customer writes and manages his own reviews (protected, CUSTOMER role only):
 *
 *   GET  /customer/reviews            -> reviews I wrote (READ)
 *   GET  /customer/reviews/new        -> write-a-review form (CREATE form)
 *   POST /customer/reviews            -> save my review 1-5 stars (CREATE)
 *   GET  /customer/reviews/{id}/edit  -> edit form (UPDATE form)
 *   POST /customer/reviews/{id}       -> save my changes (UPDATE)
 *   POST /customer/reviews/{id}/delete-> delete my review (DELETE)
 *
 * Every save/delete recalculates the restaurant's average rating (business rule in
 * ReviewServiceImpl). Ownership is checked: I can only edit MY OWN reviews.
 */
@Controller
@RequestMapping("/customer")
public class CustomerReviewController {

    private final CustomerService customerService;
    private final RestaurantService restaurantService;
    private final ReviewService reviewService;

    public CustomerReviewController(CustomerService customerService,
                                    RestaurantService restaurantService,
                                    ReviewService reviewService) {
        this.customerService = customerService;
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
    }

    private Long customerId(HttpSession session) {
        return SessionHelper.getUserId(session);
    }

    private Review requireOwnReview(Long id, HttpSession session) {
        Review review = reviewService.findById(id);
        if (!review.getCustomer().getId().equals(customerId(session))) {
            throw new IllegalStateException("This review does not belong to your account.");
        }
        return review;
    }

    // -------------------------------------------------------------- REVIEWS
    @GetMapping("/reviews")
    public String myReviews(HttpSession session, Model model) {
        Long id = customerId(session);
        model.addAttribute("customer", customerService.findById(id));
        model.addAttribute("reviews", reviewService.findByCustomer(id));
        return "customer/reviews";
    }

    @GetMapping("/reviews/new")
    public String newReviewForm(@RequestParam(required = false) Long restaurantId, Model model) {
        model.addAttribute("restaurants", restaurantService.findAll());
        model.addAttribute("restaurantId", restaurantId);
        return "customer/review-form";
    }

    @PostMapping("/reviews")
    public String createReview(@RequestParam Long restaurantId,
                               @RequestParam int rating,
                               @RequestParam(required = false) String comment,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Rating must be between 1 and 5 stars.");
            return "redirect:/customer/reviews/new";
        }
        Review review = new Review();
        review.setCustomer(customerService.findById(customerId(session)));
        review.setRestaurant(restaurantService.findById(restaurantId));
        review.setRating(rating);
        review.setComment(comment);
        reviewService.save(review);
        redirectAttributes.addFlashAttribute("success",
                "Thanks! Your review was saved and the restaurant rating was recalculated.");
        return "redirect:/customer/reviews";
    }

    @GetMapping("/reviews/{id}/edit")
    public String editReviewForm(@PathVariable Long id, HttpSession session, Model model) {
        model.addAttribute("review", requireOwnReview(id, session));
        return "customer/review-form";
    }

    @PostMapping("/reviews/{id}")
    public String updateReview(@PathVariable Long id,
                               @RequestParam int rating,
                               @RequestParam(required = false) String comment,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Review review = requireOwnReview(id, session);
        review.setRating(rating);
        review.setComment(comment);
        reviewService.save(review);
        redirectAttributes.addFlashAttribute("success", "Review updated.");
        return "redirect:/customer/reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        requireOwnReview(id, session);
        reviewService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Review deleted.");
        return "redirect:/customer/reviews";
    }
}

```
