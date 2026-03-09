package com.maplewood.controller;

import com.maplewood.dto.LoginRequest;
import com.maplewood.dto.StudentProfileDTO;
import com.maplewood.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentProfile(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(studentService.getStudentProfile(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            StudentProfileDTO profile = studentService.login(request.getEmail(), request.getStudentId());
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("{\"message\":\"Invalid credentials.\"}");
        }
    }
}
