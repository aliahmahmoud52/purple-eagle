import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithProviders } from './testUtils';
import ProfilePage from '../pages/ProfilePage';
import type { StudentProfile, ScheduleItem } from '../types';
import * as clientModule from '../api/client';

vi.mock('../api/client', () => ({
  studentsApi: { login: vi.fn(), getProfile: vi.fn() },
  coursesApi: { getAll: vi.fn() },
  enrollmentsApi: {
    getSchedule: vi.fn().mockResolvedValue([]),
    enroll: vi.fn(),
    drop: vi.fn(),
  },
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

const mockProfile: StudentProfile = {
  id: 1,
  firstName: 'Alice',
  lastName: 'Smith',
  email: 'alice@maplewood.edu',
  gradeLevel: 11,
  gpa: 3.75,
  creditsEarned: 21,
  totalCreditsToGraduate: 30,
  courseHistory: [
    { courseId: 10, courseCode: 'MATH101', courseName: 'Algebra', credits: 3, semesterId: 1, status: 'passed' },
    { courseId: 11, courseCode: 'ENG101', courseName: 'English I', credits: 3, semesterId: 1, status: 'failed' },
  ],
};

const mockSchedule: ScheduleItem[] = [
  { enrollmentId: 1, sectionId: 200, courseId: 20, courseCode: 'SCI201', courseName: 'Chemistry', credits: 3, days: 'MWF', startTime: '08:00', endTime: '09:00' },
];

describe('ProfilePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(clientModule.enrollmentsApi.getSchedule).mockResolvedValue(mockSchedule);
  });

  function renderProfile() {
    return renderWithProviders(<ProfilePage />, {
      initialState: {
        student: { profile: mockProfile, loading: false, error: null },
        courses: { list: [], filters: { gradeLevel: null, semesterOrder: null, courseType: null, searchText: '' }, loading: false, error: null },
        enrollment: { schedule: mockSchedule, loading: false, error: null, enrolling: false, enrollError: null, enrollSuccess: null, dropping: {} },
      },
    });
  }

  it('displays student name', () => {
    renderProfile();
    expect(screen.getByText('Alice Smith')).toBeInTheDocument();
  });

  it('displays GPA', () => {
    renderProfile();
    expect(screen.getByText('3.75')).toBeInTheDocument();
  });

  it('displays credits earned', () => {
    renderProfile();
    expect(screen.getByText('21')).toBeInTheDocument();
  });

  it('displays course history items', () => {
    renderProfile();
    expect(screen.getByText('Algebra')).toBeInTheDocument();
    expect(screen.getByText('English I')).toBeInTheDocument();
  });

  it('shows passed and failed badges', () => {
    renderProfile();
    expect(screen.getByText('passed')).toBeInTheDocument();
    expect(screen.getByText('failed')).toBeInTheDocument();
  });

  it('displays current schedule', () => {
    renderProfile();
    expect(screen.getByText('Chemistry')).toBeInTheDocument();
  });

  it('shows graduation progress', () => {
    renderProfile();
    expect(screen.getByText('21/30 credits')).toBeInTheDocument();
  });

  it('shows remaining credits when not graduated', () => {
    renderProfile();
    expect(screen.getByText('9 credits remaining')).toBeInTheDocument();
  });

  it('navigates to /courses when Update Schedule is clicked', async () => {
    const user = userEvent.setup();
    renderProfile();
    await user.click(screen.getByRole('button', { name: /update schedule/i }));
    expect(mockNavigate).toHaveBeenCalledWith('/courses');
  });

  it('logs out and navigates to / when Sign Out is clicked', async () => {
    const user = userEvent.setup();
    renderProfile();
    await user.click(screen.getByRole('button', { name: /sign out/i }));
    expect(mockNavigate).toHaveBeenCalledWith('/');
  });

  it('redirects to / when no profile is present', () => {
    renderWithProviders(<ProfilePage />, {
      initialState: {
        student: { profile: null, loading: false, error: null },
        courses: { list: [], filters: { gradeLevel: null, semesterOrder: null, courseType: null, searchText: '' }, loading: false, error: null },
        enrollment: { schedule: [], loading: false, error: null, enrolling: false, enrollError: null, enrollSuccess: null, dropping: {} },
      },
    });
    expect(mockNavigate).toHaveBeenCalledWith('/');
  });
});
