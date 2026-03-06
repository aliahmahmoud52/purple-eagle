package com.maplewood.dto;

import java.util.List;

public class StudentProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer gradeLevel;
    private Double gpa;
    private Double creditsEarned;
    private int totalCreditsToGraduate = 30;
    private List<CourseHistoryItemDTO> courseHistory;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }
    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }
    public Double getCreditsEarned() { return creditsEarned; }
    public void setCreditsEarned(Double creditsEarned) { this.creditsEarned = creditsEarned; }
    public int getTotalCreditsToGraduate() { return totalCreditsToGraduate; }
    public List<CourseHistoryItemDTO> getCourseHistory() { return courseHistory; }
    public void setCourseHistory(List<CourseHistoryItemDTO> courseHistory) { this.courseHistory = courseHistory; }
}
