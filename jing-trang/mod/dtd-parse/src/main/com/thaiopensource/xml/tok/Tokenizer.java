package com.thaiopensource.xml.tok;

/**
 * It provides operations on char arrays
 * that represent all or part of a parsed XML entity.
 * <p>
 * Several methods operate on char subarrays. The subarray is specified
 * by a char array <code>buf</code> and two integers,
 * <code>off</code> and <code>end</code>; <code>off</code>
 * gives the index in <code>buf</code> of the first char of the subarray
 * and <code>end</code> gives the
 * index in <code>buf</code> of the char immediately after the last char.
 * <p>
 * The main operations provided by <code>Tokenizer</code> are
 * <code>tokenizeProlog</code>, <code>tokenizeContent</code> and
 * <code>tokenizeCdataSection</code>;
 * these are used to divide up an XML entity into tokens.
 * <code>tokenizeProlog</code> is used for the prolog of an XML document
 * as well as for the external subset and parameter entities (except
 * when referenced in an <code>EntityValue</code>);
 * it can also be used for parsing the <code>Misc</code>* that follows
 * the document element.
 * <code>tokenizeContent</code> is used for the document element and for
 * parsed general entities that are referenced in <code>content</code>
 * except for CDATA sections.
 * <code>tokenizeCdataSection</code> is used for CDATA sections, following
 * the <code>&lt;![CDATA[</code> up to and including the <code>]]&gt;</code>.
 * <p>
 * <code>tokenizeAttributeValue</code> and <code>tokenizeEntityValue</code>
 * are used to further divide up tokens returned by <code>tokenizeProlog</code>
 * and <code>tokenizeContent</code>; they are also used to divide up entities
 * referenced in attribute values or entity values.
 */

public class Tokenizer {
  /**
   * Represents one or more characters of data.
   */
  public static final int TOK_DATA_CHARS = 0;

  /**
   * Represents a newline (CR, LF or CR followed by LF) in data.
   */
  public static final int TOK_DATA_NEWLINE = TOK_DATA_CHARS + 1;

  /**
   * Represents a complete start-tag <code>&lt;name&gt;</code>,
   * that doesn't have any attribute specifications.
   */
  public static final int TOK_START_TAG_NO_ATTS = TOK_DATA_NEWLINE + 1;

  /**
   * Represents a complete start-tag <code>&lt;name att="val"&gt;</code>,
   * that contains one or more attribute specifications.
   */
  public static final int TOK_START_TAG_WITH_ATTS = TOK_START_TAG_NO_ATTS + 1;

  /**
   * Represents an empty element tag <code>&lt;name/&gt;</code>,
   * that doesn't have any attribute specifications.
   */
  public static final int TOK_EMPTY_ELEMENT_NO_ATTS = TOK_START_TAG_WITH_ATTS + 1;

  /**
   * Represents an empty element tag <code>&lt;name att="val"/&gt;</code>,
   * that contains one or more attribute specifications.
   */
  public static final int TOK_EMPTY_ELEMENT_WITH_ATTS = TOK_EMPTY_ELEMENT_NO_ATTS + 1;

  /**
   * Represents a complete end-tag <code>&lt;/name&gt;</code>.
   */
  public static final int TOK_END_TAG = TOK_EMPTY_ELEMENT_WITH_ATTS + 1;

  /**
   * Represents the start of a CDATA section <code>&lt;![CDATA[</code>.
   */
  public static final int TOK_CDATA_SECT_OPEN = TOK_END_TAG + 1;

  /**
   * Represents the end of a CDATA section <code>]]></code>.
   */
  public static final int TOK_CDATA_SECT_CLOSE = TOK_CDATA_SECT_OPEN + 1;

  /**
   * Represents a general entity reference.
   */
  public static final int TOK_ENTITY_REF = TOK_CDATA_SECT_CLOSE + 1;

  /**
   * Represents a general entity reference to a one of the 5 predefined
   * entities <code>amp</code>, <code>lt</code>, <code>gt</code>,
   * <code>quot</code>, <code>apos</code>.
   */
  public static final int TOK_MAGIC_ENTITY_REF = TOK_ENTITY_REF + 1;

  /**
   * Represents a numeric character reference (decimal or hexadecimal),
   * when the referenced character is less than or equal to 0xFFFF
   * and so is represented by a single char.
   */
  public static final int TOK_CHAR_REF = TOK_MAGIC_ENTITY_REF + 1;

  /**
   * Represents a numeric character reference (decimal or hexadecimal),
   * when the referenced character is greater than 0xFFFF and so is
   * represented by a pair of chars.
   */
  public static final int TOK_CHAR_PAIR_REF = TOK_CHAR_REF + 1;

  /**
   * Represents a processing instruction.
   */
  public static final int TOK_PI = TOK_CHAR_PAIR_REF + 1;

  /**
   * Represents an XML declaration or text declaration (a processing
   * instruction whose target is <code>xml</code>).
   */
  public static final int TOK_XML_DECL = TOK_PI + 1;

  /**
   * Represents a comment <code>&lt;!-- comment --&gt;</code>.
   * This can occur both in the prolog and in content.
   */
  public static final int TOK_COMMENT = TOK_XML_DECL + 1;

  /**
   * Represents a white space character in an attribute value,
   * excluding white space characters that are part of line boundaries.
   */
  public static final int TOK_ATTRIBUTE_VALUE_S = TOK_COMMENT + 1;

  /**
   * Represents a parameter entity reference in the prolog.
   */
  public static final int TOK_PARAM_ENTITY_REF = TOK_ATTRIBUTE_VALUE_S + 1;

  /**
   * Represents whitespace in the prolog.
   * The token contains one whitespace character.
   */
  public static final int TOK_PROLOG_S = TOK_PARAM_ENTITY_REF + 1;

  /**
   * Represents <code>&lt;!NAME</code> in the prolog.
   */
  public static final int TOK_DECL_OPEN = TOK_PROLOG_S + 1;

  /**
   * Represents <code>&gt;</code> in the prolog.
   */
  public static final int TOK_DECL_CLOSE = TOK_DECL_OPEN + 1;

  /**
   * Represents an unprefixed name in the prolog.
   */
  public static final int TOK_NAME = TOK_DECL_CLOSE + 1;

  /**
   * Represents a name with a prefix.
   */
  public static final int TOK_PREFIXED_NAME = TOK_NAME + 1;

  /**
   * Represents a name token in the prolog that is not a name.
   */
  public static final int TOK_NMTOKEN = TOK_PREFIXED_NAME + 1;

  /**
   * Represents <code>#NAME</code> in the prolog.
   */
  public static final int TOK_POUND_NAME = TOK_NMTOKEN + 1;

  /**
   * Represents <code>|</code> in the prolog.
   */
  public static final int TOK_OR = TOK_POUND_NAME + 1;

  /**
   * Represents a <code>%</code> in the prolog that does not start
   * a parameter entity reference.
   * This can occur in an entity declaration.
   */
  public static final int TOK_PERCENT = TOK_OR + 1;

  /**
   * Represents a <code>(</code> in the prolog.
   */
  public static final int TOK_OPEN_PAREN = TOK_PERCENT + 1;

  /**
   * Represents a <code>)</code> in the prolog that is not
   * followed immediately by any of
   *  <code>*</code>, <code>+</code> or <code>?</code>.
   */
  public static final int TOK_CLOSE_PAREN = TOK_OPEN_PAREN + 1;

  /**
   * Represents <code>[</code> in the prolog.
   */
  public static final int TOK_OPEN_BRACKET = TOK_CLOSE_PAREN + 1;

  /**
   * Represents <code>]</code> in the prolog.
   */
  public static final int TOK_CLOSE_BRACKET = TOK_OPEN_BRACKET + 1;

  /**
   * Represents a literal (EntityValue, AttValue, SystemLiteral or
   * PubidLiteral).
   */
  public static final int TOK_LITERAL = TOK_CLOSE_BRACKET + 1;

  /**
   * Represents a name followed immediately by <code>?</code>.
   */
  public static final int TOK_NAME_QUESTION = TOK_LITERAL + 1;

  /**
   * Represents a name followed immediately by <code>*</code>.
   */
  public static final int TOK_NAME_ASTERISK = TOK_NAME_QUESTION + 1;

  /**
   * Represents a name followed immediately by <code>+</code>.
   */
  public static final int TOK_NAME_PLUS = TOK_NAME_ASTERISK + 1;

  /**
   * Represents <code>&lt;![</code> in the prolog.
   */
  public static final int TOK_COND_SECT_OPEN = TOK_NAME_PLUS + 1;

  /**
   * Represents <code>]]&gt;</code> in the prolog.
   */
  public static final int TOK_COND_SECT_CLOSE = TOK_COND_SECT_OPEN + 1;

  /**
   * Represents <code>)?</code> in the prolog.
   */
  public static final int TOK_CLOSE_PAREN_QUESTION = TOK_COND_SECT_CLOSE + 1;

  /**
   * Represents <code>)*</code> in the prolog.
   */
  public static final int TOK_CLOSE_PAREN_ASTERISK = TOK_CLOSE_PAREN_QUESTION + 1;

  /**
   * Represents <code>)+</code> in the prolog.
   */
  public static final int TOK_CLOSE_PAREN_PLUS = TOK_CLOSE_PAREN_ASTERISK + 1;

  /**
   * Represents <code>,</code> in the prolog.
   */
  public static final int TOK_COMMA = TOK_CLOSE_PAREN_PLUS + 1;

