# URL Shortener

A basic URL shortener built with Java Spring Boot, React, and MySQL. It focuses on the core backend and working dashboard rather than a fancy frontend.

## Features

- Shorten long URLs with generated short codes.
- Optional custom aliases.
- Register, sign in, and sign out with Spring Security sessions.
- Admin dashboard for promoting users, blocking/unblocking users, and deleting users by email.
- Redirect short links and track each click.
- User-specific dashboard summary with total links, total clicks, and active links.
- Top/recent links, recent click activity, copy buttons, and delete actions.

## Tech Stack

- Backend: Java 17, Spring Boot 3, Spring Web, Spring Data JPA
- Security: Spring Security with BCrypt password hashing
- Frontend: React 18, Vite
- Database: MySQL
- Tests: JUnit 5 with H2 in MySQL compatibility mode

## Database Setup

1. Start MySQL.
2. Run the schema:

```sql
SOURCE database/schema.sql;
```

Or paste the contents of `database/schema.sql` into MySQL Workbench.

If your database already existed before auth was added, run:

```sql
SOURCE database/auth-migration.sql;
```

The migration assigns old links to `admin@example.com`. New links belong to the signed-in user who creates them.

If you are adding the admin version to an existing database, run:

```sql
SOURCE database/admin-migration.sql;
```

The default admin account is:

```text
Email: admin@example.com
Password: admin123
```

Promoted users should sign out and sign in again before opening the admin dashboard.

3. Update `src/main/resources/application.properties` with your MySQL username and password:

```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

## Run The Backend

```bash
mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

Important API endpoints:

- `POST /api/urls` creates a short URL.
- `POST /api/auth/register` creates an account.
- `POST /api/auth/login` signs in.
- `POST /api/auth/logout` signs out.
- `GET /api/auth/me` returns the signed-in user.
- `GET /api/admin/users` lists users for admins.
- `POST /api/admin/users/admin` promotes a user by email.
- `POST /api/admin/users/user` demotes a user by email.
- `POST /api/admin/users/block` blocks a user by email.
- `POST /api/admin/users/unblock` unblocks a user by email.
- `DELETE /api/admin/users` deletes a user by email.
- `GET /api/urls/recent` lists recent URLs.
- `GET /api/urls/top` lists top URLs by clicks.
- `GET /api/dashboard/summary` returns dashboard totals.
- `GET /api/clicks/recent` lists recent click events.
- `DELETE /api/urls/{id}` deletes a short URL.
- `GET /{shortCode}` redirects to the original URL.

## Run The React Frontend

```bash
cd frontend
npm install
cd "D:\FY Project\url-shortener\frontend"
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

If the backend URL changes, create `frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Test

```bash
mvn test
```
