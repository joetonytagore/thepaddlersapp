describe('Player Booking Flow', () => {
  it('should allow player to book a court', () => {
    cy.visit('/leagues');
    cy.get('button').contains('Book Court').click();
    cy.get('select').select('Court 2');
    cy.get('input[type="datetime-local"]').first().type('2025-12-01T12:00');
    cy.get('input[type="datetime-local"]').last().type('2025-12-01T13:00');
    cy.contains('Confirm Booking').click();
    cy.contains('Booking confirmed!').should('be.visible');
  });
});

