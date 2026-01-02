import { test, expect } from '@playwright/test';

test.describe('Display options', () => {
  const baseURL = 'http://localhost:8888/';
  const validHTML = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body><h1>Heading</h1><p>Paragraph</p></body></html>';

  test('show source checkbox displays source code', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to textarea and enter HTML
    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(validHTML);

    // Check "show source"
    await page.locator('#showsource').check();

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results p.success, #results p.failure');

    // Should show source section
    const sourceHeading = page.locator('#source');
    await expect(sourceHeading).toBeVisible();

    // Source list should be visible
    const sourceList = page.locator('ol.source');
    await expect(sourceList).toBeVisible();
  });

  test('show outline checkbox displays document outline', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to textarea and enter HTML with headings
    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(validHTML);

    // Check "show outline"
    await page.locator('#showoutline').check();

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results p.success, #results p.failure');

    // Should show outline section (id is 'headingoutline')
    const outlineHeading = page.locator('#headingoutline');
    await expect(outlineHeading).toBeVisible();
  });

  test('show image report checkbox displays image report', async ({ page }) => {
    await page.goto(baseURL);

    // HTML with an image
    const htmlWithImage = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body><img src="test.jpg" alt="Test image"></body></html>';

    // Switch to textarea and enter HTML
    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithImage);

    // Check "show image report"
    await page.locator('#showimagereport').check();

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results p.success, #results p.failure');

    // Should show image report section
    const imageReportHeading = page.locator('#imagereport');
    await expect(imageReportHeading).toBeVisible();
  });

  test('Options button reveals extra options', async ({ page }) => {
    await page.goto(baseURL);

    // Extra options should be hidden initially
    const extraOptions = page.locator('.extraoptions');
    await expect(extraOptions).toHaveClass(/hidden/);

    // Click Options button
    await page.click('#show_options');
    await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

    // Extra options should now be visible
    await expect(extraOptions).toHaveClass(/unhidden/);
  });

  test('source is hidden when checkbox not checked', async ({ page }) => {
    await page.goto(baseURL);

    // Switch to textarea and enter HTML
    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(validHTML);

    // Make sure "show source" is NOT checked
    await page.locator('#showsource').uncheck();

    // Submit
    await page.click('#submit');

    // Wait for results
    await page.waitForSelector('#results p.success, #results p.failure');

    // Source section should not be visible
    const sourceHeading = page.locator('#source');
    await expect(sourceHeading).not.toBeVisible();
  });
});
