import { test, expect } from '@playwright/test';

test.describe('Error and warning display', () => {
  const baseURL = 'http://localhost:8888/';

  test('error messages have correct structure', async ({ page }) => {
    await page.goto(baseURL);

    // HTML with a clear error (stray end tag)
    const htmlWithError = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body></div></body></html>';

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithError);
    await page.click('#submit');

    await page.waitForSelector('#results .error');

    // Error should exist
    const error = page.locator('#results .error').first();
    await expect(error).toBeVisible();

    // Error should contain a message with a span
    const messageSpan = error.locator('p span').first();
    await expect(messageSpan).toBeVisible();
  });

  test('warning messages have correct structure', async ({ page }) => {
    await page.goto(baseURL);

    // HTML that produces a warning (section without heading)
    const htmlWithWarning = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body><section></section></body></html>';

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithWarning);
    await page.click('#submit');

    await page.waitForSelector('#results .warning, #results p.success');

    // Should have a warning about section lacking heading
    const warning = page.locator('#results .warning').first();
    await expect(warning).toBeVisible();
  });

  test('messages show line and column numbers', async ({ page }) => {
    await page.goto(baseURL);

    // HTML with an error on a specific line
    const htmlWithError = `<!DOCTYPE html>
<html lang="en">
<head><title>Test</title></head>
<body>
</div>
</body>
</html>`;

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithError);
    await page.click('#submit');

    await page.waitForSelector('#results .error');

    // Error message should contain location info (line number)
    const error = page.locator('#results .error').first();
    const errorText = await error.textContent();
    // Should mention a line number
    expect(errorText).toMatch(/line \d+/i);
  });

  test('success message shown for valid document', async ({ page }) => {
    await page.goto(baseURL);

    const validHTML = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body><p>Valid content</p></body></html>';

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(validHTML);
    await page.click('#submit');

    await page.waitForSelector('#results p.success, #results p.failure');

    // Should show success
    const success = page.locator('#results p.success');
    await expect(success).toBeVisible();
    await expect(success).toContainText('No errors or warnings');
  });

  test('failure message shown for invalid document', async ({ page }) => {
    await page.goto(baseURL);

    const invalidHTML = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body></div></body></html>';

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(invalidHTML);
    await page.click('#submit');

    await page.waitForSelector('#results p.success, #results p.failure');

    // Should show failure
    const failure = page.locator('#results p.failure');
    await expect(failure).toBeVisible();
  });

  test('extract shows code snippet from source', async ({ page }) => {
    await page.goto(baseURL);

    const htmlWithError = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body><p><button>Click</button></a></p></body></html>';

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithError);
    await page.click('#submit');

    await page.waitForSelector('#results .error');

    // Error should have an extract showing the problematic code
    const extract = page.locator('#results .error .extract, #results .error code').first();
    await expect(extract).toBeVisible();
  });
});
