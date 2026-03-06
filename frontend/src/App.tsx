import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from './store';
import { fetchStudentProfile } from './store/slices/studentSlice';
import { fetchSchedule, clearSchedule } from './store/slices/enrollmentSlice';
import StudentDashboard from './components/StudentDashboard';
import CourseBrowser from './components/CourseBrowser';
import ScheduleBuilder from './components/ScheduleBuilder';
import './App.css';

const App: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { profile } = useSelector((s: RootState) => s.student);
  const [inputId, setInputId] = useState('');

  const handleLoadStudent = (e: React.FormEvent) => {
    e.preventDefault();
    const id = parseInt(inputId, 10);
    if (!isNaN(id) && id >= 1 && id <= 400) {
      dispatch(clearSchedule());
      dispatch(fetchStudentProfile(id)).then(() => {
        dispatch(fetchSchedule(id));
      });
    }
  };

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-brand">
          <h1>Maplewood High School</h1>
          <span className="header-sub">Course Planning System</span>
        </div>
        <form className="student-picker" onSubmit={handleLoadStudent}>
          <label htmlFor="studentId">Student ID</label>
          <input
            id="studentId"
            type="number"
            min={1}
            max={400}
            placeholder="1–400"
            value={inputId}
            onChange={e => setInputId(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">Load</button>
          {profile && (
            <span className="loaded-student">
              {profile.firstName} {profile.lastName} (Grade {profile.gradeLevel})
            </span>
          )}
        </form>
      </header>

      <main className="app-main">
        <aside className="panel panel-left">
          <StudentDashboard />
        </aside>
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

export default App;
