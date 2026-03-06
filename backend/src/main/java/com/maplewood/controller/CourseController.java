package com.maplewood.controller;

import com.maplewood.dto.CourseDTO;
import com.maplewood.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getCourses(
            @RequestParam(required = false) Integer gradeLevel,
            @RequestParam(required = false) Integer semesterOrder) {
        return ResponseEntity.ok(courseService.getAllCourses(gradeLevel, semesterOrder));
    }
}
