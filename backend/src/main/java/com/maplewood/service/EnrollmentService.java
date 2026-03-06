package com.maplewood.service;

import com.maplewood.dto.EnrollmentResponse;
import com.maplewood.dto.ScheduleItemDTO;
import com.maplewood.model.*;
import com.maplewood.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final StudentRepository studentRepository;
    private final CourseSectionRepository sectionRepository;
    private final CurrentEnrollmentRepository enrollmentRepository;
    private final StudentCourseHistoryRepository historyRepository;

    public EnrollmentService(StudentRepository studentRepository,
                             CourseSectionRepository sectionRepository,
                             CurrentEnrollmentRepository enrollmentRepository,
                             StudentCourseHistoryRepository historyRepository) {
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.historyRepository = historyRepository;
    }

    public List<ScheduleItemDTO> getSchedule(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(this::toScheduleItem)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnrollmentResponse enroll(Long studentId, Long sectionId) {
        Student student = studentRepository.findById(studentId)
                .orElse(null);
        if (student == null) {
            return EnrollmentResponse.error("not_found", "Student not found.");
        }

        CourseSection section = sectionRepository.findById(sectionId)
                .orElse(null);
        if (section == null) {
            return EnrollmentResponse.error("not_found", "Course section not found.");
        }

        Course course = section.getCourse();

        // 1. Grade level check
        if (course.getGradeLevelMin() != null && student.getGradeLevel() < course.getGradeLevelMin()) {
            return EnrollmentResponse.error("grade_level",
                    "This course requires grade " + course.getGradeLevelMin()
                            + " or higher. You are in grade " + student.getGradeLevel() + ".");
        }
        if (course.getGradeLevelMax() != null && student.getGradeLevel() > course.getGradeLevelMax()) {
            return EnrollmentResponse.error("grade_level",
                    "This course is only for up to grade " + course.getGradeLevelMax()
                            + ". You are in grade " + student.getGradeLevel() + ".");
        }

        // 2. Prerequisite check
        if (course.getPrerequisite() != null) {
            Long prereqId = course.getPrerequisite().getId();
            boolean passed = historyRepository
                    .findByStudentIdAndCourse_IdAndStatus(studentId, prereqId, "passed")
                    .isPresent();
            if (!passed) {
                return EnrollmentResponse.error("prerequisite",
                        "Missing prerequisite: you must pass \""
                                + course.getPrerequisite().getName() + "\" before enrolling in \""
                                + course.getName() + "\".");
            }
        }

        // 3. Already enrolled check
        if (enrollmentRepository.findByStudentIdAndSection_Id(studentId, sectionId).isPresent()) {
            return EnrollmentResponse.error("already_enrolled",
                    "You are already enrolled in \"" + course.getName() + "\".");
        }

        // 4. Max courses check (5 per semester)
        long currentCount = enrollmentRepository.countByStudentId(studentId);
        if (currentCount >= 5) {
            return EnrollmentResponse.error("max_courses",
                    "You have reached the maximum of 5 courses per semester.");
        }

        // 5. Time conflict check
        List<CurrentEnrollment> existing = enrollmentRepository.findByStudentId(studentId);
        for (CurrentEnrollment enrolled : existing) {
            CourseSection existingSection = enrolled.getSection();
            if (hasTimeConflict(existingSection, section)) {
                return EnrollmentResponse.error("conflict",
                        "Time conflict with \"" + existingSection.getCourse().getName()
                                + "\" (" + existingSection.getDays()
                                + " " + existingSection.getStartTime()
                                + "\u2013" + existingSection.getEndTime() + ").");
            }
        }

        // All checks passed — enroll
        CurrentEnrollment newEnrollment = new CurrentEnrollment();
        newEnrollment.setStudentId(studentId);
        newEnrollment.setSection(section);
        newEnrollment = enrollmentRepository.save(newEnrollment);

        return EnrollmentResponse.success(
                "Successfully enrolled in \"" + course.getName() + "\".",
                toScheduleItem(newEnrollment));
    }

    @Transactional
    public void drop(Long enrollmentId, Long studentId) {
        CurrentEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found."));
        if (!enrollment.getStudentId().equals(studentId)) {
            throw new RuntimeException("Enrollment does not belong to this student.");
        }
        enrollmentRepository.deleteById(enrollmentId);
    }

    private boolean hasTimeConflict(CourseSection a, CourseSection b) {
        // Check day overlap
        boolean dayOverlap = false;
        for (char c : a.getDays().toCharArray()) {
            if (b.getDays().indexOf(c) >= 0) {
                dayOverlap = true;
                break;
            }
        }
        if (!dayOverlap) return false;
        // Time overlap: ranges [aStart, aEnd) and [bStart, bEnd) overlap when aStart < bEnd && bStart < aEnd
        return a.getStartTime().compareTo(b.getEndTime()) < 0
                && b.getStartTime().compareTo(a.getEndTime()) < 0;
    }

    private ScheduleItemDTO toScheduleItem(CurrentEnrollment enrollment) {
        CourseSection section = enrollment.getSection();
        Course course = section.getCourse();
        ScheduleItemDTO dto = new ScheduleItemDTO();
        dto.setEnrollmentId(enrollment.getId());
        dto.setSectionId(section.getId());
        dto.setCourseId(course.getId());
        dto.setCourseCode(course.getCode());
        dto.setCourseName(course.getName());
        dto.setCredits(course.getCredits());
        dto.setDays(section.getDays());
        dto.setStartTime(section.getStartTime());
        dto.setEndTime(section.getEndTime());
        return dto;
    }
}
