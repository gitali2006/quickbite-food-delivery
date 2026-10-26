## PART A — BACKEND (Java)

### `src/main/java/com/example/fooddelivery/model/Restaurant.java`
```java
package com.example.fooddelivery.model;

import com.example.fooddelivery.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * MEMBER 2 - RESTAURANT MODULE
 * OOP: INHERITANCE from BaseEntity, ENCAPSULATION, and a computed method
 *      (getRatingStars) that hides formatting logic inside the object.
 */
@Entity
@Table(name = "restaurants")
public class Restaurant extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String cuisineType;     // Sri Lankan, Chinese, Italian...

    private String address;

    private String phone;

    private double rating = 4.0;

    private boolean open = true;

    @Column(length = 500)
    private String description;

    /** One restaurant has many menu items -> ASSOCIATION (OOP relationship). */
    @OneToMany(mappedBy = "restaurant")
    private List<MenuItem> menuItems = new ArrayList<>();

    public Restaurant() {
    }

    public Restaurant(String name, String cuisineType, String address) {
        this.name = name;
        this.cuisineType = cuisineType;
        this.address = address;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    /** Turns 4.3 into "★★★★☆" - the frontend does not need any logic. */
    public String getRatingStars() {
        int full = (int) Math.round(rating);
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < full ? "\u2605" : "\u2606");
        }
        return stars.toString();
    }

    public String getStatusLabel() {
        return open ? "Open" : "Closed";
    }

    // ---------- Getters / Setters ----------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }
}

```

### `src/main/java/com/example/fooddelivery/repository/RestaurantRepository.java`
```java
package com.example.fooddelivery.repository;

import com.example.fooddelivery.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** MEMBER 2 - data access for restaurants. */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByNameContainingIgnoreCase(String name);

    List<Restaurant> findByCuisineTypeIgnoreCase(String cuisineType);

    List<Restaurant> findByOpenTrue();
}

```

### `src/main/java/com/example/fooddelivery/service/RestaurantService.java`
```java
package com.example.fooddelivery.service;

import com.example.fooddelivery.model.Restaurant;

import java.util.List;

public interface RestaurantService {

    List<Restaurant> findAll();

    List<Restaurant> search(String keyword);

    List<Restaurant> findOpen();

    Restaurant findById(Long id);

    Restaurant save(Restaurant restaurant);

    void deleteById(Long id);

    void toggleOpen(Long id);

    long count();
}

```

### `src/main/java/com/example/fooddelivery/service/impl/RestaurantServiceImpl.java`
```java
package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.common.ResourceNotFoundException;
import com.example.fooddelivery.model.Restaurant;
import com.example.fooddelivery.repository.MenuItemRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.example.fooddelivery.service.RestaurantService;
import org.springframework.stereotype.Service;

import java.util.List;

/** MEMBER 2 - business logic for the Restaurant module. */
@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    @Override
    public List<Restaurant> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return restaurantRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public List<Restaurant> findOpen() {
        return restaurantRepository.findByOpenTrue();
    }

    @Override
    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Restaurant", id));
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    /** Business rule: a restaurant with dishes in its menu cannot be deleted. */
    @Override
    public void deleteById(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Restaurant", id);
        }
        long dishes = menuItemRepository.countByRestaurantId(id);
        if (dishes > 0) {
            throw new IllegalStateException(
                    "This restaurant still has " + dishes + " menu item(s). Delete the menu items first.");
        }
        restaurantRepository.deleteById(id);
    }

    @Override
    public void toggleOpen(Long id) {
        Restaurant restaurant = findById(id);
        restaurant.setOpen(!restaurant.isOpen());
        restaurantRepository.save(restaurant);
    }

    @Override
    public long count() {
        return restaurantRepository.count();
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/RestaurantController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.model.Restaurant;
import com.example.fooddelivery.service.MenuItemService;
import com.example.fooddelivery.service.RestaurantService;
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
 * MEMBER 2 - RESTAURANT MODULE
 * CREATE -> GET /admin/restaurants/new + POST /restaurants
 * READ   -> GET /admin/restaurants, GET /admin/restaurants/{id}
 * UPDATE -> GET /admin/restaurants/{id}/edit + POST /restaurants/{id}
 * DELETE -> POST /restaurants/{id}/delete
 */
@Controller
@RequestMapping("/admin/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;

    public RestaurantController(RestaurantService restaurantService, MenuItemService menuItemService) {
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("restaurants", restaurantService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "restaurants/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("restaurant", new Restaurant());
        return "restaurants/form";
    }

    @PostMapping
    public String create(@ModelAttribute("restaurant") Restaurant restaurant,
                         RedirectAttributes redirectAttributes) {
        if (restaurant.getName() == null || restaurant.getName().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Restaurant name is required.");
            return "redirect:/admin/restaurants/new";
        }
        restaurantService.save(restaurant);
        redirectAttributes.addFlashAttribute("success", "Restaurant \"" + restaurant.getName() + "\" added.");
        return "redirect:/admin/restaurants";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Restaurant restaurant = restaurantService.findById(id);
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("menuItems", menuItemService.findByRestaurant(id));
        return "restaurants/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("restaurant", restaurantService.findById(id));
        return "restaurants/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("restaurant") Restaurant restaurant,
                         RedirectAttributes redirectAttributes) {
        Restaurant existing = restaurantService.findById(id);
        existing.setName(restaurant.getName());
        existing.setCuisineType(restaurant.getCuisineType());
        existing.setAddress(restaurant.getAddress());
        existing.setPhone(restaurant.getPhone());
        existing.setRating(restaurant.getRating());
        existing.setDescription(restaurant.getDescription());
        existing.setOpen(restaurant.isOpen());
        restaurantService.save(existing);
        redirectAttributes.addFlashAttribute("success", "Restaurant updated.");
        return "redirect:/admin/restaurants/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            restaurantService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Restaurant deleted.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/restaurants";
    }

    @PostMapping("/{id}/toggle")
    public String toggleOpen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        restaurantService.toggleOpen(id);
        redirectAttributes.addFlashAttribute("success", "Restaurant open/closed status changed.");
        return "redirect:/admin/restaurants";
    }
}

```
