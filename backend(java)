## PART A — BACKEND (Java)

### `src/main/java/com/example/fooddelivery/model/Customer.java`
```java
package com.example.fooddelivery.model;

import com.example.fooddelivery.common.Person;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * CUSTOMER ACCOUNT
 * OOP: INHERITANCE (BaseEntity -> Person -> Customer), POLYMORPHISM (getRole),
 *      ENCAPSULATION (private fields + getters/setters).
 *
 * SECURITY: the database only ever stores the BCrypt HASH of the password.
 * `password` is TRANSIENT - it carries the raw value from a form until the
 * service hashes it, and is never persisted.
 */
@Entity
@Table(name = "customers")
public class Customer extends Person {

    /** BCrypt hash stored in MySQL (never the plain password). */
    @Column(nullable = false, length = 100)
    private String passwordHash;

    /** Raw password from the form only - NOT a database column. */
    @Transient
    private String password;

    private int loyaltyPoints = 0;

    private boolean active = true;

    public Customer() {
    }

    /** Constructor OVERLOADING (OOP): quick creation in code/tests. */
    public Customer(String name, String email, String password) {
        setName(name);
        setEmail(email);
        this.password = password;
    }

    public Customer(String name, String email, String password, String phone, String address) {
        this(name, email, password);
        setPhone(phone);
        setAddress(address);
    }

    /** POLYMORPHISM: different implementation from Driver. */
    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    @Override
    public String getDisplayName() {
        return getId() == null ? getName() : getName() + " (Customer #" + getId() + ")";
    }

    /** Extra loyalty point for every completed order. */
    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
    }

    public String getInitials() {
        if (getName() == null || getName().isBlank()) {
            return "?";
        }
        String[] parts = getName().trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    // ---------- Getters / Setters ----------

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

```

### `src/main/java/com/example/fooddelivery/model/Admin.java`
```java
package com.example.fooddelivery.model;

import com.example.fooddelivery.common.Person;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * ADMINISTRATOR ACCOUNT  (NEW)
 * ----------------------------
 * OOP: INHERITANCE (BaseEntity -> Person -> Admin, the same branch as Customer
 * and Driver) and POLYMORPHISM (getRole() returns "ADMIN").
 *
 * SECURITY: only the BCrypt hash is stored in the database.
 * `password` is TRANSIENT (not a column) - it only carries the raw value
 * from the form until the service hashes it.
 */
@Entity
@Table(name = "admins")
public class Admin extends Person {

    /** BCrypt hash - this is what MySQL stores (60 characters). */
    @Column(nullable = false, length = 100)
    private String passwordHash;

    private boolean active = true;

    /** Raw password coming from a form - never saved to the database. */
    @Transient
    private String password;

    public Admin() {
    }

    public Admin(String name, String email, String password) {
        setName(name);
        setEmail(email);
        this.password = password;
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public String getDisplayName() {
        return getName() + " (Administrator)";
    }

    public String getInitials() {
        if (getName() == null || getName().isBlank()) {
            return "A";
        }
        String[] parts = getName().trim().split("\\s+");
        return parts.length == 1
                ? parts[0].substring(0, 1).toUpperCase()
                : (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    // ---------- Getters / Setters ----------

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

```

### `src/main/java/com/example/fooddelivery/repository/CustomerRepository.java`
```java
package com.example.fooddelivery.repository;

import com.example.fooddelivery.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** MEMBER 1 - data access for customers (CRUD comes free from JpaRepository). */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Customer> findByNameContainingIgnoreCase(String name);

    List<Customer> findByActiveTrue();
}

```

### `src/main/java/com/example/fooddelivery/repository/AdminRepository.java`
```java
package com.example.fooddelivery.repository;

import com.example.fooddelivery.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Data access for administrator accounts. */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);
}

```

### `src/main/java/com/example/fooddelivery/service/CustomerService.java`
```java
package com.example.fooddelivery.service;

import com.example.fooddelivery.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * OOP CONCEPT: ABSTRACTION
 * The controller only talks to this INTERFACE - it never knows (or cares)
 * whether the data comes from a database, a file or a test fake.
 * The implementation is in CustomerServiceImpl.
 */
public interface CustomerService {

    List<Customer> findAll();

    Customer findById(Long id);

    Customer save(Customer customer);

    Customer register(Customer customer);

    Customer update(Long id, Customer form);

    void deleteById(Long id);

    Optional<Customer> authenticate(String email, String password);

    List<Customer> search(String keyword);

    void toggleActive(Long id);

    long count();
}

```

