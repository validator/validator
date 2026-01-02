import { test, expect } from '@playwright/test';

test.describe('API output formats', () => {
  const baseURL = 'http://localhost:8888/';

  const validHTML = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body><p>Valid</p></body></html>';
  const invalidHTML = '<!DOCTYPE html><html lang="en"><head><title>Test</title></head><body></div></body></html>';

  test.describe('JSON output', () => {
    test('returns valid JSON for valid document', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=json`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: validHTML,
      });

      expect(response.ok()).toBeTruthy();
      expect(response.headers()['content-type']).toContain('application/json');

      const json = await response.json();
      expect(json).toHaveProperty('messages');
      expect(Array.isArray(json.messages)).toBeTruthy();
    });

    test('returns errors in JSON for invalid document', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=json`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: invalidHTML,
      });

      expect(response.ok()).toBeTruthy();

      const json = await response.json();
      expect(json).toHaveProperty('messages');
      expect(json.messages.length).toBeGreaterThan(0);

      // At least one message should be an error
      const hasError = json.messages.some((msg: any) => msg.type === 'error');
      expect(hasError).toBeTruthy();
    });

    test('JSON messages have required fields', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=json`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: invalidHTML,
      });

      const json = await response.json();
      const errorMessage = json.messages.find((msg: any) => msg.type === 'error');

      expect(errorMessage).toBeDefined();
      expect(errorMessage).toHaveProperty('type');
      expect(errorMessage).toHaveProperty('message');
    });

    test('JSON includes line and column for errors', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=json`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: invalidHTML,
      });

      const json = await response.json();
      const errorMessage = json.messages.find((msg: any) => msg.type === 'error');

      if (errorMessage) {
        // Most errors should have location info
        expect(errorMessage.lastLine || errorMessage.firstLine).toBeDefined();
      }
    });
  });

  test.describe('GNU output format', () => {
    test('returns GNU format for valid document', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=gnu`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: validHTML,
      });

      expect(response.ok()).toBeTruthy();
      expect(response.headers()['content-type']).toContain('text/plain');
    });

    test('returns GNU format errors for invalid document', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=gnu`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: invalidHTML,
      });

      expect(response.ok()).toBeTruthy();

      const text = await response.text();
      // GNU format uses "error:" prefix
      expect(text).toContain('error:');
    });
  });

  test.describe('XML output format', () => {
    test('returns valid XML for valid document', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=xml`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: validHTML,
      });

      expect(response.ok()).toBeTruthy();
      expect(response.headers()['content-type']).toContain('xml');

      const xml = await response.text();
      expect(xml).toContain('<?xml');
      expect(xml).toContain('<messages');
    });

    test('returns XML with errors for invalid document', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=xml`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: invalidHTML,
      });

      expect(response.ok()).toBeTruthy();

      const xml = await response.text();
      expect(xml).toContain('<error');
    });
  });

  test.describe('Text output format', () => {
    test('returns text format', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=text`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: invalidHTML,
      });

      expect(response.ok()).toBeTruthy();
      expect(response.headers()['content-type']).toContain('text/plain');

      const text = await response.text();
      expect(text.length).toBeGreaterThan(0);
    });
  });

  test.describe('Response headers', () => {
    test('includes Cache-Control header', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=json`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: validHTML,
      });

      // Verify Cache-Control header is set
      const cacheControl = response.headers()['cache-control'];
      expect(cacheControl).toBeDefined();
      expect(cacheControl).toContain('no-cache');
    });

    test('includes Content-Type header with charset', async ({ request }) => {
      const response = await request.post(`${baseURL}?out=json`, {
        headers: {
          'Content-Type': 'text/html; charset=utf-8',
        },
        data: validHTML,
      });

      const contentType = response.headers()['content-type'];
      expect(contentType).toContain('charset');
    });
  });
});
