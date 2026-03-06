import { configureStore } from '@reduxjs/toolkit';
import studentReducer from './slices/studentSlice';
import coursesReducer from './slices/coursesSlice';
import enrollmentReducer from './slices/enrollmentSlice';

export const store = configureStore({
  reducer: {
    student: studentReducer,
    courses: coursesReducer,
    enrollment: enrollmentReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
