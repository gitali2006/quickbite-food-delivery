# 🍔 QuickBite — Online Food Delivery System

A complete **Java web application** for a university OOP group project — now with a
**single sign-in page**, a **customer area**, an **admin area**, **MySQL** storage and
**BCrypt-hashed passwords**.

| Layer | Technology |
|---|---|
| Language | **Java 17** |
| Backend framework | **Spring Boot 3.2** (Spring MVC) |
| Data access | Spring Data JPA / Hibernate |
| Database | **MySQL** (default) — H2 "dev" profile for instant testing |
| Security | **BCrypt password hashing** + role-based session guard (admin / customer) |
| Frontend | **HTML** rendered server-side with Thymeleaf + **CSS** (no JavaScript) |
| Build tool | Maven (+ Maven Wrapper) |
| Tests | JUnit 5 — 10 tests (`mvn test`), incl. password-hashing tests |

---

## 1. How to run it

**Requirements:** JDK **17 or 21** (Spring Boot 3.2 does **not** support JDK 24/25/26 — in IntelliJ use
File → Project Structure → SDK → 17 or 21) and Maven or IntelliJ IDEA.

### 🚀 The fast way (no MySQL needed) — good for a quick test
Double-click **`RUN-without-MySQL.bat`** (Windows) or run `./RUN-without-MySQL.sh`, then open
http://localhost:8080. In IntelliJ: Run → Edit Configurations → **Active profiles: `dev`**.
This uses the bundled H2 database instead of MySQL.

### Step 1 — set up MySQL (one time)  ·  **XAMPP users: see `XAMPP-MYSQL-SETUP.md`**
1. **Start MySQL** — XAMPP Control Panel → click **Start** next to **MySQL** (wait for green).
   Check with **`CHECK-MySQL.bat`** if unsure. You do *not* need Apache unless you want phpMyAdmin.
2. **XAMPP default login is `root` with an EMPTY password** — the project is already configured
   for exactly that, so **you normally change nothing**:
```properties
spring.datasource.username=root
spring.datasource.password=            # empty = XAMPP default
                                       # (set your password here only if you changed it)
```
   Using MySQL Community Server / WAMP / a root password? Put your password on that line
   (or set the `MYSQL_PASSWORD` environment variable).
3. That's all — the database `quickbite` and **all tables are created automatically**
   (JDBC URL uses `createDatabaseIfNotExist=true`, Hibernate uses `ddl-auto=update`).
   A helper script also exists: `database/setup_mysql.sql`.
   Verify in phpMyAdmin: http://localhost/phpmyadmin → database **`quickbite`** → tables
   `customers`, `admins`, … (`password_hash` column = the BCrypt hashes).

> ⚠️ **`CommunicationsException: Communications link failure` / `Connection refused`?**
> That means the app could not reach MySQL — **MySQL is not running** or the password is wrong.
> It is *not* a code problem. Start MySQL (step 1), fix the password, or run the fast way above.
> The app now prints these same instructions in the console when this happens.

