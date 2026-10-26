/*---

## PART A — BACKEND (Java)

### `src/main/java/com/example/fooddelivery/model/CartItem.java`
```java
package com.example.fooddelivery.model;

import com.example.fooddelivery.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * MEMBER 4 - CART MODULE
 * One row = "this customer wants N portions of this menu item".
 */
@Entity
@Table(name = "cart_items")
public class CartItem extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    private int quantity = 1;

    public CartItem() {
    }

    public CartItem(Customer customer, MenuItem menuItem, int quantity) {
        this.customer = customer;
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    @Override
    public String getDisplayName() {
        return menuItem != null ? menuItem.getName() + " x" + quantity : "Cart item";
    }

    /** Line total = unit price x quantity. */
    public BigDecimal getLineTotal() {
        if (menuItem == null) {
            return BigDecimal.ZERO;
        }
        return menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    // ---------- Getters / Setters ----------

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

```

### `src/main/java/com/example/fooddelivery/repository/CartItemRepository.java`
```java
package com.example.fooddelivery.repository;

import com.example.fooddelivery.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** MEMBER 4 - data access for cart items. */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCustomerId(Long customerId);

    Optional<CartItem> findByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);

    void deleteByCustomerId(Long customerId);

    long countByCustomerId(Long customerId);
}

```

### `src/main/java/com/example/fooddelivery/service/CartService.java`
```java
package com.example.fooddelivery.service;

import com.example.fooddelivery.model.CartItem;
import com.example.fooddelivery.model.MenuItem;
import com.example.fooddelivery.model.Restaurant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartService {

    List<CartItem> getCartItems(Long customerId);

    CartItem addToCart(Long customerId, Long menuItemId, int quantity);

    CartItem updateQuantity(Long cartItemId, int quantity);

    void removeItem(Long cartItemId);

    void clearCart(Long customerId);

    BigDecimal getCartTotal(Long customerId);

    int getCartItemCount(Long customerId);

    Optional<Restaurant> getCartRestaurant(Long customerId);

    /** Delivery fee rule: free above Rs. 3000, otherwise Rs. 300. */
    BigDecimal getDeliveryFee(Long customerId);

    BigDecimal getGrandTotal(Long customerId);

    List<MenuItem> suggestionsForCart(Long customerId);
}

```

### `src/main/java/com/example/fooddelivery/service/impl/CartServiceImpl.java`
```java
package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.common.ResourceNotFoundException;
import com.example.fooddelivery.model.CartItem;
import com.example.fooddelivery.model.Customer;
import com.example.fooddelivery.model.MenuItem;
import com.example.fooddelivery.model.Restaurant;
import com.example.fooddelivery.repository.CartItemRepository;
import com.example.fooddelivery.repository.CustomerRepository;
import com.example.fooddelivery.repository.MenuItemRepository;
import com.example.fooddelivery.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MEMBER 4 - business logic for the Cart module.
 * Rules enforced here:
 *  - one cart can only contain items from ONE restaurant (like real delivery apps)
 *  - a sold-out item cannot be added
 *  - adding the same dish twice increases the quantity instead of duplicating the row
 */
@Service
public class CartServiceImpl implements CartService {

    private static final BigDecimal DELIVERY_FEE = new BigDecimal("300");
    private static final BigDecimal FREE_DELIVERY_ABOVE = new BigDecimal("3000");

    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final MenuItemRepository menuItemRepository;

    public CartServiceImpl(CartItemRepository cartItemRepository,
                           CustomerRepository customerRepository,
                           MenuItemRepository menuItemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public List<CartItem> getCartItems(Long customerId) {
        return cartItemRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional
    public CartItem addToCart(Long customerId, Long menuItemId, int quantity) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", customerId));
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> ResourceNotFoundException.of("Menu item", menuItemId));

        if (!menuItem.isAvailable()) {
            throw new IllegalStateException(menuItem.getName() + " is sold out right now.");
        }
        if (quantity < 1) {
            quantity = 1;
        }

        // Rule: only one restaurant per cart.
        Optional<Restaurant> current = getCartRestaurant(customerId);
        if (current.isPresent() && !current.get().getId().equals(menuItem.getRestaurant().getId())) {
            cartItemRepository.deleteByCustomerId(customerId);
        }

        Optional<CartItem> existing =
                cartItemRepository.findByCustomerIdAndMenuItemId(customerId, menuItemId);
        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            cartItem.increaseQuantity(quantity);
            return cartItemRepository.save(cartItem);
        }

