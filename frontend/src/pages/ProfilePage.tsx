import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from '../store';
import { logout } from '../store/slices/studentSlice';
import { fetchSchedule, clearSchedule } from '../store/slices/enrollmentSlice';

const ProfilePage: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { profile } = useSelector((s: RootState) => s.student);
  const { schedule, loading: scheduleLoading } = useSelector((s: RootState) => s.enrollment);

  useEffect(() => {
    if (!profile) {
      navigate('/');
      return;
    }
    dispatch(fetchSchedule(profile.id));
  }, [profile, navigate, dispatch]);

  if (!profile) return null;

  const progress = Math.min((profile.creditsEarned / profile.totalCreditsToGraduate) * 100, 100);
  const currentCredits = schedule.reduce((sum, s) => sum + s.credits, 0);

  const handleLogout = () => {
    dispatch(logout());
    dispatch(clearSchedule());
    navigate('/');
  };

  return (
    <div className="profile-page">
      <header className="app-header">
        <div className="header-brand">
          <h1>Maplewood High School</h1>
          <span className="header-sub">Course Planning System</span>
        </div>
        <button className="btn btn-secondary" onClick={handleLogout} data-cy="logout-button">
          Sign Out
        </button>
      </header>

      <main className="profile-main">
        <div className="profile-card" data-cy="profile-card">
          <div className="profile-header">
            <div>
              <h2 data-cy="student-name">{profile.firstName} {profile.lastName}</h2>
              <p className="student-meta">Grade {profile.gradeLevel} &bull; {profile.email}</p>
            </div>
          </div>

          <div className="stat-grid">
            <div className="stat-card" data-cy="gpa-card">
              <div className="stat-value">{profile.gpa.toFixed(2)}</div>
              <div className="stat-label">GPA</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{profile.creditsEarned}</div>
              <div className="stat-label">Credits Earned</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{schedule.length}<span className="stat-sub">/5</span></div>
              <div className="stat-label">This Semester</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{currentCredits}</div>
              <div className="stat-label">Enrolled Credits</div>
            </div>
          </div>

          <div className="graduation-section">
            <div className="graduation-label">
              <span>Graduation Progress</span>
              <span>{profile.creditsEarned}/{profile.totalCreditsToGraduate} credits</span>
            </div>
            <div className="progress-bar">
              <div className="progress-fill" style={{ width: `${progress}%` }} />
            </div>
            {profile.creditsEarned >= profile.totalCreditsToGraduate
              ? <p className="graduation-ready">Ready to graduate!</p>
              : <p className="graduation-remaining">{profile.totalCreditsToGraduate - profile.creditsEarned} credits remaining</p>
            }
          </div>

          <div className="profile-section">
            <h3>Current Schedule</h3>
            {scheduleLoading && <div className="loading">Loading schedule...</div>}
            {!scheduleLoading && schedule.length === 0 && (
              <p className="empty">No courses enrolled yet.</p>
            )}
            <div className="schedule-list">
              {schedule.map(item => (
                <div key={item.enrollmentId} className="schedule-card" data-cy="schedule-item">
                  <div className="schedule-card-header">
                    <span className="course-code">{item.courseCode}</span>
                    <span className="course-credits">{item.credits} cr</span>
                  </div>
                  <div className="course-name">{item.courseName}</div>
                  <div className="schedule-time">
                    <span className="time-badge">{item.days}</span>
                    <span>{item.startTime}–{item.endTime}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="profile-section">
            <h3>Course History</h3>
            <div className="history-list">
              {profile.courseHistory.length === 0 && <p className="empty">No history yet.</p>}
              {profile.courseHistory.map(h => (
                <div key={`${h.courseId}-${h.semesterId}`} className={`history-item ${h.status}`} data-cy="history-item">
                  <span className="history-code">{h.courseCode}</span>
                  <span className="history-name">{h.courseName}</span>
                  <span className={`badge badge-${h.status}`}>{h.status}</span>
                </div>
              ))}
            </div>
          </div>

          <button
            className="btn btn-primary btn-update-schedule"
            onClick={() => navigate('/courses')}
            data-cy="update-schedule-button"
          >
            Update Schedule
          </button>
        </div>
      </main>
    </div>
  );
};

export default ProfilePage;