> ⚠️ **`'C:\Users\...' is not recognized as an internal or external command` / `Cannot start maven from wrapper`?**
> This happens when the **project folder path contains a space** (e.g. `C:\Users\Asjadh Ali\Downloads\...`)
> — the Maven wrapper used to run the Maven path without quotes. **Fixed in this version's `mvnw.cmd`.**
> If you still see it: (a) run the app from **IntelliJ** instead of the .bat, or (b) move the project
> to a path without spaces such as `C:\quickbite\`. Manual 10-second fix: open `mvnw.cmd` in Notepad,
> find the line `@IF NOT "%__MVNW_CMD__%"=="" (%__MVNW_CMD__% %*)` and put quotes around it:
> `("%__MVNW_CMD__%")`.

### Step 2 — run with MySQL
```bash
cd food-delivery-system
./mvnw spring-boot:run          # Linux / macOS    (or double-click RUN-with-MySQL.bat on Windows)
mvnw.cmd spring-boot:run        # Windows
```
Or in IntelliJ: open the folder as a **Maven project** (JDK 17/21) and run `FoodDeliveryApplication`.
Then open **http://localhost:8080**.

### No MySQL right now? Use the dev profile (H2 in MySQL mode)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
Same code, same tables — just a local file database, so you can demo instantly.
(H2 console: http://localhost:8080/h2-console)

### Login accounts (seeded automatically on first run)
| Role | E-mail | Password |
|---|---|---|
| **ADMIN** | `admin@quickbite.lk` | `admin123` |
| **CUSTOMER** | `kasun@example.com` | `1234` |
| **CUSTOMER** | `nimali@example.com` | `1234` |
| **CUSTOMER** | `arun@example.com` | `1234` |

Passwords are stored in MySQL as **BCrypt hashes** (see §3) — never as plain text.

> Start completely fresh: stop the app, delete the `data/` folder (dev profile) or drop the
> MySQL schema, then run again.

---

## 2. How the app is organised — ONE login, TWO areas

```
        ┌───────────────────────────┐          ┌────────────────────────────────┐
        │  /register                │          │   /login  (single page)        │
        │  CUSTOMER SIGN-UP only    │          │  e-mail + password (BCrypt)    │
        │  name, e-mail, password   │          │  works for BOTH roles          │
        └─────────────┬─────────────┘          └───────────────┬────────────────┘
                      │  account created (BCrypt hashed)       │ checked against BOTH tables
                 ┌──────────────────┴───────────────────┐
                 ▼                                      ▼
        admins table found                     customers table found
                 │                                      │
                 ▼                                      ▼
        ╔═══════════════════╗                  ╔══════════════════════════╗
        ║   ADMIN AREA      ║                  ║   CUSTOMER AREA          ║
        ║   /admin/**       ║                  ║   /customer/**           ║
        ╠═══════════════════╣                  ╠══════════════════════════╣
        ║ Dashboard         ║                  ║ Home page                ║
        ║ Customers CRUD    ║                  ║ Menu (order food)        ║
        ║ Restaurants CRUD  ║                  ║ Cart (CRUD)              ║
        ║ Menu CRUD         ║                  ║ Checkout → places order  ║
        ║ Orders + drivers  ║                  ║ Personal order history   ║
        ║ Payments CRUD     ║                  ║ Payment portal (pay now) ║
        ║ Reviews CRUD      ║                  ║ My reviews               ║
        ║ ADMIN ACCOUNTS    ║                  ║ My profile + password    ║
        ╚═══════════════════╝                  ╚══════════════════════════╝
```
Nobody can cross the line: **`/admin/**` only opens for an ADMIN session and
`/customer/**` only for a CUSTOMER session** (`AuthInterceptor` + `WebConfig`).
Try it — sign in as a customer and open `/admin`: you are sent back to `/login`.

### Admin area (all CRUD + business actions)
| Page | Route | CRUD |
|---|---|---|
| Dashboard (all live numbers + quick links) | `/admin` | — |
| Customers | `/admin/customers` | create, list+search, view, edit, delete, enable/disable, "sign in as" |
| Restaurants | `/admin/restaurants` | create, list+search, view (+menu), edit, delete (blocked if dishes exist), open/close |
| Menu items | `/admin/menu` | create, list+search+filter, edit, delete, sold-out toggle |
| Orders | `/admin/orders` | create (staff), list+filter, track, edit, status workflow, assign driver, cancel, delete |
| Drivers | `/admin/drivers` | create, list+search, view (+deliveries), edit, availability status, delete |
| Payments | `/admin/payments` | create, list+filter, view, edit, **mark paid / failed / refund**, delete |
| Reviews | `/admin/reviews` | create, list+filter, edit, delete |
| **Admin accounts** | `/admin/admins` | **create, list, edit (e-mail/password), disable, delete** — hashed passwords, cannot delete yourself or the last admin |

### Customer area (menu, history, cart, payment portal)
| Page | Route | What |
|---|---|---|
| Home | `/customer` | greeting, cart total, active orders, loyalty points, popular dishes |
| Menu | `/customer/menu` | all dishes grouped per restaurant, "Add to cart" |
| Cart | `/customer/cart` | add / change quantity / remove / clear, subtotal, delivery fee, total |
| Checkout | `/customer/checkout` | delivery address + payment method → creates order **and** payment |
| Order history | `/customer/orders` | all my orders + `/customer/orders/{id}` tracking page + cancel |
| **Payment portal** | `/customer/payments` | all my payments, **Pay now** for pending ones |
| My reviews | `/customer/reviews` | write / edit / delete my reviews (rating recalculated) |
| My profile | `/customer/profile` | edit my details and change my password |

### Public pages (no sign-in needed)
| Page | Route | What |
|---|---|---|
| Landing page | `/` | hero + popular dishes + "Create an account" button for guests |
| Menu | `/menu` | browse all dishes (guests see "Sign in to order") |
| **Sign up (CUSTOMERS only)** | `/register` | name, e-mail, phone, password + confirm (no delivery address — added later in My profile) → creates the account and signs in automatically |
| Sign in (both roles) | `/login` | admin e-mail → admin area, customer e-mail → customer area |

**About `/register`:** only **customer** accounts can be created here — there is no admin sign-up.
Rules enforced server-side: unique e-mail (a customer e-mail may not clash with another customer
**or** with an admin account), password at least 4 characters, password typed twice. The password
is hashed with BCrypt in `CustomerServiceImpl.register(...)` before saving to MySQL.

---

## 3. Security — passwords are hashes, not text 🔒

* `SecurityConfig` provides a **`BCryptPasswordEncoder`**.
* `CustomerServiceImpl` / `AdminServiceImpl` **hash with `encoder.encode(...)`** when an
  account is created or a password is changed — the raw password is discarded immediately.
* Login never "decodes" anything: it calls **`encoder.matches(typedPassword, storedHash)`**.
* BCrypt adds a **random salt per password**, so two people with password `1234` have
  completely different hashes (proved by `PasswordSecurityTest`).

Real data from this project's database (MySQL/H2):

```
ADMINS    | admin@quickbite.lk  | $2a$10$BWvQWJbBVbYkRUgFpZgCTO... | 60 chars
CUSTOMERS | kasun@example.com   | $2a$10$kMEUswIdxXS93TEafMnQme... | 60 chars
CUSTOMERS | nimali@example.com  | $2a$10$9yi6NErNEfolv3gp/Fz3wup... | 60 chars
CUSTOMERS | arun@example.com    | $2a$10$oX5auO30KhxDf/2ak3lqQug... | 60 chars
→ same password "1234" for all three customers, three DIFFERENT hashes (salt!)
```

Useful query for your report (`database/setup_mysql.sql` has more):
```sql
SELECT id, email, LEFT(password_hash, 20) AS hash_preview FROM customers;
SELECT COUNT(DISTINCT password_hash) AS unique_hashes, COUNT(*) AS accounts FROM customers;
```

---

## 4. OOP concepts in the code (for the viva)

| Concept | Where |
|---|---|
| **Inheritance** | `BaseEntity → Person → Customer / Driver / Admin` (now three sibling classes under `Person`!) |
| **Abstraction** | abstract `BaseEntity` + `Person`; every subclass implements `getDisplayName()` / `getRole()` |
| **Polymorphism** | `getRole()` returns `"CUSTOMER"` / `"DRIVER"` / `"ADMIN"`; `getDisplayName()` differs per class |
| **Encapsulation** | private fields + getters/setters; rules inside methods: `Order.recalculateTotal()`, `Payment.markPaid()`, `Driver.isAvailableForDelivery()` |
| **Composition** | `Order` owns `OrderItem`s (cascade + orphanRemoval); `CartItem` has-a `Customer` and a `MenuItem` |
| **Interface abstraction** | controllers depend on `…Service` interfaces, not on `…Impl` classes |
| **Enums** | `OrderStatus`, `PaymentMethod`, `PaymentStatus`, `DriverStatus` |
| **Layered design** | `@Controller` (web) → `Service` (rules + hashing) → `Repository` (data) |
| **Cross-cutting concerns** | `AuthInterceptor` (route guard) + `GlobalControllerAdvice` (role info for every page, friendly error pages) |

**10 unit tests** prove all of this: `src/test/java/.../OopConceptsTest.java` (6) and
`PasswordSecurityTest.java` (4 — hashing, salting, wrong-password rejection).

---

## 5. Project structure

```
food-delivery-system/
├── pom.xml                          Maven config (Spring Boot, JPA, MySQL, security-crypto, H2 dev)
├── mvnw / mvnw.cmd                  Maven wrapper
├── README.md                        this file
├── MEMBER_DIVISION.md               who does what (6 members)
├── database/setup_mysql.sql         MySQL helper + useful queries for your report
└── src/main/
    ├── java/com/example/fooddelivery/
    │   ├── FoodDeliveryApplication.java
    │   ├── common/        BaseEntity, Person, SessionHelper (roles), AuthInterceptor, exceptions
    │   ├── config/        SecurityConfig (BCrypt bean), WebConfig (route guard), DataInitializer
    │   ├── model/         Customer, Driver, Admin, Restaurant, MenuItem, CartItem, Order, OrderItem, Payment, Review
    │   │   └── enums/     OrderStatus, PaymentMethod, PaymentStatus, DriverStatus
    │   ├── repository/    10 Spring Data JPA repositories
    │   ├── service/       interfaces + impl/  (hashing happens here)
    │   └── controller/    AuthController (single login), HomeController (/, /admin),
    │                      AdminAccountController (/admin/admins),
    │                      ── admin area: Customer / Restaurant / MenuItem / Order / Driver /
    │                         Payment / Review controllers (all /admin/*)
    │                      ── customer area: CustomerPortalController (home + profile),
    │                         CustomerCartController (cart + checkout),
    │                         CustomerOrderController (history + tracking),
    │                         CustomerPaymentController (payment portal),
    │                         CustomerReviewController (my reviews)
    └── resources/
        ├── application.properties         MySQL (default)
        ├── application-dev.properties     H2 in MySQL mode (dev profile)
        ├── static/css/style.css           all styling (one file)
        └── templates/
            ├── login.html                 SINGLE sign-in page (both roles)
            ├── register.html              CUSTOMER sign-up page (no admin sign-up)
            ├── home.html                  public landing page
            ├── fragments/layout.html      role-aware navbar + footer
            ├── admin/       dashboard.html, admins-list.html, admins-form.html
            ├── customer/    home, cart, checkout, orders, order-detail, payments, reviews, review-form, profile
            ├── customers/ restaurants/ menu/ orders/ drivers/ payments/ reviews/   ← ADMIN pages
            └── error.html
```

---

## 6. Quick demo script (2 minutes)

0. **Sign up (optional but quick):** landing page → **Create an account** (`/register`) → fill the
   form → you are signed in immediately as a new customer. Try registering the same e-mail twice
   to show the duplicate rule. (Admins cannot sign up — only an existing admin creates admin accounts.)
1. Open **http://localhost:8080** → landing page → **Sign in**.
2. Sign in as `admin@quickbite.lk` / `admin123` → you land on the **admin dashboard**
   (all counters + quick links). Open **Admin accounts** → show the “bcrypt hash stored” chip →
   add a new admin → sign out → sign in with the new admin (proves hashing works).
3. Sign out, sign in as `kasun@example.com` / `1234` → **customer home page**.
4. Menu → add 2 dishes → Cart → checkout (Cash on delivery) → order appears in **My orders**.
5. **Payment portal** → the payment is *Pending* → **Pay now** → becomes *Paid*
   (try paying again — the business rule blocks it).
6. Open `/admin` in the same browser — as a customer you are **denied** (role guard).