### `src/main/java/com/example/fooddelivery/service/impl/CustomerServiceImpl.java`
```java
package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.common.ResourceNotFoundException;
import com.example.fooddelivery.model.Customer;
import com.example.fooddelivery.repository.AdminRepository;
import com.example.fooddelivery.repository.CustomerRepository;
import com.example.fooddelivery.service.CustomerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for the Customer module.
 * SECURITY: passwords are hashed with BCrypt - plain passwords are never stored.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;      // keep e-mails unique across both tables
    private final PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               AdminRepository adminRepository,
                               PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
    }

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    /** CREATE with business rules: unique e-mail + BCrypt hashed password. */
    @Override
    public Customer register(Customer customer) {
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalStateException("E-mail is required.");
        }
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalStateException("A customer with the e-mail " + customer.getEmail() + " already exists.");
        }
        if (adminRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalStateException("This e-mail is already used by an administrator account.");
        }
        String raw = customer.getPassword();
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Password is required.");
        }

        customer.setPasswordHash(passwordEncoder.encode(raw));  // <<< BCrypt hashing
        customer.setPassword(null);                             // drop the raw value
        customer.setActive(true);
        customer.setLoyaltyPoints(0);
        return customerRepository.save(customer);
    }

    /** UPDATE: copies fields; only re-hashes when a new password was typed. */
    @Override
    public Customer update(Long id, Customer form) {
        Customer existing = findById(id);

        if (form.getEmail() != null && !form.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (customerRepository.existsByEmail(form.getEmail())
                    || adminRepository.existsByEmail(form.getEmail())) {
                throw new IllegalStateException("This e-mail is already in use.");
            }
            existing.setEmail(form.getEmail());
        }
        existing.setName(form.getName());
        existing.setPhone(form.getPhone());
        existing.setAddress(form.getAddress());

        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        return customerRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        if (!customerRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Customer", id);
        }
        customerRepository.deleteById(id);
    }

    /** Login check: BCrypt compares the typed password with the stored hash. */
    @Override
    public Optional<Customer> authenticate(String email, String password) {
        return customerRepository.findByEmail(email)
                .filter(Customer::isActive)
                .filter(c -> c.getPasswordHash() != null
                        && passwordEncoder.matches(password, c.getPasswordHash()));
    }

    @Override
    public List<Customer> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return customerRepository.findByNameContainingIgnoreCase(keyword.trim());
    }

    @Override
    public void toggleActive(Long id) {
        Customer customer = findById(id);
        customer.setActive(!customer.isActive());
        customerRepository.save(customer);
    }

    @Override
    public long count() {
        return customerRepository.count();
    }
}

```

### `src/main/java/com/example/fooddelivery/service/AdminService.java`
```java
package com.example.fooddelivery.service;

import com.example.fooddelivery.model.Admin;

import java.util.List;
import java.util.Optional;

/**
 * Business layer for administrator accounts.
 * All passwords are hashed with BCrypt before they touch the database.
 */
public interface AdminService {

    List<Admin> findAll();

    Admin findById(Long id);

    /** CREATE: hashes the raw password, rejects duplicate e-mails. */
    Admin register(Admin admin);

    /** UPDATE: copies the fields; re-hashes the password only when a new one was typed. */
    Admin update(Long id, Admin form);

    void deleteById(Long id);

    void toggleActive(Long id);

    Optional<Admin> authenticate(String email, String rawPassword);

    long count();
}

```

