import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from './testUtils';
import LoginPage from '../pages/LoginPage';
import * as clientModule from '../api/client';

vi.mock('../api/client', () => ({
  studentsApi: {
    login: vi.fn(),
    getProfile: vi.fn(),
  },
  coursesApi: { getAll: vi.fn() },
  enrollmentsApi: { getSchedule: vi.fn(), enroll: vi.fn(), drop: vi.fn() },
}));

// Mock navigation
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders email and student ID inputs', () => {
    renderWithProviders(<LoginPage />);
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/student id/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('renders the school name and title', () => {
    renderWithProviders(<LoginPage />);
    expect(screen.getByText('Maplewood High School')).toBeInTheDocument();
    expect(screen.getByText(/student login/i)).toBeInTheDocument();
  });

  it('calls login API with entered credentials on submit', async () => {
    const user = userEvent.setup();
    vi.mocked(clientModule.studentsApi.login).mockResolvedValueOnce({
      id: 1, firstName: 'Alice', lastName: 'Smith', email: 'alice@maplewood.edu',
      gradeLevel: 11, gpa: 3.5, creditsEarned: 18, totalCreditsToGraduate: 30, courseHistory: [],
    });

    renderWithProviders(<LoginPage />);

    await user.type(screen.getByLabelText(/email/i), 'alice@maplewood.edu');
    await user.type(screen.getByLabelText(/student id/i), '1');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(clientModule.studentsApi.login).toHaveBeenCalledWith('alice@maplewood.edu', 1);
    });
  });

  it('shows error message on failed login', async () => {
    const user = userEvent.setup();
    vi.mocked(clientModule.studentsApi.login).mockRejectedValueOnce(new Error('401'));

    renderWithProviders(<LoginPage />);

    await user.type(screen.getByLabelText(/email/i), 'wrong@maplewood.edu');
    await user.type(screen.getByLabelText(/student id/i), '99');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText(/invalid email or student id/i)).toBeInTheDocument();
    });
  });

  it('does not submit with empty fields', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />);

    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(clientModule.studentsApi.login).not.toHaveBeenCalled();
  });

  it('shows Signing in... when loading', async () => {
    const user = userEvent.setup();
    // Never resolves — simulates pending state
    vi.mocked(clientModule.studentsApi.login).mockImplementationOnce(() => new Promise(() => {}));

    renderWithProviders(<LoginPage />);

    await user.type(screen.getByLabelText(/email/i), 'alice@maplewood.edu');
    await user.type(screen.getByLabelText(/student id/i), '1');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /signing in/i })).toBeDisabled();
    });
  });
});
