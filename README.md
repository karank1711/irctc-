# IRCTC Admin Dashboard

Java MVC application for a local IRCTC administration portal. Pages are JSP files, SQL stays in DAO classes, and MySQL is accessed with JDBC. This project does not connect to live IRCTC systems.

## Requirements

- JDK 17 or newer (the project compiles to Java 17)
- Apache Maven 3.9+
- MySQL 8, either installed locally or started with Docker Compose

## Configure the environment

Java does not load `.env` files on its own. `EnvironmentConfig` reads the project `.env` file through the dotenv library. Operating-system environment variables override the file.

```text
copy .env.example .env
```

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to your MySQL account. Create the database named in the URL if you are not using Docker:

```sql
CREATE DATABASE irctc_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

`ADMIN_EMAIL` and `ADMIN_PASSWORD` are used only when that email does not already exist. The password is hashed with BCrypt and is not stored in the database as plain text. Restarting the application does not reset an existing administrator.

The real `.env` file is listed in `.gitignore`. Do not commit it.

### Docker database

If port 3306 is already used by another MySQL server, the included Compose file publishes MySQL on port 3307. Point `DB_URL` at `127.0.0.1:3307` and set `DB_PASSWORD` before starting:

```text
docker compose up -d
```

Compose reads `DB_PASSWORD` from `.env` and uses it as the MySQL root password.

### Sample data

Startup creates tables only. It does not insert sample bookings, passengers, trains, employees, or activities. Empty tables show zero counts and empty messages. Add records through the admin panel and the dashboard reads them back from MySQL.

If an older database still has the sample-data marker, remove it after replacing those rows:

```sql
DELETE FROM app_settings WHERE setting_key = 'data_mode';
```

## Run

From the project root:

```text
mvn test
mvn compile exec:java
```

Open http://localhost:8080/login and sign in with the administrator email and password from your `.env` file.

A deployable WAR is created with:

```text
mvn package
```

Deploy `target/irctc-admin.war` to Tomcat 11. The Tomcat process also needs the same environment variables, or a `.env` file in its working directory.

## Change the administrator password before deployment

1. Sign in and open Settings.
2. Use **Change password**. The new password must be 8 to 72 characters. This updates the hash already stored in MySQL.
3. Replace `ADMIN_PASSWORD` in `.env` only if you also want a future new email address to receive that password. Changing `.env` does not overwrite an account that already exists.
4. Use a long unique password. Do not keep the development password on a shared or production server.

To create another administrator without a public registration page, set `ADMIN_EMAIL` and `ADMIN_PASSWORD` to the new account and restart. Existing accounts are left unchanged.

## What the portal includes

- Session login, logout, CSRF checks, and prepared statements
- Dashboard totals, sparklines, and Chart.js filters from SQL
- Bookings, users, employees, trains, schedules, services, reports, and CSV export
- Search across PNR, train, user, and employee records
- Activity records written when an administrator changes data
