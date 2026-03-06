import axios from 'axios';
import type { CourseWithSection, StudentProfile, ScheduleItem, EnrollmentResponse } from '../types';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const coursesApi = {
  getAll: (params?: { gradeLevel?: number; semesterOrder?: number }) =>
    api.get<CourseWithSection[]>('/courses', { params }).then(r => r.data),
};

export const studentsApi = {
  getProfile: (id: number) =>
    api.get<StudentProfile>(`/students/${id}`).then(r => r.data),
};

export const enrollmentsApi = {
  getSchedule: (studentId: number) =>
    api.get<ScheduleItem[]>(`/students/${studentId}/schedule`).then(r => r.data),

  enroll: (studentId: number, sectionId: number) =>
    api.post<EnrollmentResponse>('/enrollments', { studentId, sectionId }).then(r => r.data).catch(err => {
      if (err.response?.data) return err.response.data as EnrollmentResponse;
      throw err;
    }),

  drop: (enrollmentId: number, studentId: number) =>
    api.delete(`/enrollments/${enrollmentId}`, { params: { studentId } }).then(r => r.data),
};
