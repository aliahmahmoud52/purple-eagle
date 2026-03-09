package com.maplewood.service;

import com.maplewood.dto.CourseDTO;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.repository.CourseSectionRepository;
import com.maplewood.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseSectionRepository sectionRepository;

    @InjectMocks
    private CourseService courseService;

    private Course mathCourse;
    private Course scienceCourse;
    private CourseSection mathSection;

    @BeforeEach
    void setUp() {
        mathCourse = makeCourse(1L, "MATH101", "Algebra", 9, 10, 1);
        scienceCourse = makeCourse(2L, "SCI201", "Chemistry", 11, 12, 2);

        mathSection = new CourseSection();
        mathSection.setId(10L);
        mathSection.setCourse(mathCourse);
        mathSection.setSemesterId(7L);
        mathSection.setDays("MWF");
        mathSection.setStartTime("08:00");
        mathSection.setEndTime("09:00");
        mathSection.setTeacherId(1L);
        mathSection.setClassroomId(1L);
    }

    @Test
    void getAllCourses_withNoFilters_returnsAllCourses() {
        when(sectionRepository.findBySemesterId(anyLong())).thenReturn(List.of(mathSection));
        when(courseRepository.findAll()).thenReturn(List.of(mathCourse, scienceCourse));

        List<CourseDTO> result = courseService.getAllCourses(null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllCourses_withGradeLevelFilter_returnsMatchingCourses() {
        when(sectionRepository.findBySemesterId(anyLong())).thenReturn(List.of(mathSection));
        when(courseRepository.findAll()).thenReturn(List.of(mathCourse, scienceCourse));

        // Grade 10 matches mathCourse (9–10) but not scienceCourse (11–12)
        List<CourseDTO> result = courseService.getAllCourses(10, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("MATH101");
    }

    @Test
    void getAllCourses_withSemesterOrderFilter_returnsMatchingCourses() {
        when(sectionRepository.findBySemesterId(anyLong())).thenReturn(List.of(mathSection));
        when(courseRepository.findAll()).thenReturn(List.of(mathCourse, scienceCourse));

        // semesterOrder=2 matches only scienceCourse
        List<CourseDTO> result = courseService.getAllCourses(null, 2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("SCI201");
    }

    @Test
    void getAllCourses_withBothFilters_returnsMatchingCourse() {
        when(sectionRepository.findBySemesterId(anyLong())).thenReturn(List.of(mathSection));
        when(courseRepository.findAll()).thenReturn(List.of(mathCourse, scienceCourse));

        List<CourseDTO> result = courseService.getAllCourses(9, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("MATH101");
    }

    @Test
    void getAllCourses_withNoCourses_returnsEmptyList() {
        when(sectionRepository.findBySemesterId(anyLong())).thenReturn(Collections.emptyList());
        when(courseRepository.findAll()).thenReturn(Collections.emptyList());

        List<CourseDTO> result = courseService.getAllCourses(null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllCourses_includesSectionInfoWhenSectionExists() {
        when(sectionRepository.findBySemesterId(anyLong())).thenReturn(List.of(mathSection));
        when(courseRepository.findAll()).thenReturn(List.of(mathCourse));

        List<CourseDTO> result = courseService.getAllCourses(null, null);

        assertThat(result).hasSize(1);
        CourseDTO dto = result.get(0);
        assertThat(dto.getSectionId()).isEqualTo(10L);
        assertThat(dto.getDays()).isEqualTo("MWF");
        assertThat(dto.getStartTime()).isEqualTo("08:00");
    }

    @Test
    void getAllCourses_hasNullSectionWhenNoSectionExists() {
        when(sectionRepository.findBySemesterId(anyLong())).thenReturn(Collections.emptyList());
        when(courseRepository.findAll()).thenReturn(List.of(mathCourse));

        List<CourseDTO> result = courseService.getAllCourses(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSectionId()).isNull();
    }

    // --- helpers ---

    private Course makeCourse(Long id, String code, String name, int minGrade, int maxGrade, int semOrder) {
        Course c = new Course();
        c.setId(id);
        c.setCode(code);
        c.setName(name);
        c.setCredits(3.0);
        c.setGradeLevelMin(minGrade);
        c.setGradeLevelMax(maxGrade);
        c.setSemesterOrder(semOrder);
        c.setCourseType("core");
        return c;
    }
}
