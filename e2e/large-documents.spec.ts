import { test, expect } from '@playwright/test';

test.describe('Large document handling', () => {
  const baseURL = 'http://localhost:8888/';

  // Helper to generate a large HTML document
  function generateLargeHTML(paragraphCount: number): string {
    const paragraphs = Array(paragraphCount)
      .fill(null)
      .map((_, i) => `  <p>This is paragraph number ${i + 1}. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>`)
      .join('\n');

    return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Large Document Test</title>
</head>
<body>
  <h1>Large Document</h1>
${paragraphs}
</body>
</html>`;
  }

  // Helper to generate HTML with many errors
  function generateHTMLWithManyErrors(errorCount: number): string {
    const errors = Array(errorCount)
      .fill(null)
      .map((_, i) => `  <p>Text ${i}</p></div>`) // stray end tag
      .join('\n');

    return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Many Errors Test</title>
</head>
<body>
${errors}
</body>
</html>`;
  }

  test('handles document with 100 paragraphs', async ({ page }) => {
    await page.goto(baseURL);

    const largeHTML = generateLargeHTML(100);

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(largeHTML);
    await page.click('#submit');

    // Should complete within reasonable time
    await page.waitForSelector('#results p.success, #results p.failure', { timeout: 30000 });

    // Should have no errors (the document is valid HTML5)
    const errors = page.locator('#results .error');
    const errorCount = await errors.count();
    expect(errorCount).toBe(0);
  });

  test('handles document with 500 paragraphs', async ({ page }) => {
    await page.goto(baseURL);

    const largeHTML = generateLargeHTML(500);

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(largeHTML);
    await page.click('#submit');

    // Should complete within reasonable time
    await page.waitForSelector('#results p.success, #results p.failure', { timeout: 60000 });

    // Should have no errors (the document is valid HTML5)
    const errors = page.locator('#results .error');
    const errorCount = await errors.count();
    expect(errorCount).toBe(0);
  });

  test('handles document with many errors (50 stray end tags)', async ({ page }) => {
    await page.goto(baseURL);

    const htmlWithErrors = generateHTMLWithManyErrors(50);

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithErrors);
    await page.click('#submit');

    // Should complete and show errors
    await page.waitForSelector('#results .error', { timeout: 30000 });

    const errors = page.locator('#results .error');
    const count = await errors.count();
    expect(count).toBeGreaterThan(0);
  });

  test('message filtering works with many messages', async ({ page }) => {
    await page.goto(baseURL);

    const htmlWithErrors = generateHTMLWithManyErrors(20);

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithErrors);
    await page.click('#submit');

    await page.waitForSelector('#results .error', { timeout: 30000 });

    // Expand message filtering
    await page.locator('#filters button').click();
    await page.waitForSelector('#filters fieldset');

    // Filter fieldset should show count
    const legend = page.locator('#filters fieldset legend').first();
    const legendText = await legend.textContent();
    expect(legendText).toMatch(/\(\d+\)/);
  });

  test('show source works with large document', async ({ page }) => {
    await page.goto(baseURL);

    const largeHTML = generateLargeHTML(100);

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(largeHTML);

    // Enable show source
    await page.locator('#showsource').check();

    await page.click('#submit');

    await page.waitForSelector('#results', { timeout: 30000 });

    // Source section should be visible
    const sourceList = page.locator('ol.source');
    await expect(sourceList).toBeVisible();

    // Should have many lines
    const lines = sourceList.locator('li');
    const lineCount = await lines.count();
    expect(lineCount).toBeGreaterThan(100);
  });

  test('deeply nested document is handled', async ({ page }) => {
    await page.goto(baseURL);

    // Create deeply nested divs
    let deepHTML = '<!DOCTYPE html><html lang="en"><head><meta charset="utf-8"><title>Deep</title></head><body>';
    for (let i = 0; i < 50; i++) {
      deepHTML += '<div>';
    }
    deepHTML += '<p>Deep content</p>';
    for (let i = 0; i < 50; i++) {
      deepHTML += '</div>';
    }
    deepHTML += '</body></html>';

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(deepHTML);
    await page.click('#submit');

    await page.waitForSelector('#results p.success, #results p.failure', { timeout: 30000 });

    // Should have no errors (deeply nested but valid)
    const errors = page.locator('#results .error');
    const errorCount = await errors.count();
    expect(errorCount).toBe(0);
  });

  test('document with many attributes is handled', async ({ page }) => {
    await page.goto(baseURL);

    // Create element with many data attributes
    const dataAttrs = Array(50)
      .fill(null)
      .map((_, i) => `data-attr-${i}="value${i}"`)
      .join(' ');

    const htmlWithManyAttrs = `<!DOCTYPE html>
<html lang="en">
<head><meta charset="utf-8"><title>Many Attrs</title></head>
<body>
  <div ${dataAttrs}>Content with many attributes</div>
</body>
</html>`;

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(htmlWithManyAttrs);
    await page.click('#submit');

    await page.waitForSelector('#results p.success, #results p.failure', { timeout: 30000 });

    // Should have no errors
    const errors = page.locator('#results .error');
    const errorCount = await errors.count();
    expect(errorCount).toBe(0);
  });

  test('long single line document is handled', async ({ page }) => {
    await page.goto(baseURL);

    // Create a very long single line
    const longText = 'x'.repeat(10000);
    const longLineHTML = `<!DOCTYPE html><html lang="en"><head><meta charset="utf-8"><title>Long Line</title></head><body><p>${longText}</p></body></html>`;

    await page.locator('#docselect').selectOption('textarea');
    await page.locator('textarea#doc').fill(longLineHTML);
    await page.click('#submit');

    await page.waitForSelector('#results p.success, #results p.failure', { timeout: 30000 });

    // Should have no errors
    const errors = page.locator('#results .error');
    const errorCount = await errors.count();
    expect(errorCount).toBe(0);
  });
});
