import { describe, it, expect } from 'vitest';
import coursesReducer, { setFilters, resetFilters, fetchCourses } from '../store/slices/coursesSlice';
import type { CourseWithSection } from '../types';

const makeCourse = (id: number, name: string, code: string): CourseWithSection => ({
  id, code, name, description: '', credits: 3, hoursPerWeek: 5, courseType: 'core',
  gradeLevelMin: 9, gradeLevelMax: 12, semesterOrder: 1,
  prerequisiteId: null, prerequisiteName: null,
  sectionId: null, days: null, startTime: null, endTime: null,
});

const initialState = {
  list: [],
  filters: { gradeLevel: null, semesterOrder: null, courseType: null, searchText: '' },
  loading: false,
  error: null,
};

describe('coursesSlice', () => {
  it('returns initial state', () => {
    expect(coursesReducer(undefined, { type: '@@init' })).toEqual(initialState);
  });

  describe('setFilters', () => {
    it('updates a single filter field', () => {
      const next = coursesReducer(initialState, setFilters({ gradeLevel: 10 }));
      expect(next.filters.gradeLevel).toBe(10);
      expect(next.filters.semesterOrder).toBeNull(); // others unchanged
    });

    it('updates multiple filter fields at once', () => {
      const next = coursesReducer(initialState, setFilters({ gradeLevel: 11, courseType: 'elective' }));
      expect(next.filters.gradeLevel).toBe(11);
      expect(next.filters.courseType).toBe('elective');
    });

    it('updates searchText', () => {
      const next = coursesReducer(initialState, setFilters({ searchText: 'math' }));
      expect(next.filters.searchText).toBe('math');
    });
  });

  describe('resetFilters', () => {
    it('clears all filters back to initial', () => {
      const withFilters = coursesReducer(
        initialState,
        setFilters({ gradeLevel: 10, courseType: 'core', searchText: 'bio' })
      );
      const reset = coursesReducer(withFilters, resetFilters());
      expect(reset.filters).toEqual(initialState.filters);
    });
  });

  describe('fetchCourses thunk', () => {
    it('pending: sets loading true', () => {
      const next = coursesReducer(initialState, fetchCourses.pending('', undefined));
      expect(next.loading).toBe(true);
      expect(next.error).toBeNull();
    });

    it('fulfilled: stores course list', () => {
      const courses = [makeCourse(1, 'Algebra', 'MATH101'), makeCourse(2, 'English', 'ENG101')];
      const next = coursesReducer(
        { ...initialState, loading: true },
        fetchCourses.fulfilled(courses, '', undefined)
      );
      expect(next.loading).toBe(false);
      expect(next.list).toHaveLength(2);
      expect(next.list[0].code).toBe('MATH101');
    });

    it('rejected: stores error message', () => {
      const next = coursesReducer(
        initialState,
        fetchCourses.rejected(new Error('Network error'), '', undefined)
      );
      expect(next.loading).toBe(false);
      expect(next.error).toBe('Network error');
    });
  });
});
