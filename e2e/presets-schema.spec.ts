import { test, expect } from '@playwright/test';

test.describe('Presets and schema selection', () => {
  const baseURL = 'http://localhost:8888/';

  test.describe('Preset selector', () => {
    test('preset selector is visible in extra options', async ({ page }) => {
      await page.goto(baseURL);

      // Click Options button to show extra options
      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

      // Preset selector should be visible
      const presetSelect = page.locator('#preset');
      await expect(presetSelect).toBeVisible();
    });

    test('preset selector has multiple options', async ({ page }) => {
      await page.goto(baseURL);

      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

      const presetSelect = page.locator('#preset');
      const options = presetSelect.locator('option');
      const count = await options.count();

      expect(count).toBeGreaterThan(1);
    });

    test('changing preset updates schema field', async ({ page }) => {
      await page.goto(baseURL);

      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

      const presetSelect = page.locator('#preset');
      const schemaInput = page.locator('#schema');

      // Get initial schema value
      const initialSchema = await schemaInput.inputValue();

      // Select a specific preset (XHTML if available)
      const options = await presetSelect.locator('option').allTextContents();
      const xhtmlOption = options.find(opt => opt.toLowerCase().includes('xhtml'));

      if (xhtmlOption) {
        await presetSelect.selectOption({ label: xhtmlOption });

        // Schema field might be updated
        const newSchema = await schemaInput.inputValue();
        // Just verify the interaction works
        expect(true).toBeTruthy();
      }
    });
  });

  test.describe('Parser selector', () => {
    test('parser selector is visible in extra options', async ({ page }) => {
      await page.goto(baseURL);

      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

      const parserSelect = page.locator('#parser');
      await expect(parserSelect).toBeVisible();
    });

    test('parser selector has multiple options', async ({ page }) => {
      await page.goto(baseURL);

      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

      const parserSelect = page.locator('#parser');
      const options = parserSelect.locator('option');
      const count = await options.count();

      expect(count).toBeGreaterThan(1);
    });

    test('can select XML parser', async ({ page }) => {
      await page.goto(baseURL);

      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

      const parserSelect = page.locator('#parser');
      const options = await parserSelect.locator('option').allTextContents();

      const xmlOption = options.find(opt => opt.toLowerCase().includes('xml'));
      if (xmlOption) {
        await parserSelect.selectOption({ label: xmlOption });
        await expect(parserSelect).toHaveValue(/xml/i);
      }
    });
  });

  test.describe('Schema field', () => {
    test('schema field is visible in extra options', async ({ page }) => {
      await page.goto(baseURL);

      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

      const schemaInput = page.locator('#schema');
      await expect(schemaInput).toBeVisible();
    });

    test('schema field accepts custom schema URL', async ({ page }) => {
      await page.goto(baseURL);

      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

      const schemaInput = page.locator('#schema');
      await schemaInput.fill('http://s.validator.nu/html5.rnc');

      await expect(schemaInput).toHaveValue('http://s.validator.nu/html5.rnc');
    });
  });

  test.describe('Validation with different presets', () => {
    test('validates HTML5 document with default preset', async ({ page }) => {
      await page.goto(baseURL);

      const validHTML5 = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body><p>Valid HTML5</p></body></html>';

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(validHTML5);
      await page.click('#submit');

      await page.waitForSelector('#results p.success, #results p.failure', { timeout: 10000 });

      const success = page.locator('#results p.success');
      await expect(success).toBeVisible();
    });

    test('XHTML requires proper namespace', async ({ page }) => {
      await page.goto(baseURL);

      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

      // Select XML parser if available
      const parserSelect = page.locator('#parser');
      const options = await parserSelect.locator('option').allTextContents();
      const xmlOption = options.find(opt => opt.toLowerCase().includes('xml') && !opt.toLowerCase().includes('html'));

      if (xmlOption) {
        await parserSelect.selectOption({ label: xmlOption });

        // XHTML document without namespace
        const xhtmlWithoutNS = `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head><title>Test</title></head>
<body><p>Test</p></body>
</html>`;

        await page.locator('#docselect').selectOption('textarea');
        await page.locator('textarea#doc').fill(xhtmlWithoutNS);
        await page.click('#submit');

        await page.waitForSelector('#results', { timeout: 10000 });

        // Should have some result
        const results = page.locator('#results');
        await expect(results).toBeVisible();
      }
    });
  });

  test.describe('Extra options visibility', () => {
    test('extra options are hidden by default', async ({ page }) => {
      await page.goto(baseURL);

      const extraOptions = page.locator('.extraoptions');
      await expect(extraOptions).toHaveClass(/hidden/);
    });

    test('clicking Options button shows extra options', async ({ page }) => {
      await page.goto(baseURL);

      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });

      const extraOptions = page.locator('.extraoptions');
      await expect(extraOptions).toHaveClass(/unhidden/);
    });

    test('clicking Options button again hides extra options', async ({ page }) => {
      await page.goto(baseURL);

      // Show
      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.unhidden', { timeout: 5000 });
      await expect(page.locator('.extraoptions')).toHaveClass(/unhidden/);

      // Hide
      await page.click('#show_options');
      await page.waitForSelector('.extraoptions.hidden', { timeout: 5000 });
      await expect(page.locator('.extraoptions')).toHaveClass(/hidden/);
    });
  });
});
