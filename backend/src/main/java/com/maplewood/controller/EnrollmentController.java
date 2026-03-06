package com.maplewood.controller;

import com.maplewood.dto.EnrollmentRequest;
import com.maplewood.dto.EnrollmentResponse;
import com.maplewood.dto.ScheduleItemDTO;
import com.maplewood.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/students/{studentId}/schedule")
    public ResponseEntity<List<ScheduleItemDTO>> getSchedule(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getSchedule(studentId));
    }

    @PostMapping("/enrollments")
    public ResponseEntity<EnrollmentResponse> enroll(@RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enroll(request.getStudentId(), request.getSectionId());
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    public ResponseEntity<?> drop(
            @PathVariable Long enrollmentId,
            @RequestParam Long studentId) {
        try {
            enrollmentService.drop(enrollmentId, studentId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
