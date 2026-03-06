import React, { useEffect, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from '../store';
import { fetchCourses, setFilters, resetFilters } from '../store/slices/coursesSlice';
import { enrollInCourse, clearEnrollMessages } from '../store/slices/enrollmentSlice';
import type { CourseWithSection } from '../types';

const CourseBrowser: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { list, filters, loading, error } = useSelector((s: RootState) => s.courses);
  const { profile } = useSelector((s: RootState) => s.student);
  const { schedule, enrolling, enrollError, enrollSuccess } = useSelector((s: RootState) => s.enrollment);

  const enrolledCourseIds = new Set(schedule.map(s => s.courseId));
  const enrolledSectionIds = new Set(schedule.map(s => s.sectionId));

  useEffect(() => {
    dispatch(fetchCourses());
  }, [dispatch]);

  // Auto-dismiss success/error messages
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  useEffect(() => {
    if (enrollError || enrollSuccess) {
      if (timerRef.current) clearTimeout(timerRef.current);
      timerRef.current = setTimeout(() => dispatch(clearEnrollMessages()), 4000);
    }
    return () => { if (timerRef.current) clearTimeout(timerRef.current); };
  }, [enrollError, enrollSuccess, dispatch]);

  const handleEnroll = (course: CourseWithSection) => {
    if (!profile || !course.sectionId) return;
    dispatch(enrollInCourse({ studentId: profile.id, sectionId: course.sectionId }));
  };

  const filteredCourses = list.filter(c => {
    if (filters.gradeLevel != null) {
      if (c.gradeLevelMin != null && c.gradeLevelMin > filters.gradeLevel) return false;
      if (c.gradeLevelMax != null && c.gradeLevelMax < filters.gradeLevel) return false;
    }
    if (filters.semesterOrder != null && c.semesterOrder !== filters.semesterOrder) return false;
    if (filters.courseType && c.courseType !== filters.courseType) return false;
    if (filters.searchText) {
      const q = filters.searchText.toLowerCase();
      if (!c.name.toLowerCase().includes(q) && !c.code.toLowerCase().includes(q)) return false;
    }
    return true;
  });

  const getEnrollButtonState = (course: CourseWithSection) => {
    if (!profile) return { disabled: true, label: 'Select Student' };
    if (enrolledCourseIds.has(course.id)) return { disabled: true, label: 'Enrolled' };
    if (!course.sectionId) return { disabled: true, label: 'No Section' };
    return { disabled: enrolling, label: enrolling ? 'Enrolling...' : 'Enroll' };
  };

  return (
    <div className="course-browser">
      <h2>Course Catalog</h2>

      {(enrollError || enrollSuccess) && (
        <div className={`toast ${enrollError ? 'toast-error' : 'toast-success'}`}>
          {enrollError || enrollSuccess}
        </div>
      )}

      <div className="filter-bar">
        <input
          type="text"
          placeholder="Search courses..."
          value={filters.searchText}
          onChange={e => dispatch(setFilters({ searchText: e.target.value }))}
          className="filter-input"
        />
        <select
          value={filters.gradeLevel ?? ''}
          onChange={e => dispatch(setFilters({ gradeLevel: e.target.value ? Number(e.target.value) : null }))}
          className="filter-select"
        >
          <option value="">All Grades</option>
          <option value="9">Grade 9</option>
          <option value="10">Grade 10</option>
          <option value="11">Grade 11</option>
          <option value="12">Grade 12</option>
        </select>
        <select
          value={filters.semesterOrder ?? ''}
          onChange={e => dispatch(setFilters({ semesterOrder: e.target.value ? Number(e.target.value) : null }))}
          className="filter-select"
        >
          <option value="">Fall & Spring</option>
          <option value="1">Fall</option>
          <option value="2">Spring</option>
        </select>
        <select
          value={filters.courseType ?? ''}
          onChange={e => dispatch(setFilters({ courseType: e.target.value || null }))}
          className="filter-select"
        >
          <option value="">All Types</option>
          <option value="core">Core</option>
          <option value="elective">Elective</option>
        </select>
        <button className="btn btn-secondary" onClick={() => dispatch(resetFilters())}>
          Clear
        </button>
      </div>

      <p className="results-count">{filteredCourses.length} course{filteredCourses.length !== 1 ? 's' : ''}</p>

      {loading && <div className="loading">Loading courses...</div>}
      {error && <div className="error">{error}</div>}

      <div className="course-list">
        {filteredCourses.map(course => {
          const { disabled, label } = getEnrollButtonState(course);
          const isEnrolled = enrolledCourseIds.has(course.id);

          return (
            <div key={course.id} className={`course-card ${isEnrolled ? 'course-card--enrolled' : ''}`}>
              <div className="course-card-header">
                <div>
                  <span className="course-code">{course.code}</span>
                  <span className={`badge badge-${course.courseType}`}>{course.courseType}</span>
                  <span className="badge badge-semester">
                    {course.semesterOrder === 1 ? 'Fall' : 'Spring'}
                  </span>
                </div>
                <span className="course-credits">{course.credits} cr</span>
              </div>

              <div className="course-name">{course.name}</div>

              <div className="course-meta">
                <span>Grades {course.gradeLevelMin}–{course.gradeLevelMax}</span>
                {course.sectionId && (
                  <span className="time-badge">
                    {course.days} {course.startTime}–{course.endTime}
                  </span>
                )}
              </div>

              {course.prerequisiteName && (
                <div className="prerequisite">
                  Requires: <em>{course.prerequisiteName}</em>
                </div>
              )}

              <button
                className={`btn ${isEnrolled ? 'btn-enrolled' : 'btn-enroll'}`}
                disabled={disabled}
                onClick={() => !disabled && handleEnroll(course)}
              >
                {label}
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default CourseBrowser;
