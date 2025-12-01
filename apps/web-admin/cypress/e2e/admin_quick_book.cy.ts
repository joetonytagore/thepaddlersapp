describe('Admin Quick Book', () => {
  it('should create a booking for a player', () => {
    cy.visit('/admin/quick-book');
    cy.get('select').select('Court 1');
    cy.get('input[type="datetime-local"]').first().type('2025-12-01T10:00');
    cy.get('input[type="datetime-local"]').last().type('2025-12-01T11:00');
    cy.get('input[placeholder*="Search player"]').type('Alice');
    cy.contains('Alice').click();
    cy.contains('Create Booking').click();
    cy.contains('Booking created!').should('be.visible');
  });
});

