package com.thaiopensource.xml.tok;

/**
 * An XML TextDecl.
 */
public class TextDecl {
  private String version;
  private String encoding;
  
  /**
   * Creates a <code>TextDecl</code> from the specified char subarray.
   * The char subarray should be a <code>TOK_XML_DECL</code> token
   * returned from Tokenizer.tokenizeProlog or Tokenizer.tokenizeContent,
   * starting with <code>&lt;?</code> and ending with <code>?&gt;</code>.
   * @exception InvalidTokenException if the specified char subarray
   * is not a legal XML TextDecl.
   */
  public TextDecl(char[] buf, int off, int end)
       throws InvalidTokenException {
    init(false, buf, off, end);
  }

  /**
   * Return the encoding specified in the declaration, or null
   * if no encoding was specified.
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Return the version specified in the declaration, or null
   * if no version was specified.
   */
  public String getVersion() {
    return version;
  }

  TextDecl() { }

  boolean init(boolean isXmlDecl, char[] buf, int off, int end) throws InvalidTokenException {
    // Skip <?xml
    off += 5;
    // Skip ?>
    end -= 2;
    ContentToken ct = new ContentToken();
    int firstErrorIndex = -1;
    try {
      parsePseudoAttributes(buf, off, end, ct);
    }
    catch (InvalidTokenException e) {
      firstErrorIndex = e.getOffset();
    }
    int nAtts = ct.getAttributeSpecifiedCount();
    if (nAtts == 0) {
      if (firstErrorIndex == -1)
	firstErrorIndex = end;
      throw new InvalidTokenException(firstErrorIndex);
    }
    String[] names = new String[nAtts];
    String[] values = new String[nAtts];
    for (int i = 0; i < nAtts; i++) {
      int s = ct.getAttributeNameStart(i);
      int e = ct.getAttributeNameEnd(i);
      names[i] = new String(buf, s, e - s);
      s = ct.getAttributeValueStart(i);
      e = ct.getAttributeValueEnd(i);
      values[i] = new String(buf, s, e - s);
    }
    int att = 0;
    if (names[0].equals("version")) {
      version = values[0];
      att++;
    }
    if ((att == 1 || !isXmlDecl)
	&& att < nAtts && names[att].equals("encoding")) {
      encoding = values[att];
      if (values[att].length() == 0
	  || !Character.isLetter(values[att].charAt(0))
	  || values[att].indexOf(':') >= 0) {
	int k = ct.getAttributeValueStart(att);
	if (firstErrorIndex == -1 || k < firstErrorIndex)
	  firstErrorIndex = k;
      }
      att++;
    }
    else if (!isXmlDecl)
      firstErrorIndex = end;	// encoding is required in a TextDecl 
    boolean standalone = false;
    if (isXmlDecl && att > 0 && att < nAtts
	&& names[att].equals("standalone")) {
      if (values[att].equals("yes"))
	standalone = true;
      else if (!values[att].equals("no")) {
	int k = ct.getAttributeValueStart(att);
	if (firstErrorIndex == -1 || k < firstErrorIndex)
	  firstErrorIndex = k;
      }
      att++;
    }
    if (att < nAtts) {
      int k = ct.getAttributeNameStart(att);
      if (firstErrorIndex == -1 || k < firstErrorIndex)
	firstErrorIndex = k;
    }
    if (firstErrorIndex != -1)
      throw new InvalidTokenException(firstErrorIndex);
    return standalone;
  }

  private final
  void parsePseudoAttributes(char[] buf, int off, int end,
			     ContentToken ct) throws InvalidTokenException {
    for (;;) {
      off = skipWS(buf, off, end);
      if (off == end)
	break;
      int nameStart = off;
      int nameEnd;
    nameLoop:
      for (;;) {
	switch (Tokenizer.charType(buf[off])) {
	case Tokenizer.CT_NMSTRT:
	  break;
	case Tokenizer.CT_EQUALS:
	  nameEnd = off;
	  break nameLoop;
	case Tokenizer.CT_S:
	case Tokenizer.CT_LF:
	case Tokenizer.CT_CR:
	  nameEnd = off;
	  off += 1;
	  off = skipWS(buf, off, end);
	  if (off == end || buf[off] != '=')
	    throw new InvalidTokenException(off);
	  break nameLoop;
	default:
	  throw new InvalidTokenException(off);
	}
	off += 1;
	if (off == end)
	  throw new InvalidTokenException(off);
      }
      off += 1;
      off = skipWS(buf, off, end);
      if (off == end || !(buf[off] == '\'' || buf[off] == '"'))
	throw new InvalidTokenException(off);
      off += 1;
      int valueStart = off;
    valueLoop:
      for (;;) {
	if (off == end)
	  throw new InvalidTokenException(off);
	switch (Tokenizer.charType(buf[off])) {
	case Tokenizer.CT_NMSTRT:
	case Tokenizer.CT_NAME:
	case Tokenizer.CT_MINUS:
	  if ((buf[off] & ~0x7F) != 0)
	    throw new InvalidTokenException(off);
	  off += 1;
	  break;
	case Tokenizer.CT_QUOT:
	case Tokenizer.CT_APOS:
	  if (buf[off] != buf[valueStart - 1])
	    throw new InvalidTokenException(off);
	  break valueLoop;
	default:
	  throw new InvalidTokenException(off);
	}
      }
      ct.appendAttribute(nameStart, nameEnd, valueStart, off, true);
      off += 1;
      if (off == end)
	break;
      switch (buf[off]) {
      case ' ':
      case '\r':
      case '\n':
      case '\t':
	off += 1;
	break;
      default:
	throw new InvalidTokenException(off);
      }
    }
  }

  private static int skipWS(char[] buf, int off, int end) {
  loop:
    while (off != end) {
      switch (buf[off]) {
      case ' ':
      case '\r':
      case '\n':
      case '\t':
	off += 1;
	break;
      default:
	break loop;
      }
    }
    return off;
  }
}
