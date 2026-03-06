# Maplewood High School — Course Planning System
## Architecture & Developer Guide

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Database Architecture](#2-database-architecture)
3. [Backend Design](#3-backend-design)
4. [Frontend Architecture](#4-frontend-architecture)
5. [High-Level Interaction Flow](#5-high-level-interaction-flow)
6. [How to Run — Manual Setup](#6-how-to-run--manual-setup)
7. [How to Run — Docker](#7-how-to-run--docker)

---

## 1. Project Overview

A full-stack course planning web application for Maplewood High School. Students can browse the course catalog, enroll in courses for the current semester, and track their graduation progress.

**Tech Stack**

| Layer | Technology |
|---|---|
| Database | SQLite |
| Backend | Spring Boot 3.2 + Spring Data JPA + Hibernate 6 |
| Frontend | React 18 + TypeScript + Redux Toolkit + Vite |
| Container | Docker + Docker Compose |

**Business Rules Enforced**
- Students must pass a course's prerequisite before enrolling
- Maximum 5 courses per student per semester
- No two enrolled courses may share a time slot conflict
- Courses are grade-level restricted (e.g. Grade 9 only, Grade 10–12)
- 30 total credits required for graduation

---

## 2. Database Architecture

### 2.1 Overview

The database is a single SQLite file: `maplewood_school.sqlite`. It contains pre-populated historical data plus two tables added for the current semester.

```
maplewood_school.sqlite
├── Pre-populated (read-only in practice)
│   ├── students               400 students across grades 9–12
│   ├── courses                57 courses (20 core + 37 elective)
│   ├── teachers               50 teachers across 9 specializations
│   ├── classrooms             60 rooms across 6 room types
│   ├── specializations        9 subject areas
│   ├── room_types             8 room type definitions
│   ├── semesters              9 semesters (6 historical + current + 2 future)
│   └── student_course_history ~6,500 historical enrollment records
│
└── Added by setup_db.py (current semester data)
    ├── course_sections        57 sections for Fall 2024
    └── current_enrollments    live student enrollments (read/write)
```

### 2.2 Table Schemas

#### `students`
```sql
id, first_name, last_name, email, grade_level (9–12),
enrollment_year, expected_graduation_year, status
```

#### `courses`
```sql
id, code, name, description, credits, hours_per_week,
specialization_id, prerequisite_id (self-ref FK),
course_type ('core'|'elective'),
grade_level_min, grade_level_max,
semester_order (1=Fall, 2=Spring)
```
- `prerequisite_id` is a self-referential foreign key — each course optionally points to the one course that must be passed first.
- `semester_order` encodes whether this course is normally offered in Fall (1) or Spring (2).

#### `student_course_history`
```sql
id, student_id (FK→students), course_id (FK→courses),
semester_id (FK→semesters), status ('passed'|'failed')
```
- Backed by DB triggers: cannot insert a record if the prerequisite course hasn't been passed, and cannot retake a passed course.

#### `course_sections` *(created by setup_db.py)*
```sql
id, course_id (FK→courses), semester_id (FK→semesters),
teacher_id (FK→teachers), classroom_id (FK→classrooms),
days ('MWF'|'TTh'), start_time, end_time
UNIQUE(course_id, semester_id)
```
- Represents a scheduled offering of a course in a specific semester with a specific time slot, teacher, and room.
- One section per course exists for semester 7 (Fall 2024).

#### `current_enrollments` *(created by setup_db.py)*
```sql
id, student_id (FK→students), section_id (FK→course_sections),
enrolled_at DATETIME
UNIQUE(student_id, section_id)
```
- Tracks which students are currently enrolled in which sections this semester.
- All enrollment business-rule validation is done at the application layer (not DB triggers) to allow returning meaningful error messages.

### 2.3 Time Slot Distribution

All 57 courses are assigned to one of 11 non-overlapping time slots:

| Days | Start | End   | # Courses |
|------|-------|-------|-----------|
| MWF  | 08:00 | 09:00 | 6 |
| MWF  | 09:00 | 10:00 | 6 |
| MWF  | 10:00 | 11:00 | 5 |
| MWF  | 11:00 | 12:00 | 5 |
| MWF  | 13:00 | 14:00 | 5 |
| MWF  | 14:00 | 15:00 | 5 |
| TTh  | 08:00 | 09:30 | 5 |
| TTh  | 09:30 | 11:00 | 5 |
| TTh  | 11:00 | 12:30 | 5 |
| TTh  | 13:00 | 14:30 | 5 |
| TTh  | 14:30 | 16:00 | 5 |

No courses are scheduled during lunch (12:00–13:00). MWF and TTh slots never conflict with each other.

### 2.4 Entity Relationship Diagram

```
specializations ──< courses >── courses (self-ref prerequisite)
                                    │
                              course_sections
                             /       │       \
                    teachers     semesters   classrooms
                                    │
                         current_enrollments
                                    │
                                 students
                                    │
                        student_course_history
                                    │
                              courses + semesters
```

### 2.5 GPA Calculation

There is no numeric grade stored — only `passed` or `failed`. GPA is computed as:

```
GPA = (sum of credits for passed courses / sum of credits for all taken courses) × 4.0
```

This gives a value on a 0.0–4.0 scale. A student who passes every course gets 4.0.

---

## 3. Backend Design

### 3.1 Package Structure

```
com.maplewood/
├── Application.java              Spring Boot entry point
├── config/
│   └── WebConfig.java            CORS configuration (allows localhost:5173 and :3000)
├── model/                        JPA entities (map to DB tables)
│   ├── Course.java
│   ├── Student.java
│   ├── CourseSection.java
│   ├── CurrentEnrollment.java
│   └── StudentCourseHistory.java
├── repository/                   Spring Data JPA interfaces
│   ├── CourseRepository.java
│   ├── StudentRepository.java
│   ├── CourseSectionRepository.java
│   ├── CurrentEnrollmentRepository.java
│   └── StudentCourseHistoryRepository.java
├── dto/                          Data Transfer Objects (API shapes)
│   ├── CourseDTO.java
│   ├── CourseHistoryItemDTO.java
│   ├── StudentProfileDTO.java
│   ├── ScheduleItemDTO.java
│   ├── EnrollmentRequest.java
│   └── EnrollmentResponse.java
├── service/                      Business logic
│   ├── CourseService.java
│   ├── StudentService.java
│   └── EnrollmentService.java    ← all enrollment validation lives here
└── controller/                   REST endpoints
    ├── CourseController.java
    ├── StudentController.java
    └── EnrollmentController.java
```

### 3.2 REST API Reference

| Method | Endpoint | Description | Query Params |
|--------|----------|-------------|--------------|
| GET | `/api/courses` | List all courses with section info | `gradeLevel`, `semesterOrder` |
| GET | `/api/students/{id}` | Student profile + GPA + history | — |
| GET | `/api/students/{id}/schedule` | Current semester enrollments | — |
| POST | `/api/enrollments` | Enroll in a course section | Body: `{studentId, sectionId}` |
| DELETE | `/api/enrollments/{id}` | Drop an enrolled course | `?studentId=` |

**Successful enrollment response:**
```json
{
  "success": true,
  "message": "Successfully enrolled in \"Algebra II\".",
  "enrollment": {
    "enrollmentId": 1,
    "sectionId": 11,
    "courseId": 11,
    "courseCode": "MAT201",
    "courseName": "Algebra II",
    "credits": 1.0,
    "days": "TTh",
    "startTime": "14:30",
    "endTime": "16:00"
  }
}
```

**Failed enrollment response (HTTP 400):**
```json
{
  "success": false,
  "errorType": "prerequisite",
  "message": "Missing prerequisite: you must pass \"Geometry\" before enrolling in \"Algebra II\"."
}
```

**Error types:** `prerequisite` | `grade_level` | `max_courses` | `conflict` | `already_enrolled` | `not_found`

### 3.3 Enrollment Validation Logic

All validation runs in `EnrollmentService.enroll()` in this exact order:

```
1. Student and section exist?           → 404-style error if not
2. Grade level eligible?                → course.gradeLevelMin ≤ student.gradeLevel ≤ course.gradeLevelMax
3. Prerequisite passed?                 → student_course_history has (studentId, prereqCourseId, 'passed')
4. Already enrolled in this section?    → current_enrollments has (studentId, sectionId)
5. Under 5 course limit?                → COUNT(current_enrollments WHERE studentId) < 5
6. No time conflict?                    → for each enrolled section: check day overlap AND time range overlap
```

**Time conflict algorithm:**
```java
// Two sections conflict when:
// (1) they share at least one day character (M, W, F, T, h)
// (2) their time ranges overlap: aStart < bEnd AND bStart < aEnd
boolean dayOverlap = a.getDays().chars().anyMatch(c -> b.getDays().indexOf(c) >= 0);
boolean timeOverlap = a.getStartTime().compareTo(b.getEndTime()) < 0
                   && b.getStartTime().compareTo(a.getEndTime()) < 0;
```

### 3.4 JPA / SQLite Notes

- **`GenerationType.IDENTITY`** is used for all entities but requires `hibernate.jdbc.use_get_generated_keys=false` because the SQLite JDBC driver does not implement `getGeneratedKeys()`. Hibernate falls back to `SELECT last_insert_rowid()` instead.
- **`ddl-auto=none`** — Hibernate never modifies the schema. All tables are created by `setup_db.py` and the original `create_database.sql`.
- **Lazy loading** is used on `Course.prerequisite` to avoid N+1 queries when listing courses. All other `@ManyToOne` relationships are `EAGER` for simplicity.
- The DB path is configurable via the `DB_URL` environment variable (used by Docker), defaulting to `jdbc:sqlite:../maplewood_school.sqlite` for local development.

---

## 4. Frontend Architecture

### 4.1 Directory Structure

```
frontend/src/
├── main.tsx                  React root — wraps app in Redux Provider
├── App.tsx                   Root component: header + 3-panel layout
├── App.css                   All styling (single CSS file, no UI library)
├── index.css                 CSS reset and body defaults
├── types/
│   └── index.ts              Shared TypeScript interfaces
├── api/
│   └── client.ts             Axios-based API client functions
├── store/
│   ├── index.ts              Redux store (configureStore)
│   └── slices/
│       ├── studentSlice.ts   Student profile state + fetchStudentProfile thunk
│       ├── coursesSlice.ts   Course list state + filters + fetchCourses thunk
│       └── enrollmentSlice.ts Schedule state + enroll/drop thunks
└── components/
    ├── StudentDashboard.tsx  Left panel: GPA, credits, progress, history
    ├── CourseBrowser.tsx     Center panel: filterable catalog + enroll buttons
    └── ScheduleBuilder.tsx   Right panel: current schedule + drop buttons
```

### 4.2 Redux State Shape

```typescript
{
  student: {
    currentStudentId: number | null,
    profile: StudentProfile | null,    // includes gpa, creditsEarned, courseHistory
    loading: boolean,
    error: string | null
  },

  courses: {
    list: CourseWithSection[],         // all 57 courses with section time info
    filters: {
      gradeLevel: number | null,
      semesterOrder: number | null,    // 1=Fall, 2=Spring
      courseType: string | null,       // 'core' | 'elective'
      searchText: string
    },
    loading: boolean,
    error: string | null
  },

  enrollment: {
    schedule: ScheduleItem[],          // student's current enrollments
    loading: boolean,
    error: string | null,
    enrolling: boolean,                // true while POST /enrollments is in-flight
    enrollError: string | null,        // last enrollment failure message
    enrollSuccess: string | null,      // last enrollment success message
    dropping: Record<number, boolean>  // keyed by enrollmentId, true while DELETE in-flight
  }
}
```

### 4.3 Data Flow

```
User types student ID → App dispatches fetchStudentProfile(id)
                     → studentSlice sets loading=true
                     → GET /api/students/{id}
                     → studentSlice stores profile
                     → App dispatches fetchSchedule(id)
                     → enrollmentSlice stores schedule

CourseBrowser mounts  → dispatches fetchCourses()
                      → GET /api/courses
                      → coursesSlice stores list

User clicks Enroll    → CourseBrowser dispatches enrollInCourse({studentId, sectionId})
                      → enrollmentSlice sets enrolling=true
                      → POST /api/enrollments
                      → on success: pushes new item into schedule[], sets enrollSuccess
                      → on failure: sets enrollError (displayed as auto-dismissing toast)

User clicks Drop      → ScheduleBuilder dispatches dropCourse({enrollmentId, studentId})
                      → enrollmentSlice sets dropping[enrollmentId]=true
                      → DELETE /api/enrollments/{id}?studentId=
                      → on success: filters item out of schedule[]
```

### 4.4 Component Responsibilities

| Component | Reads from Store | Dispatches |
|---|---|---|
| `App` | `student.profile` | `fetchStudentProfile`, `fetchSchedule`, `clearSchedule` |
| `StudentDashboard` | `student.*`, `enrollment.schedule` | — |
| `CourseBrowser` | `courses.*`, `student.profile`, `enrollment.*` | `fetchCourses`, `setFilters`, `resetFilters`, `enrollInCourse`, `clearEnrollMessages` |
| `ScheduleBuilder` | `enrollment.*`, `student.profile` | `dropCourse` |

### 4.5 API Client

`src/api/client.ts` uses Axios with `baseURL: '/api'`. In development, Vite proxies `/api` → `http://localhost:8080`. In Docker, nginx handles the same proxy.

Enrollment errors (HTTP 400) are caught in the client and returned as data (not thrown), so Redux can store the error message without triggering the `rejected` case:

```typescript
enroll: (studentId, sectionId) =>
  api.post('/enrollments', { studentId, sectionId })
     .then(r => r.data)
     .catch(err => err.response?.data ?? { success: false, message: 'Network error' })
```

---

## 5. High-Level Interaction Flow

```
Browser                  Frontend (Redux)           Backend (Spring Boot)        SQLite
  │                           │                             │                       │
  │── Enter student ID ──────>│                             │                       │
  │                           │── fetchStudentProfile(1) ──>│                       │
  │                           │                             │── SELECT students ───>│
  │                           │                             │── SELECT history  ───>│
  │                           │<── StudentProfileDTO ───────│                       │
  │                           │   (GPA calculated here)     │                       │
  │                           │── fetchSchedule(1) ─────────>                       │
  │                           │<── ScheduleItem[] ──────────│                       │
  │<── Dashboard renders ─────│                             │                       │
  │                           │                             │                       │
  │── Page load ──────────────│                             │                       │
  │                           │── fetchCourses() ──────────>│                       │
  │                           │                             │── SELECT courses ────>│
  │                           │                             │── SELECT sections ───>│
  │                           │<── CourseDTO[] ─────────────│                       │
  │<── Course list renders ───│                             │                       │
  │                           │                             │                       │
  │── Click "Enroll" ─────────│                             │                       │
  │   (on Algebra II) ────────│── enrollInCourse() ────────>│                       │
  │                           │   {studentId:1,sectionId:11}│── grade level check   │
  │                           │                             │── prereq check ──────>│
  │                           │                             │── duplicate check ───>│
  │                           │                             │── max 5 check ───────>│
  │                           │                             │── conflict check ────>│
  │                           │                             │── INSERT enrollment ─>│
  │                           │<── EnrollmentResponse ──────│                       │
  │                           │   {success:true,...}        │                       │
  │<── Toast: "Enrolled!" ────│                             │                       │
  │<── Schedule updates ──────│                             │                       │
  │                           │                             │                       │
  │── Click "Enroll" again ───│                             │                       │
  │   (conflicting course) ───│── enrollInCourse() ────────>│                       │
  │                           │                             │── conflict detected   │
  │                           │<── {success:false,          │                       │
  │                           │    errorType:"conflict",...}│                       │
  │<── Toast: "Time conflict" ─│                             │                       │
  │                           │                             │                       │
  │── Click "Drop" ───────────│                             │                       │
  │                           │── dropCourse() ────────────>│                       │
  │                           │                             │── DELETE enrollment ─>│
  │                           │<── 200 OK ──────────────────│                       │
  │<── Schedule updates ──────│                             │                       │
```

---

## 6. How to Run — Manual Setup

### Prerequisites

| Tool | Version | Install |
|---|---|---|
| Java JDK | 17+ | `brew install openjdk@17` |
| Maven | 3.8+ | `brew install maven` |
| Node.js | 20+ | `brew install node` |
| Python | 3.x | pre-installed on macOS |
| SQLite | 3.x | pre-installed on macOS |

### Step 1 — Initialize the Database

This only needs to be run once. It creates the `course_sections` and `current_enrollments` tables and seeds time slots for the current semester.

```bash
cd /path/to/purple_eagle
python3 setup_db.py
# Expected output: Done. Total sections: 57
```

Verify:
```bash
sqlite3 maplewood_school.sqlite ".tables"
# Should show: course_sections  current_enrollments  (plus existing tables)
```

### Step 2 — Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The server starts on **http://localhost:8080**. First startup takes ~15–20 seconds to download dependencies.

Test it:
```bash
curl http://localhost:8080/api/courses | python3 -m json.tool | head -20
curl http://localhost:8080/api/students/101
```

### Step 3 — Start the Frontend

In a second terminal:

```bash
cd frontend
npm install       # first time only
npm run dev
```

The dev server starts on **http://localhost:5173** with hot-reload. All `/api` requests are proxied to the backend automatically.

### Step 4 — Use the App

1. Open **http://localhost:5173**
2. Enter any student ID between **1 and 400** and click **Load**
   - Students with rich history: try IDs **101–400** (Grade 10–12 students)
   - Student 1–100 are Grade 9 with minimal history
3. Browse courses, apply filters, and click **Enroll**
4. View your schedule in the right panel and click **Drop** to remove a course

### Useful Test Scenarios

```bash
# Get student profile with history
curl http://localhost:8080/api/students/150

# Enroll in a course (replace sectionId with a valid one from /api/courses)
curl -X POST http://localhost:8080/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{"studentId": 150, "sectionId": 9}'

# Get current schedule
curl http://localhost:8080/api/students/150/schedule

# Drop a course (use enrollmentId from schedule response)
curl -X DELETE "http://localhost:8080/api/enrollments/1?studentId=150"
```

### Reset Enrollments

To clear all current enrollments and start fresh:

```bash
sqlite3 maplewood_school.sqlite "DELETE FROM current_enrollments;"
```

---

## 7. How to Run — Docker

### Prerequisites

- Docker Desktop running

### Run Everything

```bash
cd /path/to/purple_eagle
docker-compose up --build
```

- Frontend → **http://localhost:3000**
- Backend  → **http://localhost:8080**

The first build takes a few minutes (Maven downloads dependencies inside the container). Subsequent starts are fast.

### How Docker is Structured

```
docker-compose.yml
├── backend service
│   ├── Build context: project root (needs access to backend/ folder)
│   ├── Dockerfile: backend/Dockerfile (2-stage: Maven build → JRE runtime)
│   ├── Port: 8080:8080
│   ├── Volume: ./maplewood_school.sqlite → /app/maplewood_school.sqlite
│   └── Env: DB_URL=jdbc:sqlite:/app/maplewood_school.sqlite
│
└── frontend service
    ├── Build context: frontend/
    ├── Dockerfile: frontend/Dockerfile (2-stage: Node build → nginx serve)
    ├── Port: 3000:80
    └── depends_on: backend (waits for health check to pass)
```

The SQLite database is **mounted as a volume** (not baked into the image) so that:
- Enrollments made through the app persist across container restarts
- The same database file is shared with any local development

### Useful Docker Commands

```bash
# Start in background
docker-compose up --build -d

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Stop everything
docker-compose down

# Reset all enrollments
docker-compose exec backend sh -c \
  'sqlite3 /app/maplewood_school.sqlite "DELETE FROM current_enrollments;"'

# Rebuild only one service
docker-compose up --build backend
```

---

*Generated for Maplewood High School Fullstack Coding Challenge*
