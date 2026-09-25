# InBank — Bank Management System

A full-stack **Java web banking application** for user accounts, money movement, transaction history, alerts, admin tools, and an in-app account assistant. Built for learning and portfolio demos.

**Author:** [samiksha1803](https://github.com/samiksha1803)

## Tech stack

| Layer | Technology |
|--------|------------|
| Backend | Java 21, Servlets, JSP |
| ORM | Hibernate 5.6, JPA |
| Database | PostgreSQL |
| Build | Maven (WAR) |
| Server | Apache Tomcat 9 |
| Tools | Lombok, pgAdmin |

## Features

- User registration, login, logout, session management (20 min timeout)
- Dashboard with live balance and 12-digit account numbers
- Deposit, withdraw, insufficient-balance checks
- Transfer between customers (shared reference number, ledger entries for both sides)
- Transaction history with date/time, ID, reference, search and filters
- Profile and change password (PBKDF2 password hashing)
- In-app alerts; optional SMTP in `mail.properties`
- Admin dashboard: customers, activate/deactivate, balance adjustments, full ledger
- Built-in account assistant (answers from the user’s own data; no external AI API)

## Prerequisites

- JDK 21
- Apache Tomcat 9
- PostgreSQL + pgAdmin
- Eclipse IDE for Enterprise Java (or similar) with Maven support

## Database setup

1. Create a database named **`inbank`** in PostgreSQL.
2. Edit `src/main/resources/META-INF/persistence.xml` if your PostgreSQL user/password differ from the defaults (`postgres` / `root`).
3. On first Tomcat start, Hibernate `hbm2ddl.auto=update` creates/updates tables.
4. A default **admin** user is created if missing:
   - Username: `admin`
   - Password: `admin123`

## Run in Eclipse

1. Import as **Existing Maven Project** (`InBank` folder).
2. Add the project to **Tomcat v9.0** on the Servers view.
3. **Maven → Update Project**, then start Tomcat.
4. Open: **http://localhost:8080/InBank/**

## Demo flow (for interviews)

1. Register two users → note each **12-digit account number** on Profile.
2. Deposit on sender account → **Transfer** to the other account number.
3. Show **Transactions** on both accounts and **Admin → Ledger**.

## Project structure (high level)

```
src/main/java/
  controller/   # Servlets (login, transfer, admin, …)
  service/      # BankService, AssistantService
  dao/          # JPA data access
  dto/          # User, Transaction, Notification
  filter/       # Auth, encoding
  util/         # Money, passwords, validation
src/main/webapp/
  WEB-INF/views/   # JSP pages
  css/bank.css
```

## Security notes (portfolio)

- Do not commit real database or SMTP passwords to a public repo.
- Self-service **Deposit** is simplified for demos; real banks usually credit accounts via branch/payment channels or admin approval.

## License

MIT — free to use for learning and portfolio.
