import React from 'react';
import { useSelector } from 'react-redux';
import type { RootState } from '../store';

const StudentDashboard: React.FC = () => {
  const { profile, loading, error } = useSelector((s: RootState) => s.student);
  const schedule = useSelector((s: RootState) => s.enrollment.schedule);

  if (loading) return <div className="dashboard"><div className="loading">Loading student...</div></div>;
  if (error) return <div className="dashboard"><div className="error">{error}</div></div>;
  if (!profile) return <div className="dashboard"><div className="placeholder">Select a student to begin.</div></div>;

  const progress = Math.min((profile.creditsEarned / profile.totalCreditsToGraduate) * 100, 100);
  const currentCredits = schedule.reduce((sum, s) => sum + s.credits, 0);

  return (
    <div className="dashboard">
      <h2>{profile.firstName} {profile.lastName}</h2>
      <p className="student-meta">Grade {profile.gradeLevel} &bull; {profile.email}</p>

      <div className="stat-grid">
        <div className="stat-card">
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

      <div className="history-section">
        <h3>Course History</h3>
        <div className="history-list">
          {profile.courseHistory.length === 0 && <p className="empty">No history yet.</p>}
          {profile.courseHistory.map(h => (
            <div key={`${h.courseId}-${h.semesterId}`} className={`history-item ${h.status}`}>
              <span className="history-code">{h.courseCode}</span>
              <span className="history-name">{h.courseName}</span>
              <span className={`badge badge-${h.status}`}>{h.status}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default StudentDashboard;
