import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { coursesApi } from '../../api/client';
import type { CourseWithSection } from '../../types';

interface Filters {
  gradeLevel: number | null;
  semesterOrder: number | null;
  courseType: string | null;
  searchText: string;
}

interface CoursesState {
  list: CourseWithSection[];
  filters: Filters;
  loading: boolean;
  error: string | null;
}

const initialState: CoursesState = {
  list: [],
  filters: { gradeLevel: null, semesterOrder: null, courseType: null, searchText: '' },
  loading: false,
  error: null,
};

export const fetchCourses = createAsyncThunk('courses/fetchAll', async () =>
  coursesApi.getAll()
);

const coursesSlice = createSlice({
  name: 'courses',
  initialState,
  reducers: {
    setFilters(state, action: PayloadAction<Partial<Filters>>) {
      state.filters = { ...state.filters, ...action.payload };
    },
    resetFilters(state) {
      state.filters = initialState.filters;
    },
  },
  extraReducers: builder => {
    builder
      .addCase(fetchCourses.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCourses.fulfilled, (state, action) => {
        state.loading = false;
        state.list = action.payload;
      })
      .addCase(fetchCourses.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message ?? 'Failed to load courses.';
      });
  },
});

export const { setFilters, resetFilters } = coursesSlice.actions;
export default coursesSlice.reducer;
