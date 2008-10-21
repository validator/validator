package com.thaiopensource.validate.picl;

import com.thaiopensource.util.Localizer;
import com.thaiopensource.xml.util.Naming;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

class PatternParser {
  private final ErrorHandler eh;
  private final Localizer localizer;
  private String pattern;
  private int patternOffset;
  private int patternLength;
  private int currentToken;
  private int tokenStartOffset;
  private String tokenNamespaceUri;
  private String tokenLocalName;
  private final PatternBuilder builder = new PatternBuilder();
  private NamespaceContext namespaceContext;
  private final StringBuffer nameBuffer = new StringBuffer();

  private static final int TOKEN_EOF  = 0;
  private static final int TOKEN_SLASH  = 1;
  private static final int TOKEN_SLASH_SLASH  = 2;
  private static final int TOKEN_CHOICE  = 3;
  private static final int TOKEN_CHILD_AXIS  = 4;
  private static final int TOKEN_ATTRIBUTE_AXIS  = 5;
  private static final int TOKEN_DOT = 6;
  private static final int TOKEN_QNAME = 7;
  private static final int TOKEN_NCNAME_STAR = 8;
  private static final int TOKEN_STAR = 9;
  private Locator locator;

  PatternParser(ErrorHandler eh, Localizer localizer) {
    this.eh = eh;
    this.localizer = localizer;
  }

  Pattern parse(String pattern, Locator locator, NamespaceContext namespaceContext) throws SAXException, InvalidPatternException {
    this.pattern = pattern;
    this.patternOffset = 0;
    this.patternLength = pattern.length();
    this.locator = locator;
    this.namespaceContext = namespaceContext;
    try {
      do {
        parseChoice();
      } while (currentToken == TOKEN_CHOICE);
      return builder.createPattern();
    }
    finally {
      builder.cleanup();
    }
  }

  private void parseChoice() throws SAXException, InvalidPatternException {
    for (;;) {
      parseStep();
      advance();
      switch (currentToken) {
      case TOKEN_SLASH:
        break;
      case TOKEN_SLASH_SLASH:
        builder.addDescendantsOrSelf();
        break;
      case TOKEN_CHOICE:
        builder.alternative();
        return;
      case TOKEN_EOF:
        return;
      default:
        throw error("expected_step_connector");
      }
    }
  }

  private void parseStep() throws SAXException, InvalidPatternException {
    advance();
    byte type;
    switch (currentToken) {
    case TOKEN_ATTRIBUTE_AXIS:
      type = PatternBuilder.ATTRIBUTE;
      advance();
      break;
    case TOKEN_CHILD_AXIS:
      type = PatternBuilder.CHILD;
      advance();
      break;
    case TOKEN_DOT:
      return;
    default:
      type = PatternBuilder.CHILD;
      break;
    }
    switch (currentToken) {
    case TOKEN_QNAME:
      builder.addName(type, tokenNamespaceUri, tokenLocalName);
      break;
    case TOKEN_STAR:
      builder.addAnyName(type);
      break;
    case TOKEN_NCNAME_STAR:
      builder.addNsName(type, tokenNamespaceUri);
      break;
    default:
      throw error("expected_name_test");
    }
  }


  private void advance() throws SAXException, InvalidPatternException {
    for (;;) {
      tokenStartOffset = patternOffset;
      if (patternOffset >= patternLength) {
        currentToken = TOKEN_EOF;
        return;
      }
      char ch = pattern.charAt(patternOffset);
      switch (ch) {
      case ' ':
      case '\t':
      case '\r':
      case '\n':
        patternOffset++;
        continue;
      case '.':
        patternOffset++;
        currentToken = TOKEN_DOT;
        return;
      case '@':
        patternOffset++;
        currentToken = TOKEN_ATTRIBUTE_AXIS;
        return;
      case '|':
        patternOffset++;
        currentToken = TOKEN_CHOICE;
        return;
      case '/':
        if (++patternOffset < patternLength && pattern.charAt(patternOffset) == '/') {
          patternOffset++;
          currentToken = TOKEN_SLASH_SLASH;
        }
        else
          currentToken = TOKEN_SLASH;
        return;
      case '*':
        patternOffset++;
        currentToken = TOKEN_STAR;
        return;
      }
      String name = scanNCName("illegal_char");
      if ((name.equals("child") || name.equals("attribute")) && tryScanDoubleColon()) {
        currentToken = name.charAt(0) == 'c' ? TOKEN_CHILD_AXIS : TOKEN_ATTRIBUTE_AXIS;
        return;
      }
      if (patternOffset < patternLength && pattern.charAt(patternOffset) == ':') {
        tokenNamespaceUri = expandPrefix(name);
        patternOffset++;
        if (patternOffset == patternLength)
          throw error("expected_star_or_ncname");
        if (pattern.charAt(patternOffset) == '*') {
          patternOffset++;
          currentToken = TOKEN_NCNAME_STAR;
          return;
        }
        tokenLocalName = scanNCName("expected_star_or_ncname");
        currentToken = TOKEN_QNAME;
        return;
      }
      tokenLocalName = name;
      tokenNamespaceUri = namespaceContext.defaultPrefix();
      currentToken = TOKEN_QNAME;
      return;
    }
  }

