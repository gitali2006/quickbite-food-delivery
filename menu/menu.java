## PART A — BACKEND (Java)

### `src/main/java/com/example/fooddelivery/model/MenuItem.java`
```java
package com.example.fooddelivery.model;

import com.example.fooddelivery.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * MEMBER 3 - MENU MODULE
 * OOP: INHERITANCE from BaseEntity, ENCAPSULATION (price can only change
 *      through methods), ASSOCIATION with Restaurant.
 */
@Entity
@Table(name = "menu_items")
public class MenuItem extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    private String category;      // Starters, Main, Dessert, Drinks

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    private boolean available = true;

    private boolean vegetarian = false;

    /** Many menu items belong to one restaurant. */
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    public MenuItem() {
    }

    public MenuItem(String name, String category, BigDecimal price, Restaurant restaurant) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.restaurant = restaurant;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    /** BUSINESS LOGIC inside the object (ENCAPSULATION): price of N portions. */
    public BigDecimal totalFor(int quantity) {
        if (quantity <= 0) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public String getAvailabilityLabel() {
        return available ? "Available" : "Sold out";
    }

    // ---------- Getters / Setters ----------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}

```

### `src/main/java/com/example/fooddelivery/repository/MenuItemRepository.java`
```java
package com.example.fooddelivery.repository;

import com.example.fooddelivery.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** MEMBER 3 - data access for menu items. */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    List<MenuItem> findByNameContainingIgnoreCase(String name);

    List<MenuItem> findByCategoryIgnoreCase(String category);

    long countByRestaurantId(Long restaurantId);
}

```

### `src/main/java/com/example/fooddelivery/service/MenuItemService.java`
```java
package com.example.fooddelivery.service;

import com.example.fooddelivery.model.MenuItem;
import com.example.fooddelivery.model.Restaurant;

import java.util.List;

public interface MenuItemService {

    List<MenuItem> findAll();

    List<MenuItem> findByRestaurant(Long restaurantId);

    List<MenuItem> findAvailableByRestaurant(Long restaurantId);

    List<MenuItem> search(String keyword);

    List<MenuItem> findByCategory(String category);

    MenuItem findById(Long id);

    MenuItem save(MenuItem menuItem);

    void deleteById(Long id);

    void toggleAvailability(Long id);

    long count();

    /** Every dish grouped by its restaurant (used by the customer menu page). */
    java.util.Map<Restaurant, List<MenuItem>> groupByRestaurant();
}

```

