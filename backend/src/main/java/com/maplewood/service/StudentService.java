package com.maplewood.service;

import com.maplewood.dto.CourseHistoryItemDTO;
import com.maplewood.dto.StudentProfileDTO;
import com.maplewood.model.Student;
import com.maplewood.model.StudentCourseHistory;
import com.maplewood.repository.StudentCourseHistoryRepository;
import com.maplewood.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentCourseHistoryRepository historyRepository;

    public StudentService(StudentRepository studentRepository,
                          StudentCourseHistoryRepository historyRepository) {
        this.studentRepository = studentRepository;
        this.historyRepository = historyRepository;
    }

    public StudentProfileDTO login(String email, Long studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null || !student.getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Invalid credentials.");
        }
        return getStudentProfile(studentId);
    }

    public StudentProfileDTO getStudentProfile(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        List<StudentCourseHistory> history = historyRepository.findByStudentId(studentId);

        double totalCreditsTaken = history.stream()
                .mapToDouble(h -> h.getCourse().getCredits())
                .sum();

        double creditsPassed = history.stream()
                .filter(h -> "passed".equals(h.getStatus()))
                .mapToDouble(h -> h.getCourse().getCredits())
                .sum();

        double gpa = totalCreditsTaken > 0
                ? Math.round((creditsPassed / totalCreditsTaken) * 4.0 * 100.0) / 100.0
                : 0.0;

        List<CourseHistoryItemDTO> historyDTOs = history.stream().map(h -> {
            CourseHistoryItemDTO dto = new CourseHistoryItemDTO();
            dto.setCourseId(h.getCourse().getId());
            dto.setCourseCode(h.getCourse().getCode());
            dto.setCourseName(h.getCourse().getName());
            dto.setCredits(h.getCourse().getCredits());
            dto.setSemesterId(h.getSemesterId());
            dto.setStatus(h.getStatus());
            return dto;
        }).collect(Collectors.toList());

        StudentProfileDTO profile = new StudentProfileDTO();
        profile.setId(student.getId());
        profile.setFirstName(student.getFirstName());
        profile.setLastName(student.getLastName());
        profile.setEmail(student.getEmail());
        profile.setGradeLevel(student.getGradeLevel());
        profile.setGpa(gpa);
        profile.setCreditsEarned(creditsPassed);
        profile.setCourseHistory(historyDTOs);
        return profile;
    }
}
