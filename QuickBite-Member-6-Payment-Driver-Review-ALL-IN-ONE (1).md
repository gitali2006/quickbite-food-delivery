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


## PART B — FRONTEND (HTML templates)

### `src/main/resources/templates/payments/list.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>QuickBite - Payments (Member 6)</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1>Payments</h1>
            <p class="muted">Admin &mdash; full CRUD for payments + business actions (pay, fail, refund).</p>
        </div>
        <a th:href="@{/admin/payments/new}" class="btn">+ Create payment</a>
    </div>

    <div class="stats-grid">
        <div class="stat-card accent-green">
            <div class="stat-value" th:text="'Rs. ' + ${#numbers.formatDecimal(totalCollected, 1, 'COMMA', 2, 'POINT')}">Rs. 0</div>
            <div class="stat-label">Total collected (PAID only)</div>
        </div>
    </div>

    <form class="filter-form" th:action="@{/admin/payments}" method="get">
        <div class="form-group">
            <label for="status">Filter by status</label>
            <select id="status" name="status">
                <option value="">All statuses</option>
                <option th:each="s : ${statuses}" th:value="${s}" th:text="${s.label}"
                        th:selected="${selectedStatus != null and selectedStatus == s}">Status</option>
            </select>
        </div>
        <button type="submit" class="btn btn-slate">Apply filter</button>
        <a th:href="@{/admin/payments}" class="btn btn-outline">Reset</a>
    </form>

    <div class="card empty" th:if="${#lists.isEmpty(payments)}">
        <span class="big">&#128179;</span>
        No payments recorded.
    </div>

    <div class="table-wrap" th:unless="${#lists.isEmpty(payments)}">
        <table class="table">
            <thead>
            <tr>
                <th>Reference</th>
                <th>Order</th>
                <th>Customer</th>
                <th>Amount (Rs.)</th>
                <th>Method</th>
                <th>Status</th>
                <th>Paid at</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr th:each="payment : ${payments}">
                <td><strong th:text="${payment.transactionRef}">TXN</strong></td>
                <td><a th:href="@{/admin/orders/{id}(id=${payment.order.id})}" th:text="${payment.order.orderNumber}">ORD</a></td>
                <td th:text="${payment.order.customer.name}">Customer</td>
                <td th:text="${#numbers.formatDecimal(payment.amount, 1, 'COMMA', 2, 'POINT')}">0.00</td>
                <td th:text="${payment.method.label}">Method</td>
                <td><span class="badge" th:classappend="${payment.status.cssClass}" th:text="${payment.status.label}">Status</span></td>
                <td th:text="${payment.paidAt != null ? #temporals.format(payment.paidAt, 'dd MMM, HH:mm') : '-'}">-</td>
                <td>
                    <div class="actions">
                        <a th:href="@{/admin/payments/{id}(id=${payment.id})}" class="btn btn-sm btn-outline">View</a>
                        <form th:if="${payment.status.name() == 'PENDING'}"
                              th:action="@{/admin/payments/{id}/pay(id=${payment.id})}" method="post" class="inline-form">
                            <button type="submit" class="btn btn-sm">Mark paid</button>
                        </form>
                        <form th:action="@{/admin/payments/{id}/delete(id=${payment.id})}" method="post" class="inline-form"
                              >
                            <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                        </form>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/payments/view.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="'QuickBite - ' + ${payment.transactionRef}">Payment</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1 th:text="'Payment ' + ${payment.transactionRef}">Payment</h1>
            <p class="muted">Admin &mdash; payment details and business actions</p>
        </div>
        <div class="btn-row">
            <a th:href="@{/admin/payments}" class="btn btn-outline">&larr; All payments</a>
            <a th:href="@{/admin/payments/{id}/edit(id=${payment.id})}" class="btn btn-slate">Edit</a>
        </div>
    </div>

    <div class="card">
        <dl class="kv">
            <dt>Reference</dt>
            <dd th:text="${payment.transactionRef}">TXN</dd>
            <dt>Order</dt>
            <dd><a th:href="@{/admin/orders/{id}(id=${payment.order.id})}"
                   th:text="${payment.order.orderNumber}">ORD-1</a></dd>
            <dt>Customer</dt>
            <dd th:text="${payment.order.customer.name}">Customer</dd>
            <dt>Restaurant</dt>
            <dd th:text="${payment.order.restaurant.name}">Restaurant</dd>
            <dt>Amount</dt>
            <dd th:text="'Rs. ' + ${#numbers.formatDecimal(payment.amount, 1, 'COMMA', 2, 'POINT')}">Rs. 0.00</dd>
            <dt>Method</dt>
            <dd th:text="${payment.method.label}">Method</dd>
            <dt>Status</dt>
            <dd><span class="badge" th:classappend="${payment.status.cssClass}" th:text="${payment.status.label}">Status</span></dd>
            <dt>Paid at</dt>
            <dd th:text="${payment.paidAt != null ? #temporals.format(payment.paidAt, 'dd MMM yyyy, HH:mm') : 'Not paid yet'}">-</dd>
        </dl>

        <div class="btn-row mt">
            <form th:if="${payment.status.name() == 'PENDING'}"
                  th:action="@{/admin/payments/{id}/pay(id=${payment.id})}" method="post" class="inline-form">
                <button type="submit" class="btn">Mark as PAID</button>
            </form>
            <form th:if="${payment.status.name() == 'PENDING'}"
                  th:action="@{/admin/payments/{id}/failed(id=${payment.id})}" method="post" class="inline-form">
                <button type="submit" class="btn btn-amber">Mark as FAILED</button>
            </form>
            <form th:if="${payment.status.name() == 'PAID'}"
                  th:action="@{/admin/payments/{id}/refund(id=${payment.id})}" method="post" class="inline-form"
                  >
                <button type="submit" class="btn btn-amber">Refund</button>
            </form>
            <form th:action="@{/admin/payments/{id}/delete(id=${payment.id})}" method="post" class="inline-form"
                  >
                <button type="submit" class="btn btn-danger">Delete payment</button>
            </form>
        </div>
        <p class="muted mt" style="font-size:.85rem;">
            Business rules live inside the Payment object itself: a payment can only be refunded when it is PAID,
            and it can never be paid twice.
        </p>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/payments/form.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>QuickBite - New payment</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1>Create a payment</h1>
            <p class="muted">Admin &mdash; CREATE. The amount is taken from the order total automatically.</p>
        </div>
        <a th:href="@{/admin/payments}" class="btn btn-outline">&larr; Back to payments</a>
    </div>

    <div class="card">
        <form th:action="@{/admin/payments}" method="post">
            <div class="form-grid">
                <div class="form-group full">
                    <label for="orderId">Order *</label>
                    <select id="orderId" name="orderId" required>
                        <option value="">-- select order --</option>
                        <option th:each="order : ${orders}" th:value="${order.id}"
                                th:text="${order.orderNumber} + '  |  ' + ${order.customer.name} + '  |  Rs. ' + ${order.totalAmount} + '  |  ' + ${order.status.label}">
                            Order
                        </option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="method">Payment method</label>
                    <select id="method" name="method">
                        <option th:each="m : ${methods}" th:value="${m}" th:text="${m.label}">Method</option>
                    </select>
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Create payment</button>
                <a th:href="@{/admin/payments}" class="btn btn-outline">Cancel</a>
            </div>
        </form>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/payments/form-edit.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="'QuickBite - Edit ' + ${payment.transactionRef}">Edit payment</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1 th:text="'Edit payment ' + ${payment.transactionRef}">Edit payment</h1>
            <p class="muted">Admin &mdash; UPDATE form (amount and method). Use the payment page for status changes.</p>
        </div>
        <a th:href="@{/admin/payments/{id}(id=${payment.id})}" class="btn btn-outline">&larr; Back to payment</a>
    </div>

    <div class="card">
        <dl class="kv mb">
            <dt>Order</dt>
            <dd th:text="${payment.order.orderNumber}">ORD-1</dd>
            <dt>Current status</dt>
            <dd><span class="badge" th:classappend="${payment.status.cssClass}" th:text="${payment.status.label}">Status</span></dd>
        </dl>

        <form th:action="@{/admin/payments/{id}(id=${payment.id})}" method="post">
            <div class="form-grid">
                <div class="form-group">
                    <label for="amount">Amount (Rs.)</label>
                    <input type="number" id="amount" name="amount" step="0.01" min="0"
                           th:value="${payment.amount}">
                </div>
                <div class="form-group">
                    <label for="method">Method</label>
                    <select id="method" name="method">
                        <option th:each="m : ${methods}" th:value="${m}" th:text="${m.label}"
                                th:selected="${payment.method == m}">Method</option>
                    </select>
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Save changes</button>
                <a th:href="@{/admin/payments/{id}(id=${payment.id})}" class="btn btn-outline">Cancel</a>
            </div>
        </form>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/drivers/list.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>QuickBite - Drivers (Member 6)</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1>Delivery drivers</h1>
            <p class="muted">Admin &mdash; full CRUD for the delivery fleet + availability status.</p>
        </div>
        <a th:href="@{/admin/drivers/new}" class="btn">+ Add driver</a>
    </div>

    <form class="filter-form" th:action="@{/admin/drivers}" method="get">
        <div class="form-group">
            <label for="keyword">Search by name</label>
            <input type="text" id="keyword" name="keyword" th:value="${keyword}" placeholder="e.g. Ruwan">
        </div>
        <button type="submit" class="btn btn-slate">Search</button>
        <a th:href="@{/admin/drivers}" class="btn btn-outline">Reset</a>
    </form>

    <div class="card empty" th:if="${#lists.isEmpty(drivers)}">
        <span class="big">&#128666;</span>
        No drivers in the fleet yet.
    </div>

    <div class="table-wrap" th:unless="${#lists.isEmpty(drivers)}">
        <table class="table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Phone</th>
                <th>Vehicle</th>
                <th>Licence no.</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr th:each="driver : ${drivers}">
                <td th:text="${driver.id}">1</td>
                <td><strong th:text="${driver.name}">Ruwan</strong></td>
                <td th:text="${driver.phone} ?: '-'">-</td>
                <td th:text="${driver.vehicleType} ?: '-'">-</td>
                <td th:text="${driver.licenseNumber} ?: '-'">-</td>
                <td><span class="badge" th:classappend="${driver.status.cssClass}" th:text="${driver.status.label}">Status</span></td>
                <td>
                    <div class="actions">
                        <a th:href="@{/admin/drivers/{id}(id=${driver.id})}" class="btn btn-sm btn-outline">View</a>
                        <a th:href="@{/admin/drivers/{id}/edit(id=${driver.id})}" class="btn btn-sm btn-slate">Edit</a>
                        <form th:action="@{/admin/drivers/{id}/delete(id=${driver.id})}" method="post" class="inline-form"
                              >
                            <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                        </form>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/drivers/view.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="'QuickBite - ' + ${driver.name}">Driver</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1 th:text="${driver.name}">Driver</h1>
            <p class="muted">Admin &mdash; driver profile and assigned deliveries (READ)</p>
        </div>
        <div class="btn-row">
            <a th:href="@{/admin/drivers}" class="btn btn-outline">&larr; All drivers</a>
            <a th:href="@{/admin/drivers/{id}/edit(id=${driver.id})}" class="btn btn-slate">Edit</a>
        </div>
    </div>

    <div class="card">
        <dl class="kv">
            <dt>Driver ID</dt>
            <dd th:text="${driver.id}">1</dd>
            <dt>Role</dt>
            <dd><span class="chip" th:text="${driver.role}">DRIVER</span></dd>
            <dt>Status</dt>
            <dd><span class="badge" th:classappend="${driver.status.cssClass}" th:text="${driver.status.label}">Status</span></dd>
            <dt>E-mail</dt>
            <dd th:text="${driver.email}">-</dd>
            <dt>Phone</dt>
            <dd th:text="${driver.phone} ?: '-'">-</dd>
            <dt>Vehicle</dt>
            <dd th:text="${driver.vehicleType} ?: '-'">-</dd>
            <dt>Licence number</dt>
            <dd th:text="${driver.licenseNumber} ?: '-'">-</dd>
            <dt>Available now?</dt>
            <dd th:text="${driver.availableForDelivery} ? 'Yes' : 'No'">-</dd>
        </dl>

        <div class="btn-row mt">
            <form th:each="s : ${statuses}" th:action="@{/admin/drivers/{id}/status(id=${driver.id})}" method="post"
                  class="inline-form">
                <input type="hidden" name="status" th:value="${s}">
                <button type="submit" class="btn btn-sm btn-outline"
                        th:disabled="${driver.status == s}" th:text="'Set ' + ${s.label}">Set</button>
            </form>
        </div>
    </div>

    <div class="section-title"><h2>Deliveries</h2></div>
    <div class="card empty" th:if="${#lists.isEmpty(orders)}">
        <span class="big">&#128230;</span>
        This driver has not been assigned any order yet.
    </div>

    <div class="table-wrap" th:unless="${#lists.isEmpty(orders)}">
        <table class="table">
            <thead>
            <tr>
                <th>Order</th>
                <th>Customer</th>
                <th>Restaurant</th>
                <th>Total (Rs.)</th>
                <th>Status</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <tr th:each="order : ${orders}">
                <td th:text="${order.orderNumber}">ORD-1</td>
                <td th:text="${order.customer.name}">Customer</td>
                <td th:text="${order.restaurant.name}">Restaurant</td>
                <td th:text="${#numbers.formatDecimal(order.totalAmount, 1, 'COMMA', 2, 'POINT')}">0.00</td>
                <td><span class="badge" th:classappend="${order.status.cssClass}" th:text="${order.status.label}">Status</span></td>
                <td><a th:href="@{/admin/orders/{id}(id=${order.id})}" class="btn btn-sm btn-outline">View</a></td>
            </tr>
            </tbody>
        </table>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/drivers/form.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${driver.id == null} ? 'QuickBite - New driver' : 'QuickBite - Edit driver'">Driver form</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1 th:text="${driver.id == null} ? 'Add a delivery driver' : 'Edit driver'">Driver</h1>
            <p class="muted">Admin &mdash; CREATE / UPDATE form</p>
        </div>
        <a th:href="@{/admin/drivers}" class="btn btn-outline">&larr; Back to drivers</a>
    </div>

    <div class="card">
        <form th:action="${driver.id == null} ? @{/admin/drivers} : @{/admin/drivers/{id}(id=${driver.id})}" method="post">
            <div class="form-grid">
                <div class="form-group">
                    <label for="name">Full name *</label>
                    <input type="text" id="name" name="name" th:value="${driver.name}" required placeholder="Ruwan Jayasinghe">
                </div>
                <div class="form-group">
                    <label for="email">E-mail *</label>
                    <input type="email" id="email" name="email" th:value="${driver.email}" required placeholder="ruwan@quickbite.lk">
                </div>
                <div class="form-group">
                    <label for="phone">Phone</label>
                    <input type="text" id="phone" name="phone" th:value="${driver.phone}" placeholder="0775551234">
                </div>
                <div class="form-group">
                    <label for="vehicleType">Vehicle</label>
                    <input type="text" id="vehicleType" name="vehicleType" th:value="${driver.vehicleType}"
                           placeholder="Bike / Scooter / Car">
                </div>
                <div class="form-group">
                    <label for="licenseNumber">Licence number</label>
                    <input type="text" id="licenseNumber" name="licenseNumber" th:value="${driver.licenseNumber}"
                           placeholder="B-1234567">
                </div>
                <div class="form-group">
                    <label for="status">Status</label>
                    <select id="status" name="status">
                        <option th:each="s : ${statuses}" th:value="${s}" th:text="${s.label}"
                                th:selected="${driver.status == s}">Status</option>
                    </select>
                </div>
                <div class="form-group full">
                    <label for="address">Address</label>
                    <input type="text" id="address" name="address" th:value="${driver.address}" placeholder="Home address">
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn" th:text="${driver.id == null} ? 'Add driver' : 'Save changes'">Save</button>
                <a th:href="@{/admin/drivers}" class="btn btn-outline">Cancel</a>
            </div>
        </form>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/reviews/list.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>QuickBite - Reviews (Member 6)</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1>Customer reviews</h1>
            <p class="muted">Admin &mdash; full CRUD for reviews. Saving a review recalculates the restaurant rating.</p>
        </div>
        <div class="btn-row">
            <a th:href="@{/admin/reviews/new}" class="btn">+ Write a review</a>
        </div>
    </div>

    <form class="filter-form" th:action="@{/admin/reviews}" method="get">
        <div class="form-group">
            <label for="restaurantId">Filter by restaurant</label>
            <select id="restaurantId" name="restaurantId">
                <option value="">All restaurants</option>
                <option th:each="r : ${restaurants}" th:value="${r.id}" th:text="${r.name}"
                        th:selected="${restaurantId != null and restaurantId == r.id}">Restaurant</option>
            </select>
        </div>
        <button type="submit" class="btn btn-slate">Filter</button>
        <a th:href="@{/admin/reviews}" class="btn btn-outline">Reset</a>
    </form>

    <div class="alert alert-info" th:if="${filterRestaurant != null}">
        Showing reviews for <strong th:text="${filterRestaurant.name}">Restaurant</strong>
        (average <span th:text="${filterRestaurant.rating}">0</span> / 5).
    </div>

    <div class="card empty" th:if="${#lists.isEmpty(reviews)}">
        <span class="big">&#11088;</span>
        No reviews yet.
    </div>

    <div class="table-wrap" th:unless="${#lists.isEmpty(reviews)}">
        <table class="table">
            <thead>
            <tr>
                <th>Restaurant</th>
                <th>Customer</th>
                <th>Rating</th>
                <th>Comment</th>
                <th>Sentiment</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr th:each="review : ${reviews}">
                <td th:text="${review.restaurant.name}">Restaurant</td>
                <td th:text="${review.customer.name}">Customer</td>
                <td><span class="stars" th:text="${review.ratingStars}">*****</span>
                    <span class="muted" th:text="'(' + ${review.rating} + '/5)'">(5/5)</span></td>
                <td th:text="${review.comment} ?: '-'">Comment</td>
                <td><span class="chip" th:text="${review.sentiment}">Positive</span></td>
                <td>
                    <div class="actions">
                        <a th:href="@{/admin/reviews/{id}/edit(id=${review.id})}" class="btn btn-sm btn-slate">Edit</a>
                        <form th:action="@{/admin/reviews/{id}/delete(id=${review.id})}" method="post" class="inline-form"
                              >
                            <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                        </form>
                    </div>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/reviews/form.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>QuickBite - Write a review</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1>Write a review</h1>
            <p class="muted">Admin &mdash; CREATE. Give 1 to 5 stars and tell others how the food was.</p>
        </div>
        <a th:href="@{/admin/reviews}" class="btn btn-outline">&larr; All reviews</a>
    </div>

    <div class="card">
        <form th:action="@{/admin/reviews}" method="post">
            <div class="form-grid">
                <div class="form-group">
                    <label for="restaurantId">Restaurant *</label>
                    <select id="restaurantId" name="restaurantId" required>
                        <option value="">-- select restaurant --</option>
                        <option th:each="r : ${restaurants}" th:value="${r.id}" th:text="${r.name}"
                                th:selected="${restaurantId != null and restaurantId == r.id}">Restaurant</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="customerId">Customer *</label>
                    <select id="customerId" name="customerId" required>
                        <option value="">-- select customer --</option>
                        <option th:each="c : ${customers}" th:value="${c.id}" th:text="${c.name}">Customer</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="rating">Rating (1-5 stars) *</label>
                    <select id="rating" name="rating">
                        <option value="5">5 - Excellent</option>
                        <option value="4">4 - Very good</option>
                        <option value="3">3 - Okay</option>
                        <option value="2">2 - Poor</option>
                        <option value="1">1 - Bad</option>
                    </select>
                </div>
                <div class="form-group full">
                    <label for="comment">Comment</label>
                    <textarea id="comment" name="comment" placeholder="How was the food and the delivery?"></textarea>
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Submit review</button>
                <a th:href="@{/admin/reviews}" class="btn btn-outline">Cancel</a>
            </div>
        </form>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/reviews/form-edit.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>QuickBite - Edit review</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1>Edit review</h1>
            <p class="muted">Admin &mdash; UPDATE form</p>
        </div>
        <a th:href="@{/admin/reviews}" class="btn btn-outline">&larr; All reviews</a>
    </div>

    <div class="card">
        <dl class="kv mb">
            <dt>Restaurant</dt>
            <dd th:text="${review.restaurant.name}">Restaurant</dd>
            <dt>Customer</dt>
            <dd th:text="${review.customer.name}">Customer</dd>
        </dl>

        <form th:action="@{/admin/reviews/{id}(id=${review.id})}" method="post">
            <div class="form-grid">
                <div class="form-group">
                    <label for="rating">Rating (1-5 stars)</label>
                    <select id="rating" name="rating">
                        <option value="5" th:selected="${review.rating == 5}">5 - Excellent</option>
                        <option value="4" th:selected="${review.rating == 4}">4 - Very good</option>
                        <option value="3" th:selected="${review.rating == 3}">3 - Okay</option>
                        <option value="2" th:selected="${review.rating == 2}">2 - Poor</option>
                        <option value="1" th:selected="${review.rating == 1}">1 - Bad</option>
                    </select>
                </div>
                <div class="form-group full">
                    <label for="comment">Comment</label>
                    <textarea id="comment" name="comment" th:text="${review.comment}"></textarea>
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Save changes</button>
                <a th:href="@{/admin/reviews}" class="btn btn-outline">Cancel</a>
            </div>
        </form>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/customer/payments.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>QuickBite - Payment portal</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1>Payment portal</h1>
            <p class="muted">All your payments in one place &mdash; pay pending ones here.
                Paid payments can never be paid twice (business rule).</p>
        </div>
        <a th:href="@{/customer/orders}" class="btn btn-outline">My orders</a>
    </div>

    <div class="stats-grid">
        <div class="stat-card accent-amber">
            <div class="stat-value" th:text="${#lists.size(payments)}">0</div>
            <div class="stat-label">Payments</div>
        </div>
        <div class="stat-card accent-orange">
            <div class="stat-value" th:text="${pendingCount}">0</div>
            <div class="stat-label">Waiting to be paid</div>
        </div>
    </div>

    <div class="card empty" th:if="${#lists.isEmpty(payments)}">
        <span class="big">&#128179;</span>
        No payments yet &mdash; place an order to see your payment records here.
    </div>

    <div class="table-wrap" th:unless="${#lists.isEmpty(payments)}">
        <table class="table">
            <thead>
            <tr>
                <th>Reference</th>
                <th>Order</th>
                <th>Restaurant</th>
                <th>Amount (Rs.)</th>
                <th>Method</th>
                <th>Status</th>
                <th>Paid at</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <tr th:each="payment : ${payments}">
                <td><strong th:text="${payment.transactionRef}">TXN</strong></td>
                <td><a th:href="@{/customer/orders/{id}(id=${payment.order.id})}"
                       th:text="${payment.order.orderNumber}">ORD</a></td>
                <td th:text="${payment.order.restaurant.name}">Restaurant</td>
                <td th:text="${#numbers.formatDecimal(payment.amount, 1, 'COMMA', 2, 'POINT')}">0.00</td>
                <td th:text="${payment.method.label}">Method</td>
                <td><span class="badge" th:classappend="${payment.status.cssClass}" th:text="${payment.status.label}">Status</span></td>
                <td th:text="${payment.paidAt != null ? #temporals.format(payment.paidAt, 'dd MMM, HH:mm') : '-'}">-</td>
                <td>
                    <form th:if="${payment.status.name() == 'PENDING'}"
                          th:action="@{/customer/payments/{id}/pay(id=${payment.id})}" method="post" class="inline-form">
                        <button type="submit" class="btn btn-sm">Pay now</button>
                    </form>
                    <span class="muted" th:if="${payment.status.name() == 'PAID'}">Completed &#10004;</span>
                    <span class="muted" th:if="${payment.status.name() == 'REFUNDED'}">Refunded</span>
                    <span class="muted" th:if="${payment.status.name() == 'FAILED'}">Failed</span>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="alert alert-info">
        &#128274; Payments are stored against your account only &mdash; the portal shows your own records,
        and the server rejects any attempt to open someone else's payment.
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/customer/reviews.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>QuickBite - My reviews</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1>My reviews</h1>
            <p class="muted" th:text="'Reviews written by ' + ${customer.name} + ' — saving one updates the restaurant rating.'">Reviews</p>
        </div>
        <a th:href="@{/customer/reviews/new}" class="btn">+ Write a review</a>
    </div>

    <div class="card empty" th:if="${#lists.isEmpty(reviews)}">
        <span class="big">&#11088;</span>
        You have not written any reviews yet.
        <div class="btn-row" style="justify-content:center;margin-top:14px;">
            <a th:href="@{/customer/reviews/new}" class="btn btn-orange">Write your first review</a>
        </div>
    </div>

    <div class="card-grid" th:unless="${#lists.isEmpty(reviews)}">
        <div class="card" th:each="review : ${reviews}">
            <div style="display:flex;justify-content:space-between;gap:10px;">
                <h3 th:text="${review.restaurant.name}">Restaurant</h3>
                <span class="stars" th:text="${review.ratingStars}">*****</span>
            </div>
            <p th:text="${review.comment} ?: 'No comment'">Comment</p>
            <p><span class="chip" th:text="${review.sentiment}">Sentiment</span></p>
            <div class="btn-row">
                <a th:href="@{/customer/reviews/{id}/edit(id=${review.id})}" class="btn btn-sm btn-slate">Edit</a>
                <form th:action="@{/customer/reviews/{id}/delete(id=${review.id})}" method="post" class="inline-form">
                    <button type="submit" class="btn btn-sm btn-danger">Delete</button>
                </form>
            </div>
        </div>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```

### `src/main/resources/templates/customer/review-form.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${review != null and review.id != null} ? 'QuickBite - Edit review' : 'QuickBite - Write a review'">Review form</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
<div th:replace="~{fragments/layout :: nav}"></div>

