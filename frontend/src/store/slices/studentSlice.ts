import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { studentsApi } from '../../api/client';
import type { StudentProfile } from '../../types';

interface StudentState {
  currentStudentId: number | null;
  profile: StudentProfile | null;
  loading: boolean;
  error: string | null;
}

const initialState: StudentState = {
  currentStudentId: null,
  profile: null,
  loading: false,
  error: null,
};

export const fetchStudentProfile = createAsyncThunk(
  'student/fetchProfile',
  async (studentId: number) => studentsApi.getProfile(studentId)
);

const studentSlice = createSlice({
  name: 'student',
  initialState,
  reducers: {
    setCurrentStudentId(state, action: PayloadAction<number | null>) {
      state.currentStudentId = action.payload;
      if (action.payload === null) {
        state.profile = null;
        state.error = null;
      }
    },
  },
  extraReducers: builder => {
    builder
      .addCase(fetchStudentProfile.pending, state => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchStudentProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.profile = action.payload;
        state.currentStudentId = action.payload.id;
      })
      .addCase(fetchStudentProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message ?? 'Student not found.';
        state.profile = null;
      });
  },
});

export const { setCurrentStudentId } = studentSlice.actions;
export default studentSlice.reducer;
