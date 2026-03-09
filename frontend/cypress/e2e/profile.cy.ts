const mockProfile = {
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

const mockSchedule = [
  { enrollmentId: 1, sectionId: 200, courseId: 20, courseCode: 'SCI201', courseName: 'Chemistry', credits: 3, days: 'MWF', startTime: '08:00', endTime: '09:00' },
];

describe('Profile Page', () => {
  beforeEach(() => {
    // Log in via the login page
    cy.intercept('POST', '/api/students/login', { statusCode: 200, body: mockProfile }).as('login');
    cy.intercept('GET', '/api/students/1/schedule', { body: mockSchedule }).as('schedule');

    cy.visit('/');
    cy.get('[data-cy="email-input"]').type('alice@maplewood.edu');
    cy.get('[data-cy="student-id-input"]').type('1');
    cy.get('[data-cy="login-button"]').click();
    cy.wait('@login');
    cy.url().should('include', '/profile');
  });

  it('displays student name and GPA', () => {
    cy.get('[data-cy="student-name"]').should('contain', 'Alice Smith');
    cy.get('[data-cy="gpa-card"]').should('contain', '3.75');
  });

  it('displays current schedule', () => {
    cy.wait('@schedule');
    cy.get('[data-cy="schedule-item"]').should('have.length', 1);
    cy.get('[data-cy="schedule-item"]').first().should('contain', 'Chemistry');
  });

  it('displays course history', () => {
    cy.get('[data-cy="history-item"]').should('have.length', 2);
    cy.get('[data-cy="history-item"]').first().should('contain', 'Algebra');
  });

  it('shows passed and failed badges', () => {
    cy.get('.badge-passed').should('exist');
    cy.get('.badge-failed').should('exist');
  });

  it('navigates to courses page when Update Schedule is clicked', () => {
    cy.intercept('GET', '/api/courses', { body: [] }).as('courses');
    cy.get('[data-cy="update-schedule-button"]').click();
    cy.url().should('include', '/courses');
  });

  it('logs out and redirects to login', () => {
    cy.get('[data-cy="logout-button"]').click();
    cy.url().should('eq', Cypress.config('baseUrl') + '/');
    cy.get('[data-cy="login-form"]').should('be.visible');
  });
});
