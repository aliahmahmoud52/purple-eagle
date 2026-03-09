import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from '../store';
import { loginStudent } from '../store/slices/studentSlice';

const LoginPage: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { profile, loading, error } = useSelector((s: RootState) => s.student);

  const [email, setEmail] = useState('');
  const [studentId, setStudentId] = useState('');

  useEffect(() => {
    if (profile) navigate('/profile');
  }, [profile, navigate]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const id = parseInt(studentId, 10);
    if (email.trim() && !isNaN(id)) {
      dispatch(loginStudent({ email: email.trim(), studentId: id }));
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-brand">
          <h1>Maplewood High School</h1>
          <p>Course Planning System</p>
        </div>

        <form className="login-form" onSubmit={handleSubmit} data-cy="login-form">
          <h2>Student Login</h2>

          {error && <div className="login-error" data-cy="login-error">{error}</div>}

          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              type="email"
              placeholder="your.name@maplewood.edu"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              data-cy="email-input"
            />
          </div>

          <div className="form-group">
            <label htmlFor="studentId">Student ID</label>
            <input
              id="studentId"
              type="number"
              placeholder="1–400"
              min={1}
              max={400}
              value={studentId}
              onChange={e => setStudentId(e.target.value)}
              required
              data-cy="student-id-input"
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-login"
            disabled={loading}
            data-cy="login-button"
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
