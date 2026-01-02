import { test, expect } from '@playwright/test';

test.describe('Specific error message tests', () => {
  const baseURL = 'http://localhost:8888/';

  test.describe('Transparent content model (issue #569)', () => {
    test('error for element in transparent parent includes note about content model', async ({ page }) => {
      await page.goto(baseURL);

      // HTML where h5 is inside <a> which is inside <cite>
      // <a> has transparent content model, so h5 is actually not allowed
      // because cite only allows phrasing content
      const htmlWithTransparentIssue = `<!DOCTYPE html>
<html lang="en">
<head><title>Test</title></head>
<body>
  <cite><a href="#"><h5>Heading in link in cite</h5></a></cite>
</body>
</html>`;

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(htmlWithTransparentIssue);
      await page.click('#submit');

      await page.waitForSelector('#results .error', { timeout: 10000 });

      // Error should mention transparent content model
      const errorText = await page.locator('#results .error').first().textContent();
      expect(errorText?.toLowerCase()).toContain('transparent');
    });

    test('error for block element in <ins> inside inline context mentions transparent', async ({ page }) => {
      await page.goto(baseURL);

      const htmlWithInsTransparent = `<!DOCTYPE html>
<html lang="en">
<head><title>Test</title></head>
<body>
  <span><ins><div>Block in ins in span</div></ins></span>
</body>
</html>`;

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(htmlWithInsTransparent);
      await page.click('#submit');

      await page.waitForSelector('#results .error', { timeout: 10000 });

      const errorText = await page.locator('#results .error').first().textContent();
      expect(errorText?.toLowerCase()).toContain('transparent');
    });

    test('error for block element in <del> inside inline context mentions transparent', async ({ page }) => {
      await page.goto(baseURL);

      const htmlWithDelTransparent = `<!DOCTYPE html>
<html lang="en">
<head><title>Test</title></head>
<body>
  <span><del><div>Block in del in span</div></del></span>
</body>
</html>`;

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(htmlWithDelTransparent);
      await page.click('#submit');

      await page.waitForSelector('#results .error', { timeout: 10000 });

      const errorText = await page.locator('#results .error').first().textContent();
      expect(errorText?.toLowerCase()).toContain('transparent');
    });
  });

  test.describe('Meta charset validation (issue #877)', () => {
    test('accepts valid meta charset declaration', async ({ page }) => {
      await page.goto(baseURL);

      const htmlWithValidCharset = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <title>Test</title>
</head>
<body><p>Content</p></body>
</html>`;

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(htmlWithValidCharset);
      await page.click('#submit');

      await page.waitForSelector('#results p.success, #results p.failure', { timeout: 10000 });

      // Should not have charset-related errors
      const errors = page.locator('#results .error');
      const errorCount = await errors.count();

      if (errorCount > 0) {
        const errorTexts = await errors.allTextContents();
        // None of the errors should be about charset
        for (const text of errorTexts) {
          expect(text.toLowerCase()).not.toContain('charset=');
        }
      }
    });

    test('accepts charset with space before charset= (issue #877 case)', async ({ page }) => {
      await page.goto(baseURL);

      // This was previously rejected incorrectly
      const htmlWithCharsetSpace = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset charset=utf-8">
  <title>Test</title>
</head>
<body><p>Content</p></body>
</html>`;

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(htmlWithCharsetSpace);
      await page.click('#submit');

      await page.waitForSelector('#results', { timeout: 10000 });

      // Should not show the old error about "did not contain charset="
      const pageContent = await page.content();
      expect(pageContent).not.toContain('did not contain charset=');
    });
  });

  test.describe('Common validation errors', () => {
    test('missing doctype produces error', async ({ page }) => {
      await page.goto(baseURL);

      const htmlWithoutDoctype = `<html lang="en">
<head><title>Test</title></head>
<body><p>No doctype</p></body>
</html>`;

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(htmlWithoutDoctype);
      await page.click('#submit');

      await page.waitForSelector('#results .error', { timeout: 10000 });

      const errorText = await page.locator('#results .error').first().textContent();
      expect(errorText?.toLowerCase()).toMatch(/doctype|document type/i);
    });

    test('stray end tag produces clear error', async ({ page }) => {
      await page.goto(baseURL);

      const htmlWithStrayEndTag = `<!DOCTYPE html>
<html lang="en">
<head><title>Test</title></head>
<body><p>Text</p></div></body>
</html>`;

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(htmlWithStrayEndTag);
      await page.click('#submit');

      await page.waitForSelector('#results .error', { timeout: 10000 });

      const errorText = await page.locator('#results .error').first().textContent();
      expect(errorText?.toLowerCase()).toContain('stray');
    });

    test('duplicate id attribute produces error', async ({ page }) => {
      await page.goto(baseURL);

      const htmlWithDuplicateId = `<!DOCTYPE html>
<html lang="en">
<head><title>Test</title></head>
<body>
  <div id="foo">First</div>
  <div id="foo">Duplicate</div>
</body>
</html>`;

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(htmlWithDuplicateId);
      await page.click('#submit');

      await page.waitForSelector('#results .error', { timeout: 10000 });

      const errorText = await page.locator('#results .error').first().textContent();
      expect(errorText?.toLowerCase()).toContain('duplicate');
    });

    test('obsolete element produces error', async ({ page }) => {
      await page.goto(baseURL);

      const htmlWithObsoleteElement = `<!DOCTYPE html>
<html lang="en">
<head><title>Test</title></head>
<body>
  <center>Centered text</center>
</body>
</html>`;

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(htmlWithObsoleteElement);
      await page.click('#submit');

      await page.waitForSelector('#results .error', { timeout: 10000 });

      const errorText = await page.locator('#results .error').first().textContent();
      expect(errorText?.toLowerCase()).toMatch(/obsolete|center/i);
    });

    test('img without alt produces warning', async ({ page }) => {
      await page.goto(baseURL);

      const htmlWithoutAlt = `<!DOCTYPE html>
<html lang="en">
<head><title>Test</title></head>
<body>
  <img src="test.jpg">
</body>
</html>`;

      await page.locator('#docselect').selectOption('textarea');
      await page.locator('textarea#doc').fill(htmlWithoutAlt);
      await page.click('#submit');

      await page.waitForSelector('#results .error, #results .warning', { timeout: 10000 });

      const pageContent = await page.content();
      expect(pageContent.toLowerCase()).toContain('alt');
    });
  });
});
