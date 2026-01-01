import { test, expect } from '@playwright/test';

test.describe('Message filtering', () => {
  const baseURL = 'http://localhost:8888/';

  // HTML that produces info messages (trailing slash on void element)
  const htmlWithInfo = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body><br/></body></html>';

  // HTML that produces errors
  const htmlWithError = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body><p><div>Nested wrong</div></p></body></html>';

  test('shows info messages when "errors & warnings only" is unchecked', async ({ page }) => {
    await page.goto(baseURL);

    // Clear any saved preference
    await page.evaluate(() => localStorage.removeItem('warningsOnly'));

    // Switch to textarea
    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithInfo);

    // Make sure "errors & warnings only" is NOT checked
    await page.locator('#level').uncheck();

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results p.success, #results p.failure');

    // Should show info messages
    const infoMessages = page.locator('#results .info');
    const count = await infoMessages.count();
    expect(count).toBeGreaterThan(0);
  });

  test('hides info messages when "errors & warnings only" is checked', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to textarea
    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithInfo);

    // Check "errors & warnings only"
    await page.locator('#level').check();

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results p.success, #results p.failure');

    // Should NOT show info messages (only .info without .warning)
    const infoOnlyMessages = page.locator('#results li.info:not(.warning)');
    const count = await infoOnlyMessages.count();
    expect(count).toBe(0);
  });

  test('still shows errors when "errors & warnings only" is checked', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to textarea
    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithError);

    // Check "errors & warnings only"
    await page.locator('#level').check();

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results .error');

    // Should still show errors
    const errorMessages = page.locator('#results .error');
    const count = await errorMessages.count();
    expect(count).toBeGreaterThan(0);
  });

  test('Message Filtering button shows filter controls', async ({ page }) => {
    await page.goto(baseURL);

    // Submit some HTML that produces messages
    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithError);
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results .error, #results .warning');

    // Find and click the Message Filtering button
    const filterButton = page.locator('#filters button');
    await expect(filterButton).toBeVisible();
    await filterButton.click();

    // Filter fieldsets should become visible
    const fieldsets = page.locator('#filters fieldset');
    await expect(fieldsets.first()).toBeVisible();
  });
});