  // Chars with type < 0 may not be data in content.
  // The negation of the lead char type gives the total number of chars.
  static final int CT_LEAD2 = -2;
  static final int CT_NONXML = CT_LEAD2 - 1;
  static final int CT_MALFORM = CT_NONXML - 1;
  static final int CT_LT = CT_MALFORM - 1;
  static final int CT_AMP = CT_LT - 1;
  static final int CT_RSQB = CT_AMP - 1;
  static final int CT_CR = CT_RSQB - 1;
  static final int CT_LF = CT_CR - 1;
  // Chars with type >= 0 are treated as data in content.
  static final int CT_GT = 0;
  static final int CT_QUOT = CT_GT + 1;
  static final int CT_APOS = CT_QUOT + 1;
  static final int CT_EQUALS = CT_APOS + 1;
  static final int CT_QUEST = CT_EQUALS + 1;
  static final int CT_EXCL = CT_QUEST + 1;
  static final int CT_SOL = CT_EXCL + 1;
  static final int CT_SEMI = CT_SOL + 1;
  static final int CT_NUM = CT_SEMI + 1;
  static final int CT_LSQB = CT_NUM + 1;
  static final int CT_S = CT_LSQB + 1;
  static final int CT_NMSTRT = CT_S + 1;
  static final int CT_COLON = CT_NMSTRT + 1;
  static final int CT_NAME = CT_COLON + 1;
  static final int CT_MINUS = CT_NAME + 1;
  static final int CT_OTHER = CT_MINUS + 1;
  static final int CT_PERCNT = CT_OTHER + 1;
  static final int CT_LPAR = CT_PERCNT + 1;
  static final int CT_RPAR = CT_LPAR + 1;
  static final int CT_AST = CT_RPAR + 1;
  static final int CT_PLUS = CT_AST + 1;
  static final int CT_COMMA = CT_PLUS + 1;
  static final int CT_VERBAR = CT_COMMA + 1;

  final static byte[] asciiTypeTable = {
    /* 0x00 */ CT_NONXML, CT_NONXML, CT_NONXML, CT_NONXML,
    /* 0x04 */ CT_NONXML, CT_NONXML, CT_NONXML, CT_NONXML,
    /* 0x08 */ CT_NONXML, CT_S, CT_LF, CT_NONXML,
    /* 0x0C */ CT_NONXML, CT_CR, CT_NONXML, CT_NONXML,
    /* 0x10 */ CT_NONXML, CT_NONXML, CT_NONXML, CT_NONXML,
    /* 0x14 */ CT_NONXML, CT_NONXML, CT_NONXML, CT_NONXML,
    /* 0x18 */ CT_NONXML, CT_NONXML, CT_NONXML, CT_NONXML,
    /* 0x1C */ CT_NONXML, CT_NONXML, CT_NONXML, CT_NONXML,
    /* 0x20 */ CT_S, CT_EXCL, CT_QUOT, CT_NUM,
    /* 0x24 */ CT_OTHER, CT_PERCNT, CT_AMP, CT_APOS,
    /* 0x28 */ CT_LPAR, CT_RPAR, CT_AST, CT_PLUS,
    /* 0x2C */ CT_COMMA, CT_MINUS, CT_NAME, CT_SOL,
    /* 0x30 */ CT_NAME, CT_NAME, CT_NAME, CT_NAME,
    /* 0x34 */ CT_NAME, CT_NAME, CT_NAME, CT_NAME,
    /* 0x38 */ CT_NAME, CT_NAME, CT_COLON, CT_SEMI,
    /* 0x3C */ CT_LT, CT_EQUALS, CT_GT, CT_QUEST,
    /* 0x40 */ CT_OTHER, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x44 */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x48 */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x4C */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x50 */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x54 */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x58 */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_LSQB,
    /* 0x5C */ CT_OTHER, CT_RSQB, CT_OTHER, CT_NMSTRT,
    /* 0x60 */ CT_OTHER, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x64 */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x68 */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x6C */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x70 */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x74 */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_NMSTRT,
    /* 0x78 */ CT_NMSTRT, CT_NMSTRT, CT_NMSTRT, CT_OTHER,
    /* 0x7C */ CT_VERBAR, CT_OTHER, CT_OTHER, CT_OTHER
  };

  /**
   * Moves a position forward.
   * On entry, <code>pos</code> gives the position of the char at index
   * <code>off</code> in <code>buf</code>.
   * On exit, it <code>pos</code> will give the position of the char at index
   * <code>end</code>, which must be greater than or equal to <code>off</code>.
   * The chars between <code>off</code> and <code>end</code> must encode
   * one or more complete characters.
   * A carriage return followed by a line feed will be treated as a single
   * line delimiter provided that they are given to <code>movePosition</code>
   * together.
   */
  public static void movePosition(final char[] buf, int off, int end, Position pos) {
    int lineNumber = pos.lineNumber;
    /* Maintain invariant: off - colStart = colNumber */
    int colStart = off - pos.columnNumber;
    while (off != end) {
      switch (buf[off++]) {
      case '\n':
	lineNumber++;
	colStart = off;
	break;
      case '\r':
	if (off != end && buf[off] == '\n')
	  off += 1;
	lineNumber++;
	colStart = off;
	break;
      }
    }
    pos.lineNumber = lineNumber;
    pos.columnNumber = off - colStart;
  }

  private static
  void checkCharMatches(char[] buf, int off, char c) throws InvalidTokenException {
    if (buf[off] != c)
      throw new InvalidTokenException(off);
  }

  /* off points to character following "<!-" */

  private static
  int scanComment(char[] buf, int off, int end, Token token)
       throws InvalidTokenException, PartialTokenException {
    if (off != end) {
      checkCharMatches(buf, off, '-');
      off += 1;
      while (off != end) {
	switch (charType(buf[off])) {
	case CT_LEAD2:
	  if (end - off < 2)
	    throw new PartialCharException(off);
	  check2(buf, off);
	  off += 2;
	  break;
	case CT_NONXML:
	case CT_MALFORM:
	  throw new InvalidTokenException(off);
	case CT_MINUS:
	  if (++off == end)
	    throw new PartialTokenException();
	  if (buf[off] == '-') {
	    if (++off == end)
	      throw new PartialTokenException();
	    checkCharMatches(buf, off, '>');
	    token.tokenEnd = off + 1;
	    return TOK_COMMENT;
	  }
	  break;
	default:
	  off += 1;
	  break;
	}
      }
    }
    throw new PartialTokenException();
  }

  /* off points to character following "<!" */

