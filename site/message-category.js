/**
 * Message categorization for the Nu Html Checker.
 * This module is used by nu-script.js and can be tested independently.
 *
 * @module message-category
 */

/**
 * Categorizes a validation message as 'css', 'i18n', or 'html'.
 *
 * @param {string} messageText - The text content of the validation message
 * @returns {'css' | 'i18n' | 'html'} The category of the message
 */
function getMessageCategory(messageText) {
  // CSS validation errors (always prefixed with "CSS:")
  if (/^CSS:/.test(messageText)) {
    return 'css';
  }

  // Encoding and internationalization issues
  if (
    /\b(encoding|charset|UTF-8|windows-\d+|iso-\d+|Content-Language)\b/i.test(
      messageText
    ) ||
    /appears to be written in/i.test(messageText) ||
    /\b(lang|dir)=/i.test(messageText) ||
    /"lang"/.test(messageText) ||
    /"dir"/.test(messageText) ||
    /Unicode Normalization/.test(messageText)
  ) {
    return 'i18n';
  }

  // Everything else is HTML (including ARIA)
  return 'html';
}

// Export for Node.js/testing environments
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { getMessageCategory };
}
