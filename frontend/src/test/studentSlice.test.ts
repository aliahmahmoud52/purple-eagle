import { describe, it, expect } from 'vitest';
import studentReducer, { logout, loginStudent, fetchStudentProfile } from '../store/slices/studentSlice';
import type { StudentProfile } from '../types';

const mockProfile: StudentProfile = {
  id: 1,
  firstName: 'Alice',
  lastName: 'Smith',
  email: 'alice@maplewood.edu',
  gradeLevel: 11,
  gpa: 3.75,
  creditsEarned: 21,
  totalCreditsToGraduate: 30,
  courseHistory: [],
};

const initialState = { profile: null, loading: false, error: null };

describe('studentSlice', () => {
  it('returns initial state', () => {
    expect(studentReducer(undefined, { type: '@@init' })).toEqual(initialState);
  });

  it('logout clears profile and error', () => {
    const state = { profile: mockProfile, loading: false, error: 'some error' };
    const next = studentReducer(state, logout());
    expect(next.profile).toBeNull();
    expect(next.error).toBeNull();
  });

  describe('loginStudent thunk', () => {
    it('pending: sets loading true and clears error', () => {
      const next = studentReducer(initialState, loginStudent.pending('', { email: '', studentId: 1 }));
      expect(next.loading).toBe(true);
      expect(next.error).toBeNull();
    });

    it('fulfilled: sets profile and clears loading', () => {
      const next = studentReducer(
        { ...initialState, loading: true },
        loginStudent.fulfilled(mockProfile, '', { email: '', studentId: 1 })
      );
      expect(next.loading).toBe(false);
      expect(next.profile).toEqual(mockProfile);
    });

    it('rejected: sets error message and clears profile', () => {
      const next = studentReducer(
        { profile: mockProfile, loading: true, error: null },
        loginStudent.rejected(new Error('401'), '', { email: '', studentId: 1 })
      );
      expect(next.loading).toBe(false);
      expect(next.profile).toBeNull();
      expect(next.error).toMatch(/invalid/i);
    });
  });

  describe('fetchStudentProfile thunk', () => {
    it('pending: sets loading true', () => {
      const next = studentReducer(initialState, fetchStudentProfile.pending('', 1));
      expect(next.loading).toBe(true);
    });

    it('fulfilled: stores profile', () => {
      const next = studentReducer(
        { ...initialState, loading: true },
        fetchStudentProfile.fulfilled(mockProfile, '', 1)
      );
      expect(next.profile).toEqual(mockProfile);
      expect(next.loading).toBe(false);
    });

    it('rejected: stores error', () => {
      const next = studentReducer(
        initialState,
        fetchStudentProfile.rejected(new Error('not found'), '', 1)
      );
      expect(next.error).toBeTruthy();
      expect(next.profile).toBeNull();
    });
  });
});
