package com.maplewood.repository;

import com.maplewood.model.StudentCourseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCourseHistoryRepository extends JpaRepository<StudentCourseHistory, Long> {
    List<StudentCourseHistory> findByStudentId(Long studentId);
    Optional<StudentCourseHistory> findByStudentIdAndCourse_IdAndStatus(Long studentId, Long courseId, String status);
}
