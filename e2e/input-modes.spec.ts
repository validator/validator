import { test, expect } from '@playwright/test';

test.describe('Input mode switching', () => {
  const baseURL = 'http://localhost:8888/';

  test('starts in address mode by default', async ({ page }) => {
    await page.goto(baseURL);

    // Should have URL input visible
    const urlInput = page.locator('input#doc[type="url"]');
    await expect(urlInput).toBeVisible();

    // Mode selector should show address
    const modeSelect = page.locator('#docselect');
    await expect(modeSelect).toHaveValue('');
  });

  test('switches to file upload mode', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to file upload
    const modeSelect = page.locator('#docselect');
    await modeSelect.selectOption('file');

    // Should show file input
    const fileInput = page.locator('input#doc[type="file"]');
    await expect(fileInput).toBeVisible();

    // URL hash should update
    await expect(page).toHaveURL(/#file/);
  });

  test('switches to text input mode', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to textarea
    const modeSelect = page.locator('#docselect');
    await modeSelect.selectOption('textarea');

    // Should show textarea
    const textarea = page.locator('textarea#doc');
    await expect(textarea).toBeVisible();

    // URL hash should update
    await expect(page).toHaveURL(/#textarea/);
  });

  test('remembers last input mode across page loads', async ({ page }) => {
    await page.goto(baseURL);

    // Clear localStorage first
    await page.evaluate(() => localStorage.clear());
    await page.reload();

    // Switch to textarea mode
    const modeSelect = page.locator('#docselect');
    await modeSelect.selectOption('textarea');

    // Navigate away and back (simulated by going to base URL without hash)
    await page.goto(baseURL);

    // Should remember textarea mode (check localStorage)
    const lastMode = await page.evaluate(() => localStorage.getItem('lastInputMode'));
    expect(lastMode).toBe('textarea');
  });

  test('hash #file opens file upload mode directly', async ({ page }) => {
    await page.goto(baseURL + '#file');

    // Should show file input
    const fileInput = page.locator('input#doc[type="file"]');
    await expect(fileInput).toBeVisible();

    // Mode selector should show file
    const modeSelect = page.locator('#docselect');
    await expect(modeSelect).toHaveValue('file');
  });

  test('hash #textarea opens text input mode directly', async ({ page }) => {
    await page.goto(baseURL + '#textarea');

    // Should show textarea
    const textarea = page.locator('textarea#doc');
    await expect(textarea).toBeVisible();

    // Mode selector should show textarea
    const modeSelect = page.locator('#docselect');
    await expect(modeSelect).toHaveValue('textarea');
  });

  test('textarea shows HTML boilerplate by default', async ({ page }) => {
    await page.goto(baseURL);
    await page.evaluate(() => localStorage.clear());
    await page.reload();

    // Switch to textarea
    await page.locator('#docselect').selectOption('textarea');

    // Should contain HTML boilerplate
    const textarea = page.locator('textarea#doc');
    const value = await textarea.inputValue();
    expect(value).toContain('<!DOCTYPE html>');
    expect(value).toContain('<html');
  });
});