<main class="container">
    <div class="page-head">
        <div>
            <h1 th:text="${review != null and review.id != null} ? 'Edit review' : 'Write a review'">Review</h1>
            <p class="muted">Tell others how the food and the delivery were.</p>
        </div>
        <a th:href="@{/customer/reviews}" class="btn btn-outline">&larr; My reviews</a>
    </div>

    <div class="card">
        <!-- EDIT -->
        <form th:if="${review != null and review.id != null}"
              th:action="@{/customer/reviews/{id}(id=${review.id})}" method="post">
            <dl class="kv mb">
                <dt>Restaurant</dt>
                <dd th:text="${review.restaurant.name}">Restaurant</dd>
            </dl>
            <div class="form-grid">
                <div class="form-group">
                    <label for="rating">Rating (1-5 stars)</label>
                    <select id="rating" name="rating">
                        <option value="5" th:selected="${review.rating == 5}">5 - Excellent</option>
                        <option value="4" th:selected="${review.rating == 4}">4 - Very good</option>
                        <option value="3" th:selected="${review.rating == 3}">3 - Okay</option>
                        <option value="2" th:selected="${review.rating == 2}">2 - Poor</option>
                        <option value="1" th:selected="${review.rating == 1}">1 - Bad</option>
                    </select>
                </div>
                <div class="form-group full">
                    <label for="comment">Comment</label>
                    <textarea id="comment" name="comment" th:text="${review.comment}"></textarea>
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Save changes</button>
                <a th:href="@{/customer/reviews}" class="btn btn-outline">Cancel</a>
            </div>
        </form>

        <!-- CREATE -->
        <form th:unless="${review != null and review.id != null}"
              th:action="@{/customer/reviews}" method="post">
            <div class="form-grid">
                <div class="form-group">
                    <label for="restaurantId">Restaurant *</label>
                    <select id="restaurantId" name="restaurantId" required>
                        <option value="">-- select restaurant --</option>
                        <option th:each="r : ${restaurants}" th:value="${r.id}" th:text="${r.name}"
                                th:selected="${restaurantId != null and restaurantId == r.id}">Restaurant</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="rating2">Rating (1-5 stars) *</label>
                    <select id="rating2" name="rating">
                        <option value="5">5 - Excellent</option>
                        <option value="4">4 - Very good</option>
                        <option value="3">3 - Okay</option>
                        <option value="2">2 - Poor</option>
                        <option value="1">1 - Bad</option>
                    </select>
                </div>
                <div class="form-group full">
                    <label for="comment2">Comment</label>
                    <textarea id="comment2" name="comment" placeholder="How was the food and the delivery?"></textarea>
                </div>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn">Submit review</button>
                <a th:href="@{/customer/reviews}" class="btn btn-outline">Cancel</a>
            </div>
        </form>
    </div>
