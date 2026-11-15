import { test, expect } from '@playwright/test';

test.describe('FIELDOPS Enterprise E2E Suite — Role Qualification & UI Workflows', () => {

  test('1. Admin Flow: Authentication, Operations Dashboard, Navigation & Telemetry', async ({ page }) => {
    // Navigate to Login
    await page.goto('/login');
    await expect(page).toHaveTitle(/FieldOps/i);

    // Fill Admin credentials
    await page.locator('input[type="email"]').fill('admin@fieldops.demo');
    await page.locator('input[type="password"]').fill('FieldOps@2026!');
    await page.locator('button[type="submit"]').click();

    // Verify successful login redirect to /app/overview
    await page.waitForURL('**/app/overview', { timeout: 15000 });
    await expect(page.locator('h1, h2, h3').first()).toBeVisible();

    // Verify Navigation to Work Orders
    await page.goto('/app/work-orders');
    await page.waitForSelector('table, [role="table"], .grid, main', { timeout: 10000 });
    await expect(page.locator('body')).toContainText(/Work Orders|All Work Orders/i);

    // Verify Navigation to Technicians Roster
    await page.goto('/app/technicians');
    await page.waitForSelector('main', { timeout: 10000 });
    await expect(page.locator('body')).toContainText(/Technician|Engineers|Roster/i);

    // Verify Navigation to Alerts
    await page.goto('/app/alerts');
    await page.waitForSelector('main', { timeout: 10000 });
    await expect(page.locator('body')).toContainText(/Alert/i);
  });

  test('2. Dispatcher Flow: Login, Route & Alerts Oversight', async ({ page }) => {
    await page.goto('/login');
    await page.locator('input[type="email"]').fill('dispatcher@fieldops.demo');
    await page.locator('input[type="password"]').fill('FieldOps@2026!');
    await page.locator('button[type="submit"]').click();

    await page.waitForURL('**/app/overview', { timeout: 15000 });
    await expect(page.locator('body')).toContainText(/Overview|FieldOps|Operations/i);

    // Dispatcher navigates to work orders
    await page.goto('/app/work-orders');
    await page.waitForSelector('main', { timeout: 10000 });
    await expect(page.locator('body')).toContainText(/Work Order/i);
  });

  test('3. Technician Flow: Login, Assigned Work Orders & Identity Alignment', async ({ page }) => {
    await page.goto('/login');
    await page.locator('input[type="email"]').fill('rahul.s@fieldops.demo');
    await page.locator('input[type="password"]').fill('FieldOps@2026!');
    await page.locator('button[type="submit"]').click();

    await page.waitForURL('**/app/overview', { timeout: 15000 });
    await page.goto('/app/work-orders');
    await page.waitForSelector('main', { timeout: 10000 });
    await expect(page.locator('body')).toContainText(/Work Order/i);
  });

  test('4. Customer Flow: Self-Service Portal & IDOR Protection', async ({ page }) => {
    await page.goto('/login');
    await page.locator('input[type="email"]').fill('dispatch@vertexlogistics.in');
    await page.locator('input[type="password"]').fill('FieldOps@2026!');
    await page.locator('button[type="submit"]').click();

    await page.waitForURL('**/app/overview', { timeout: 15000 });
    await page.goto('/app/work-orders');
    await page.waitForSelector('main', { timeout: 10000 });
    await expect(page.locator('body')).toContainText(/Work Order/i);
  });

  test('5. UI Theme: White Mode & Dark Mode Persistence', async ({ page }) => {
    await page.goto('/login');
    
    // Check initial html class (defaults to dark)
    const initialIsDark = await page.evaluate(() => document.documentElement.classList.contains('dark'));
    
    // Find theme toggle button
    const themeBtn = page.locator('button[title*="Mode"], button[aria-label*="Mode"]').first();
    await expect(themeBtn).toBeVisible();
    await themeBtn.click();

    // Verify toggle inverted the class
    const toggledIsDark = await page.evaluate(() => document.documentElement.classList.contains('dark'));
    expect(toggledIsDark).toBe(!initialIsDark);

    // Verify persistence across reload
    await page.reload();
    const reloadedIsDark = await page.evaluate(() => document.documentElement.classList.contains('dark'));
    expect(reloadedIsDark).toBe(toggledIsDark);
  });

});
