import { describe, it, expect } from 'vitest';
import { getMessageCategory } from './message-category.js';

// Real validation messages from the Nu Html Checker for regression testing
const realMessages = {
  css: [
    'CSS: "font-family": Parse Error.',
    'CSS: "background-color": "abc" is not a "color" value.',
    'CSS: "width": only "0" can be a "length". You must put a unit after your number.',
    'CSS: "margin": too many values or values are not recognized.',
  ],
  i18n: [
    'This document appears to be written in English. Consider adding "lang="en"" (or variant) to the "html" start tag.',
    'Consider adding a "lang" attribute to the "html" start tag to declare the language of this document.',
    'The value of the "HTTP" "Content-Language" header is "en-US".',
    'Text run is not in Unicode Normalization Form C.',
    'The character encoding was not declared. Proceeding using "windows-1252".',
    'Using "windows-1252" instead of the declared encoding "iso-8859-1".',
    'Internal encoding declaration "utf-8" disagrees with the actual encoding of the document ("windows-1252").',
    'Consider adding "dir="rtl"" to the "html" start tag.',
    'This document appears to be written in Lorem ipsum text. Consider adding "lang=""" to the "html" start tag.',
  ],
  html: [
    'Element "div" not allowed as child of element "span" in this context.',
    'Stray end tag "div".',
    'Duplicate ID "main".',
    'Bad value "" for attribute "href" on element "a": Must be non-empty.',
    'The "type" attribute is unnecessary for JavaScript resources.',
    'Section lacks heading. Consider using "h2"-"h6" elements to add identifying headings to all sections.',
    'Attribute "aria-label" not allowed on element "span" at this point.',
    'The "role" attribute must not be used on a "button" element which has default implicit ARIA semantics.',
    'Start tag seen without seeing a doctype first. Expected "<!DOCTYPE html>".',
    'Element "p" not allowed as child of element "ul" in this context.',
  ],
};

