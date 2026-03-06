package com.maplewood.dto;

public class CourseHistoryItemDTO {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Double credits;
    private Long semesterId;
    private String status;

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Double getCredits() { return credits; }
    public void setCredits(Double credits) { this.credits = credits; }
    public Long getSemesterId() { return semesterId; }
    public void setSemesterId(Long semesterId) { this.semesterId = semesterId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
