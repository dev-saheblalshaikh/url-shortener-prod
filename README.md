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
- Containerization: Docker, Docker Compose, Nginx (reverse proxy)
- Tests: JUnit 5 with H2 in MySQL compatibility mode

## Running With Docker Compose (Recommended)

This is the fastest way to run the full stack (backend, frontend, MySQL) with one command.

### Prerequisites

- Docker Desktop installed and running

### Setup

1. Create a `.env` file in the project root with the following variables:

```env
DB_HOST=mysql
DB_NAME=url_shortener
DB_USERNAME=appuser
DB_PASSWORD=your_db_password
DB_ROOT_PASSWORD=your_root_password
MYSQL_USER=appuser
MYSQL_PASSWORD=your_db_password
APP_BASE_URL=http://localhost
```

2. From the project root, run:

```bash
docker compose up --build
```

This builds and starts three containers:

- `mysql` — MySQL 8, auto-initialized with `database/schema.sql` on first run
- `backend` — Spring Boot app (waits for MySQL to be healthy before starting)
- `frontend` — React app built and served via Nginx, which also reverse-proxies `/api/**` and short-code redirects to the backend

3. Once all three containers are up, open:

```text
http://localhost
```

The frontend, backend, and database all communicate over an internal Docker network — no manual configuration needed beyond the `.env` file.

### Default Admin Account

```text
Email: admin@example.com
Password: admin123
```

Promoted users should sign out and sign in again before opening the admin dashboard. **Change this password before deploying anywhere publicly reachable.**

### Stopping the Stack

```bash
docker compose down
```

Add `-v` to also remove the MySQL data volume (this wipes your database):

```bash
docker compose down -v
```

## Running Without Docker (Manual Setup)

If you'd rather run each piece directly on your machine:

### Database Setup

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

3. Update `src/main/resources/application.properties` with your MySQL username and password (or set the equivalent environment variables — see below):

```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

> Since adding Docker support, these properties read from environment variables with local fallbacks (`${DB_USERNAME:root}`, etc.), so manual local runs still work with no extra setup — Docker Compose overrides them via `.env` instead.

### Run The Backend

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

### Run The React Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

By default, the frontend calls the API using relative paths (same-origin), which works when served behind the Nginx proxy in Docker. For local dev against a differently-hosted backend, create `frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Test

```bash
mvn test
```

## Deployment

This project is deployed on AWS EC2 using Docker Compose — a single instance runs all three containers (Nginx/frontend, Spring Boot backend, MySQL) behind the instance's public IP/domain.