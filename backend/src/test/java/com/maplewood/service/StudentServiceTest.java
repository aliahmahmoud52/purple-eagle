package com.maplewood.service;

import com.maplewood.dto.StudentProfileDTO;
import com.maplewood.model.Course;
import com.maplewood.model.Student;
import com.maplewood.model.StudentCourseHistory;
import com.maplewood.repository.StudentCourseHistoryRepository;
import com.maplewood.repository.StudentRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentCourseHistoryRepository historyRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setFirstName("Alice");
        student.setLastName("Smith");
        student.setEmail("alice@maplewood.edu");
        student.setGradeLevel(11);
    }

    // --- getStudentProfile ---

    @Test
    void getStudentProfile_returnsProfileWithCalculatedGpa() {
        Course course = makeCourse(1L, "Math 101", 3.0);
        StudentCourseHistory h = makeHistory(student, course, "passed");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of(h));

        StudentProfileDTO profile = studentService.getStudentProfile(1L);

        assertThat(profile.getId()).isEqualTo(1L);
        assertThat(profile.getFirstName()).isEqualTo("Alice");
        assertThat(profile.getGpa()).isEqualTo(4.0);
        assertThat(profile.getCreditsEarned()).isEqualTo(3.0);
        assertThat(profile.getCourseHistory()).hasSize(1);
    }

    @Test
    void getStudentProfile_withMixedPassFail_calculatesGpaCorrectly() {
        Course c1 = makeCourse(1L, "Math", 3.0);
        Course c2 = makeCourse(2L, "English", 3.0);
        StudentCourseHistory passed = makeHistory(student, c1, "passed");
        StudentCourseHistory failed = makeHistory(student, c2, "failed");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of(passed, failed));

        StudentProfileDTO profile = studentService.getStudentProfile(1L);

        // 3 credits passed / 6 total * 4.0 = 2.0
        assertThat(profile.getGpa()).isEqualTo(2.0);
        assertThat(profile.getCreditsEarned()).isEqualTo(3.0);
    }

    @Test
    void getStudentProfile_withNoCourseHistory_returnsZeroGpa() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(Collections.emptyList());

        StudentProfileDTO profile = studentService.getStudentProfile(1L);

        assertThat(profile.getGpa()).isEqualTo(0.0);
        assertThat(profile.getCreditsEarned()).isEqualTo(0.0);
        assertThat(profile.getCourseHistory()).isEmpty();
    }

    @Test
    void getStudentProfile_studentNotFound_throwsException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentProfile(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found");
    }

    // --- login ---

    @Test
    void login_withValidCredentials_returnsProfile() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(Collections.emptyList());

        StudentProfileDTO profile = studentService.login("alice@maplewood.edu", 1L);

        assertThat(profile.getId()).isEqualTo(1L);
        assertThat(profile.getEmail()).isEqualTo("alice@maplewood.edu");
    }

    @Test
    void login_withEmailCaseInsensitive_succeeds() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(Collections.emptyList());

        StudentProfileDTO profile = studentService.login("ALICE@MAPLEWOOD.EDU", 1L);

        assertThat(profile.getId()).isEqualTo(1L);
    }

    @Test
    void login_withInvalidStudentId_throwsException() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.login("alice@maplewood.edu", 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials.");
    }

    @Test
    void login_withWrongEmail_throwsException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> studentService.login("wrong@maplewood.edu", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials.");
    }

    // --- helpers ---

    private Course makeCourse(Long id, String name, double credits) {
        Course c = new Course();
        c.setId(id);
        c.setName(name);
        c.setCredits(credits);
        return c;
    }

    private StudentCourseHistory makeHistory(Student s, Course c, String status) {
        StudentCourseHistory h = new StudentCourseHistory();
        h.setStudentId(s.getId());
        h.setCourse(c);
        h.setStatus(status);
        h.setSemesterId(1L);
        return h;
    }
}
