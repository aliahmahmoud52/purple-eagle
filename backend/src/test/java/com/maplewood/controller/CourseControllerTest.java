package com.maplewood.controller;

import com.maplewood.dto.CourseDTO;
import com.maplewood.service.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Test
    void getCourses_noFilters_returns200WithList() throws Exception {
        CourseDTO c1 = makeCourse(1L, "MATH101", "Algebra");
        CourseDTO c2 = makeCourse(2L, "SCI201", "Chemistry");

        when(courseService.getAllCourses(null, null)).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("MATH101"))
                .andExpect(jsonPath("$[1].code").value("SCI201"));
    }

    @Test
    void getCourses_withGradeLevelFilter_passesParamToService() throws Exception {
        CourseDTO c1 = makeCourse(1L, "MATH101", "Algebra");

        when(courseService.getAllCourses(10, null)).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/courses").param("gradeLevel", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("MATH101"));
    }

    @Test
    void getCourses_withSemesterOrderFilter_passesParamToService() throws Exception {
        CourseDTO c1 = makeCourse(2L, "SCI201", "Chemistry");

        when(courseService.getAllCourses(null, 2)).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/courses").param("semesterOrder", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("SCI201"));
    }

    @Test
    void getCourses_withBothFilters_passesParamsToService() throws Exception {
        when(courseService.getAllCourses(9, 1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/courses")
                        .param("gradeLevel", "9")
                        .param("semesterOrder", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCourses_emptyList_returns200WithEmptyArray() throws Exception {
        when(courseService.getAllCourses(null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- helper ---

    private CourseDTO makeCourse(Long id, String code, String name) {
        CourseDTO dto = new CourseDTO();
        dto.setId(id);
        dto.setCode(code);
        dto.setName(name);
        dto.setCredits(3.0);
        dto.setCourseType("core");
        dto.setGradeLevelMin(9);
        dto.setGradeLevelMax(12);
        dto.setSemesterOrder(1);
        return dto;
    }
}