### `src/main/java/com/example/fooddelivery/service/impl/AdminServiceImpl.java`
```java
package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.common.ResourceNotFoundException;
import com.example.fooddelivery.model.Admin;
import com.example.fooddelivery.repository.AdminRepository;
import com.example.fooddelivery.repository.CustomerRepository;
import com.example.fooddelivery.service.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SECURITY + BUSINESS RULES for admin accounts.
 * Notice: the PasswordEncoder hashes passwords here - the raw password is
 * never stored and never logged.
 */
@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;   // to keep e-mails unique across BOTH tables
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(AdminRepository adminRepository,
                            CustomerRepository customerRepository,
                            PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    @Override
    public Admin findById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Admin", id));
    }

    @Override
    public Admin register(Admin admin) {
        if (admin.getEmail() == null || admin.getEmail().isBlank()) {
            throw new IllegalStateException("E-mail is required.");
        }
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new IllegalStateException("An administrator with this e-mail already exists.");
        }
        if (customerRepository.existsByEmail(admin.getEmail())) {
            throw new IllegalStateException("This e-mail is already used by a customer account.");
        }
        String raw = admin.getPassword();
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Password is required.");
        }

        admin.setPasswordHash(passwordEncoder.encode(raw));   // <<< BCrypt hashing
        admin.setPassword(null);                              // never keep the raw value
        admin.setActive(true);
        return adminRepository.save(admin);
    }

    @Override
    public Admin update(Long id, Admin form) {
        Admin existing = findById(id);

        if (form.getEmail() != null && !form.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (adminRepository.existsByEmail(form.getEmail())
                    || customerRepository.existsByEmail(form.getEmail())) {
                throw new IllegalStateException("This e-mail is already in use.");
            }
            existing.setEmail(form.getEmail());
        }
        existing.setName(form.getName());
        existing.setPhone(form.getPhone());

        // Only re-hash when the admin typed a new password.
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        return adminRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        if (!adminRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Admin", id);
        }
        if (adminRepository.count() <= 1) {
            throw new IllegalStateException("You cannot delete the last administrator account.");
        }
        adminRepository.deleteById(id);
    }

    @Override
    public void toggleActive(Long id) {
        Admin admin = findById(id);
        admin.setActive(!admin.isActive());
        adminRepository.save(admin);
    }

    /** BCrypt check: encoder.matches(rawPassword, storedHash). */
    @Override
    public Optional<Admin> authenticate(String email, String rawPassword) {
        return adminRepository.findByEmail(email)
                .filter(Admin::isActive)
                .filter(a -> a.getPasswordHash() != null
                        && passwordEncoder.matches(rawPassword, a.getPasswordHash()));
    }

    @Override
    public long count() {
        return adminRepository.count();
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/CustomerController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.common.SessionHelper;
import com.example.fooddelivery.model.Customer;
import com.example.fooddelivery.service.CustomerService;
import jakarta.servlet.http.HttpSession;
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
 * ADMIN AREA - CUSTOMER MANAGEMENT  (/admin/customers)
 * Full CRUD over customer accounts. New passwords are BCrypt-hashed by the service.
 *
 * CREATE -> GET /admin/customers/new + POST /admin/customers
 * READ   -> GET /admin/customers (+ search), GET /admin/customers/{id}
 * UPDATE -> GET /admin/customers/{id}/edit + POST /admin/customers/{id}
 * DELETE -> POST /admin/customers/{id}/delete
 */
@Controller
@RequestMapping("/admin/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("customers", customerService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "customers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @PostMapping
    public String create(@ModelAttribute("customer") Customer customer, RedirectAttributes redirectAttributes) {
        if (customer.getName() == null || customer.getName().isBlank()
                || customer.getEmail() == null || customer.getEmail().isBlank()
                || customer.getPassword() == null || customer.getPassword().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Name, e-mail and password are required.");
            return "redirect:/admin/customers/new";
        }
        try {
            Customer saved = customerService.register(customer);   // password hashed here
            redirectAttributes.addFlashAttribute("success",
                    "Customer \"" + saved.getName() + "\" registered (password stored as a BCrypt hash).");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/customers/new";
        }
        return "redirect:/admin/customers";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.findById(id));
        return "customers/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("customer", customerService.findById(id));
        return "customers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("customer") Customer customer,
                         RedirectAttributes redirectAttributes) {
        try {
            customerService.update(id, customer);   // re-hashes only when a new password was typed
            redirectAttributes.addFlashAttribute("success", "Customer updated.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/customers/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Customer deleted.");
        return "redirect:/admin/customers";
    }

    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Customer status changed.");
        return "redirect:/admin/customers";
    }

    /** Demo helper for the admin: jump into the customer's shoes (session becomes CUSTOMER). */
    @PostMapping("/{id}/login-as")
    public String loginAs(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = customerService.findById(id);
        SessionHelper.loginCustomer(session, customer);
        redirectAttributes.addFlashAttribute("success", "You are now browsing as " + customer.getName() + ".");
        return "redirect:/customer";
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/AdminAccountController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.common.SessionHelper;
import com.example.fooddelivery.model.Admin;
import com.example.fooddelivery.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ADMIN ACCOUNT MANAGEMENT  (/admin/admins)
 * -----------------------------------------
 * The administrator can CRUD the e-mail accounts / passwords that can sign in to
 * the admin area. Passwords are ALWAYS stored as BCrypt hashes (AdminService).
 *
 * CREATE -> GET /admin/admins/new   + POST /admin/admins
 * READ   -> GET /admin/admins
 * UPDATE -> GET /admin/admins/{id}/edit + POST /admin/admins/{id}
 * DELETE -> POST /admin/admins/{id}/delete   (+ enable/disable toggle)
 */
@Controller
@RequestMapping("/admin/admins")
public class AdminAccountController {

    private final AdminService adminService;

    public AdminAccountController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("admins", adminService.findAll());
        model.addAttribute("currentAdminId", SessionHelper.getUserId(session));
        return "admin/admins-list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("admin", new Admin());
        return "admin/admins-form";
    }

    @PostMapping
    public String create(@ModelAttribute("admin") Admin admin, RedirectAttributes redirectAttributes) {
        if (admin.getName() == null || admin.getName().isBlank()
                || admin.getEmail() == null || admin.getEmail().isBlank()
                || admin.getPassword() == null || admin.getPassword().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Name, e-mail and password are required.");
            return "redirect:/admin/admins/new";
        }
        try {
            Admin saved = adminService.register(admin);   // hashes the password with BCrypt
            redirectAttributes.addFlashAttribute("success",
                    "Administrator \"" + saved.getName() + "\" created (password stored as a BCrypt hash).");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/admins/new";
        }
        return "redirect:/admin/admins";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("admin", adminService.findById(id));
        return "admin/admins-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("admin") Admin admin,
                         RedirectAttributes redirectAttributes) {
        try {
            adminService.update(id, admin);   // re-hashes only if a new password was typed
            redirectAttributes.addFlashAttribute("success", "Administrator updated.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/admins";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (id.equals(SessionHelper.getUserId(session))) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account while signed in.");
            return "redirect:/admin/admins";
        }
        try {
            adminService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Administrator deleted.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/admins";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (id.equals(SessionHelper.getUserId(session))) {
            redirectAttributes.addFlashAttribute("error", "You cannot disable your own account.");
            return "redirect:/admin/admins";
        }
        adminService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Administrator status changed.");
        return "redirect:/admin/admins";
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/AuthController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.common.SessionHelper;
import com.example.fooddelivery.model.Admin;
import com.example.fooddelivery.model.Customer;
import com.example.fooddelivery.service.AdminService;
import com.example.fooddelivery.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * SINGLE SIGN-IN PAGE FOR BOTH ROLES  +  CUSTOMER SIGN-UP
 * -------------------------------------------------------
 * GET  /login     -> one form (admin and customer use the same page)
 * POST /login     -> checks the admins table first, then the customers table:
 *                      admin found    -> redirect /admin
 *                      customer found -> redirect /customer
 *                      nothing found  -> back to /login with an error
 * GET  /register  -> CUSTOMER SIGN-UP page (open to everybody)
 * POST /register  -> creates a CUSTOMER account (password BCrypt-hashed) and signs them in
 *                    Administrators can NOT sign up here - only an existing admin creates
 *                    admin accounts in /admin/admins.
 * POST /logout    -> clears the session
 *
 * Passwords are checked with BCrypt (PasswordEncoder.matches), never compared as text.
 */
@Controller
public class AuthController {

    private final AdminService adminService;
    private final CustomerService customerService;

    public AuthController(AdminService adminService, CustomerService customerService) {
        this.adminService = adminService;
        this.customerService = customerService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        String typedEmail = email == null ? "" : email.trim();

        // 1) Is it an administrator?  -> /admin
        Optional<Admin> admin = adminService.authenticate(typedEmail, password);
        if (admin.isPresent()) {
            SessionHelper.loginAdmin(session, admin.get());
            redirectAttributes.addFlashAttribute("success",
                    "Welcome back, " + admin.get().getName() + " (Administrator).");
            return "redirect:/admin";
        }

        // 2) Is it a customer?  -> /customer
        Optional<Customer> customer = customerService.authenticate(typedEmail, password);
        if (customer.isPresent()) {
            SessionHelper.loginCustomer(session, customer.get());
            redirectAttributes.addFlashAttribute("success",
                    "Welcome back, " + customer.get().getName() + "!");
            return "redirect:/customer";
        }

        // 3) Neither - show the same login page again
        redirectAttributes.addFlashAttribute("error",
                "Wrong e-mail or password (or the account has been disabled).");
        return "redirect:/login";
    }

    // ================================================================ SIGN UP
    /** The public sign-up page - CUSTOMERS only. */
    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    /**
     * CREATE a new customer account.
     * Validation: name + password required, password confirmed, at least 4 characters.
     * NOTE: the delivery address is NOT asked on the sign-up page - the customer can add or
     * change it later in /customer/profile (or the admin can set it in /admin/customers).
     * The e-mail uniqueness rules (and the BCrypt hashing) live inside
     * CustomerServiceImpl.register(), so this controller stays thin.
     */
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String phone,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        if (name == null || name.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Please enter your full name.");
            return "redirect:/register";
        }
        if (password == null || password.length() < 4) {
            redirectAttributes.addFlashAttribute("error",
                    "The password must be at least 4 characters long.");
            return "redirect:/register";
        }
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error",
                    "The two passwords do not match. Please type them again.");
            return "redirect:/register";
        }

        Customer form = new Customer();
        form.setName(name.trim());
        form.setEmail(email == null ? null : email.trim());
        form.setPhone(phone);                // no address here - added later in /customer/profile
        form.setPassword(password);          // raw only for a moment - register() hashes it

        try {
            Customer saved = customerService.register(form);   // unique e-mail check + BCrypt hash
            SessionHelper.loginCustomer(session, saved);       // sign the new customer straight in
            redirectAttributes.addFlashAttribute("success",
                    "Welcome to QuickBite, " + saved.getName()
                            + "! Your account was created and your password is stored as a BCrypt hash.");
            return "redirect:/customer";
        } catch (IllegalStateException ex) {
            // e.g. "A customer with the e-mail ... already exists."
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        SessionHelper.logout(session);
        redirectAttributes.addFlashAttribute("success", "You have signed out.");
        return "redirect:/login";
    }
}

```

