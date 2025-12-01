// Example Detox E2E test for booking flow
const { device, expect, element, by } = require('detox')

describe('Booking Flow', () => {
  beforeAll(async () => {
    await device.launchApp()
  })

  it('should login and book a court', async () => {
    await expect(element(by.id('login-email'))).toBeVisible()
    await element(by.id('login-email')).typeText('demo@paddlers.test')
    await element(by.id('login-password')).typeText('testpass')
    await element(by.id('login-submit')).tap()
    await expect(element(by.text('Courts'))).toBeVisible()
    await element(by.text('Book')).tap()
    await element(by.id('court-select')).tap()
    await element(by.id('book-submit')).tap()
    await expect(element(by.text('Booking confirmed.'))).toBeVisible()
  })
})

