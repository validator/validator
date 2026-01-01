import { test, expect } from '@playwright/test';

test.describe('Errors & warnings only checkbox', () => {
  const baseURL = 'http://localhost:8888/';

  // Helper to start with clean localStorage
  async function setupCleanState(page) {
    await page.goto(baseURL);
    await page.evaluate(() => localStorage.clear());
    await page.reload();
  }

  test('checkbox state persists across page reloads', async ({ page }) => {
    await setupCleanState(page);

    const checkbox = page.locator('#level');

    // Verify checkbox exists and is initially unchecked
    await expect(checkbox).toBeVisible();
    await expect(checkbox).not.toBeChecked();

    // Check the checkbox
    await checkbox.check();
    await expect(checkbox).toBeChecked();

    // Reload the page
    await page.reload();

    // Verify the checkbox is still checked after reload
    await expect(page.locator('#level')).toBeChecked();
  });

  test('unchecked state also persists', async ({ page }) => {
    await setupCleanState(page);

    const checkbox = page.locator('#level');

    // Check the checkbox, then uncheck it
    await checkbox.check();
    await checkbox.uncheck();
    await expect(checkbox).not.toBeChecked();

    // Reload and verify it stays unchecked
    await page.reload();
    await expect(page.locator('#level')).not.toBeChecked();
  });

  test('localStorage value is set correctly', async ({ page }) => {
    await setupCleanState(page);

    const checkbox = page.locator('#level');

    // Check the checkbox
    await checkbox.check();

    // Verify localStorage value
    const storageValue = await page.evaluate(() => localStorage.getItem('warningsOnly'));
    expect(storageValue).toBe('yes');

    // Uncheck the checkbox
    await checkbox.uncheck();

    // Verify localStorage value changed
    const storageValueAfter = await page.evaluate(() => localStorage.getItem('warningsOnly'));
    expect(storageValueAfter).toBe('no');
  });
});
