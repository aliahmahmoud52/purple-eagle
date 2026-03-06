#!/usr/bin/env python3
"""Creates course_sections and current_enrollments tables, then seeds course sections for Fall 2024."""
import sqlite3

DB_PATH = "maplewood_school.sqlite"

# specialization_id -> list of teacher ids
TEACHERS_BY_SPEC = {
    1: [1,2,3,4,5,6,7,8],        # Mathematics
    2: [9,10,11,12,13,14,15,16], # English
    3: [17,18,19,20,21,22,23,24,25,26], # Science
    4: [27,28,29,30,31,32],      # Social Studies
    5: [33,34,35,36],            # Arts
    6: [37,38,39,40],            # Music
    7: [41,42,43,44],            # PE
    8: [45,46,47],               # CS
    9: [48,49,50],               # Foreign Language
}

# specialization_id -> list of classroom ids (matching required room type)
CLASSROOMS_BY_SPEC = {
    1: list(range(1, 31)),   # Math → standard classrooms
    2: list(range(1, 31)),   # English → standard classrooms
    3: list(range(31, 41)),  # Science → science labs
    4: list(range(1, 31)),   # Social Studies → standard classrooms
    5: list(range(41, 47)),  # Arts → art studios
    6: list(range(56, 61)),  # Music → music rooms
    7: list(range(47, 50)),  # PE → gyms
    8: list(range(50, 56)),  # CS → computer labs
    9: list(range(1, 31)),   # Foreign Language → standard classrooms
}

TIME_SLOTS = [
    ("MWF", "08:00", "09:00"),
    ("MWF", "09:00", "10:00"),
    ("MWF", "10:00", "11:00"),
    ("MWF", "11:00", "12:00"),
    ("TTh", "08:00", "09:30"),
    ("TTh", "09:30", "11:00"),
    ("TTh", "11:00", "12:30"),
    ("MWF", "13:00", "14:00"),
    ("MWF", "14:00", "15:00"),
    ("TTh", "13:00", "14:30"),
    ("TTh", "14:30", "16:00"),
]

CURRENT_SEMESTER_ID = 7  # Fall 2024 (is_active = 1)

def setup():
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()

    cur.executescript("""
        CREATE TABLE IF NOT EXISTS course_sections (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            course_id INTEGER NOT NULL,
            semester_id INTEGER NOT NULL,
            teacher_id INTEGER NOT NULL,
            classroom_id INTEGER NOT NULL,
            days VARCHAR(10) NOT NULL,
            start_time VARCHAR(10) NOT NULL,
            end_time VARCHAR(10) NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (course_id) REFERENCES courses(id),
            FOREIGN KEY (semester_id) REFERENCES semesters(id),
            FOREIGN KEY (teacher_id) REFERENCES teachers(id),
            FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
            UNIQUE(course_id, semester_id)
        );

        CREATE TABLE IF NOT EXISTS current_enrollments (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            section_id INTEGER NOT NULL,
            enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (student_id) REFERENCES students(id),
            FOREIGN KEY (section_id) REFERENCES course_sections(id),
            UNIQUE(student_id, section_id)
        );

        CREATE INDEX IF NOT EXISTS idx_sections_semester ON course_sections(semester_id);
        CREATE INDEX IF NOT EXISTS idx_sections_course ON course_sections(course_id);
        CREATE INDEX IF NOT EXISTS idx_enrollments_student ON current_enrollments(student_id);
        CREATE INDEX IF NOT EXISTS idx_enrollments_section ON current_enrollments(section_id);
    """)

    # Fetch all courses
    cur.execute("SELECT id, specialization_id FROM courses ORDER BY id")
    courses = cur.fetchall()

    # Check if sections already seeded
    cur.execute("SELECT COUNT(*) FROM course_sections WHERE semester_id = ?", (CURRENT_SEMESTER_ID,))
    if cur.fetchone()[0] > 0:
        print("Course sections already seeded. Skipping.")
        conn.close()
        return

    # Track usage counters for round-robin teacher/classroom assignment
    teacher_idx = {spec_id: 0 for spec_id in TEACHERS_BY_SPEC}
    classroom_idx = {spec_id: 0 for spec_id in CLASSROOMS_BY_SPEC}

    for i, (course_id, spec_id) in enumerate(courses):
        days, start, end = TIME_SLOTS[i % len(TIME_SLOTS)]

        teachers = TEACHERS_BY_SPEC[spec_id]
        teacher_id = teachers[teacher_idx[spec_id] % len(teachers)]
        teacher_idx[spec_id] += 1

        classrooms = CLASSROOMS_BY_SPEC[spec_id]
        classroom_id = classrooms[classroom_idx[spec_id] % len(classrooms)]
        classroom_idx[spec_id] += 1

        cur.execute(
            "INSERT OR IGNORE INTO course_sections (course_id, semester_id, teacher_id, classroom_id, days, start_time, end_time) VALUES (?,?,?,?,?,?,?)",
            (course_id, CURRENT_SEMESTER_ID, teacher_id, classroom_id, days, start, end)
        )

    conn.commit()
    cur.execute("SELECT COUNT(*) FROM course_sections")
    print(f"Done. Total sections: {cur.fetchone()[0]}")
    conn.close()

if __name__ == "__main__":
    setup()