  private boolean tryScanDoubleColon() {
    for (int i = patternOffset; i < patternLength; i++) {
      switch (pattern.charAt(i)) {
      case ' ':
      case '\t':
      case '\r':
      case '\n':
        break;
      case ':':
        if (++i < patternLength && pattern.charAt(i) == ':') {
          patternOffset = i + 1;
          return true;
        }
      default:
        return false;
      }

    }
    return false;
  }

  private String expandPrefix(String prefix) throws SAXException, InvalidPatternException {
    String ns = namespaceContext.getNamespaceUri(prefix);
    if (ns == null)
      throw error("unbound_prefix", prefix);
    return ns;
  }

  private String scanNCName(String message) throws SAXException, InvalidPatternException {
    char ch = pattern.charAt(patternOffset++);
    if (!maybeNameStartChar(ch))
      throw error(message);
    nameBuffer.setLength(0);
    nameBuffer.append(ch);
    for (; patternOffset < patternLength; patternOffset++) {
      ch = pattern.charAt(patternOffset);
      if (!maybeNameChar(ch))
        break;
      nameBuffer.append(ch);
    }
    String name = nameBuffer.toString();
    if (!Naming.isNcname(name))
      throw error("illegal_ncname", name);
    return name;
  }

  private static boolean maybeNameStartChar(char ch) {
    return ch > 0x80 || Character.isLetter(ch) || ch == '_';
  }

  private static boolean maybeNameChar(char ch) {
    return ch > 0x80 || Character.isLetterOrDigit(ch) || ".-_".indexOf(ch) >= 0;
  }

  private InvalidPatternException error(String key) throws SAXException {
    if (eh != null)
      eh.error(new SAXParseException(addContext(localizer.message(key)), locator));
    return new InvalidPatternException();
  }

  private InvalidPatternException error(String key, String arg) throws SAXException {
    if (eh != null)
      eh.error(new SAXParseException(addContext(localizer.message(key, arg)), locator));
    return new InvalidPatternException();
  }

  private String addContext(String message) {
    return localizer.message("context",
                             new Object[] {
                               message,
                               pattern.substring(0, tokenStartOffset),
                               pattern.substring(tokenStartOffset, patternOffset),
                               pattern.substring(patternOffset)
                             });
  }

  static public void main(String[] args) throws SAXException {
    PatternParser parser = new PatternParser(new com.thaiopensource.xml.sax.ErrorHandlerImpl(),
                                             new Localizer(PatternParser.class));
    String[] tests = {
      "foo//bar",
      ".",
      ".//.//././././/foo",
      "foo:bar",
      "bar:*",
      "*",
      "/",
      "foo/bar|bar/baz",
      "foo/",
      "",
      ".//.",
      ".//",
      "foo / @ bar",
      "child::foo:bar",
      "attribute::baz"
    };
    NamespaceContext nsc = new NamespaceContext() {
      public String getNamespaceUri(String prefix) {
        return "http://" + prefix;
      }

      public String defaultPrefix() {
        return "";
      }
    };
    for (int i = 0; i < tests.length; i++) {
      try {
        Pattern pattern = parser.parse(tests[i], null, nsc);
        System.out.println(tests[i] + " => " + pattern.toString());
      }
      catch (InvalidPatternException e) {
      }
    }
  }
}
