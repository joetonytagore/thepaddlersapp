import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import BookingRuleManager from './BookingRuleManager';

describe('BookingRuleManager', () => {
  it('renders without crashing', () => {
    render(<BookingRuleManager />);
    expect(screen.getByText(/Booking Rule Management/i)).toBeInTheDocument();
  });
  // Add more tests for form validation, API calls, etc.
});

