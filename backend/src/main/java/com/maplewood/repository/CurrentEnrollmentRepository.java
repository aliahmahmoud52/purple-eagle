package com.maplewood.repository;

import com.maplewood.model.CurrentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrentEnrollmentRepository extends JpaRepository<CurrentEnrollment, Long> {
    List<CurrentEnrollment> findByStudentId(Long studentId);
    Optional<CurrentEnrollment> findByStudentIdAndSection_Id(Long studentId, Long sectionId);
    long countByStudentId(Long studentId);
}
