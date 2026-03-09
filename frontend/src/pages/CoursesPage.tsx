import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import type { RootState, AppDispatch } from '../store';
import { logout } from '../store/slices/studentSlice';
import { clearSchedule } from '../store/slices/enrollmentSlice';
import CourseBrowser from '../components/CourseBrowser';
import ScheduleBuilder from '../components/ScheduleBuilder';

const CoursesPage: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const { profile } = useSelector((s: RootState) => s.student);

  useEffect(() => {
    if (!profile) navigate('/');
  }, [profile, navigate]);

  if (!profile) return null;

  const handleLogout = () => {
    dispatch(logout());
    dispatch(clearSchedule());
    navigate('/');
  };

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-brand">
          <h1>Maplewood High School</h1>
          <span className="header-sub">Course Planning System</span>
        </div>
        <div className="header-actions">
          <button
            className="btn btn-secondary"
            onClick={() => navigate('/profile')}
            data-cy="back-to-profile-button"
          >
            ← My Profile
          </button>
          <span className="loaded-student">{profile.firstName} {profile.lastName}</span>
          <button className="btn btn-secondary" onClick={handleLogout} data-cy="logout-button">
            Sign Out
          </button>
        </div>
      </header>

      <main className="app-main">
        <section className="panel panel-center">
          <CourseBrowser />
        </section>
        <aside className="panel panel-right">
          <ScheduleBuilder />
        </aside>
      </main>
    </div>
  );
};

export default CoursesPage;
