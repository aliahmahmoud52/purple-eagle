package com.maplewood.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_course_history")
public class StudentCourseHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "student_id", nullable = false) private Long studentId;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "course_id", nullable = false) private Course course;
    @Column(name = "semester_id", nullable = false) private Long semesterId;
    @Column(nullable = false) private String status;

    public StudentCourseHistory() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Long getSemesterId() { return semesterId; }
    public void setSemesterId(Long semesterId) { this.semesterId = semesterId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
