import { test, expect } from '@playwright/test';

test.describe('IO error handling', () => {
  const baseURL = 'http://localhost:8888/';

  test('shows IO error for unreachable domain', async ({ page }) => {
    await page.goto(baseURL);

    const urlInput = page.locator('input#doc[type="url"]');
    // Use a domain that definitely doesn't exist
    await urlInput.fill('https://this-domain-definitely-does-not-exist-12345.example/');

    await page.click('#submit');

    // Wait for results (may take a bit due to DNS timeout)
    await page.waitForSelector('#results', { timeout: 60000 });

    // Should show an IO error
    const resultsText = await page.locator('#results').textContent();
    expect(resultsText?.toLowerCase()).toMatch(/error|io|failed/i);
  });

  test('IO error message includes the URL', async ({ page }) => {
    await page.goto(baseURL);

    const testDomain = 'nonexistent-test-domain-xyz-12345.example';
    const urlInput = page.locator('input#doc[type="url"]');
    await urlInput.fill(`https://${testDomain}/`);

    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results', { timeout: 60000 });

    // The error message should include the domain/URL
    // This tests the fix for issue #1783
    const resultsText = await page.locator('#results').textContent();
    expect(resultsText).toContain(testDomain);
  });

  test('shows error for connection refused', async ({ page }) => {
    await page.goto(baseURL);

    const urlInput = page.locator('input#doc[type="url"]');
    // localhost on a port that's unlikely to be in use
    await urlInput.fill('http://localhost:54321/');

    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results', { timeout: 30000 });

    // Should show an error about connection
    const resultsText = await page.locator('#results').textContent();
    expect(resultsText?.toLowerCase()).toMatch(/error|refused|connect|failed/i);
  });

  test('shows error for invalid TLS certificate', async ({ page }) => {
    await page.goto(baseURL);

    const urlInput = page.locator('input#doc[type="url"]');
    // expired.badssl.com has an expired certificate
    await urlInput.fill('https://expired.badssl.com/');

    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results', { timeout: 30000 });

    // Should show some kind of error (certificate or IO error)
    const results = page.locator('#results');
    await expect(results).toBeVisible();
  });

  test('handles URL that returns non-HTML content type gracefully', async ({ page }) => {
    await page.goto(baseURL);

    const urlInput = page.locator('input#doc[type="url"]');
    // A URL that returns JSON
    await urlInput.fill('https://httpbin.org/json');

    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results', { timeout: 30000 });

    // Should either validate it or show appropriate message
    const results = page.locator('#results');
    await expect(results).toBeVisible();
  });

  test('handles URL that returns 404', async ({ page }) => {
    await page.goto(baseURL);

    const urlInput = page.locator('input#doc[type="url"]');
    await urlInput.fill('https://httpbin.org/status/404');

    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results', { timeout: 30000 });

    // Should show an error about 404
    const resultsText = await page.locator('#results').textContent();
    expect(resultsText?.toLowerCase()).toMatch(/error|404|not found/i);
  });

  test('handles URL that returns 500', async ({ page }) => {
    await page.goto(baseURL);

    const urlInput = page.locator('input#doc[type="url"]');
    await urlInput.fill('https://httpbin.org/status/500');

    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results', { timeout: 30000 });

    // Should show an error about 500
    const resultsText = await page.locator('#results').textContent();
    expect(resultsText?.toLowerCase()).toMatch(/error|500|internal/i);
  });
});