### `src/main/java/com/example/fooddelivery/controller/CustomerPortalController.java`
```java
package com.example.fooddelivery.controller;

import com.example.fooddelivery.common.SessionHelper;
import com.example.fooddelivery.model.Customer;
import com.example.fooddelivery.model.Order;
import com.example.fooddelivery.service.CartService;
import com.example.fooddelivery.service.CustomerService;
import com.example.fooddelivery.service.MenuItemService;
import com.example.fooddelivery.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * CUSTOMER AREA - MY ACCOUNT   (Member 1)
 * --------------------------------------
 * The customer's own pages inside /customer (protected by AuthInterceptor, CUSTOMER role only):
 *
 *   GET  /customer           -> my home page (cart total, active orders, loyalty points, featured dishes)
 *   GET  /customer/profile   -> my details + change my password
 *   POST /customer/profile   -> save my details (password is re-hashed with BCrypt)
 *
 * READ   -> home page and profile page
 * UPDATE -> profile (name, e-mail, phone, address, password)
 */
@Controller
@RequestMapping("/customer")
public class CustomerPortalController {

    private final CustomerService customerService;
    private final CartService cartService;
    private final OrderService orderService;
    private final MenuItemService menuItemService;

    public CustomerPortalController(CustomerService customerService,
                                    CartService cartService,
                                    OrderService orderService,
                                    MenuItemService menuItemService) {
        this.customerService = customerService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.menuItemService = menuItemService;
    }

    /** The customer id can never be null here - the interceptor guarantees a CUSTOMER session. */
    private Long customerId(HttpSession session) {
        return SessionHelper.getUserId(session);
    }

    // ---------------------------------------------------------------- HOME
    @GetMapping
    public String home(HttpSession session, Model model) {
        Long id = customerId(session);
        Customer customer = customerService.findById(id);
        List<Order> orders = orderService.findByCustomer(id);

        model.addAttribute("customer", customer);
        model.addAttribute("recentOrders", orders.stream().limit(3).toList());
        model.addAttribute("activeOrders", orders.stream().filter(Order::isActive).count());
        model.addAttribute("cartTotal", cartService.getGrandTotal(id));
        model.addAttribute("featured", menuItemService.findAll().stream()
                .filter(i -> i.isAvailable()).limit(6).toList());
        return "customer/home";
    }

    // -------------------------------------------------------------- PROFILE
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        model.addAttribute("customer", customerService.findById(customerId(session)));
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) String newPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Customer form = new Customer();
        form.setName(name);
        form.setEmail(email);
        form.setPhone(phone);
        form.setAddress(address);
        form.setPassword(newPassword);
        try {
            customerService.update(customerId(session), form);
            redirectAttributes.addFlashAttribute("success", "Profile updated.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/customer/profile";
    }
}
