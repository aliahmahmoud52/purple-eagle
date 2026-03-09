package com.maplewood.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maplewood.dto.LoginRequest;
import com.maplewood.dto.StudentProfileDTO;
import com.maplewood.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    // --- GET /api/students/{id} ---

    @Test
    void getStudentProfile_existingStudent_returns200WithProfile() throws Exception {
        StudentProfileDTO profile = makeProfile(1L, "Alice", "Smith", "alice@maplewood.edu", 11, 3.5);

        when(studentService.getStudentProfile(1L)).thenReturn(profile);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.gpa").value(3.5));
    }

    @Test
    void getStudentProfile_nonExistentStudent_returns404() throws Exception {
        when(studentService.getStudentProfile(999L))
                .thenThrow(new RuntimeException("Student not found"));

        mockMvc.perform(get("/api/students/999"))
                .andExpect(status().isNotFound());
    }

    // --- POST /api/students/login ---

    @Test
    void login_validCredentials_returns200WithProfile() throws Exception {
        StudentProfileDTO profile = makeProfile(1L, "Alice", "Smith", "alice@maplewood.edu", 11, 3.5);

        when(studentService.login("alice@maplewood.edu", 1L)).thenReturn(profile);

        LoginRequest request = new LoginRequest();
        request.setEmail("alice@maplewood.edu");
        request.setStudentId(1L);

        mockMvc.perform(post("/api/students/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@maplewood.edu"));
    }

    @Test
    void login_invalidStudentId_returns401() throws Exception {
        when(studentService.login("alice@maplewood.edu", 999L))
                .thenThrow(new RuntimeException("Invalid credentials."));

        LoginRequest request = new LoginRequest();
        request.setEmail("alice@maplewood.edu");
        request.setStudentId(999L);

        mockMvc.perform(post("/api/students/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_wrongEmail_returns401() throws Exception {
        when(studentService.login("wrong@maplewood.edu", 1L))
                .thenThrow(new RuntimeException("Invalid credentials."));

        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@maplewood.edu");
        request.setStudentId(1L);

        mockMvc.perform(post("/api/students/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- helper ---

    private StudentProfileDTO makeProfile(Long id, String first, String last, String email,
                                          int grade, double gpa) {
        StudentProfileDTO p = new StudentProfileDTO();
        p.setId(id);
        p.setFirstName(first);
        p.setLastName(last);
        p.setEmail(email);
        p.setGradeLevel(grade);
        p.setGpa(gpa);
        p.setCreditsEarned(0.0);
        p.setCourseHistory(Collections.emptyList());
        return p;
    }
}
