import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { studentsApi } from '../../api/client';
import type { StudentProfile } from '../../types';

interface StudentState {
  profile: StudentProfile | null;
  loading: boolean;
  error: string | null;
}

const initialState: StudentState = {
  profile: null,
  loading: false,
  error: null,
};

export const loginStudent = createAsyncThunk(
  'student/login',
  async ({ email, studentId }: { email: string; studentId: number }) =>
    studentsApi.login(email, studentId)
);

export const fetchStudentProfile = createAsyncThunk(
  'student/fetchProfile',
  async (studentId: number) => studentsApi.getProfile(studentId)
);

const studentSlice = createSlice({
  name: 'student',
  initialState,
  reducers: {
    logout(state) {
      state.profile = null;
      state.error = null;
    },
  },
  extraReducers: builder => {
    builder
      .addCase(loginStudent.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginStudent.fulfilled, (state, action) => {
        state.loading = false;
        state.profile = action.payload;
      })
      .addCase(loginStudent.rejected, state => {
        state.loading = false;
        state.error = 'Invalid email or student ID. Please try again.';
        state.profile = null;
      })
      .addCase(fetchStudentProfile.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchStudentProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.profile = action.payload;
      })
      .addCase(fetchStudentProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message ?? 'Student not found.';
        state.profile = null;
      });
  },
});

export const { logout } = studentSlice.actions;
export default studentSlice.reducer;
