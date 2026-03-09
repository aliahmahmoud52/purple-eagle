package com.maplewood.dto;

public class LoginRequest {
    private String email;
    private Long studentId;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
}
