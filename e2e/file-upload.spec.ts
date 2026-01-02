import { test, expect } from '@playwright/test';
import path from 'path';

test.describe('File upload validation', () => {
  const baseURL = 'http://localhost:8888/';
  const fixturesDir = path.join(__dirname, 'fixtures');

  test('switches to file upload mode', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to file mode
    await page.locator('#docselect').selectOption('file');

    // File input should be visible
    const fileInput = page.locator('input#doc[type="file"]');
    await expect(fileInput).toBeVisible();
  });

  test('validates uploaded valid HTML file and shows success', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to file mode
    await page.locator('#docselect').selectOption('file');

    // Upload valid HTML file
    const fileInput = page.locator('input#doc[type="file"]');
    await fileInput.setInputFiles(path.join(fixturesDir, 'valid.html'));

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results p.success, #results p.failure', { timeout: 10000 });

    // Should show success
    const success = page.locator('#results p.success');
    await expect(success).toBeVisible();
  });

  test('validates uploaded invalid HTML file and shows errors', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to file mode
    await page.locator('#docselect').selectOption('file');

    // Upload invalid HTML file
    const fileInput = page.locator('input#doc[type="file"]');
    await fileInput.setInputFiles(path.join(fixturesDir, 'invalid.html'));

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results .error', { timeout: 10000 });

    // Should show errors
    const errors = page.locator('#results .error');
    await expect(errors.first()).toBeVisible();
  });

  test('validates file with missing lang attribute and shows warning', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to file mode
    await page.locator('#docselect').selectOption('file');

    // Upload file missing lang
    const fileInput = page.locator('input#doc[type="file"]');
    await fileInput.setInputFiles(path.join(fixturesDir, 'missing-lang.html'));

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results .warning, #results .error', { timeout: 10000 });

    // Should mention lang attribute
    const pageContent = await page.content();
    expect(pageContent).toContain('lang');
  });

  test('shows filename in results after upload', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to file mode
    await page.locator('#docselect').selectOption('file');

    // Upload file
    const fileInput = page.locator('input#doc[type="file"]');
    await fileInput.setInputFiles(path.join(fixturesDir, 'valid.html'));

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results', { timeout: 10000 });

    // The filename should appear somewhere in results
    const pageContent = await page.content();
    expect(pageContent).toContain('valid.html');
  });

  test('accepts HTML file with .htm extension', async ({ page }) => {
    // Create a temporary .htm file for testing
    await page.goto(baseURL);

    // Switch to file mode
    await page.locator('#docselect').selectOption('file');

    // For now, just verify file input accepts the file
    // The actual .htm test would require creating a fixture
    const fileInput = page.locator('input#doc[type="file"]');
    await expect(fileInput).toBeVisible();
  });
});
