import { describe, it, expect } from 'vitest';
import enrollmentReducer, {
  clearEnrollMessages,
  clearSchedule,
  fetchSchedule,
  enrollInCourse,
  dropCourse,
} from '../store/slices/enrollmentSlice';
import type { ScheduleItem } from '../types';

const makeScheduleItem = (id: number): ScheduleItem => ({
  enrollmentId: id,
  sectionId: 200 + id,
  courseId: 100 + id,
  courseCode: `MATH${id}`,
  courseName: `Course ${id}`,
  credits: 3,
  days: 'MWF',
  startTime: '08:00',
  endTime: '09:00',
});

const initialState = {
  schedule: [],
  loading: false,
  error: null,
  enrolling: false,
  enrollError: null,
  enrollSuccess: null,
  dropping: {},
};

describe('enrollmentSlice', () => {
  it('returns initial state', () => {
    expect(enrollmentReducer(undefined, { type: '@@init' })).toEqual(initialState);
  });

  describe('clearSchedule', () => {
    it('empties the schedule array', () => {
      const state = { ...initialState, schedule: [makeScheduleItem(1)] };
      expect(enrollmentReducer(state, clearSchedule()).schedule).toHaveLength(0);
    });
  });

  describe('clearEnrollMessages', () => {
    it('clears enrollError and enrollSuccess', () => {
      const state = { ...initialState, enrollError: 'oops', enrollSuccess: 'done' };
      const next = enrollmentReducer(state, clearEnrollMessages());
      expect(next.enrollError).toBeNull();
      expect(next.enrollSuccess).toBeNull();
    });
  });

  describe('fetchSchedule thunk', () => {
    it('pending: sets loading', () => {
      expect(enrollmentReducer(initialState, fetchSchedule.pending('', 1)).loading).toBe(true);
    });

    it('fulfilled: stores schedule items', () => {
      const items = [makeScheduleItem(1), makeScheduleItem(2)];
      const next = enrollmentReducer(
        { ...initialState, loading: true },
        fetchSchedule.fulfilled(items, '', 1)
      );
      expect(next.loading).toBe(false);
      expect(next.schedule).toHaveLength(2);
    });

    it('rejected: stores error', () => {
      const next = enrollmentReducer(
        initialState,
        fetchSchedule.rejected(new Error('fetch failed'), '', 1)
      );
      expect(next.error).toBe('fetch failed');
    });
  });

  describe('enrollInCourse thunk', () => {
    const arg = { studentId: 1, sectionId: 200 };

    it('pending: sets enrolling true', () => {
      const next = enrollmentReducer(initialState, enrollInCourse.pending('', arg));
      expect(next.enrolling).toBe(true);
      expect(next.enrollError).toBeNull();
      expect(next.enrollSuccess).toBeNull();
    });

    it('fulfilled with success response: adds item and sets success message', () => {
      const item = makeScheduleItem(1);
      const response = { success: true, message: 'Enrolled!', enrollment: item };
      const next = enrollmentReducer(
        { ...initialState, enrolling: true },
        enrollInCourse.fulfilled(response, '', arg)
      );
      expect(next.enrolling).toBe(false);
      expect(next.schedule).toHaveLength(1);
      expect(next.enrollSuccess).toBe('Enrolled!');
    });

    it('fulfilled with error response: sets enrollError, does not add to schedule', () => {
      const response = { success: false, message: 'Time conflict!', errorType: 'conflict' };
      const next = enrollmentReducer(
        { ...initialState, enrolling: true },
        enrollInCourse.fulfilled(response, '', arg)
      );
      expect(next.enrolling).toBe(false);
      expect(next.schedule).toHaveLength(0);
      expect(next.enrollError).toBe('Time conflict!');
    });

    it('rejected: sets enrollError', () => {
      const next = enrollmentReducer(
        { ...initialState, enrolling: true },
        enrollInCourse.rejected(new Error('Network error'), '', arg)
      );
      expect(next.enrolling).toBe(false);
      expect(next.enrollError).toBe('Network error');
    });
  });

  describe('dropCourse thunk', () => {
    const arg = { enrollmentId: 1, studentId: 1 };

    it('pending: marks dropping[enrollmentId] as true', () => {
      const next = enrollmentReducer(initialState, dropCourse.pending('', arg));
      expect(next.dropping[1]).toBe(true);
    });

    it('fulfilled: removes item from schedule and clears dropping flag', () => {
      const state = {
        ...initialState,
        schedule: [makeScheduleItem(1), makeScheduleItem(2)],
        dropping: { 1: true },
      };
      const next = enrollmentReducer(state, dropCourse.fulfilled(1, '', arg));
      expect(next.schedule).toHaveLength(1);
      expect(next.schedule[0].enrollmentId).toBe(2);
      expect(next.dropping[1]).toBeUndefined();
    });

    it('rejected: clears dropping flag', () => {
      const state = { ...initialState, dropping: { 1: true } };
      const next = enrollmentReducer(
        state,
        dropCourse.rejected(new Error('fail'), '', arg)
      );
      expect(next.dropping[1]).toBeUndefined();
    });
  });
});