describe('getMessageCategory', () => {
  describe('CSS messages', () => {
    it('categorizes messages starting with "CSS:" as css', () => {
      expect(getMessageCategory('CSS: Parse Error')).toBe('css');
    });

    it('categorizes CSS property errors as css', () => {
      expect(getMessageCategory('CSS: "color" is not a valid property name')).toBe('css');
    });

    it('categorizes CSS value errors as css', () => {
      expect(getMessageCategory('CSS: "font-size": "abc" is not a valid value')).toBe('css');
    });

    it('does not categorize messages mentioning CSS elsewhere as css', () => {
      // "CSS" not at the start should not match
      expect(getMessageCategory('The CSS property is invalid')).toBe('html');
    });

    it('requires CSS: to be at the start of the message', () => {
      expect(getMessageCategory('Error: CSS: something')).toBe('html');
    });
  });

  describe('i18n messages (encoding and internationalization)', () => {
    describe('encoding-related messages', () => {
      it('categorizes messages mentioning "encoding" as i18n', () => {
        expect(getMessageCategory('The character encoding was not declared')).toBe('i18n');
      });

      it('categorizes messages mentioning "charset" as i18n', () => {
        expect(getMessageCategory('Bad value for attribute charset')).toBe('i18n');
      });

      it('categorizes messages mentioning "UTF-8" as i18n', () => {
        expect(getMessageCategory('Document should be encoded in UTF-8')).toBe('i18n');
      });

      it('categorizes messages mentioning Windows encodings as i18n', () => {
        expect(getMessageCategory('Encoding windows-1252 detected')).toBe('i18n');
        expect(getMessageCategory('Using windows-1251 encoding')).toBe('i18n');
      });

      it('categorizes messages mentioning ISO encodings as i18n', () => {
        expect(getMessageCategory('Encoding iso-8859-1 is deprecated')).toBe('i18n');
        expect(getMessageCategory('Document uses iso-8859-15')).toBe('i18n');
      });

      it('categorizes messages mentioning Content-Language as i18n', () => {
        expect(getMessageCategory('The Content-Language header is set incorrectly')).toBe('i18n');
      });
    });

    describe('language detection messages', () => {
      it('categorizes "appears to be written in" messages as i18n', () => {
        expect(getMessageCategory('This document appears to be written in English')).toBe('i18n');
      });

      it('categorizes lang attribute messages as i18n', () => {
        expect(getMessageCategory('Consider adding a lang= attribute')).toBe('i18n');
        expect(getMessageCategory('The lang= value is invalid')).toBe('i18n');
      });

      it('categorizes dir attribute messages as i18n', () => {
        expect(getMessageCategory('Consider adding a dir= attribute for RTL text')).toBe('i18n');
      });

      it('categorizes quoted "lang" attribute messages as i18n', () => {
        expect(getMessageCategory('Consider adding a "lang" attribute to the html element')).toBe('i18n');
      });

      it('categorizes quoted "dir" attribute messages as i18n', () => {
        expect(getMessageCategory('Consider adding a "dir" attribute to the html element')).toBe('i18n');
      });
    });

    describe('Unicode normalization messages', () => {
      it('categorizes Unicode Normalization messages as i18n', () => {
        expect(getMessageCategory('Text is not in Unicode Normalization Form C')).toBe('i18n');
      });
    });

    describe('case insensitivity', () => {
      it('matches encoding keywords case-insensitively', () => {
        expect(getMessageCategory('ENCODING declaration missing')).toBe('i18n');
        expect(getMessageCategory('CHARSET attribute invalid')).toBe('i18n');
        expect(getMessageCategory('utf-8 required')).toBe('i18n');
      });

      it('matches "appears to be written in" case-insensitively', () => {
        expect(getMessageCategory('APPEARS TO BE WRITTEN IN Spanish')).toBe('i18n');
      });

      it('matches lang=/dir= case-insensitively', () => {
        expect(getMessageCategory('LANG= attribute recommended')).toBe('i18n');
        expect(getMessageCategory('DIR= attribute for bidirectional text')).toBe('i18n');
      });

      it('Unicode Normalization is case-sensitive', () => {
        // This should NOT match because the regex is case-sensitive
        expect(getMessageCategory('unicode normalization issue')).toBe('html');
      });
    });
  });

  describe('HTML messages (default category)', () => {
    it('categorizes element errors as html', () => {
      expect(getMessageCategory('Element "div" not allowed here')).toBe('html');
    });

    it('categorizes attribute errors as html', () => {
      expect(getMessageCategory('Attribute "onclick" not allowed on element "div"')).toBe('html');
    });

    it('categorizes stray tag errors as html', () => {
      expect(getMessageCategory('Stray end tag "div"')).toBe('html');
    });

    it('categorizes ARIA errors as html', () => {
      expect(getMessageCategory('Bad value for attribute "aria-label"')).toBe('html');
      expect(getMessageCategory('ARIA role "button" not allowed')).toBe('html');
    });

    it('categorizes doctype errors as html', () => {
      expect(getMessageCategory('Start tag seen without seeing a doctype first')).toBe('html');
    });

    it('categorizes duplicate ID errors as html', () => {
      expect(getMessageCategory('Duplicate ID "main"')).toBe('html');
    });

    it('categorizes unclosed element errors as html', () => {
      expect(getMessageCategory('Unclosed element "p"')).toBe('html');
    });

    it('categorizes empty messages as html', () => {
      expect(getMessageCategory('')).toBe('html');
    });

    it('categorizes generic validation messages as html', () => {
      expect(getMessageCategory('Validation error')).toBe('html');
      expect(getMessageCategory('Document is not valid')).toBe('html');
    });
  });

  describe('edge cases', () => {
    it('handles messages with multiple potential matches', () => {
      // CSS takes precedence
      expect(getMessageCategory('CSS: encoding issue')).toBe('css');
    });

    it('handles messages with special characters', () => {
      expect(getMessageCategory('Element <div> not allowed')).toBe('html');
      expect(getMessageCategory('Value "test & value" invalid')).toBe('html');
    });

    it('handles very long messages', () => {
      const longMessage = 'A'.repeat(10000) + ' encoding issue';
      expect(getMessageCategory(longMessage)).toBe('i18n');
    });

    it('handles messages with newlines', () => {
      expect(getMessageCategory('Line 1\nLine 2 with encoding')).toBe('i18n');
    });

    it('handles word boundaries correctly for encoding keywords', () => {
      // "encoding" should match as a word
      expect(getMessageCategory('character encoding declaration')).toBe('i18n');
      // "encoding" as part of another word should NOT match (word boundary \b)
      expect(getMessageCategory('reencoding the file')).toBe('html');
    });

    it('does not match partial keywords without word boundaries', () => {
      // "lang=" requires the equals sign
      expect(getMessageCategory('language detection')).toBe('html');
      // But "lang=" should match
      expect(getMessageCategory('lang= attribute')).toBe('i18n');
    });
  });

  describe('real validation messages (regression tests)', () => {
    describe('CSS messages from actual checker output', () => {
      realMessages.css.forEach((message, index) => {
        it(`correctly categorizes CSS message ${index + 1}`, () => {
          expect(getMessageCategory(message)).toBe('css');
        });
      });
    });

    describe('i18n messages from actual checker output', () => {
      realMessages.i18n.forEach((message, index) => {
        it(`correctly categorizes i18n message ${index + 1}`, () => {
          expect(getMessageCategory(message)).toBe('i18n');
        });
      });
    });

    describe('HTML messages from actual checker output', () => {
      realMessages.html.forEach((message, index) => {
        it(`correctly categorizes HTML message ${index + 1}`, () => {
          expect(getMessageCategory(message)).toBe('html');
        });
      });
    });
  });
});