        CartItem cartItem = new CartItem(customer, menuItem, quantity);
        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem updateQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> ResourceNotFoundException.of("Cart item", cartItemId));
        if (quantity < 1) {
            cartItemRepository.delete(cartItem);
            return null;
        }
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Override
    public void removeItem(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw ResourceNotFoundException.of("Cart item", cartItemId);
        }
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        cartItemRepository.deleteByCustomerId(customerId);
    }

    @Override
    public BigDecimal getCartTotal(Long customerId) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : getCartItems(customerId)) {
            total = total.add(item.getLineTotal());
        }
        return total;
    }

    @Override
    public int getCartItemCount(Long customerId) {
        int count = 0;
        for (CartItem item : getCartItems(customerId)) {
            count += item.getQuantity();
        }
        return count;
    }

    @Override
    public Optional<Restaurant> getCartRestaurant(Long customerId) {
        List<CartItem> items = getCartItems(customerId);
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(items.get(0).getMenuItem().getRestaurant());
    }

    @Override
    public BigDecimal getDeliveryFee(Long customerId) {
        BigDecimal total = getCartTotal(customerId);
        if (total.signum() == 0 || total.compareTo(FREE_DELIVERY_ABOVE) >= 0) {
            return BigDecimal.ZERO;
        }
        return DELIVERY_FEE;
    }

    @Override
    public BigDecimal getGrandTotal(Long customerId) {
        return getCartTotal(customerId).add(getDeliveryFee(customerId));
    }

    /** "You may also like" - cheap dishes from the same restaurant, not yet in the cart. */
    @Override
    public List<MenuItem> suggestionsForCart(Long customerId) {
        Optional<Restaurant> restaurant = getCartRestaurant(customerId);
        if (restaurant.isEmpty()) {
            return List.of();
        }
        List<Long> inCart = new ArrayList<>();
        for (CartItem item : getCartItems(customerId)) {
            inCart.add(item.getMenuItem().getId());
        }
        List<MenuItem> suggestions = new ArrayList<>();
        for (MenuItem item : menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurant.get().getId())) {
            if (!inCart.contains(item.getId()) && suggestions.size() < 4) {
                suggestions.add(item);
            }
        }
        return suggestions;
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/CustomerCartController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.common.SessionHelper;
import com.example.fooddelivery.model.Customer;
import com.example.fooddelivery.model.Order;
import com.example.fooddelivery.model.Payment;
import com.example.fooddelivery.model.enums.PaymentMethod;
import com.example.fooddelivery.service.CartService;
import com.example.fooddelivery.service.CustomerService;
import com.example.fooddelivery.service.OrderService;
import com.example.fooddelivery.service.PaymentService;
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
 * CART & CHECKOUT   (Member 4)   -   /customer/cart/**  and  /customer/checkout
 * ---------------------------------------------------------------------------
 * The shopping part of the customer area (protected, CUSTOMER role only):
 *
 *   GET  /customer/cart            -> cart page: lines, money summary, suggestions
 *   POST /customer/cart/add        -> add a dish (CREATE)
 *   POST /customer/cart/update     -> change a line's quantity (UPDATE)
 *   POST /customer/cart/remove/{id}-> remove one line (DELETE)
 *   POST /customer/cart/clear      -> empty the whole cart (DELETE all)
 *   GET  /customer/checkout        -> checkout form: address + payment method
 *   POST /customer/checkout        -> places the order (Member 5) + creates the payment (Member 6)
 *
 * All business rules (one restaurant per cart, sold-out blocked, free delivery over Rs. 3,000)
 * live inside CartServiceImpl and OrderServiceImpl - the controller only moves data around.
 */
@Controller
@RequestMapping("/customer")
public class CustomerCartController {

    private final CustomerService customerService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public CustomerCartController(CustomerService customerService,
                                  CartService cartService,
                                  OrderService orderService,
                                  PaymentService paymentService) {
        this.customerService = customerService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    /** The customer id can never be null here - the interceptor guarantees a CUSTOMER session. */
    private Long customerId(HttpSession session) {
        return SessionHelper.getUserId(session);
    }

    // ---------------------------------------------------------------- CART
    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        Long id = customerId(session);
        model.addAttribute("cartItems", cartService.getCartItems(id));
        model.addAttribute("subtotal", cartService.getCartTotal(id));
        model.addAttribute("deliveryFee", cartService.getDeliveryFee(id));
        model.addAttribute("grandTotal", cartService.getGrandTotal(id));
        model.addAttribute("restaurant", cartService.getCartRestaurant(id).orElse(null));
        model.addAttribute("suggestions", cartService.suggestionsForCart(id));
        return "customer/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long menuItemId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(customerId(session), menuItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Item added to your cart.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/menu";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Long cartItemId,
                             @RequestParam int quantity,
                             RedirectAttributes redirectAttributes) {
        cartService.updateQuantity(cartItemId, quantity);
        redirectAttributes.addFlashAttribute("success", "Cart updated.");
        return "redirect:/customer/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        cartService.removeItem(id);
        redirectAttributes.addFlashAttribute("success", "Item removed from cart.");
        return "redirect:/customer/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        cartService.clearCart(customerId(session));
        redirectAttributes.addFlashAttribute("success", "Cart cleared.");
        return "redirect:/customer/cart";
    }

    // ------------------------------------------------------------- CHECKOUT
    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long id = customerId(session);
        if (cartService.getCartItems(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty - add some food first!");
            return "redirect:/customer/cart";
        }
        Customer customer = customerService.findById(id);
        model.addAttribute("cartItems", cartService.getCartItems(id));
        model.addAttribute("subtotal", cartService.getCartTotal(id));
        model.addAttribute("deliveryFee", cartService.getDeliveryFee(id));
        model.addAttribute("grandTotal", cartService.getGrandTotal(id));
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("customer", customer);
        return "customer/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String deliveryAddress,
                             @RequestParam(required = false) String notes,
                             @RequestParam PaymentMethod paymentMethod,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Long id = customerId(session);
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Delivery address is required.");
            return "redirect:/customer/checkout";
        }
        try {
            Order order = orderService.placeOrderFromCart(id, deliveryAddress, notes);
            Payment payment = paymentService.createForOrder(order, paymentMethod);
            redirectAttributes.addFlashAttribute("success",
                    "Order " + order.getOrderNumber() + " placed! Payment status: " + payment.getStatus().getLabel() + ".");
            return "redirect:/customer/orders/" + order.getId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/customer/cart";
        }
    }
}

```