### `src/main/java/com/example/fooddelivery/service/impl/MenuItemServiceImpl.java`
```java
package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.common.ResourceNotFoundException;
import com.example.fooddelivery.model.MenuItem;
import com.example.fooddelivery.model.Restaurant;
import com.example.fooddelivery.repository.MenuItemRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.example.fooddelivery.service.MenuItemService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** MEMBER 3 - business logic for the Menu module. */
@Service
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemServiceImpl(MenuItemRepository menuItemRepository,
                               RestaurantRepository restaurantRepository) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public List<MenuItem> findAll() {
        return menuItemRepository.findAll();
    }

    @Override
    public List<MenuItem> findByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    @Override
    public List<MenuItem> findAvailableByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId);
    }

    @Override
    public List<MenuItem> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return menuItemRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public List<MenuItem> findByCategory(String category) {
        if (category == null || category.isBlank()) {
            return findAll();
        }
        return menuItemRepository.findByCategoryIgnoreCase(category.trim());
    }

    @Override
    public MenuItem findById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Menu item", id));
    }

    @Override
    public MenuItem save(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    @Override
    public void deleteById(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Menu item", id);
        }
        menuItemRepository.deleteById(id);
    }

    @Override
    public void toggleAvailability(Long id) {
        MenuItem item = findById(id);
        item.setAvailable(!item.isAvailable());
        menuItemRepository.save(item);
    }

    @Override
    public long count() {
        return menuItemRepository.count();
    }

    /** Groups menu items so the menu page can show one section per restaurant. */
    @Override
    public Map<Restaurant, List<MenuItem>> groupByRestaurant() {
        Map<Restaurant, List<MenuItem>> grouped = new LinkedHashMap<>();
        for (Restaurant restaurant : restaurantRepository.findAll()) {
            List<MenuItem> items = menuItemRepository.findByRestaurantId(restaurant.getId());
            if (!items.isEmpty()) {
                grouped.put(restaurant, items);
            }
        }
        return grouped;
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/MenuItemController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.model.MenuItem;
import com.example.fooddelivery.service.MenuItemService;
import com.example.fooddelivery.service.RestaurantService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * MENU
 * ----
 * PUBLIC  (anyone):        GET  /menu            -> browse dishes grouped per restaurant
 * ADMIN   (/admin/menu/**): full CRUD for dishes  [protected]
 *
 * CREATE -> GET /admin/menu/new + POST /admin/menu
 * READ   -> GET /admin/menu (search + restaurant filter)
 * UPDATE -> GET /admin/menu/{id}/edit + POST /admin/menu/{id}
 * DELETE -> POST /admin/menu/{id}/delete
 */
@Controller
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final RestaurantService restaurantService;

    public MenuItemController(MenuItemService menuItemService, RestaurantService restaurantService) {
        this.menuItemService = menuItemService;
        this.restaurantService = restaurantService;
    }

    // ------------------------------------------------------- PUBLIC (everyone)
    /** /menu = open to everyone (guests see "sign in to order");
     *  /customer/menu = the same page for a signed-in customer, with working Add-to-cart buttons. */
    @GetMapping({"/menu", "/customer/menu"})
    public String publicMenu(Model model) {
        model.addAttribute("menuByRestaurant", menuItemService.groupByRestaurant());
        return "menu/menu";
    }

    // ------------------------------------------------------- ADMIN AREA
    @GetMapping("/admin/menu")
    public String manage(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long restaurantId,
                         Model model) {
        if (restaurantId != null) {
            model.addAttribute("menuItems", menuItemService.findByRestaurant(restaurantId));
        } else {
            model.addAttribute("menuItems", menuItemService.search(keyword));
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("restaurants", restaurantService.findAll());
        return "menu/manage";
    }

    @GetMapping("/admin/menu/new")
    public String createForm(Model model) {
        model.addAttribute("menuItem", new MenuItem());
        model.addAttribute("restaurants", restaurantService.findAll());
        return "menu/form";
    }

    @PostMapping("/admin/menu")
    public String create(@ModelAttribute("menuItem") MenuItem menuItem,
                         @RequestParam(required = false) Long restaurantId,
                         RedirectAttributes redirectAttributes) {
        if (menuItem.getName() == null || menuItem.getName().isBlank() || restaurantId == null
                || menuItem.getPrice() == null) {
            redirectAttributes.addFlashAttribute("error", "Name, price and restaurant are required.");
            return "redirect:/admin/menu/new";
        }
        if (menuItem.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            redirectAttributes.addFlashAttribute("error", "Price cannot be negative.");
            return "redirect:/admin/menu/new";
        }
        menuItem.setRestaurant(restaurantService.findById(restaurantId));
        menuItemService.save(menuItem);
        redirectAttributes.addFlashAttribute("success", "\"" + menuItem.getName() + "\" was added to the menu.");
        return "redirect:/admin/menu";
    }

    @GetMapping("/admin/menu/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("menuItem", menuItemService.findById(id));
        model.addAttribute("restaurants", restaurantService.findAll());
        return "menu/form";
    }

    @PostMapping("/admin/menu/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("menuItem") MenuItem menuItem,
                         @RequestParam(required = false) Long restaurantId,
                         RedirectAttributes redirectAttributes) {
        MenuItem existing = menuItemService.findById(id);
        existing.setName(menuItem.getName());
        existing.setDescription(menuItem.getDescription());
        existing.setCategory(menuItem.getCategory());
        existing.setPrice(menuItem.getPrice());
        existing.setVegetarian(menuItem.isVegetarian());
        existing.setAvailable(menuItem.isAvailable());
        if (restaurantId != null) {
            existing.setRestaurant(restaurantService.findById(restaurantId));
        }
        menuItemService.save(existing);
        redirectAttributes.addFlashAttribute("success", "Menu item updated.");
        return "redirect:/admin/menu";
    }

    @PostMapping("/admin/menu/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            menuItemService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Menu item deleted.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error",
                    "This dish is used in existing orders, so it cannot be deleted. Mark it as 'sold out' instead.");
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/admin/menu/{id}/toggle")
    public String toggleAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        menuItemService.toggleAvailability(id);
        redirectAttributes.addFlashAttribute("success", "Availability changed.");
        return "redirect:/admin/menu";
    }
}

```
