package com.maplewood.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maplewood.dto.EnrollmentRequest;
import com.maplewood.dto.EnrollmentResponse;
import com.maplewood.dto.ScheduleItemDTO;
import com.maplewood.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentController.class)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;

    // --- GET /api/students/{id}/schedule ---

    @Test
    void getSchedule_returnsScheduleItems() throws Exception {
        ScheduleItemDTO item = makeScheduleItem(1L, "MATH101", "Algebra");
        when(enrollmentService.getSchedule(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/students/1/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].courseCode").value("MATH101"))
                .andExpect(jsonPath("$[0].courseName").value("Algebra"));
    }

    @Test
    void getSchedule_noEnrollments_returnsEmptyArray() throws Exception {
        when(enrollmentService.getSchedule(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/students/1/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- POST /api/enrollments ---

    @Test
    void enroll_success_returns200WithEnrollment() throws Exception {
        ScheduleItemDTO item = makeScheduleItem(1L, "MATH101", "Algebra");
        EnrollmentResponse response = EnrollmentResponse.success("Successfully enrolled in \"Algebra\".", item);

        when(enrollmentService.enroll(1L, 200L)).thenReturn(response);

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(1L);
        request.setSectionId(200L);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Successfully enrolled in \"Algebra\"."));
    }

    @Test
    void enroll_gradeLevelError_returns400() throws Exception {
        EnrollmentResponse response = EnrollmentResponse.error("grade_level",
                "This course requires grade 11 or higher.");

        when(enrollmentService.enroll(1L, 200L)).thenReturn(response);

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(1L);
        request.setSectionId(200L);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorType").value("grade_level"));
    }

    @Test
    void enroll_prerequisiteMissing_returns400() throws Exception {
        EnrollmentResponse response = EnrollmentResponse.error("prerequisite",
                "Missing prerequisite: you must pass \"Pre-Algebra\".");

        when(enrollmentService.enroll(1L, 200L)).thenReturn(response);

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(1L);
        request.setSectionId(200L);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value("prerequisite"));
    }

    @Test
    void enroll_timeConflict_returns400() throws Exception {
        EnrollmentResponse response = EnrollmentResponse.error("conflict",
                "Time conflict with \"English\" (MWF 08:00–09:00).");

        when(enrollmentService.enroll(1L, 200L)).thenReturn(response);

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(1L);
        request.setSectionId(200L);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value("conflict"));
    }

    @Test
    void enroll_maxCourses_returns400() throws Exception {
        EnrollmentResponse response = EnrollmentResponse.error("max_courses",
                "You have reached the maximum of 5 courses per semester.");

        when(enrollmentService.enroll(1L, 200L)).thenReturn(response);

        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(1L);
        request.setSectionId(200L);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorType").value("max_courses"));
    }

    // --- DELETE /api/enrollments/{id} ---

    @Test
    void drop_success_returns200() throws Exception {
        doNothing().when(enrollmentService).drop(10L, 1L);

        mockMvc.perform(delete("/api/enrollments/10").param("studentId", "1"))
                .andExpect(status().isOk());

        verify(enrollmentService).drop(10L, 1L);
    }

    @Test
    void drop_wrongStudent_returns400() throws Exception {
        doThrow(new RuntimeException("Enrollment does not belong to this student."))
                .when(enrollmentService).drop(10L, 2L);

        mockMvc.perform(delete("/api/enrollments/10").param("studentId", "2"))
                .andExpect(status().isBadRequest());
    }

    // --- helper ---

    private ScheduleItemDTO makeScheduleItem(Long enrollmentId, String code, String name) {
        ScheduleItemDTO dto = new ScheduleItemDTO();
        dto.setEnrollmentId(enrollmentId);
        dto.setSectionId(200L);
        dto.setCourseId(100L);
        dto.setCourseCode(code);
        dto.setCourseName(name);
        dto.setCredits(3.0);
        dto.setDays("MWF");
        dto.setStartTime("08:00");
        dto.setEndTime("09:00");
        return dto;
    }
}
