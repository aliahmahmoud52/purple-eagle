import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from '../store';
import { dropCourse } from '../store/slices/enrollmentSlice';

const ScheduleBuilder: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { schedule, loading, dropping } = useSelector((s: RootState) => s.enrollment);
  const { profile } = useSelector((s: RootState) => s.student);

  if (!profile) {
    return (
      <div className="schedule-builder">
        <h2>Current Schedule</h2>
        <div className="placeholder">Select a student to see their schedule.</div>
      </div>
    );
  }

  const totalCredits = schedule.reduce((sum, s) => sum + s.credits, 0);

  const handleDrop = (enrollmentId: number) => {
    dispatch(dropCourse({ enrollmentId, studentId: profile.id }));
  };

  return (
    <div className="schedule-builder">
      <h2>Current Schedule</h2>
      <p className="schedule-meta">
        {schedule.length}/5 courses &bull; {totalCredits} credits
      </p>

      {loading && <div className="loading">Loading schedule...</div>}

      {!loading && schedule.length === 0 && (
        <div className="placeholder">No courses enrolled yet. Browse the catalog to add courses.</div>
      )}

      <div className="schedule-list">
        {schedule.map(item => (
          <div key={item.enrollmentId} className="schedule-card">
            <div className="schedule-card-header">
              <span className="course-code">{item.courseCode}</span>
              <span className="course-credits">{item.credits} cr</span>
            </div>
            <div className="course-name">{item.courseName}</div>
            <div className="schedule-time">
              <span className="time-badge">{item.days}</span>
              <span>{item.startTime}–{item.endTime}</span>
            </div>
            <button
              className="btn btn-drop"
              disabled={dropping[item.enrollmentId]}
              onClick={() => handleDrop(item.enrollmentId)}
            >
              {dropping[item.enrollmentId] ? 'Dropping...' : 'Drop'}
            </button>
          </div>
        ))}
      </div>

      {schedule.length === 5 && (
        <div className="info-banner">Maximum of 5 courses reached.</div>
      )}
    </div>
  );
};

export default ScheduleBuilder;
