export interface CourseWithSection {
  id: number;
  code: string;
  name: string;
  description: string;
  credits: number;
  hoursPerWeek: number;
  courseType: 'core' | 'elective';
  gradeLevelMin: number;
  gradeLevelMax: number;
  semesterOrder: number;
  prerequisiteId: number | null;
  prerequisiteName: string | null;
  sectionId: number | null;
  days: string | null;
  startTime: string | null;
  endTime: string | null;
}

export interface CourseHistoryItem {
  courseId: number;
  courseCode: string;
  courseName: string;
  credits: number;
  semesterId: number;
  status: 'passed' | 'failed';
}

export interface StudentProfile {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  gradeLevel: number;
  gpa: number;
  creditsEarned: number;
  totalCreditsToGraduate: number;
  courseHistory: CourseHistoryItem[];
}

export interface ScheduleItem {
  enrollmentId: number;
  sectionId: number;
  courseId: number;
  courseCode: string;
  courseName: string;
  credits: number;
  days: string;
  startTime: string;
  endTime: string;
}

export interface EnrollmentResponse {
  success: boolean;
  message: string;
  errorType?: string;
  enrollment?: ScheduleItem;
}
