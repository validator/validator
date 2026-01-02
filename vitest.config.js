import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    exclude: [
      '**/node_modules/**',
      '**/e2e/**', // Exclude Playwright e2e tests
      '**/test-results/**',
    ],
  },
});