  private static
  int scanDecl(char[] buf, int off, int end, Token token)
       throws InvalidTokenException, PartialTokenException {
    if (off == end)
      throw new PartialTokenException();
    switch (charType(buf[off])) {
    case CT_MINUS:
      return scanComment(buf, off + 1, end, token);
    case CT_LSQB:
      token.tokenEnd = off + 1;
      return TOK_COND_SECT_OPEN;
    case CT_NMSTRT:
      off += 1;
      break;
    default:
      throw new InvalidTokenException(off);
    }
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_PERCNT:
	if (off + 1 == end)
	  throw new PartialTokenException();
	/* don't allow <!ENTITY% foo "whatever"> */
	switch (charType(buf[off + 1])) {
	case CT_S:
	case CT_CR:
	case CT_LF:
	case CT_PERCNT:
	  throw new InvalidTokenException(off);
	}
	/* fall through */
      case CT_S:
      case CT_CR:
      case CT_LF:
	token.tokenEnd = off;
	return TOK_DECL_OPEN;
      case CT_NMSTRT:
	off += 1;
	break;
      default:
	throw new InvalidTokenException(off);
      }
    }
    throw new PartialTokenException();
  }

  private static
  boolean targetIsXml(char[] buf, int off, int end) throws InvalidTokenException {
    boolean upper = false;
    if (end - off != 3)
      return false;
    switch (buf[off]) {
    case 'x':
      break;
    case 'X':
      upper = true;
      break;
    default:
      return false;
    }
    off += 1;
    switch (buf[off]) {
    case 'm':
      break;
    case 'M':
      upper = true;
      break;
    default:
      return false;
    }
    off += 1;
    switch (buf[off]) {
    case 'l':
      break;
    case 'L':
      upper = true;
      break;
    default:
      return false;
    }
    if (upper)
      throw new InvalidTokenException(off, InvalidTokenException.XML_TARGET);
    return true;
  }

  /* off points to character following "<?" */

  private static
  int scanPi(char[] buf, int off, int end, Token token)
       throws PartialTokenException, InvalidTokenException {
    int target = off;
    if (off == end)
      throw new PartialTokenException();
    switch (charType(buf[off])) {
    case CT_NMSTRT:
      off += 1;
      break;
    case CT_LEAD2:
      if (end - off < 2)
	throw new PartialCharException(off);
      if (charType2(buf, off) != CT_NMSTRT)
	throw new InvalidTokenException(off);
      off += 2;
      break;
    default:
      throw new InvalidTokenException(off);
    }
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_NMSTRT:
      case CT_NAME:
      case CT_MINUS:
	off += 1;
	break;
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	if (!isNameChar2(buf, off))
	  throw new InvalidTokenException(off);
	off += 2;
	break;
      case CT_S:
      case CT_CR:
      case CT_LF:
	boolean isXml = targetIsXml(buf, target, off);
	token.nameEnd = off;
	off += 1;
	while (off != end) {
	  switch (charType(buf[off])) {
	  case CT_LEAD2:
	    if (end - off < 2)
	      throw new PartialCharException(off);
	    check2(buf, off);
	    off += 2;
	    break;
	  case CT_NONXML:
	  case CT_MALFORM:
	    throw new InvalidTokenException(off);
	  case CT_QUEST:
	    off += 1;
	    if (off == end)
	      throw new PartialTokenException();
	    if (buf[off] == '>') {
	      token.tokenEnd = off + 1;
	      if (isXml)
		return TOK_XML_DECL;
	      else
		return TOK_PI;
	    }
	    break;
	  default:
	    off += 1;
	    break;
	  }
	}
	throw new PartialTokenException();
      case CT_QUEST:
	token.nameEnd = off;
	off += 1;
	if (off == end)
	  throw new PartialTokenException();
	checkCharMatches(buf, off, '>');
	token.tokenEnd = off + 1;
	return (targetIsXml(buf, target, token.nameEnd)
		? TOK_XML_DECL
		: TOK_PI);
      default:
	throw new InvalidTokenException(off);
      }
    }
    throw new PartialTokenException();
  }

  /* off points to character following "<![" */

  private static final String CDATA = "CDATA[";

  private static
  int scanCdataSection(char[] buf, int off, int end, Token token) 
       throws PartialTokenException, InvalidTokenException {
    /* "CDATA[".length() == 6 */
    if (end - off < 6)
      throw new PartialTokenException();
    for (int i = 0; i < CDATA.length(); i++, off += 1)
      checkCharMatches(buf, off, CDATA.charAt(i));
    token.tokenEnd = off;
    return TOK_CDATA_SECT_OPEN;
  }

  /**
   * Scans the first token of a char subarrary that starts with the
   * content of a CDATA section.
   * Returns one of the following integers according to the type of token
   * that the subarray starts with:
   * <ul>
   * <li><code>TOK_DATA_CHARS</code>
   * <li><code>TOK_DATA_NEWLINE</code>
   * <li><code>TOK_CDATA_SECT_CLOSE</code>
   * </ul>
   * <p>
   * Information about the token is stored in <code>token</code>.
   * <p>
   * After <code>TOK_CDATA_SECT_CLOSE</code> is returned, the application
   * should use <code>tokenizeContent</code>.
   *
   * @exception EmptyTokenException if the subarray is empty
   * @exception PartialTokenException if the subarray contains only part of
   * a legal token
   * @exception InvalidTokenException if the subarrary does not start
   * with a legal token or part of one
   * @exception ExtensibleTokenException if the subarray encodes just a carriage
   * return ('\r')
   *
   * @see #TOK_DATA_CHARS
   * @see #TOK_DATA_NEWLINE
   * @see #TOK_CDATA_SECT_CLOSE
   * @see Token
   * @see EmptyTokenException
   * @see PartialTokenException
   * @see InvalidTokenException
   * @see ExtensibleTokenException
   * @see #tokenizeContent
   */
  public static int tokenizeCdataSection(char[] buf, int off, int end, Token token) throws EmptyTokenException, PartialTokenException, InvalidTokenException, ExtensibleTokenException {
    if (off == end)
      throw new EmptyTokenException();
    switch (charType(buf[off])) {
    case CT_RSQB:
      off += 1;
      if (off == end)
	throw new PartialTokenException();
      if (buf[off] != ']')
	break;
      off += 1;
      if (off == end)
	throw new PartialTokenException();
      if (buf[off] != '>') {
	off -= 1;
	break;
      }
      token.tokenEnd = off + 1;
      return TOK_CDATA_SECT_CLOSE;
    case CT_CR:
      off += 1;
      if (off == end)
	throw new ExtensibleTokenException(TOK_DATA_NEWLINE);
      if (charType(buf[off]) == CT_LF)
	off += 1;
      token.tokenEnd = off;
      return TOK_DATA_NEWLINE;
    case CT_LF:
      token.tokenEnd = off + 1;
      return TOK_DATA_NEWLINE;
    case CT_NONXML:
    case CT_MALFORM:
      throw new InvalidTokenException(off);
    case CT_LEAD2:
      if (end - off < 2)
	throw new PartialCharException(off);
      check2(buf, off);
      off += 2;
      break;
    default:
      off += 1;
      break;
    }
    token.tokenEnd = extendCdata(buf, off, end);
    return TOK_DATA_CHARS;
  }

  private static
  int extendCdata(final char[] buf, int off, final int end) throws InvalidTokenException {
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_LEAD2:
	if (end - off < 2)
	  return off;
	check2(buf, off);
	off += 2;
	break;
      case CT_RSQB:
      case CT_NONXML:
      case CT_MALFORM:
      case CT_CR:
      case CT_LF:
	return off;
      default:
	off += 1;
	break;
      }
    }
    return off;
  }


  /* off points to character following "</" */

  private static
  int scanEndTag(char[] buf, int off, int end, Token token)
       throws PartialTokenException, InvalidTokenException {
    if (off == end)
      throw new PartialTokenException();
    switch (charType(buf[off])) {
    case CT_NMSTRT:
      off += 1;
      break;
    case CT_LEAD2:
      if (end - off < 2)
	throw new PartialCharException(off);
      if (charType2(buf, off) != CT_NMSTRT)
	throw new InvalidTokenException(off);
      off += 2;
      break;
    default:
      throw new InvalidTokenException(off);
    }
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_NMSTRT:
      case CT_NAME:
      case CT_MINUS:
      case CT_COLON:
	off += 1;
	break;
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	if (!isNameChar2(buf, off))
	  throw new InvalidTokenException(off);
	off += 2;
	break;
      case CT_S:
      case CT_CR:
      case CT_LF:
	token.nameEnd = off;
	for (off += 1; off != end; off += 1) {
	  switch (charType(buf[off])) {
	  case CT_S:
	  case CT_CR:
	  case CT_LF:
	    break;
	  case CT_GT:
	    token.tokenEnd = off + 1;
	    return TOK_END_TAG;
	  default:
	    throw new InvalidTokenException(off);
	  }
	}
	throw new PartialTokenException();
      case CT_GT:
	token.nameEnd = off;
	token.tokenEnd = off + 1;
	return TOK_END_TAG;
      default:
	throw new InvalidTokenException(off);
      }
    }
    throw new PartialTokenException();
  }

  /* off points to character following "&#X" */

  private static
  int scanHexCharRef(char[] buf, int off, int end, Token token)
       throws PartialTokenException, InvalidTokenException {
    if (off != end) {
      int c = buf[off];
      int num;
      switch (c) {
      case '0': case '1': case '2': case '3': case '4':
      case '5': case '6': case '7': case '8': case '9':
	num = c - '0';
	break;
      case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
	num = c - ('A' - 10);
	break;
      case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': 
	num = c - ('a' - 10);
	break;
      default:
	throw new InvalidTokenException(off);
      }
      for (off += 1; off != end; off += 1) {
	c = buf[off];
	switch (c) {
	case '0': case '1': case '2': case '3': case '4':
	case '5': case '6': case '7': case '8': case '9':
	  num = (num << 4) + c - '0';
	  break;
	case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
	  num = (num << 4) + c - ('A' - 10);
	  break;
	case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': 
	  num = (num << 4) + c - ('a' - 10);
	  break;
	case ';':
	  token.tokenEnd = off + 1;
	  return setRefChar(num, token);
	default:
	  throw new InvalidTokenException(off);
	}
	if (num >= 0x110000)
	  throw new InvalidTokenException(off);
      }
    }
    throw new PartialTokenException();
  }

  /* off points to character following "&#" */
  
  private static
  int scanCharRef(char[] buf, int off, int end, Token token)
       throws PartialTokenException, InvalidTokenException {
    if (off != end) {
      int c = buf[off];
      switch (c) {
      case 'x':
	return scanHexCharRef(buf, off + 1, end, token);
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
	break;
      default:
	throw new InvalidTokenException(off);
      }
      int num = c - '0';
      for (off += 1; off != end; off += 1) {
	c = buf[off];
	switch (c) {
	case '0':
	case '1':
	case '2':
	case '3':
	case '4':
	case '5':
	case '6':
	case '7':
	case '8':
	case '9':
	  num = num * 10 + (c - '0');
	  if (num < 0x110000)
	    break;
	  /* fall through */
	default:
	  throw new InvalidTokenException(off);
	case ';':
	  token.tokenEnd = off + 1;
	  return setRefChar(num, token);
	}
      }
    }
    throw new PartialTokenException();
  }

  /* num is known to be < 0x110000; return the token code */
  private static int setRefChar(int num, Token token) 
     throws InvalidTokenException {
    if (num < 0x10000) {
      switch (charTypeTable[num >> 8][num & 0xFF]) {
      case CT_NONXML:
      case CT_LEAD2:
      case CT_MALFORM:
	throw new InvalidTokenException(token.tokenEnd - 1);
      }
      token.refChar1 = (char)num;
      return TOK_CHAR_REF;
    }
    else {
      num -= 0x10000;
      token.refChar1 = (char)((num >> 10) + 0xD800);
      token.refChar2 = (char)((num & ((1 << 10) - 1)) + 0xDC00);
      return TOK_CHAR_PAIR_REF;
    }
  }

  private static
  boolean isMagicEntityRef(char[] buf, int off, int end, Token token) {
    switch (buf[off]) {
    case 'a':
      if (end - off < 4)
	break;
      switch (buf[off + 1]) {
      case 'm':
	if (buf[off + 2] == 'p'
	    && buf[off + 3] == ';') {
	  token.tokenEnd = off + 4;
	  token.refChar1 = '&';
	  return true;
	}
	break;
      case 'p':
	if (end - off >= 5
	    && buf[off + 2] == 'o'
	    && buf[off + 3] == 's'
	    && buf[off + 4] == ';') {
	  token.tokenEnd = off + 5;
	  token.refChar1 = '\'';
	  return true;
	}
	break;
      }
      break;
    case 'l':
      if (end - off >= 3
	  && buf[off + 1] == 't'
	  && buf[off + 2] == ';') {
	token.tokenEnd = off + 3;
	token.refChar1 = '<';
	return true;
      }
      break;
    case 'g':
      if (end - off >= 3
	  && buf[off + 1] == 't'
	  && buf[off + 2] == ';') {
	token.tokenEnd = off + 3;
	token.refChar1 = '>';
	return true;
      }
      break;
    case 'q':
      if (end - off >= 5
	  && buf[off + 1] == 'u'
	  && buf[off + 2] == 'o'
	  && buf[off + 3] == 't'
	  && buf[off + 4] == ';') {
	token.tokenEnd = off + 5;
	token.refChar1 = '"';
	return true;
      }
      break;
    }
    return false;
  }
  /* off points to character following "&" */

  private static
  int scanRef(char[] buf, int off, int end, Token token)
       throws PartialTokenException, InvalidTokenException {
    if (off == end)
      throw new PartialTokenException();
    if (isMagicEntityRef(buf, off, end, token))
      return TOK_MAGIC_ENTITY_REF;
    switch (charType(buf[off])) {
    case CT_NMSTRT:
      off += 1;
      break;
    case CT_LEAD2:
      if (end - off < 2)
	throw new PartialCharException(off);
      if (charType2(buf, off) != CT_NMSTRT)
	throw new InvalidTokenException(off);
      off += 2;
      break;
    case CT_NUM:
      return scanCharRef(buf, off + 1, end, token);
    default:
      throw new InvalidTokenException(off);
    }
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_NMSTRT:
      case CT_NAME:
      case CT_MINUS:
	off += 1;
	break;
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	if (!isNameChar2(buf, off))
	  throw new InvalidTokenException(off);
	off += 2;
	break;
      case CT_SEMI:
	token.nameEnd = off;
	token.tokenEnd = off + 1;
	return TOK_ENTITY_REF;
      default:
	throw new InvalidTokenException(off);
      }
    }
    throw new PartialTokenException();
  }

  /* off points to character following first character of attribute name */

  private static
  int scanAtts(int nameStart, char[] buf, int off, int end, ContentToken token)
       throws PartialTokenException, InvalidTokenException {
    boolean hadColon = false;
    int nameEnd = -1;
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_NMSTRT:
      case CT_NAME:
      case CT_MINUS:
	off += 1;
	break;
      case CT_COLON:
	if (hadColon)
	  throw new InvalidTokenException(off);
	hadColon = true;
	off += 1;
	if (off == end)
	  throw new PartialTokenException();
	switch (charType(buf[off])) {
	case CT_NMSTRT:
	  off += 1;
	  break;
	case CT_LEAD2:
	  if (end - off < 2)
	    throw new PartialCharException(off);
	  if (charType2(buf, off) != CT_NMSTRT)
	    throw new InvalidTokenException(off);
	  off += 2;
	  break;
	default:
	  throw new InvalidTokenException(off);
	}
	break;
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	if (!isNameChar2(buf, off))
	  throw new InvalidTokenException(off);
	off += 2;
	break;
      case CT_S:
      case CT_CR:
      case CT_LF:
	nameEnd = off;
      loop:
	for (;;) {
	  off += 1;
	  if (off == end)
	    throw new PartialTokenException();
	  switch (charType(buf[off])) {
	  case CT_EQUALS:
	    break loop;
	  case CT_S:
	  case CT_LF:
	  case CT_CR:
	    break;
	  default:
	    throw new InvalidTokenException(off);
	  }
	}
	/* fall through */
      case CT_EQUALS:
	{
	  if (nameEnd < 0)
	    nameEnd = off;
	  int open;
	  hadColon = false;
	  for (;;) {
	  
	    off += 1;
	    if (off == end)
	      throw new PartialTokenException();
	    open = charType(buf[off]);
	    if (open == CT_QUOT || open == CT_APOS)
	      break;
	    switch (open) {
	    case CT_S:
	    case CT_LF:
	    case CT_CR:
	      break;
	    default:
	      throw new InvalidTokenException(off);
	    }
	  }
	  off += 1;
	  int valueStart = off;
	  boolean normalized = true;
	  /* in attribute value */
	  for (;;) {
	    int t;
	    if (off == end)
	      throw new PartialTokenException();
	    t = charType(buf[off]);
	    if (t == open)
	      break;
	    switch (t) {
	    case CT_NONXML:
	    case CT_MALFORM:
	      throw new InvalidTokenException(off);
	    case CT_LEAD2:
	      if (end - off < 2)
		throw new PartialCharException(off);
	      check2(buf, off);
	      off += 2;
	      break;
	    case CT_AMP:
	      {
		normalized = false;
		int saveNameEnd = token.nameEnd;
		scanRef(buf, off + 1, end, token);
		token.nameEnd = saveNameEnd;
		off = token.tokenEnd;
		break;
	      }
	    case CT_S:
	      if (normalized
		  && (off == valueStart
		      || buf[off] != ' '
		      || (off + 1 != end
			  && (buf[off + 1] == ' '
			      || charType(buf[off + 1]) == open))))
		normalized = false;
	      off += 1;
	      break;
	    case CT_LT:
	      throw new InvalidTokenException(off);
	    case CT_LF:
	    case CT_CR:
	      normalized = false;
	      /* fall through */
	    default:
	      off += 1;
	      break;
	    }
	  }
	  token.appendAttribute(nameStart, nameEnd, valueStart, off,
				normalized);
	  off += 1;
	  if (off == end)
	    throw new PartialTokenException();
	  int t = charType(buf[off]);
	  switch (t) {
	  case CT_S:
	  case CT_CR:
	  case CT_LF:
	    off += 1;
	    if (off == end)
	      throw new PartialTokenException();
	    t = charType(buf[off]);
	    break;
	  case CT_GT:
	  case CT_SOL:
	    break;
	  default:
	    throw new InvalidTokenException(off);
	  }
	  /* off points to closing quote */
	skipToName:
	  for (;;) {
	    switch (t) {
	    case CT_NMSTRT:
	      nameStart = off;
	      off += 1;
	      break skipToName;
	    case CT_LEAD2:
	      if (end - off < 2)
		throw new PartialCharException(off);
	      if (charType2(buf, off) != CT_NMSTRT)
		throw new InvalidTokenException(off);
	      nameStart = off;
	      off += 2;
	      break skipToName;
	    case CT_S:
	    case CT_CR:
	    case CT_LF:
	      break;
	    case CT_GT:
	      token.checkAttributeUniqueness(buf);
	      token.tokenEnd = off + 1;
	      return TOK_START_TAG_WITH_ATTS;
	    case CT_SOL:
	      off += 1;
	      if (off == end)
		throw new PartialTokenException();
	      checkCharMatches(buf, off, '>');
	      token.checkAttributeUniqueness(buf);
	      token.tokenEnd = off + 1;
	      return TOK_EMPTY_ELEMENT_WITH_ATTS;
	    default:
	      throw new InvalidTokenException(off);
	    }
	    off += 1;
	    if (off == end)
	      throw new PartialTokenException();
	    t = charType(buf[off]);
	  }
	  nameEnd = -1;
	  break;
	}
      default:
	throw new InvalidTokenException(off);
      }
    }
    throw new PartialTokenException();
  }

  /* off points to character following "<" */

  private static
  int scanLt(char[] buf, int off, int end, ContentToken token)
       throws PartialTokenException, InvalidTokenException {
    if (off == end)
      throw new PartialTokenException();
    switch (charType(buf[off])) {
    case CT_NMSTRT:
      off += 1;
      break;
    case CT_LEAD2:
      if (end - off < 2)
	throw new PartialCharException(off);
      if (charType2(buf, off) != CT_NMSTRT)
	throw new InvalidTokenException(off);
      off += 2;
      break;
    case CT_EXCL:
      if (++off == end)
	throw new PartialTokenException();
      switch (charType(buf[off])) {
      case CT_MINUS:
	return scanComment(buf, off + 1, end, token);
      case CT_LSQB:
	return scanCdataSection(buf, off + 1, end, token);
      }
      throw new InvalidTokenException(off);
    case CT_QUEST:
      return scanPi(buf, off + 1, end, token);
    case CT_SOL:
      return scanEndTag(buf, off + 1, end, token);
    default:
      throw new InvalidTokenException(off);
    }
    /* we have a start-tag */
    boolean hadColon = false;
    token.nameEnd = -1;
    token.clearAttributes();
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_NMSTRT:
      case CT_NAME:
      case CT_MINUS:
	off += 1;
	break;
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	if (!isNameChar2(buf, off))
	  throw new InvalidTokenException(off);
	off += 2;
	break;
      case CT_COLON:
	if (hadColon)
	  throw new InvalidTokenException(off);
	hadColon = true;
	off += 1;
	if (off == end)
	  throw new PartialTokenException();
	switch (charType(buf[off])) {
	case CT_NMSTRT:
	  off += 1;
	  break;
	case CT_LEAD2:
	  if (end - off < 2)
	    throw new PartialCharException(off);
	  if (charType2(buf, off) != CT_NMSTRT)
	    throw new InvalidTokenException(off);
	  off += 2;
	  break;
	default:
	  throw new InvalidTokenException(off);
	}
	break;
      case CT_S:
      case CT_CR:
      case CT_LF:
	token.nameEnd = off;
	off += 1;
      loop:
	for (;;) {
	  if (off == end)
	    throw new PartialTokenException();
	  switch (charType(buf[off])) {
	  case CT_NMSTRT:
	    return scanAtts(off, buf, off + 1, end, token);
	  case CT_LEAD2:
	    if (end - off < 2)
	      throw new PartialCharException(off);
	    if (charType2(buf, off) != CT_NMSTRT)
	      throw new InvalidTokenException(off);
	    return scanAtts(off, buf, off + 2, end, token);
	  case CT_GT:
	  case CT_SOL:
	    break loop;
	  case CT_S:
	  case CT_CR:
	  case CT_LF:
	    off += 1;
	    break;
	  default:
	    throw new InvalidTokenException(off);
	  }
	}
	break;
      case CT_GT:
	if (token.nameEnd < 0)
	  token.nameEnd = off;
	token.tokenEnd = off + 1;
	return TOK_START_TAG_NO_ATTS;
      case CT_SOL:
	if (token.nameEnd < 0)
	  token.nameEnd = off;
	off += 1;
	if (off == end)
	  throw new PartialTokenException();
	checkCharMatches(buf, off, '>');
	token.tokenEnd = off + 1;
	return TOK_EMPTY_ELEMENT_NO_ATTS;
      default:
	throw new InvalidTokenException(off);
      }
    }
    throw new PartialTokenException();
  }

  /**
   * Scans the first token of a char subarrary that contains content.
   * Returns one of the following integers according to the type of token
   * that the subarray starts with:
   * <ul>
   * <li><code>TOK_START_TAG_NO_ATTS</code>
   * <li><code>TOK_START_TAG_WITH_ATTS</code>
   * <li><code>TOK_EMPTY_ELEMENT_NO_ATTS</code>
   * <li><code>TOK_EMPTY_ELEMENT_WITH_ATTS</code>
   * <li><code>TOK_END_TAG</code>
   * <li><code>TOK_DATA_CHARS</code>
   * <li><code>TOK_DATA_NEWLINE</code>
   * <li><code>TOK_CDATA_SECT_OPEN</code>
   * <li><code>TOK_ENTITY_REF</code>
   * <li><code>TOK_MAGIC_ENTITY_REF</code>
   * <li><code>TOK_CHAR_REF</code>
   * <li><code>TOK_CHAR_PAIR_REF</code>
   * <li><code>TOK_PI</code>
   * <li><code>TOK_XML_DECL</code>
   * <li><code>TOK_COMMENT</code>
   * </ul>
   * <p>
   * Information about the token is stored in <code>token</code>.
   * <p>
   * When <code>TOK_CDATA_SECT_OPEN</code> is returned,
   * <code>tokenizeCdataSection</code> should be called until
   * it returns <code>TOK_CDATA_SECT</code>.
   *
   * @exception EmptyTokenException if the subarray is empty
   * @exception PartialTokenException if the subarray contains only part of
   * a legal token
   * @exception InvalidTokenException if the subarrary does not start
   * with a legal token or part of one
   * @exception ExtensibleTokenException if the subarray encodes just a carriage
   * return ('\r')
   *
   * @see #TOK_START_TAG_NO_ATTS
   * @see #TOK_START_TAG_WITH_ATTS
   * @see #TOK_EMPTY_ELEMENT_NO_ATTS
   * @see #TOK_EMPTY_ELEMENT_WITH_ATTS
   * @see #TOK_END_TAG
   * @see #TOK_DATA_CHARS
   * @see #TOK_DATA_NEWLINE
   * @see #TOK_CDATA_SECT_OPEN
   * @see #TOK_ENTITY_REF
   * @see #TOK_MAGIC_ENTITY_REF
   * @see #TOK_CHAR_REF
   * @see #TOK_CHAR_PAIR_REF
   * @see #TOK_PI
   * @see #TOK_XML_DECL
   * @see #TOK_COMMENT
   * @see ContentToken
   * @see EmptyTokenException
   * @see PartialTokenException
   * @see InvalidTokenException
   * @see ExtensibleTokenException
   * @see #tokenizeCdataSection
   */
  public static int tokenizeContent(char[] buf, int off, int end, ContentToken token)
       throws PartialTokenException, InvalidTokenException, EmptyTokenException, ExtensibleTokenException {

    if (off == end)
      throw new EmptyTokenException();
    switch (charType(buf[off])) {
    case CT_LT:
      return scanLt(buf, off + 1, end, token);
    case CT_AMP:
      return scanRef(buf, off + 1, end, token);
    case CT_CR:
      off += 1;
      if (off == end)
	throw new ExtensibleTokenException(TOK_DATA_NEWLINE);
      if (charType(buf[off]) == CT_LF)
	off += 1;
      token.tokenEnd = off;
      return TOK_DATA_NEWLINE;
    case CT_LF:
      token.tokenEnd = off + 1;
      return TOK_DATA_NEWLINE;
    case CT_RSQB:
      off += 1;
      if (off == end)
	throw new ExtensibleTokenException(TOK_DATA_CHARS);
      if (buf[off] != ']')
	break;
      off += 1;
      if (off == end)
	throw new ExtensibleTokenException(TOK_DATA_CHARS);
      if (buf[off] != '>') {
	off -= 1;
	break;
      }
      throw new InvalidTokenException(off);
    case CT_NONXML:
    case CT_MALFORM:
      throw new InvalidTokenException(off);
    case CT_LEAD2:
      if (end - off < 2)
	throw new PartialCharException(off);
      check2(buf, off);
      off += 2;
      break;
    default:
      off += 1;
      break;
    }
    token.tokenEnd = extendData(buf, off, end);
    return TOK_DATA_CHARS;
  }

  private static
  int extendData(final char[] buf, int off, final int end) throws InvalidTokenException {
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_LEAD2:
	if (end - off < 2)
	  return off;
	check2(buf, off);
	off += 2;
	break;
      case CT_RSQB:
      case CT_AMP:
      case CT_LT:
      case CT_NONXML:
      case CT_MALFORM:
      case CT_CR:
      case CT_LF:
	return off;
      default:
	off += 1;
	break;
      }
    }
    return off;
  }

  /* off points to character following "%" */

  private static
  int scanPercent(char[] buf, int off, int end, Token token)
       throws PartialTokenException, InvalidTokenException {
    if (off == end)
      throw new PartialTokenException();
    switch (charType(buf[off])) {
    case CT_NMSTRT:
      off += 1;
      break;
    case CT_LEAD2:
      if (end - off < 2)
	throw new PartialCharException(off);
      if (charType2(buf, off) != CT_NMSTRT)
	throw new InvalidTokenException(off);
      off += 2;
      break;
    case CT_S:
    case CT_LF:
    case CT_CR:
    case CT_PERCNT:
      token.tokenEnd = off;
      return TOK_PERCENT;
    default:
      throw new InvalidTokenException(off);
    }
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_NMSTRT:
      case CT_NAME:
      case CT_MINUS:
	off += 1;
	break;
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	if (!isNameChar2(buf, off))
	  throw new InvalidTokenException(off);
	off += 2;
	break;
      case CT_SEMI:
	token.nameEnd = off;
	token.tokenEnd = off + 1;
	return TOK_PARAM_ENTITY_REF;
      default:
	throw new InvalidTokenException(off);
      }
    }
    throw new PartialTokenException();
  }


  private static
  int scanPoundName(char[] buf, int off, int end, Token token) 
       throws PartialTokenException, InvalidTokenException, ExtensibleTokenException {
    if (off == end)
      throw new PartialTokenException();
    switch (charType(buf[off])) {
    case CT_NMSTRT:
      off += 1;
      break;
    case CT_LEAD2:
      if (end - off < 2)
	throw new PartialCharException(off);
      if (charType2(buf, off) != CT_NMSTRT)
	throw new InvalidTokenException(off);
      off += 2;
      break;
    default:
      throw new InvalidTokenException(off);
    }
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_NMSTRT:
      case CT_NAME:
      case CT_MINUS:
	off += 1;
	break;
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	if (!isNameChar2(buf, off))
	  throw new InvalidTokenException(off);
	off += 2;
	break;
      case CT_CR:
      case CT_LF:
      case CT_S:
      case CT_RPAR:
      case CT_GT:
      case CT_PERCNT:
      case CT_VERBAR:
	token.tokenEnd = off;
	return TOK_POUND_NAME;
      default:
	throw new InvalidTokenException(off);
      }
    }
    throw new ExtensibleTokenException(TOK_POUND_NAME);
  }

  private static
  int scanLit(int open, char[] buf, int off, int end, Token token)
       throws PartialTokenException, InvalidTokenException, ExtensibleTokenException {
    while (off != end) {
      int t = charType(buf[off]);
      switch (t) {
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialTokenException();
	check2(buf, off);
	off += 2;
	break;
      case CT_NONXML:
      case CT_MALFORM:
	throw new InvalidTokenException(off);
      case CT_QUOT:
      case CT_APOS:
	off += 1;
	if (t != open)
	  break;
	if (off == end)
	  throw new ExtensibleTokenException(TOK_LITERAL);
	switch (charType(buf[off])) {
	case CT_S:
	case CT_CR:
	case CT_LF:
	case CT_GT:
	case CT_PERCNT:
	case CT_LSQB:
	  token.tokenEnd = off;
	  return TOK_LITERAL;
	default:
	  throw new InvalidTokenException(off);
	}
      default:
	off += 1;
	break;
      }
    }
    throw new PartialTokenException();
  }


  /**
   * Scans the first token of a char subarray that contains part of a
   * prolog.
   * Returns one of the following integers according to the type of token
   * that the subarray starts with:
   * <ul>
   * <li><code>TOK_PI</code>
   * <li><code>TOK_XML_DECL</code>
   * <li><code>TOK_COMMENT</code>
   * <li><code>TOK_PARAM_ENTITY_REF</code>
   * <li><code>TOK_PROLOG_S</code>
   * <li><code>TOK_DECL_OPEN</code>
   * <li><code>TOK_DECL_CLOSE</code>
   * <li><code>TOK_NAME</code>
   * <li><code>TOK_NMTOKEN</code>
   * <li><code>TOK_POUND_NAME</code>
   * <li><code>TOK_OR</code>
   * <li><code>TOK_PERCENT</code>
   * <li><code>TOK_OPEN_PAREN</code>
   * <li><code>TOK_CLOSE_PAREN</code>
   * <li><code>TOK_OPEN_BRACKET</code>
   * <li><code>TOK_CLOSE_BRACKET</code>
   * <li><code>TOK_LITERAL</code>
   * <li><code>TOK_NAME_QUESTION</code>
   * <li><code>TOK_NAME_ASTERISK</code>
   * <li><code>TOK_NAME_PLUS</code>
   * <li><code>TOK_COND_SECT_OPEN</code>
   * <li><code>TOK_COND_SECT_CLOSE</code>
   * <li><code>TOK_CLOSE_PAREN_QUESTION</code>
   * <li><code>TOK_CLOSE_PAREN_ASTERISK</code>
   * <li><code>TOK_CLOSE_PAREN_PLUS</code>
   * <li><code>TOK_COMMA</code>
   * </ul>
   * @exception EmptyTokenException if the subarray is empty
   * @exception PartialTokenException if the subarray contains only part of
   * a legal token
   * @exception InvalidTokenException if the subarrary does not start
   * with a legal token or part of one
   * @exception EndOfPrologException if the subarray starts with the document
   * element; <code>tokenizeContent</code> should be used on the remainder
   * of the entity
   * @exception ExtensibleTokenException if the subarray is a legal token
   * but subsequent chars in the same entity could be part of the token
   * @see #TOK_PI
   * @see #TOK_XML_DECL
   * @see #TOK_COMMENT
   * @see #TOK_PARAM_ENTITY_REF
   * @see #TOK_PROLOG_S
   * @see #TOK_DECL_OPEN
   * @see #TOK_DECL_CLOSE
   * @see #TOK_NAME
   * @see #TOK_NMTOKEN
   * @see #TOK_POUND_NAME
   * @see #TOK_OR
   * @see #TOK_PERCENT
   * @see #TOK_OPEN_PAREN
   * @see #TOK_CLOSE_PAREN
   * @see #TOK_OPEN_BRACKET
   * @see #TOK_CLOSE_BRACKET
   * @see #TOK_LITERAL
   * @see #TOK_NAME_QUESTION
   * @see #TOK_NAME_ASTERISK
   * @see #TOK_NAME_PLUS
   * @see #TOK_COND_SECT_OPEN
   * @see #TOK_COND_SECT_CLOSE
   * @see #TOK_CLOSE_PAREN_QUESTION
   * @see #TOK_CLOSE_PAREN_ASTERISK
   * @see #TOK_CLOSE_PAREN_PLUS
   * @see #TOK_COMMA
   * @see ContentToken
   * @see EmptyTokenException
   * @see PartialTokenException
   * @see InvalidTokenException
   * @see ExtensibleTokenException
   * @see EndOfPrologException
   */

  public static
  int tokenizeProlog(char[] buf, int off, int end, Token token) 
       throws PartialTokenException,
              InvalidTokenException,
              EmptyTokenException,
              ExtensibleTokenException,
              EndOfPrologException {
    int tok;
    if (off == end)
      throw new EmptyTokenException();
    switch (charType(buf[off])) {
    case CT_QUOT:
      return scanLit(CT_QUOT, buf, off + 1, end, token);
    case CT_APOS:
      return scanLit(CT_APOS, buf, off + 1, end, token);
    case CT_LT:
      {
	off += 1;
	if (off == end)
	  throw new PartialTokenException();
	switch (charType(buf[off])) {
	case CT_EXCL:
	  return scanDecl(buf, off + 1, end, token);
	case CT_QUEST:
	  return scanPi(buf, off + 1, end, token);
	case CT_NMSTRT:
	case CT_LEAD2:
	  token.tokenEnd = off - 1;
	  throw new EndOfPrologException();
	}
	throw new InvalidTokenException(off);
      }
    case CT_CR:
      off += 1;
      if (off == end)
	throw new ExtensibleTokenException(TOK_PROLOG_S);
      if (charType(buf[off]) == CT_LF)
	off += 1;
      token.tokenEnd = off;
      return TOK_PROLOG_S;
    case CT_S:
    case CT_LF:
      token.tokenEnd = off + 1;
      return TOK_PROLOG_S;
    case CT_PERCNT:
      return scanPercent(buf, off + 1, end, token);
    case CT_COMMA:
      token.tokenEnd = off + 1;
      return TOK_COMMA;
    case CT_LSQB:
      token.tokenEnd = off + 1;
      return TOK_OPEN_BRACKET;
    case CT_RSQB:
      off += 1;
      if (off == end)
	throw new ExtensibleTokenException(TOK_CLOSE_BRACKET);
      if (buf[off] == ']') {
	if (off + 1 == end)
	  throw new PartialTokenException();
	if (buf[off + 1] == '>') {
	  token.tokenEnd = off + 2;
	  return TOK_COND_SECT_CLOSE;
	}
      }
      token.tokenEnd = off;
      return TOK_CLOSE_BRACKET;
    case CT_LPAR:
      token.tokenEnd = off + 1;
      return TOK_OPEN_PAREN;
    case CT_RPAR:
      off += 1;
      if (off == end)
	throw new ExtensibleTokenException(TOK_CLOSE_PAREN);
      switch (charType(buf[off])) {
      case CT_AST:
	token.tokenEnd = off + 1;
	return TOK_CLOSE_PAREN_ASTERISK;
      case CT_QUEST:
	token.tokenEnd = off + 1;
	return TOK_CLOSE_PAREN_QUESTION;
      case CT_PLUS:
	token.tokenEnd = off + 1;
	return TOK_CLOSE_PAREN_PLUS;
      case CT_CR:
      case CT_LF:
      case CT_S:
      case CT_GT:
      case CT_COMMA:
      case CT_VERBAR:
      case CT_RPAR:
	token.tokenEnd = off;
	return TOK_CLOSE_PAREN;
      }
      throw new InvalidTokenException(off);
    case CT_VERBAR:
      token.tokenEnd = off + 1;
      return TOK_OR;
    case CT_GT:
      token.tokenEnd = off + 1;
      return TOK_DECL_CLOSE;
    case CT_NUM:
      return scanPoundName(buf, off + 1, end, token);
    case CT_LEAD2:
      if (end - off < 2)
	throw new PartialCharException(off);
      switch (charType2(buf, off)) {
      case CT_NMSTRT:
	off += 2;
	tok = TOK_NAME;
	break;
      case CT_NAME:
	off += 2;
	tok = TOK_NMTOKEN;
	break;
      default:
	throw new InvalidTokenException(off);
      }
      break;
    case CT_NMSTRT:
      tok = TOK_NAME;
      off += 1;
      break;
    case CT_NAME:
    case CT_MINUS:
    case CT_COLON:
      tok = TOK_NMTOKEN;
      off += 1;
      break;
    default:
      throw new InvalidTokenException(off);
    }
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_NMSTRT:
      case CT_NAME:
      case CT_MINUS:
	off += 1;
	break;
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	if (!isNameChar2(buf, off))
	  throw new InvalidTokenException(off);
	off += 2;
	break;
      case CT_GT:
      case CT_RPAR:
      case CT_COMMA:
      case CT_VERBAR:
      case CT_LSQB:
      case CT_PERCNT:
      case CT_S:
      case CT_CR:
      case CT_LF:
	token.tokenEnd = off;
	return tok;
      case CT_COLON:
	off += 1;
	switch (tok) {
	case TOK_NAME:
	  if (off == end)
	    throw new PartialCharException(off);
	  tok = TOK_PREFIXED_NAME;
	  switch (charType(buf[off])) {
	  case CT_NMSTRT:
	    off += 1;
	    break;
	  case CT_LEAD2:
	    if (end - off < 2)
	      throw new PartialCharException(off);
	    if (isNameChar2(buf, off)) {
	      off += 2;
	      break;
	    }
	    // fall through
	  default:
	    tok = TOK_NMTOKEN;
	    break;
	  }
	  break;
	case TOK_PREFIXED_NAME:
	  tok = TOK_NMTOKEN;
	  break;
	}
	break;
      case CT_PLUS:
	if (tok == TOK_NMTOKEN)
	  throw new InvalidTokenException(off);
	token.tokenEnd = off + 1;
	return TOK_NAME_PLUS;
      case CT_AST:
	if (tok == TOK_NMTOKEN)
	  throw new InvalidTokenException(off);
	token.tokenEnd = off + 1;
	return TOK_NAME_ASTERISK;
      case CT_QUEST:
	if (tok == TOK_NMTOKEN)
	  throw new InvalidTokenException(off);
	token.tokenEnd = off + 1;
	return TOK_NAME_QUESTION;
      default:
	throw new InvalidTokenException(off);
      }
    }
    throw new ExtensibleTokenException(tok);
  }

  /**
   * Scans the first token of a char subarrary that contains part of
   * literal attribute value.  The opening and closing delimiters
   * are not included in the subarrary.
   * Returns one of the following integers according to the type of
   * token that the subarray starts with:
   * <ul>
   * <li><code>TOK_DATA_CHARS</code>
   * <li><code>TOK_DATA_NEWLINE</code>
   * <li><code>TOK_ATTRIBUTE_VALUE_S</code>
   * <li><code>TOK_MAGIC_ENTITY_REF</code>
   * <li><code>TOK_ENTITY_REF</code>
   * <li><code>TOK_CHAR_REF</code>
   * <li><code>TOK_CHAR_PAIR_REF</code>
   * </ul>
   * @exception EmptyTokenException if the subarray is empty
   * @exception PartialTokenException if the subarray contains only part of
   * a legal token
   * @exception InvalidTokenException if the subarrary does not start
   * with a legal token or part of one
   * @exception ExtensibleTokenException if the subarray encodes just a carriage
   * return ('\r')
   * @see #TOK_DATA_CHARS
   * @see #TOK_DATA_NEWLINE
   * @see #TOK_ATTRIBUTE_VALUE_S
   * @see #TOK_MAGIC_ENTITY_REF
   * @see #TOK_ENTITY_REF
   * @see #TOK_CHAR_REF
   * @see #TOK_CHAR_PAIR_REF
   * @see Token
   * @see EmptyTokenException
   * @see PartialTokenException
   * @see InvalidTokenException
   * @see ExtensibleTokenException
   */
  public static
  int tokenizeAttributeValue(char[] buf, int off, int end, Token token)
       throws PartialTokenException, InvalidTokenException, 
              EmptyTokenException, ExtensibleTokenException {
    if (off == end)
      throw new EmptyTokenException();
    int start = off;
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	off += 2;
	break;
      case CT_AMP:
	if (off == start)
	  return scanRef(buf, off + 1, end, token);
	token.tokenEnd = off;
	return TOK_DATA_CHARS;
      case CT_LT:
	/* this is for inside entity references */
	throw new InvalidTokenException(off);
      case CT_S:
	if (off == start) {
	  token.tokenEnd = off + 1;
	  return TOK_ATTRIBUTE_VALUE_S;
	}
	token.tokenEnd = off;
	return TOK_DATA_CHARS;
      case CT_LF:
	if (off == start) {
	  token.tokenEnd = off + 1;
	  return TOK_DATA_NEWLINE;
	}
	token.tokenEnd = off;
	return TOK_DATA_CHARS;
      case CT_CR:
	if (off == start) {
	  off += 1;
	  if (off == end)
	    throw new ExtensibleTokenException(TOK_DATA_NEWLINE);
	  if (charType(buf[off]) == CT_LF)
	    off += 1;
	  token.tokenEnd = off;
	  return TOK_DATA_NEWLINE;
	}
	token.tokenEnd = off;
	return TOK_DATA_CHARS;
      default:
	off += 1;
	break;
      }
    }
    token.tokenEnd = off;
    return TOK_DATA_CHARS;
  }

  /**
   * Scans the first token of a char subarrary that contains part of
   * literal entity value.  The opening and closing delimiters
   * are not included in the subarrary.
   * Returns one of the following integers according to the type of
   * token that the subarray starts with:
   * <ul>
   * <li><code>TOK_DATA_CHARS</code>
   * <li><code>TOK_DATA_NEWLINE</code>
   * <li><code>TOK_PARAM_ENTITY_REF</code>
   * <li><code>TOK_MAGIC_ENTITY_REF</code>
   * <li><code>TOK_ENTITY_REF</code>
   * <li><code>TOK_CHAR_REF</code>
   * <li><code>TOK_CHAR_PAIR_REF</code>
   * </ul>
   * @exception EmptyTokenException if the subarray is empty
   * @exception PartialTokenException if the subarray contains only part of
   * a legal token
   * @exception InvalidTokenException if the subarrary does not start
   * with a legal token or part of one
   * @exception ExtensibleTokenException if the subarray encodes just a carriage
   * return ('\r')
   * @see #TOK_DATA_CHARS
   * @see #TOK_DATA_NEWLINE
   * @see #TOK_MAGIC_ENTITY_REF
   * @see #TOK_ENTITY_REF
   * @see #TOK_PARAM_ENTITY_REF
   * @see #TOK_CHAR_REF
   * @see #TOK_CHAR_PAIR_REF
   * @see Token
   * @see EmptyTokenException
   * @see PartialTokenException
   * @see InvalidTokenException
   * @see ExtensibleTokenException
   */
  public static
  int tokenizeEntityValue(char[] buf, int off, int end, Token token)
       throws PartialTokenException, InvalidTokenException, 
              EmptyTokenException, ExtensibleTokenException {
    if (off == end)
      throw new EmptyTokenException();
    int start = off;
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	off += 2;
	break;
      case CT_AMP:
	if (off == start)
	  return scanRef(buf, off + 1, end, token);
	token.tokenEnd = off;
	return TOK_DATA_CHARS;
      case CT_PERCNT:
	if (off == start) {
	  int tok = scanPercent(buf, off + 1, end, token);
	  if (tok == TOK_PERCENT)
	    throw new InvalidTokenException(off + 1);
	  return tok;
	}
	token.tokenEnd = off;
	return TOK_DATA_CHARS;
      case CT_LF:
	if (off == start) {
	  token.tokenEnd = off + 1;
	  return TOK_DATA_NEWLINE;
	}
	token.tokenEnd = off;
	return TOK_DATA_CHARS;
      case CT_CR:
	if (off == start) {
	  off += 1;
	  if (off == end)
	    throw new ExtensibleTokenException(TOK_DATA_NEWLINE);
	  if (charType(buf[off]) == CT_LF)
	    off += 1;
	  token.tokenEnd = off;
	  return TOK_DATA_NEWLINE;
	}
	token.tokenEnd = off;
	return TOK_DATA_CHARS;
      default:
	off += 1;
	break;
      }
    }
    token.tokenEnd = off;
    return TOK_DATA_CHARS;
  }

  /**
   * Skips over an ignored conditional section.
   * The subarray starts following the <code>&lt;![ IGNORE [</code>.
   *
   * @return the index of the character following the closing
   * <code>]]></code>
   *
   * @exception PartialTokenException if the subarray does not contain the
   * complete ignored conditional section
   * @exception InvalidTokenException if the ignored conditional section
   * contains illegal characters
   */
  public static
  int skipIgnoreSect(char[] buf, int off, int end) throws PartialTokenException, InvalidTokenException {
    int level = 0;
  loop:
    while (off != end) {
      switch (charType(buf[off])) {
      case CT_LEAD2:
	if (end - off < 2)
	  throw new PartialCharException(off);
	check2(buf, off);
	off += 2;
	break;
      case CT_NONXML:
      case CT_MALFORM:
	throw new InvalidTokenException(off);
      case CT_LT:
	off += 1;
	if (off == end)
	  break loop;
	if (buf[off] != '!')
	  break;
	off += 1;
	if (off == end)
	  break loop;
	if (buf[off] != '[')
	  break;
	level++;
	off += 1;
	break;
      case CT_RSQB:
	off += 1;
	if (off == end)
	  break loop;
	if (buf[off] != ']')
	  break;
	off += 1;
	if (off == end)
	  break loop;
	if (buf[off] == '>') {
	  if (level == 0)
	    return off + 1;
	  level--;
	}
	else if (buf[off] == ']')
	  break;
	off += 1;
	break;
      default:
	off += 1;
	break;
      }
    }
    throw new PartialTokenException();
  }

  /**
   * Checks that a literal contained in the specified char subarray
   * is a legal public identifier and returns a string with
   * the normalized content of the public id.
   * The subarray includes the opening and closing quotes.
   * @exception InvalidTokenException if it is not a legal public identifier
   */
  public static
  String getPublicId(char[] buf, int off, int end) throws InvalidTokenException {
    StringBuffer sbuf = new StringBuffer();
    off += 1;
    end -= 1;
    for (; off != end; off += 1) {
      char c = buf[off];
      switch (charType(buf[off])) {
      case CT_MINUS:
      case CT_APOS:
      case CT_LPAR:
      case CT_RPAR:
      case CT_PLUS:
      case CT_COMMA:
      case CT_SOL:
      case CT_EQUALS:
      case CT_QUEST:
      case CT_SEMI:
      case CT_EXCL:
      case CT_AST:
      case CT_PERCNT:
      case CT_NUM:
      case CT_COLON:
	sbuf.append(c);
	break;
      case CT_S:
	if (buf[off] == '\t')
	  throw new InvalidTokenException(off);
	/* fall through */
      case CT_CR:
      case CT_LF:
	if (sbuf.length() > 0 && sbuf.charAt(sbuf.length() - 1) != ' ')
	  sbuf.append(' ');
	break;
      case CT_NAME:
      case CT_NMSTRT:
	if ((c & ~0x7f) == 0) {
	  sbuf.append(c);
	  break;
	}
	// fall through
      default:
	switch (c) {
	case '$':
	case '@':
	  break;
	default:
	  throw new InvalidTokenException(off);
	}
	break;
      }
    }
    if (sbuf.length() > 0 && sbuf.charAt(sbuf.length() - 1) == ' ')
      sbuf.setLength(sbuf.length() - 1);
    return sbuf.toString();
  }

  /**
   * Returns true if the specified char subarray is equal to the string.
   * The string must contain only XML significant characters.
   */
  public static
  boolean matchesXMLString(char[] buf, int off, int end, String str) {
    int len = str.length();
    if (len != end - off)
      return false;
    for (int i = 0; i < len; off += 1, i++) {
      if (buf[off] != str.charAt(i))
	return false;
    }
    return true;
  }

  /**
   * Skips over XML whitespace characters at the start of the specified
   * subarray.
   *
   * @return the index of the first non-whitespace character,
   * <code>end</code> if there is the subarray is all whitespace
   */
  public static
  int skipS(char[] buf, int off, int end) {
  loop:
    while (off < end) {
      switch (charType(buf[off])) {
      case CT_S:
      case CT_CR:
      case CT_LF:
	off += 1;
	break;
      default:
	break loop;
      }
    }
    return off;
  }

  private static boolean isNameChar2(char[] buf, int off) {
    int ct = charType2(buf, off);
    return ct == CT_NAME || ct == CT_NMSTRT;
  }

  private static final String nameStartSingles =
  "\u003a\u005f\u0386\u038c\u03da\u03dc\u03de\u03e0\u0559\u06d5\u093d\u09b2" +
  "\u0a5e\u0a8d\u0abd\u0ae0\u0b3d\u0b9c\u0cde\u0e30\u0e84\u0e8a\u0e8d\u0ea5" +
  "\u0ea7\u0eb0\u0ebd\u1100\u1109\u113c\u113e\u1140\u114c\u114e\u1150\u1159" +
  "\u1163\u1165\u1167\u1169\u1175\u119e\u11a8\u11ab\u11ba\u11eb\u11f0\u11f9" +
  "\u1f59\u1f5b\u1f5d\u1fbe\u2126\u212e\u3007";
  private static final String nameStartRanges =
  "\u0041\u005a\u0061\u007a\u00c0\u00d6\u00d8\u00f6\u00f8\u00ff\u0100\u0131" +
  "\u0134\u013e\u0141\u0148\u014a\u017e\u0180\u01c3\u01cd\u01f0\u01f4\u01f5" +
  "\u01fa\u0217\u0250\u02a8\u02bb\u02c1\u0388\u038a\u038e\u03a1\u03a3\u03ce" +
  "\u03d0\u03d6\u03e2\u03f3\u0401\u040c\u040e\u044f\u0451\u045c\u045e\u0481" +
  "\u0490\u04c4\u04c7\u04c8\u04cb\u04cc\u04d0\u04eb\u04ee\u04f5\u04f8\u04f9" +
  "\u0531\u0556\u0561\u0586\u05d0\u05ea\u05f0\u05f2\u0621\u063a\u0641\u064a" +
  "\u0671\u06b7\u06ba\u06be\u06c0\u06ce\u06d0\u06d3\u06e5\u06e6\u0905\u0939" +
  "\u0958\u0961\u0985\u098c\u098f\u0990\u0993\u09a8\u09aa\u09b0\u09b6\u09b9" +
  "\u09dc\u09dd\u09df\u09e1\u09f0\u09f1\u0a05\u0a0a\u0a0f\u0a10\u0a13\u0a28" +
  "\u0a2a\u0a30\u0a32\u0a33\u0a35\u0a36\u0a38\u0a39\u0a59\u0a5c\u0a72\u0a74" +
  "\u0a85\u0a8b\u0a8f\u0a91\u0a93\u0aa8\u0aaa\u0ab0\u0ab2\u0ab3\u0ab5\u0ab9" +
  "\u0b05\u0b0c\u0b0f\u0b10\u0b13\u0b28\u0b2a\u0b30\u0b32\u0b33\u0b36\u0b39" +
  "\u0b5c\u0b5d\u0b5f\u0b61\u0b85\u0b8a\u0b8e\u0b90\u0b92\u0b95\u0b99\u0b9a" +
  "\u0b9e\u0b9f\u0ba3\u0ba4\u0ba8\u0baa\u0bae\u0bb5\u0bb7\u0bb9\u0c05\u0c0c" +
  "\u0c0e\u0c10\u0c12\u0c28\u0c2a\u0c33\u0c35\u0c39\u0c60\u0c61\u0c85\u0c8c" +
  "\u0c8e\u0c90\u0c92\u0ca8\u0caa\u0cb3\u0cb5\u0cb9\u0ce0\u0ce1\u0d05\u0d0c" +
  "\u0d0e\u0d10\u0d12\u0d28\u0d2a\u0d39\u0d60\u0d61\u0e01\u0e2e\u0e32\u0e33" +
  "\u0e40\u0e45\u0e81\u0e82\u0e87\u0e88\u0e94\u0e97\u0e99\u0e9f\u0ea1\u0ea3" +
  "\u0eaa\u0eab\u0ead\u0eae\u0eb2\u0eb3\u0ec0\u0ec4\u0f40\u0f47\u0f49\u0f69" +
  "\u10a0\u10c5\u10d0\u10f6\u1102\u1103\u1105\u1107\u110b\u110c\u110e\u1112" +
  "\u1154\u1155\u115f\u1161\u116d\u116e\u1172\u1173\u11ae\u11af\u11b7\u11b8" +
  "\u11bc\u11c2\u1e00\u1e9b\u1ea0\u1ef9\u1f00\u1f15\u1f18\u1f1d\u1f20\u1f45" +
  "\u1f48\u1f4d\u1f50\u1f57\u1f5f\u1f7d\u1f80\u1fb4\u1fb6\u1fbc\u1fc2\u1fc4" +
  "\u1fc6\u1fcc\u1fd0\u1fd3\u1fd6\u1fdb\u1fe0\u1fec\u1ff2\u1ff4\u1ff6\u1ffc" +
  "\u212a\u212b\u2180\u2182\u3041\u3094\u30a1\u30fa\u3105\u312c\uac00\ud7a3" +
  "\u4e00\u9fa5\u3021\u3029";
  private static final String nameSingles =
  "\u002d\u002e\u05bf\u05c4\u0670\u093c\u094d\u09bc\u09be\u09bf\u09d7\u0a02" +
  "\u0a3c\u0a3e\u0a3f\u0abc\u0b3c\u0bd7\u0d57\u0e31\u0eb1\u0f35\u0f37\u0f39" +
  "\u0f3e\u0f3f\u0f97\u0fb9\u20e1\u3099\u309a\u00b7\u02d0\u02d1\u0387\u0640" +
  "\u0e46\u0ec6\u3005";
  private static final String nameRanges =
  "\u0300\u0345\u0360\u0361\u0483\u0486\u0591\u05a1\u05a3\u05b9\u05bb\u05bd" +
  "\u05c1\u05c2\u064b\u0652\u06d6\u06dc\u06dd\u06df\u06e0\u06e4\u06e7\u06e8" +
  "\u06ea\u06ed\u0901\u0903\u093e\u094c\u0951\u0954\u0962\u0963\u0981\u0983" +
  "\u09c0\u09c4\u09c7\u09c8\u09cb\u09cd\u09e2\u09e3\u0a40\u0a42\u0a47\u0a48" +
  "\u0a4b\u0a4d\u0a70\u0a71\u0a81\u0a83\u0abe\u0ac5\u0ac7\u0ac9\u0acb\u0acd" +
  "\u0b01\u0b03\u0b3e\u0b43\u0b47\u0b48\u0b4b\u0b4d\u0b56\u0b57\u0b82\u0b83" +
  "\u0bbe\u0bc2\u0bc6\u0bc8\u0bca\u0bcd\u0c01\u0c03\u0c3e\u0c44\u0c46\u0c48" +
  "\u0c4a\u0c4d\u0c55\u0c56\u0c82\u0c83\u0cbe\u0cc4\u0cc6\u0cc8\u0cca\u0ccd" +
  "\u0cd5\u0cd6\u0d02\u0d03\u0d3e\u0d43\u0d46\u0d48\u0d4a\u0d4d\u0e34\u0e3a" +
  "\u0e47\u0e4e\u0eb4\u0eb9\u0ebb\u0ebc\u0ec8\u0ecd\u0f18\u0f19\u0f71\u0f84" +
  "\u0f86\u0f8b\u0f90\u0f95\u0f99\u0fad\u0fb1\u0fb7\u20d0\u20dc\u302a\u302f" +
  "\u0030\u0039\u0660\u0669\u06f0\u06f9\u0966\u096f\u09e6\u09ef\u0a66\u0a6f" +
  "\u0ae6\u0aef\u0b66\u0b6f\u0be7\u0bef\u0c66\u0c6f\u0ce6\u0cef\u0d66\u0d6f" +
  "\u0e50\u0e59\u0ed0\u0ed9\u0f20\u0f29\u3031\u3035\u309d\u309e\u30fc\u30fe";

  private final static byte[][] charTypeTable;

  private static void setCharType(char c, int type) {
    if (c < 0x80)
      return;
    int hi = c >> 8;
    if (charTypeTable[hi] == null) {
      charTypeTable[hi] = new byte[256];
      for (int i = 0; i < 256; i++)
	charTypeTable[hi][i] = CT_OTHER;
    }
    charTypeTable[hi][c & 0xFF] = (byte)type;
  }

  private static void setCharType(char min, char max, int type) {
    byte[] shared = null;
    do {
      if ((min & 0xFF) == 0) {
	for (; min + 0xFF <= max; min += 0x100) {
	  if (shared == null) {
	    shared = new byte[256];
	    for (int i = 0; i < 256; i++)
	      shared[i] = (byte)type;
	  }
	  charTypeTable[min >> 8] = shared;
	  if (min + 0xFF == max)
	    return;
	}
      }
      setCharType(min, type);
    } while (min++ != max);
  }

  static {
    charTypeTable = new byte[256][];
    for (int i = 0; i < nameSingles.length(); i++)
      setCharType(nameSingles.charAt(i), CT_NAME);
    for (int i = 0; i < nameRanges.length(); i += 2)
      setCharType(nameRanges.charAt(i), nameRanges.charAt(i + 1), CT_NAME);
    for (int i = 0; i < nameStartSingles.length(); i++)
      setCharType(nameStartSingles.charAt(i), CT_NMSTRT);
    for (int i = 0; i < nameStartRanges.length(); i += 2)
      setCharType(nameStartRanges.charAt(i),
		  nameStartRanges.charAt(i + 1),
		  CT_NMSTRT);
    setCharType('\uD800', '\uDBFF', CT_LEAD2);
    setCharType('\uDC00', '\uDFFF', CT_MALFORM);
    setCharType('\uFFFE', '\uFFFF', CT_NONXML);
    byte[] other = new byte[256];
    for (int i = 0; i < 256; i++)
      other[i] = CT_OTHER;
    for (int i = 0; i < 256; i++)
      if (charTypeTable[i] == null)
	charTypeTable[i] = other;
    System.arraycopy(asciiTypeTable, 0, charTypeTable[0], 0, 128);
  }

  static int charType(char c) {
    return charTypeTable[c >> 8][c & 0xFF];
  }

  // Called only when charType(buf[off]) == CT_LEAD2
  static private int charType2(char[] buf, int off) {
    return CT_OTHER;
  }

  static private void check2(char[] buf, int off) throws InvalidTokenException { }

}