</main>

<div th:replace="~{fragments/layout :: footer}"></div>
</body>
</html>

```


## PART C — CSS (shared stylesheet)

All pages share `src/main/resources/static/css/style.css`. For your pages the most relevant classes are `.status-pending/paid/failed/refunded/available/on-delivery/offline`, `.kv`, `.table`. Full file below:

```css
/* ==========================================================================
   QUICKBITE - Online Food Delivery System
   Single stylesheet used by every page (pure CSS, no frameworks, no JS).
   ========================================================================== */

:root {
    --green: #16a34a;
    --green-dark: #15803d;
    --green-light: #dcfce7;
    --orange: #f97316;
    --orange-light: #ffedd5;
    --red: #dc2626;
    --red-light: #fee2e2;
    --amber: #d97706;
    --amber-light: #fef3c7;
    --blue: #2563eb;
    --blue-light: #dbeafe;
    --slate: #0f172a;
    --slate-soft: #1e293b;
    --muted: #64748b;
    --border: #e2e8f0;
    --bg: #f1f5f9;
    --card: #ffffff;
    --text: #1e293b;
    --radius: 14px;
    --shadow: 0 1px 3px rgba(15, 23, 42, .08), 0 4px 14px rgba(15, 23, 42, .06);
}

* { box-sizing: border-box; }

