package com.maplewood.repository;

import com.maplewood.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByGradeLevelMinLessThanEqualAndGradeLevelMaxGreaterThanEqual(int gradeLevelMax, int gradeLevelMin);
    List<Course> findBySemesterOrder(int semesterOrder);
}
