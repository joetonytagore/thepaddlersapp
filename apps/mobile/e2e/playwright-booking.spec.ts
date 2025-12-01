// Example Playwright E2E test for booking via webview
import { test, expect } from '@playwright/test'

test('Webview booking flow', async ({ page }) => {
  await page.goto('http://localhost:19006') // Expo web
  await page.fill('input[placeholder="email"]', 'demo@paddlers.test')
  await page.fill('input[placeholder="password"]', 'testpass')
  await page.click('text=Login')
  await expect(page.locator('text=Courts')).toBeVisible()
  await page.click('text=Book')
  await page.click('text=Submit')
  await expect(page.locator('text=Booking confirmed.')).toBeVisible()
})

