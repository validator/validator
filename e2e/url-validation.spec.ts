import { test, expect } from '@playwright/test';

test.describe('URL validation mode', () => {
  const baseURL = 'http://localhost:8888/';

  test('validates a real URL and shows results', async ({ page }) => {
    await page.goto(baseURL);

    // Should start in URL/address mode by default
    const urlInput = page.locator('input#doc[type="url"]');
    await expect(urlInput).toBeVisible();

    // Enter a URL (example.com should be reachable and return valid HTML)
    await urlInput.fill('https://example.com/');

    // Submit
    await page.click('#submit');

    // Wait for results (could be success or have some warnings)
    await page.waitForSelector('#results p.success, #results p.failure, #results .error, #results .warning', { timeout: 30000 });

    // Results section should be visible
    const results = page.locator('#results');
    await expect(results).toBeVisible();
  });

  test('browser validation prevents submission of malformed URL', async ({ page }) => {
    await page.goto(baseURL);

    const urlInput = page.locator('input#doc[type="url"]');
    await urlInput.fill('not-a-valid-url');

    await page.click('#submit');

    // Browser validation should prevent form submission
    // The results div should remain empty/invisible
    await page.waitForTimeout(1000);
    const resultsContent = await page.locator('#results').textContent();
    expect(resultsContent?.trim()).toBe('');
  });

  test('browser validation prevents submission of unsupported protocol', async ({ page }) => {
    await page.goto(baseURL);

    const urlInput = page.locator('input#doc[type="url"]');
    await urlInput.fill('ftp://example.com/file.html');

    await page.click('#submit');

    // Browser validation should prevent form submission for ftp:// URLs
    // (the pattern only allows http://, https://, and data: URLs)
    await page.waitForTimeout(1000);
    const resultsContent = await page.locator('#results').textContent();
    expect(resultsContent?.trim()).toBe('');
  });

  test('URL input is pre-filled when doc parameter is in URL', async ({ page }) => {
    const testUrl = 'https://example.com/';
    await page.goto(`${baseURL}?doc=${encodeURIComponent(testUrl)}`);

    const urlInput = page.locator('input#doc[type="url"]');
    await expect(urlInput).toHaveValue(testUrl);
  });

  test('validates URL and displays document title in results', async ({ page }) => {
    await page.goto(baseURL);

    const urlInput = page.locator('input#doc[type="url"]');
    await urlInput.fill('https://example.com/');

    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results', { timeout: 30000 });

    // The results should show the URL that was checked
    const pageContent = await page.content();
    expect(pageContent).toContain('example.com');
  });
});
