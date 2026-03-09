describe('Login Page', () => {
  beforeEach(() => {
    cy.visit('/');
  });

  it('displays the login form', () => {
    cy.get('[data-cy="login-form"]').should('be.visible');
    cy.get('[data-cy="email-input"]').should('be.visible');
    cy.get('[data-cy="student-id-input"]').should('be.visible');
    cy.get('[data-cy="login-button"]').should('be.visible').and('contain', 'Sign In');
  });

  it('shows error on invalid credentials', () => {
    cy.intercept('POST', '/api/students/login', {
      statusCode: 401,
      body: { message: 'Invalid credentials.' },
    }).as('loginFail');

    cy.get('[data-cy="email-input"]').type('wrong@maplewood.edu');
    cy.get('[data-cy="student-id-input"]').type('1');
    cy.get('[data-cy="login-button"]').click();

    cy.wait('@loginFail');
    cy.get('[data-cy="login-error"]').should('be.visible');
  });

  it('navigates to profile on successful login', () => {
    cy.intercept('POST', '/api/students/login', {
      statusCode: 200,
      body: {
        id: 1,
        firstName: 'Alice',
        lastName: 'Smith',
        email: 'alice@maplewood.edu',
        gradeLevel: 11,
        gpa: 3.5,
        creditsEarned: 18,
        totalCreditsToGraduate: 30,
        courseHistory: [],
      },
    }).as('loginSuccess');

    cy.intercept('GET', '/api/students/1/schedule', { body: [] }).as('schedule');

    cy.get('[data-cy="email-input"]').type('alice@maplewood.edu');
    cy.get('[data-cy="student-id-input"]').type('1');
    cy.get('[data-cy="login-button"]').click();

    cy.wait('@loginSuccess');
    cy.url().should('include', '/profile');
  });

  it('disables the button while loading', () => {
    cy.intercept('POST', '/api/students/login', (req) => {
      req.reply({ delay: 500, statusCode: 200, body: {} });
    }).as('loginSlow');

    cy.get('[data-cy="email-input"]').type('alice@maplewood.edu');
    cy.get('[data-cy="student-id-input"]').type('1');
    cy.get('[data-cy="login-button"]').click();
    cy.get('[data-cy="login-button"]').should('be.disabled');
  });
});
