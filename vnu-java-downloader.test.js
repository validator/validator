import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import {
  describeError,
  getProxyFromEnv,
  getPlatformArchiveName,
  getLocalJavaExecutablePath,
} from './vnu-java-downloader.js';

const PROXY_VARS = [
  'npm_config_https_proxy',
  'npm_config_proxy',
  'HTTPS_PROXY',
  'https_proxy',
  'HTTP_PROXY',
  'http_proxy',
  'ALL_PROXY',
  'all_proxy',
];

describe('describeError', () => {
  it('returns a fixed string for a missing error', () => {
    expect(describeError(null)).toBe('unknown error');
    expect(describeError(undefined)).toBe('unknown error');
  });

  it('returns the message of a plain error', () => {
    expect(describeError(new Error('boom'))).toBe('boom');
  });

  it('appends the code of a cause', () => {
    const err = new Error('fetch failed');
    err.cause = { code: 'ENOTFOUND' };
    expect(describeError(err)).toBe('fetch failed (cause: ENOTFOUND)');
  });

  it('walks a nested chain of causes', () => {
    const err = new Error('fetch failed');
    err.cause = { message: 'connect error', cause: { code: 'ECONNRESET' } };
    expect(describeError(err)).toBe(
      'fetch failed (cause: connect error) (cause: ECONNRESET)');
  });

  it('terminates on a cyclic cause chain', () => {
    const err = new Error('looping');
    err.cause = err;
    expect(describeError(err)).toBe('looping');
  });
});

describe('getProxyFromEnv', () => {
  let saved;

  beforeEach(() => {
    saved = {};
    for (const name of PROXY_VARS) {
      saved[name] = process.env[name];
      delete process.env[name];
    }
  });

  afterEach(() => {
    for (const name of PROXY_VARS) {
      if (saved[name] === undefined) {
        delete process.env[name];
      } else {
        process.env[name] = saved[name];
      }
    }
  });

  it('returns null when no proxy variable is set', () => {
    expect(getProxyFromEnv()).toBeNull();
  });

  it('reads a proxy from the shell environment', () => {
    process.env.HTTPS_PROXY = 'http://proxy.example:8080';
    expect(getProxyFromEnv()).toBe('http://proxy.example:8080');
  });

  it('prefers the npm proxy configuration over shell variables', () => {
    process.env.HTTPS_PROXY = 'http://shell.example:8080';
    process.env.npm_config_https_proxy = 'http://npm.example:3128';
    expect(getProxyFromEnv()).toBe('http://npm.example:3128');
  });
});

describe('getPlatformArchiveName', () => {
  it('names a Temurin 17 JRE archive for the current platform', () => {
    expect(getPlatformArchiveName()).toMatch(
      /^OpenJDK17U-jre_.+_(linux|mac|windows)_hotspot_17\.0\.17_10\.(zip|tar\.gz)$/);
  });
});

describe('getLocalJavaExecutablePath', () => {
  it('points at a java executable in the Temurin 17 runtime', () => {
    const javaPath = getLocalJavaExecutablePath();
    expect(javaPath).toContain('jdk-17.0.17+10-jre');
    expect(javaPath).toMatch(/java(\.exe)?$/);
  });
});
