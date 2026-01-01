import { test, expect } from '@playwright/test';

test.describe('Basic validation flow', () => {
  const baseURL = 'http://localhost:8888/';

  test('validates HTML via textarea and shows errors', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to text input mode
    const modeSelect = page.locator('#docselect');
    await modeSelect.selectOption('textarea');

    // Enter invalid HTML (missing doctype, missing lang)
    const textarea = page.locator('textarea#doc');
    await textarea.fill('<html><head><title>Test</title></head><body><p>Hello</p></body></html>');

    // Submit the form
    await page.click('#submit');

    // Wait for results (errors or warnings to appear)
    await page.waitForSelector('#results .error, #results .warning');

    // Should have errors or warnings (missing doctype, missing lang attribute)
    const messages = page.locator('#results .error, #results .warning');
    await expect(messages.first()).toBeVisible();
  });

  test('validates valid HTML and shows success', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to text input mode
    const modeSelect = page.locator('#docselect');
    await modeSelect.selectOption('textarea');

    // Enter valid HTML
    const textarea = page.locator('textarea#doc');
    await textarea.fill('<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body><p>Hello</p></body></html>');

    // Submit the form
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results p.success, #results p.failure');

    // Should show success (no errors)
    const success = page.locator('#results p.success');
    await expect(success).toBeVisible();
  });

  test('shows specific error for missing lang attribute', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to text input mode
    await page.locator('#docselect').selectOption('textarea');

    // HTML without lang attribute
    const textarea = page.locator('textarea#doc');
    await textarea.fill('<!DOCTYPE html><html><head><title>Test</title></head><body><p>Hello</p></body></html>');

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results .error, #results .warning');

    // Should warn about missing lang
    const pageContent = await page.content();
    expect(pageContent).toContain('lang');
  });
});
