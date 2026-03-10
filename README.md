# Maplewood High School Course Planning System

A full-stack web application for students to browse courses, view prerequisites, and manage their semester enrollments with real-time schedule conflict detection.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, TypeScript, Redux Toolkit, Vite |
| Backend | Spring Boot 3.2, Spring Data JPA, Java 17 |
| Database | SQLite |
| Containerization | Docker + Docker Compose |

---

## Prerequisites

**Local development:**
- Java 17+
- Maven 3.8+
- Node.js 18+
- Python 3.8+ (for database setup)

**Docker:**
- Docker
- Docker Compose

---

## Running Locally

### 1. Initialize the database

Run this once before starting the app:

```bash
python3 setup_db.py
```

This creates the `course_sections` and `current_enrollments` tables for the current semester. The `maplewood_school.sqlite` file is already pre-populated with students, courses, teachers, and historical data.

### 2. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### 3. Start the frontend

In a new terminal:

```bash
cd frontend
npm install   # first time only
npm run dev
```

The app will be available at `http://localhost:5173`.

---

## Running with Docker

```bash
docker-compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |

To stop:

```bash
docker-compose down
```

---

## Login

Use any student ID from the database to log in. Some example student IDs to get started:

- `S001`, `S002`, `S003` (Grade 9 students)
- `S100`, `S200` (upper grade students)

---

## Project Structure

```
purple_eagle/
├── backend/                  # Spring Boot API
│   └── src/main/java/com/maplewood/
│       ├── controller/       # REST endpoints
│       ├── service/          # Business logic & enrollment validation
│       ├── repository/       # Spring Data JPA
│       ├── model/            # JPA entities
│       └── dto/              # Request/response DTOs
├── frontend/                 # React app
│   └── src/
│       ├── pages/            # LoginPage, ProfilePage, CoursesPage
│       ├── components/       # Dashboard, CourseBrowser, ScheduleBuilder
│       ├── store/            # Redux slices
│       └── api/              # Axios client
├── maplewood_school.sqlite   # Pre-populated SQLite database
├── setup_db.py               # Creates current semester tables
├── create_database.sql       # Original schema definitions
├── docker-compose.yml
└── ARCHITECTURE.md           # Detailed architecture & API reference
```

---

## Running Tests

**Frontend unit tests:**

```bash
cd frontend
npm run test
```

**Frontend E2E tests (Cypress):**

```bash
cd frontend
npm run cypress:open
```

---

## Configuration

The backend reads the database path from the `DB_URL` environment variable:

```properties
# Default (local)
DB_URL=jdbc:sqlite:../maplewood_school.sqlite

# Docker (set automatically by docker-compose.yml)
DB_URL=jdbc:sqlite:/app/maplewood_school.sqlite
```

No additional environment variables are required for local development.

---

## More Documentation

See [ARCHITECTURE.md](ARCHITECTURE.md) for a detailed guide covering the database schema, REST API reference, Redux state shape, enrollment validation logic, and example curl commands.
