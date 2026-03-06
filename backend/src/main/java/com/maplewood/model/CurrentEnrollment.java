package com.maplewood.model;

import jakarta.persistence.*;

@Entity
@Table(name = "current_enrollments")
public class CurrentEnrollment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "student_id", nullable = false) private Long studentId;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "section_id", nullable = false) private CourseSection section;
    @Column(name = "enrolled_at") private String enrolledAt;

    public CurrentEnrollment() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public CourseSection getSection() { return section; }
    public void setSection(CourseSection section) { this.section = section; }
    public String getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(String enrolledAt) { this.enrolledAt = enrolledAt; }
}
