package com.maplewood.model;

import jakarta.persistence.*;

@Entity
@Table(name = "course_sections")
public class CourseSection {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "course_id", nullable = false) private Course course;
    @Column(name = "semester_id", nullable = false) private Long semesterId;
    @Column(name = "teacher_id", nullable = false) private Long teacherId;
    @Column(name = "classroom_id", nullable = false) private Long classroomId;
    @Column(nullable = false) private String days;
    @Column(name = "start_time", nullable = false) private String startTime;
    @Column(name = "end_time", nullable = false) private String endTime;

    public CourseSection() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Long getSemesterId() { return semesterId; }
    public void setSemesterId(Long semesterId) { this.semesterId = semesterId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }
    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
