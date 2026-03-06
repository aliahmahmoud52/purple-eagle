import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { enrollmentsApi } from '../../api/client';
import type { ScheduleItem } from '../../types';

interface EnrollmentState {
  schedule: ScheduleItem[];
  loading: boolean;
  error: string | null;
  enrolling: boolean;
  enrollError: string | null;
  enrollSuccess: string | null;
  dropping: Record<number, boolean>;
}

const initialState: EnrollmentState = {
  schedule: [],
  loading: false,
  error: null,
  enrolling: false,
  enrollError: null,
  enrollSuccess: null,
  dropping: {},
};

export const fetchSchedule = createAsyncThunk('enrollment/fetchSchedule', async (studentId: number) =>
  enrollmentsApi.getSchedule(studentId)
);

export const enrollInCourse = createAsyncThunk(
  'enrollment/enroll',
  async ({ studentId, sectionId }: { studentId: number; sectionId: number }) =>
    enrollmentsApi.enroll(studentId, sectionId)
);

export const dropCourse = createAsyncThunk(
  'enrollment/drop',
  async ({ enrollmentId, studentId }: { enrollmentId: number; studentId: number }) => {
    await enrollmentsApi.drop(enrollmentId, studentId);
    return enrollmentId;
  }
);

const enrollmentSlice = createSlice({
  name: 'enrollment',
  initialState,
  reducers: {
    clearEnrollMessages(state) {
      state.enrollError = null;
      state.enrollSuccess = null;
    },
    clearSchedule(state) {
      state.schedule = [];
    },
  },
  extraReducers: builder => {
    builder
      // fetchSchedule
      .addCase(fetchSchedule.pending, state => { state.loading = true; state.error = null; })
      .addCase(fetchSchedule.fulfilled, (state, action) => {
        state.loading = false;
        state.schedule = action.payload;
      })
      .addCase(fetchSchedule.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message ?? 'Failed to load schedule.';
      })
      // enroll
      .addCase(enrollInCourse.pending, state => {
        state.enrolling = true;
        state.enrollError = null;
        state.enrollSuccess = null;
      })
      .addCase(enrollInCourse.fulfilled, (state, action) => {
        state.enrolling = false;
        const response = action.payload;
        if (response.success && response.enrollment) {
          state.schedule.push(response.enrollment);
          state.enrollSuccess = response.message;
        } else {
          state.enrollError = response.message;
        }
      })
      .addCase(enrollInCourse.rejected, (state, action) => {
        state.enrolling = false;
        state.enrollError = action.error.message ?? 'Enrollment failed.';
      })
      // drop
      .addCase(dropCourse.pending, (state, action) => {
        state.dropping[action.meta.arg.enrollmentId] = true;
      })
      .addCase(dropCourse.fulfilled, (state, action) => {
        delete state.dropping[action.payload];
        state.schedule = state.schedule.filter(s => s.enrollmentId !== action.payload);
      })
      .addCase(dropCourse.rejected, (state, action) => {
        delete state.dropping[action.meta.arg.enrollmentId];
      });
  },
});

export const { clearEnrollMessages, clearSchedule } = enrollmentSlice.actions;
export default enrollmentSlice.reducer;
