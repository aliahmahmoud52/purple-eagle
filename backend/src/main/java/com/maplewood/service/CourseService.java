package com.maplewood.service;

import com.maplewood.dto.CourseDTO;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.repository.CourseSectionRepository;
import com.maplewood.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final long CURRENT_SEMESTER_ID = 7L;

    private final CourseRepository courseRepository;
    private final CourseSectionRepository sectionRepository;

    public CourseService(CourseRepository courseRepository, CourseSectionRepository sectionRepository) {
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
    }

    public List<CourseDTO> getAllCourses(Integer gradeLevel, Integer semesterOrder) {
        // Build a map of courseId -> section for the current semester
        List<CourseSection> sections = sectionRepository.findBySemesterId(CURRENT_SEMESTER_ID);
        Map<Long, CourseSection> sectionByCourseId = sections.stream()
                .collect(Collectors.toMap(s -> s.getCourse().getId(), s -> s));

        List<Course> courses = courseRepository.findAll();

        List<CourseDTO> result = new ArrayList<>();
        for (Course course : courses) {
            // Apply grade level filter
            if (gradeLevel != null) {
                if (course.getGradeLevelMin() != null && course.getGradeLevelMin() > gradeLevel) continue;
                if (course.getGradeLevelMax() != null && course.getGradeLevelMax() < gradeLevel) continue;
            }
            // Apply semester order filter
            if (semesterOrder != null && !semesterOrder.equals(course.getSemesterOrder())) continue;

            CourseDTO dto = toDTO(course, sectionByCourseId.get(course.getId()));
            result.add(dto);
        }
        return result;
    }

    private CourseDTO toDTO(Course course, CourseSection section) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setCode(course.getCode());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setCredits(course.getCredits());
        dto.setHoursPerWeek(course.getHoursPerWeek());
        dto.setCourseType(course.getCourseType());
        dto.setGradeLevelMin(course.getGradeLevelMin());
        dto.setGradeLevelMax(course.getGradeLevelMax());
        dto.setSemesterOrder(course.getSemesterOrder());

        if (course.getPrerequisite() != null) {
            dto.setPrerequisiteId(course.getPrerequisite().getId());
            dto.setPrerequisiteName(course.getPrerequisite().getName());
        }

        if (section != null) {
            dto.setSectionId(section.getId());
            dto.setDays(section.getDays());
            dto.setStartTime(section.getStartTime());
            dto.setEndTime(section.getEndTime());
        }
        return dto;
    }
}
