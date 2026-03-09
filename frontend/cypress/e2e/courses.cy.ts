const mockProfile = {
  id: 1,
  firstName: 'Alice',
  lastName: 'Smith',
  email: 'alice@maplewood.edu',
  gradeLevel: 11,
  gpa: 3.75,
  creditsEarned: 21,
  totalCreditsToGraduate: 30,
  courseHistory: [],
};

const mockCourses = [
  {
    id: 100, code: 'MATH101', name: 'Algebra', description: 'Basic algebra', credits: 3,
    hoursPerWeek: 5, courseType: 'core', gradeLevelMin: 9, gradeLevelMax: 12,
    semesterOrder: 1, prerequisiteId: null, prerequisiteName: null,
    sectionId: 200, days: 'MWF', startTime: '08:00', endTime: '09:00',
  },
  {
    id: 101, code: 'ENG101', name: 'English I', description: 'English basics', credits: 3,
    hoursPerWeek: 5, courseType: 'core', gradeLevelMin: 9, gradeLevelMax: 10,
    semesterOrder: 1, prerequisiteId: null, prerequisiteName: null,
    sectionId: 201, days: 'TTh', startTime: '10:00', endTime: '11:30',
  },
];

const mockSchedule = [
  { enrollmentId: 1, sectionId: 200, courseId: 100, courseCode: 'MATH101', courseName: 'Algebra', credits: 3, days: 'MWF', startTime: '08:00', endTime: '09:00' },
];

describe('Courses Page', () => {
  beforeEach(() => {
    cy.intercept('POST', '/api/students/login', { statusCode: 200, body: mockProfile }).as('login');
    cy.intercept('GET', '/api/students/1/schedule', { body: mockSchedule }).as('schedule');
    cy.intercept('GET', '/api/courses', { body: mockCourses }).as('courses');

    // Navigate through login → profile → courses
    cy.visit('/');
    cy.get('[data-cy="email-input"]').type('alice@maplewood.edu');
    cy.get('[data-cy="student-id-input"]').type('1');
    cy.get('[data-cy="login-button"]').click();
    cy.wait('@login');
    cy.get('[data-cy="update-schedule-button"]').click();
    cy.url().should('include', '/courses');
    cy.wait('@courses');
  });

  it('displays the course catalog', () => {
    cy.get('.course-card').should('have.length.gte', 1);
  });

  it('shows course code and name', () => {
    cy.contains('.course-card', 'Algebra').should('be.visible');
    cy.contains('.course-code', 'MATH101').should('be.visible');
  });

  it('search filter narrows results', () => {
    cy.get('.filter-input').type('Algebra');
    cy.get('.course-card').should('have.length', 1);
    cy.get('.course-card').should('contain', 'Algebra');
  });

  it('shows Enrolled for already-enrolled courses', () => {
    cy.contains('.course-card', 'Algebra').find('.btn-enrolled').should('exist');
  });

  it('enrolls in a course successfully', () => {
    cy.intercept('POST', '/api/enrollments', {
      statusCode: 200,
      body: {
        success: true,
        message: 'Successfully enrolled in "English I".',
        enrollment: {
          enrollmentId: 5, sectionId: 201, courseId: 101, courseCode: 'ENG101',
          courseName: 'English I', credits: 3, days: 'TTh', startTime: '10:00', endTime: '11:30',
        },
      },
    }).as('enroll');

    cy.contains('.course-card', 'English I').find('.btn-enroll').click();
    cy.wait('@enroll');
    cy.get('.toast-success').should('contain', 'English I');
  });

  it('shows error toast on enrollment failure', () => {
    cy.intercept('POST', '/api/enrollments', {
      statusCode: 400,
      body: {
        success: false,
        errorType: 'max_courses',
        message: 'You have reached the maximum of 5 courses per semester.',
      },
    }).as('enrollFail');

    cy.contains('.course-card', 'English I').find('.btn-enroll').click();
    cy.wait('@enrollFail');
    cy.get('.toast-error').should('contain', '5 courses');
  });

  it('navigates back to profile', () => {
    cy.get('[data-cy="back-to-profile-button"]').click();
    cy.url().should('include', '/profile');
  });

  it('logs out from courses page', () => {
    cy.get('[data-cy="logout-button"]').click();
    cy.url().should('eq', Cypress.config('baseUrl') + '/');
    cy.get('[data-cy="login-form"]').should('be.visible');
  });
});
