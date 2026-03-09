import React from 'react';
import { render } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';
import studentReducer from '../store/slices/studentSlice';
import coursesReducer from '../store/slices/coursesSlice';
import enrollmentReducer from '../store/slices/enrollmentSlice';
import type { RootState } from '../store';

export function renderWithProviders(
  ui: React.ReactElement,
  {
    initialState,
    route = '/',
  }: { initialState?: Partial<RootState>; route?: string } = {}
) {
  const store = configureStore({
    reducer: {
      student: studentReducer,
      courses: coursesReducer,
      enrollment: enrollmentReducer,
    },
    preloadedState: initialState as RootState,
  });

  return {
    ...render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[route]}>
          {ui}
        </MemoryRouter>
      </Provider>
    ),
    store,
  };
}