body {
    margin: 0;
    font-family: "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    background: var(--bg);
    color: var(--text);
    line-height: 1.55;
}

.container { max-width: 1150px; margin: 0 auto; padding: 0 20px; }
main.container { padding-top: 26px; padding-bottom: 40px; min-height: 60vh; }

h1, h2, h3, h4 { color: var(--slate); line-height: 1.25; }
h1 { font-size: 1.75rem; margin: 0 0 6px; }
h2 { font-size: 1.3rem; margin: 0 0 14px; }
h3 { font-size: 1.05rem; margin: 0 0 8px; }
a { color: var(--green-dark); text-decoration: none; }
a:hover { text-decoration: underline; }
.muted { color: var(--muted); font-size: .9rem; }
.center { text-align: center; }
.mt { margin-top: 18px; }
.mb { margin-bottom: 18px; }

/* ------------------------------- navbar --------------------------------- */
.navbar {
    background: linear-gradient(90deg, var(--slate) 0%, var(--slate-soft) 100%);
    color: #fff;
    box-shadow: var(--shadow);
}
.nav-inner {
    display: flex;
    align-items: center;
    gap: 18px;
    flex-wrap: wrap;
    padding: 12px 20px;
}
.brand {
    font-size: 1.35rem;
    font-weight: 800;
    color: #fff;
    letter-spacing: .3px;
}
.brand:hover { text-decoration: none; }
.brand span { color: var(--orange); }
.nav-links { display: flex; gap: 4px; flex-wrap: wrap; flex: 1; }
.nav-links a {
    color: #e2e8f0;
    padding: 6px 11px;
    border-radius: 8px;
    font-size: .92rem;
}
.nav-links a:hover { background: rgba(255, 255, 255, .12); text-decoration: none; color: #fff; }
.nav-actions { display: flex; align-items: center; gap: 10px; }
.user-chip { display: flex; align-items: center; gap: 8px; color: #e2e8f0; font-size: .9rem; }
.user-chip form { margin: 0; }
.cart-link { color: #fff; border-color: #475569; }
.cart-link:hover { background: rgba(255,255,255,.1); }
.badge-count {
    background: var(--orange);
    color: #fff;
    border-radius: 999px;
    padding: 1px 8px;
    font-size: .78rem;
    margin-left: 4px;
}

/* ------------------------------- alerts --------------------------------- */
.alert {
    padding: 11px 16px;
    border-radius: 10px;
    margin: 12px 0 0;
    font-size: .93rem;
    border: 1px solid transparent;
}
.alert-success { background: var(--green-light); color: #14532d; border-color: #86efac; }
.alert-error { background: var(--red-light); color: #7f1d1d; border-color: #fca5a5; }
.alert-info { background: var(--blue-light); color: #1e3a8a; border-color: #93c5fd; }

/* ------------------------------- buttons -------------------------------- */
.btn {
    display: inline-block;
    background: var(--green);
    color: #fff !important;
    border: 1px solid var(--green);
    padding: 9px 16px;
    border-radius: 10px;
    font-size: .92rem;
    font-weight: 600;
    cursor: pointer;
    text-decoration: none !important;
    transition: background .15s ease, transform .05s ease;
}
.btn:hover { background: var(--green-dark); }
.btn:active { transform: translateY(1px); }
.btn-sm { padding: 5px 11px; font-size: .82rem; border-radius: 8px; }
.btn-outline { background: transparent; color: var(--green-dark) !important; }
.btn-outline:hover { background: var(--green-light); }
.btn-orange { background: var(--orange); border-color: var(--orange); }
.btn-orange:hover { background: #ea580c; }
.btn-danger { background: var(--red); border-color: var(--red); }
.btn-danger:hover { background: #b91c1c; }
.btn-amber { background: var(--amber); border-color: var(--amber); }
.btn-amber:hover { background: #b45309; }
.btn-slate { background: var(--slate-soft); border-color: var(--slate-soft); }
.btn-slate:hover { background: #0f172a; }
.btn-row { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }
.inline { display: inline; }
.inline-form { display: inline; margin: 0; }

/* -------------------------------- cards --------------------------------- */
.card {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    box-shadow: var(--shadow);
    padding: 20px;
    margin-bottom: 20px;
}
.card.tight { padding: 14px 16px; }
.card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(265px, 1fr));
    gap: 18px;
    margin-bottom: 22px;
}
.page-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 14px;
    flex-wrap: wrap;
    margin-bottom: 16px;
}
.page-head p { margin: 0; }

/* ------------------------------- hero ----------------------------------- */
.hero {
    background: linear-gradient(120deg, var(--slate) 0%, #14532d 60%, var(--green-dark) 100%);
    color: #fff;
    border-radius: var(--radius);
    padding: 34px 30px;
    margin-bottom: 22px;
    box-shadow: var(--shadow);
}
.hero h1 { color: #fff; font-size: 2rem; margin-bottom: 8px; }
.hero p { color: #dcfce7; margin: 0 0 18px; max-width: 640px; }

/* ------------------------------ stat cards ------------------------------ */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
    gap: 14px;
    margin-bottom: 24px;
}
.stat-card {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    box-shadow: var(--shadow);
    padding: 16px 18px;
}
.stat-card .stat-value { font-size: 1.6rem; font-weight: 800; color: var(--slate); }
.stat-card .stat-label { color: var(--muted); font-size: .85rem; }
.stat-card.accent-green { border-left: 4px solid var(--green); }
.stat-card.accent-orange { border-left: 4px solid var(--orange); }
.stat-card.accent-blue { border-left: 4px solid var(--blue); }
.stat-card.accent-amber { border-left: 4px solid var(--amber); }

/* -------------------------------- tables -------------------------------- */
.table-wrap { overflow-x: auto; }
table.table {
    width: 100%;
    border-collapse: collapse;
    background: var(--card);
    border-radius: 12px;
    overflow: hidden;
    box-shadow: var(--shadow);
    font-size: .92rem;
}
.table th {
    background: var(--slate-soft);
    color: #e2e8f0;
    text-align: left;
    padding: 11px 14px;
    font-size: .82rem;
    text-transform: uppercase;
    letter-spacing: .4px;
}
.table td { padding: 11px 14px; border-bottom: 1px solid var(--border); vertical-align: middle; }
.table tr:last-child td { border-bottom: none; }
.table tbody tr:hover { background: #f8fafc; }
.table .actions { white-space: nowrap; display: flex; gap: 6px; flex-wrap: wrap; }

/* -------------------------------- badges -------------------------------- */
.badge {
    display: inline-block;
    padding: 3px 10px;
    border-radius: 999px;
    font-size: .76rem;
    font-weight: 700;
    border: 1px solid transparent;
}
.status-placed { background: var(--blue-light); color: #1e40af; border-color: #93c5fd; }
.status-confirmed { background: #e0e7ff; color: #3730a3; border-color: #a5b4fc; }
.status-preparing { background: var(--amber-light); color: #92400e; border-color: #fcd34d; }
.status-out-for-delivery { background: var(--orange-light); color: #9a3412; border-color: #fdba74; }
.status-delivered { background: var(--green-light); color: #166534; border-color: #86efac; }
.status-cancelled { background: var(--red-light); color: #991b1b; border-color: #fca5a5; }
.status-pending { background: var(--amber-light); color: #92400e; border-color: #fcd34d; }
.status-paid { background: var(--green-light); color: #166534; border-color: #86efac; }
.status-failed { background: var(--red-light); color: #991b1b; border-color: #fca5a5; }
.status-refunded { background: #e2e8f0; color: #334155; border-color: #cbd5e1; }
.status-available { background: var(--green-light); color: #166534; border-color: #86efac; }
.status-on-delivery { background: var(--orange-light); color: #9a3412; border-color: #fdba74; }
.status-offline { background: #e2e8f0; color: #334155; border-color: #cbd5e1; }
.chip {
    display: inline-block;
    background: var(--bg);
    border: 1px solid var(--border);
    border-radius: 999px;
    padding: 2px 10px;
    font-size: .76rem;
    color: var(--muted);
}
.chip-veg { background: var(--green-light); color: #166534; border-color: #86efac; }

/* ------------------------------ menu cards ------------------------------ */
.dish-card { display: flex; flex-direction: column; }
.dish-card .dish-head { display: flex; justify-content: space-between; gap: 10px; align-items: flex-start; }
.dish-card .price { font-weight: 800; color: var(--green-dark); font-size: 1.05rem; white-space: nowrap; }
.dish-card p { color: var(--muted); font-size: .88rem; margin: 6px 0 10px; flex: 1; }
.dish-card .dish-actions { display: flex; gap: 8px; align-items: center; }
.qty-input {
    width: 62px;
    padding: 7px 8px;
    border: 1px solid var(--border);
    border-radius: 8px;
    font-size: .9rem;
}
.stars { color: #f59e0b; letter-spacing: 2px; font-size: 1rem; }

/* -------------------------------- forms --------------------------------- */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px 18px; }
.form-grid .full { grid-column: 1 / -1; }
@media (max-width: 700px) { .form-grid { grid-template-columns: 1fr; } }
.form-group label { display: block; font-weight: 600; font-size: .88rem; margin-bottom: 5px; color: var(--slate); }
.form-group input,
.form-group select,
.form-group textarea {
    width: 100%;
    padding: 9px 12px;
    border: 1px solid var(--border);
    border-radius: 10px;
    font-size: .93rem;
    font-family: inherit;
    background: #fff;
    color: var(--text);
}
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
    outline: 2px solid var(--green);
    outline-offset: 1px;
    border-color: var(--green);
}
.form-group textarea { resize: vertical; min-height: 84px; }
.check-row { display: flex; align-items: center; gap: 8px; margin-top: 26px; }
.check-row input { width: auto; }
.form-actions { margin-top: 18px; display: flex; gap: 10px; flex-wrap: wrap; }
.filter-form {
    display: flex;
    gap: 10px;
    align-items: flex-end;
    flex-wrap: wrap;
    margin-bottom: 16px;
}
.filter-form .form-group { margin: 0; min-width: 200px; }

/* ------------------------------- totals --------------------------------- */
.totals { max-width: 420px; margin-left: auto; }
.totals .row { display: flex; justify-content: space-between; padding: 7px 0; border-bottom: 1px dashed var(--border); }
.totals .row:last-child { border-bottom: none; font-weight: 800; font-size: 1.1rem; color: var(--slate); }
.totals .grand { color: var(--green-dark); }

/* ------------------------------ empty state ----------------------------- */
.empty {
    text-align: center;
    padding: 44px 20px;
    color: var(--muted);
    background: var(--card);
    border: 1px dashed var(--border);
    border-radius: var(--radius);
}
.empty .big { font-size: 2.4rem; display: block; margin-bottom: 8px; }

/* -------------------------------- footer -------------------------------- */
.footer {
    background: var(--slate);
    color: #94a3b8;
    padding: 22px 0;
    margin-top: 30px;
    font-size: .88rem;
}
.footer p { margin: 4px 0; }
.footer .muted { color: #64748b; }

/* ------------------------------ misc bits ------------------------------- */
.avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: var(--green-light);
    color: var(--green-dark);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 800;
    font-size: 1rem;
}
.kv { display: grid; grid-template-columns: 170px 1fr; gap: 6px 14px; font-size: .93rem; }
.kv dt { color: var(--muted); }
.kv dd { margin: 0; font-weight: 600; }
.section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 26px 0 12px;
}
.section-title h2 { margin: 0; }
.restaurant-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
    border-bottom: 2px solid var(--border);
    padding-bottom: 8px;
    margin-bottom: 14px;
}

/* ==========================================================================
   ROLE-BASED UI - single login page, admin & customer areas
   ========================================================================== */

/* role badges shown in the navbar */
.role-chip {
    display: inline-block;
    padding: 3px 10px;
    border-radius: 999px;
    font-size: .72rem;
    font-weight: 800;
    letter-spacing: .5px;
}
.role-chip-admin { background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; }
.role-chip-customer { background: #dcfce7; color: #166534; border: 1px solid #86efac; }

/* ---------- sign-in page ---------- */
.auth-wrap { display: flex; justify-content: center; padding: 12px 0 30px; }
.auth-card { width: 100%; max-width: 560px; }
.auth-split {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin-top: 18px;
}
@media (max-width: 560px) { .auth-split { grid-template-columns: 1fr; } }
.auth-box {
    border: 1px solid var(--border);
    border-radius: 12px;
    padding: 12px 14px;
    background: #f8fafc;
}
.auth-box p { margin: 4px 0 0; font-size: .82rem; }
.auth-box-admin { border-left: 4px solid var(--red); }
.auth-box-customer { border-left: 4px solid var(--green); }

/* ---------- clickable shortcut cards ---------- */
.quick-link { display: block; color: inherit; }
.quick-link:hover {
    text-decoration: none;
    border-color: var(--green);
    box-shadow: 0 6px 18px rgba(22, 163, 74, .18);
    transform: translateY(-1px);
}
.quick-link h3 { color: var(--green-dark); }

/* little "hashed" chip */
.hash-chip { background: #eef2ff; border-color: #c7d2fe; color: #3730a3; }

/* ---------- sign-up page: required-field marker ---------- */
.required { color: var(--red); font-weight: 700; }

```
