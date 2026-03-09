package com.maplewood.service;

import com.maplewood.dto.EnrollmentResponse;
import com.maplewood.dto.ScheduleItemDTO;
import com.maplewood.model.*;
import com.maplewood.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CourseSectionRepository sectionRepository;
    @Mock
    private CurrentEnrollmentRepository enrollmentRepository;
    @Mock
    private StudentCourseHistoryRepository historyRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student student;
    private Course course;
    private CourseSection section;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setGradeLevel(10);

        course = new Course();
        course.setId(100L);
        course.setName("Algebra");
        course.setCode("MATH101");
        course.setCredits(3.0);
        course.setGradeLevelMin(9);
        course.setGradeLevelMax(12);

        section = new CourseSection();
        section.setId(200L);
        section.setCourse(course);
        section.setDays("MWF");
        section.setStartTime("08:00");
        section.setEndTime("09:00");
    }

    // --- enroll success ---

    @Test
    void enroll_withValidInput_returnsSuccess() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(200L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findByStudentIdAndSection_Id(1L, 200L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByStudentId(1L)).thenReturn(0L);
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(Collections.emptyList());
        CurrentEnrollment saved = new CurrentEnrollment();
        saved.setId(1L);
        saved.setStudentId(1L);
        saved.setSection(section);
        when(enrollmentRepository.save(any())).thenReturn(saved);

        EnrollmentResponse response = enrollmentService.enroll(1L, 200L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("Algebra");
    }

    // --- enroll validation failures ---

    @Test
    void enroll_studentNotFound_returnsNotFoundError() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        EnrollmentResponse response = enrollmentService.enroll(99L, 200L);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorType()).isEqualTo("not_found");
    }

    @Test
    void enroll_sectionNotFound_returnsNotFoundError() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(999L)).thenReturn(Optional.empty());

        EnrollmentResponse response = enrollmentService.enroll(1L, 999L);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorType()).isEqualTo("not_found");
    }

    @Test
    void enroll_gradeLevelTooLow_returnsGradeLevelError() {
        student.setGradeLevel(8); // below min grade 9
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(200L)).thenReturn(Optional.of(section));

        EnrollmentResponse response = enrollmentService.enroll(1L, 200L);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorType()).isEqualTo("grade_level");
        assertThat(response.getMessage()).contains("grade 9");
    }

    @Test
    void enroll_gradeLevelTooHigh_returnsGradeLevelError() {
        course.setGradeLevelMax(10);
        student.setGradeLevel(11); // above max grade 10
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(200L)).thenReturn(Optional.of(section));

        EnrollmentResponse response = enrollmentService.enroll(1L, 200L);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorType()).isEqualTo("grade_level");
    }

    @Test
    void enroll_prerequisiteNotPassed_returnsPrerequisiteError() {
        Course prereq = new Course();
        prereq.setId(50L);
        prereq.setName("Pre-Algebra");
        course.setPrerequisite(prereq);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(200L)).thenReturn(Optional.of(section));
        when(historyRepository.findByStudentIdAndCourse_IdAndStatus(1L, 50L, "passed"))
                .thenReturn(Optional.empty());

        EnrollmentResponse response = enrollmentService.enroll(1L, 200L);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorType()).isEqualTo("prerequisite");
        assertThat(response.getMessage()).contains("Pre-Algebra");
    }

    @Test
    void enroll_alreadyEnrolled_returnsAlreadyEnrolledError() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(200L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findByStudentIdAndSection_Id(1L, 200L))
                .thenReturn(Optional.of(new CurrentEnrollment()));

        EnrollmentResponse response = enrollmentService.enroll(1L, 200L);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorType()).isEqualTo("already_enrolled");
    }

    @Test
    void enroll_maxCoursesReached_returnsMaxCoursesError() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(200L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findByStudentIdAndSection_Id(1L, 200L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByStudentId(1L)).thenReturn(5L);

        EnrollmentResponse response = enrollmentService.enroll(1L, 200L);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorType()).isEqualTo("max_courses");
    }

    @Test
    void enroll_timeConflictSameDaysOverlappingTime_returnsConflictError() {
        // Set up an existing enrollment that conflicts
        CourseSection conflicting = new CourseSection();
        conflicting.setId(201L);
        conflicting.setCourse(course);
        conflicting.setDays("MWF");
        conflicting.setStartTime("08:30"); // overlaps with 08:00–09:00
        conflicting.setEndTime("09:30");

        CurrentEnrollment existing = new CurrentEnrollment();
        existing.setSection(conflicting);

        // New section: MWF 08:00–09:00 conflicts with MWF 08:30–09:30
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(200L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findByStudentIdAndSection_Id(1L, 200L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByStudentId(1L)).thenReturn(1L);
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(existing));

        EnrollmentResponse response = enrollmentService.enroll(1L, 200L);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorType()).isEqualTo("conflict");
    }

    @Test
    void enroll_differentDaysNoConflict_returnsSuccess() {
        CourseSection noConflict = new CourseSection();
        Course otherCourse = new Course();
        otherCourse.setId(101L);
        otherCourse.setName("English");
        otherCourse.setCredits(3.0);
        noConflict.setId(202L);
        noConflict.setCourse(otherCourse);
        noConflict.setDays("TTh"); // different days
        noConflict.setStartTime("08:00");
        noConflict.setEndTime("09:30");

        CurrentEnrollment existing = new CurrentEnrollment();
        existing.setSection(noConflict);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(200L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.findByStudentIdAndSection_Id(1L, 200L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByStudentId(1L)).thenReturn(1L);
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(existing));
        CurrentEnrollment saved = new CurrentEnrollment();
        saved.setId(5L);
        saved.setStudentId(1L);
        saved.setSection(section);
        when(enrollmentRepository.save(any())).thenReturn(saved);

        EnrollmentResponse response = enrollmentService.enroll(1L, 200L);

        assertThat(response.isSuccess()).isTrue();
    }

    // --- drop ---

    @Test
    void drop_withValidEnrollment_deletesIt() {
        CurrentEnrollment enrollment = new CurrentEnrollment();
        enrollment.setId(10L);
        enrollment.setStudentId(1L);
        enrollment.setSection(section);

        when(enrollmentRepository.findById(10L)).thenReturn(Optional.of(enrollment));

        enrollmentService.drop(10L, 1L);

        verify(enrollmentRepository).deleteById(10L);
    }

    @Test
    void drop_enrollmentNotFound_throwsException() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.drop(99L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Enrollment not found");
    }

    @Test
    void drop_wrongStudent_throwsException() {
        CurrentEnrollment enrollment = new CurrentEnrollment();
        enrollment.setId(10L);
        enrollment.setStudentId(2L); // belongs to student 2
        enrollment.setSection(section);

        when(enrollmentRepository.findById(10L)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> enrollmentService.drop(10L, 1L)) // student 1 tries to drop
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("does not belong");
    }

    // --- getSchedule ---

    @Test
    void getSchedule_returnsScheduleItems() {
        CurrentEnrollment enrollment = new CurrentEnrollment();
        enrollment.setId(1L);
        enrollment.setStudentId(1L);
        enrollment.setSection(section);

        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(enrollment));

        List<ScheduleItemDTO> schedule = enrollmentService.getSchedule(1L);

        assertThat(schedule).hasSize(1);
        assertThat(schedule.get(0).getCourseName()).isEqualTo("Algebra");
        assertThat(schedule.get(0).getDays()).isEqualTo("MWF");
    }

    @Test
    void getSchedule_noEnrollments_returnsEmptyList() {
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(Collections.emptyList());

        List<ScheduleItemDTO> schedule = enrollmentService.getSchedule(1L);

        assertThat(schedule).isEmpty();
    }
}
