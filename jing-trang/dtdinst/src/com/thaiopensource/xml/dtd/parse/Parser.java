package com.thaiopensource.xml.dtd.parse;

import java.io.Reader;
import java.io.IOException;
import java.util.Vector;
import java.util.Hashtable;

import com.thaiopensource.xml.tok.*;
import com.thaiopensource.xml.em.*;
import com.thaiopensource.util.Localizer;

class Parser extends Token {
  static final Localizer localizer = new Localizer(Parser.class);
  private Parser parent;
  private Reader in;
  private char[] buf;
  private int bufStart = 0;
  private int bufEnd;
  private int currentTokenStart = 0;
  // The offset in buffer corresponding to pos.
  private int posOff = 0;
  private long bufEndStreamOffset = 0;
  private final Position pos = new Position();

  private static final int READSIZE = 1024*8;
  // Some temporary buffers
  private final ReplacementTextBuffer valueBuf;
  private final DtdBuilder db;
  private final Vector atoms = new Vector();
  private final boolean isInternal;
  private final String baseUri;
  private final EntityManager entityManager;
  // for error messages
  private String location;
  
  private final Hashtable atomTable;
  private final Hashtable elementTable;

  static class DeclState {
    Entity entity;
    Notation notation;
  }

  Parser(OpenEntity entity, EntityManager entityManager) {
    this.in = entity.getReader();
    this.baseUri = entity.getBaseUri();
    this.location = entity.getLocation();
    this.entityManager = entityManager;
    this.buf = new char[READSIZE * 2];
    this.valueBuf = new ReplacementTextBuffer();
    this.bufEnd = 0;
    this.db = new DtdBuilder(atoms);
    this.isInternal = false;
    this.elementTable = new Hashtable();
    this.atomTable = new Hashtable();
  }

  private Parser(OpenEntity entity, Parser parent) {
    this.in = entity.getReader();
    this.baseUri = entity.getBaseUri();
    this.location = entity.getLocation();
    this.entityManager = parent.entityManager;
    this.parent = parent;
    this.buf = new char[READSIZE * 2];
    this.valueBuf = new ReplacementTextBuffer();
    this.bufEnd = 0;
    this.db = parent.db;
    this.isInternal = false;
    this.elementTable = parent.elementTable;
    this.atomTable = parent.atomTable;
  }

  private Parser(char[] buf, String entityName, Parser parent) {
    // this.internalEntityName = entityName;
    this.buf = buf;
    this.parent = parent;
    this.baseUri = parent.baseUri;
    this.entityManager = parent.entityManager;
    this.bufEnd = buf.length;
    this.bufEndStreamOffset = buf.length;
    this.valueBuf = parent.valueBuf;
    this.db = parent.db;
    this.isInternal = true;
    this.elementTable = parent.elementTable;
    this.atomTable = parent.atomTable;
  }

  DtdBuilder parse() throws IOException {
    skipTextDecl();
    parseDecls(false);
    return db;
  }

  private void parseDecls(boolean isInternal) throws IOException {
    PrologParser pp = new PrologParser(isInternal
				       ? PrologParser.INTERNAL_ENTITY
				       : PrologParser.EXTERNAL_ENTITY);
    DeclState declState = new DeclState();
    try {
      for (;;) {
	int tok;
	try {
	  tok = tokenizeProlog();
	}
	catch (EndOfPrologException e) {
	  fatal("SYNTAX_ERROR");
          break;
	}
	catch (EmptyTokenException e) {
	  pp.end();
	  break;
	}
	prologAction(tok, pp, declState);
      }
    }
    catch (PrologSyntaxException e) {
      fatal("SYNTAX_ERROR");
    }
    finally {
      if (!isInternal && in != null) {
	in.close();
	in = null;
      }
    }
  }

  private void prologAction(int tok, PrologParser pp, DeclState declState)
    throws IOException, PrologSyntaxException {
    Atom a = makeAtom(tok, currentTokenStart, bufStart);
    addAtom(a);
    String token = a.getToken();
    int action = pp.action(tok, token);
    switch (action) {
    case PrologParser.ACTION_IGNORE_SECT:
      skipIgnoreSect();
      break;
    case PrologParser.ACTION_GENERAL_ENTITY_NAME:
      declState.entity = db.createGeneralEntity(token);
      break;
    case PrologParser.ACTION_PARAM_ENTITY_NAME:
      declState.entity = db.createParamEntity(token);
      break;
    case PrologParser.ACTION_ENTITY_PUBLIC_ID:
      try {
	declState.entity.publicId = Tokenizer.getPublicId(buf,
							  currentTokenStart,
							  bufStart);
      }
      catch (InvalidTokenException e) {
	currentTokenStart = e.getOffset();
	fatal("INVALID_PUBLIC_ID");
      }
      break;
    case PrologParser.ACTION_ENTITY_SYSTEM_ID:
      declState.entity.systemId = token.substring(1, token.length() - 1);
      declState.entity.baseUri = baseUri;
      break;
    case PrologParser.ACTION_ENTITY_NOTATION_NAME:
      declState.entity.notationName = token;
      break;
    case PrologParser.ACTION_ENTITY_VALUE_WITH_PEREFS:
      makeReplacementText();
      declState.entity.text = valueBuf.getChars();
      declState.entity.entityValue = token.substring(1, token.length() - 1);
      declState.entity.mustReparse = valueBuf.getMustReparse();
      declState.entity.references = valueBuf.getReferences();
      if (declState.entity.mustReparse)
	declState.entity.problem = Entity.REPARSE_PROBLEM;
      else if (declState.entity.overridden && declState.entity.isParameter)
	declState.entity.atoms = tokenizeOverriddenEntity(declState.entity.text);
      break;
    case PrologParser.ACTION_INNER_PARAM_ENTITY_REF:
    case PrologParser.ACTION_OUTER_PARAM_ENTITY_REF:
      {
	int nameStart = currentTokenStart + 1;
	String name = new String(buf, nameStart, getNameEnd() - nameStart);
	Entity entity = db.lookupParamEntity(name);
	if (entity == null) {
	  fatal("UNDEF_PEREF", name);
	  break;
	}
	Parser parser = makeParserForEntity(entity, name);
	if (parser == null) {
	  //XXX
	  break;
	}
	entity.open = true;
	if (action == PrologParser.ACTION_OUTER_PARAM_ENTITY_REF)
	  parser.parseDecls(entity.text != null);
	else
	  parser.parseInnerParamEntity(pp, declState);
	entity.atoms = parser.atoms;
	setLastAtomEntity(entity);
	entity.open = false;
	break;
      }
    case PrologParser.ACTION_ELEMENT_NAME:
      if (elementTable.get(token) != null)
	fatal("DUPLICATE_ELEMENT", token);
      elementTable.put(token, token);
      break;
    case PrologParser.ACTION_NOTATION_NAME:
      declState.notation = db.createNotation(token);
      if (declState.notation == null)
	fatal("DUPLICATE_NOTATION", token);
      break;
    case PrologParser.ACTION_NOTATION_PUBLIC_ID:
      try {
	declState.notation.publicId = Tokenizer.getPublicId(buf,
							    currentTokenStart,
							    bufStart);
      }
      catch (InvalidTokenException e) {
	currentTokenStart = e.getOffset();
	fatal("INVALID_PUBLIC_ID");
      }
      break;
    case PrologParser.ACTION_NOTATION_SYSTEM_ID:
      declState.notation.systemId = token.substring(1, token.length() - 1);
      declState.notation.baseUri = baseUri;
      break;
    case PrologParser.ACTION_DEFAULT_ATTRIBUTE_VALUE:
      {
	String origValue = token.substring(1, token.length() - 1);
	if (db.getNormalized(origValue) != null)
	  break;
	StringBuffer tem = new StringBuffer();
	try {
	  normalizeAttributeValue(buf,
				  currentTokenStart + 1,
				  bufStart - 1,
				  tem);
	}
	catch (AttributeValueException e) {
	  currentTokenStart = e.offset;
	  if (e.arg != null)
	    fatal(e.key, e.arg);
	  else
	    fatal(e.key);
	}
	db.setNormalized(origValue, tem.toString());
	break;
      }
    }
  }

  void parseInnerParamEntity(PrologParser pp, DeclState declState) throws IOException {
    int groupLevel = pp.getGroupLevel();
    try {
      for (;;) {
	int tok = tokenizeProlog();
	prologAction(tok, pp, declState);
	switch (tok) {
	case Tokenizer.TOK_DECL_CLOSE:
	case Tokenizer.TOK_OPEN_BRACKET:
	  fatal("PE_DECL_NESTING");
	}
      }
    }
    catch (EndOfPrologException e) {
      fatal("SYNTAX_ERROR");
    }
    catch (PrologSyntaxException e) {
      fatal("SYNTAX_ERROR");
    }
    catch (EmptyTokenException e) { }
    if (pp.getGroupLevel() != groupLevel)
      fatal("PE_GROUP_NESTING");
  }


  private Parser makeParserForEntity(Entity entity, String name) throws IOException {
    entity.noteReferenced();
    if (entity.open)
      fatal("RECURSION");
    if (entity.notationName != null)
      fatal("UNPARSED_REF");
    if (entity.text != null)
      return new Parser(entity.text, name, this);

    OpenEntity openEntity
      = entityManager.open(new ExternalId(entity.systemId, entity.publicId, entity.baseUri));
    if (openEntity == null)
      return null;
    entity.encoding = openEntity.getEncoding();
    entity.uri = openEntity.getBaseUri();
    Parser p = new Parser(openEntity, this);
    p.skipTextDecl();
    return p;
  }


  /*
   * Make the replacement text for an entity out of the literal in the
   * current token.
   */
  private void makeReplacementText() throws IOException {
    valueBuf.clear();
    Token t = new Token();
    int start = currentTokenStart + 1;
    final int end = bufStart - 1;
    try {
      for (;;) {
	int tok;
	int nextStart;
	try {
	  tok = Tokenizer.tokenizeEntityValue(buf, start, end, t);
	  nextStart = t.getTokenEnd();
	}
	catch (ExtensibleTokenException e) {
	  tok = e.getTokenType();
	  nextStart = end;
	}
	handleEntityValueToken(valueBuf, tok, start, nextStart, t);
	start = nextStart;
      }
    }
    catch (PartialTokenException e) {
      currentTokenStart = end;
      fatal("NOT_WELL_FORMED");
    }
    catch (InvalidTokenException e) {
      currentTokenStart = e.getOffset();
      reportInvalidToken(e);
    }
    catch (EmptyTokenException e) { }
  }

  private void parseEntityValue(ReplacementTextBuffer value) throws IOException {
    final Token t = new Token();
    for (;;) {
      int tok;
      for (;;) {
	try {
	  tok = Tokenizer.tokenizeEntityValue(buf, bufStart, bufEnd, t);
	  currentTokenStart = bufStart;
	  bufStart = t.getTokenEnd();
	  break;
	}
	catch (EmptyTokenException e) {
	  if (!fill())
	    return;
	}
	catch (PartialTokenException e) {
	  if (!fill()) {
	    currentTokenStart = bufStart;
	    bufStart = bufEnd;
	    fatal("UNCLOSED_TOKEN");
	  }
	}
	catch (ExtensibleTokenException e) {
	  if (!fill()) {
	    currentTokenStart = bufStart;
	    bufStart = bufEnd;
	    tok = e.getTokenType();
	    break;
	  }
	}
	catch (InvalidTokenException e) {
	  currentTokenStart = e.getOffset();
	  reportInvalidToken(e);
	}
      }
      handleEntityValueToken(value, tok, currentTokenStart, bufStart, t);
    }
  }

  private void handleEntityValueToken(ReplacementTextBuffer value,
				      int tok, int start, int end, Token t) throws IOException {
    switch (tok) {
    case Tokenizer.TOK_DATA_NEWLINE:
      if (!isInternal) {
	value.append('\n');
	break;
      }
      // fall through
    case Tokenizer.TOK_DATA_CHARS:
    case Tokenizer.TOK_ENTITY_REF:
    case Tokenizer.TOK_MAGIC_ENTITY_REF:
      value.append(buf, start, end);
      break;
    case Tokenizer.TOK_CHAR_REF:
      {
	char c = t.getRefChar();
	if (c == '&' || c == '%')
	  value.setMustReparse();
	value.append(t.getRefChar());
      }
      break;
    case Tokenizer.TOK_CHAR_PAIR_REF:
      value.appendRefCharPair(t);
      break;
    case Tokenizer.TOK_PARAM_ENTITY_REF:
      String name = new String(buf, start + 1, end - start - 2);
      Entity entity = db.lookupParamEntity(name);
      if (entity == null) {
	fatal("UNDEF_PEREF", name);
	break;
      }
      if (entity.text != null && !entity.mustReparse) {
	entity.noteReferenced();
	value.appendReplacementText(entity);
      }
      else {
	Parser parser = makeParserForEntity(entity, name);
	if (parser != null) {
	  entity.open = true;
	  parser.parseEntityValue(value);
	  entity.open = false;
	}
      }
      break;
    default:
      throw new Error("replacement text botch");
    }
  }

  private void skipTextDecl() throws IOException {
    try {
      if (tokenizeProlog() != Tokenizer.TOK_XML_DECL) {
	currentTokenStart = bufStart = 0;
	return;
      }
      try {
	new TextDecl(buf, currentTokenStart, bufStart);
      }
      catch (InvalidTokenException e) {
	currentTokenStart = e.getOffset();
	fatal("INVALID_TEXT_DECL");
      }
    }
    catch (EmptyTokenException e) { }
    catch (EndOfPrologException e) { }
  }

  private final int tokenizeProlog()
       throws IOException, EmptyTokenException, EndOfPrologException {
    for (;;) {
      try {
	int tok = Tokenizer.tokenizeProlog(buf, bufStart, bufEnd, this);
	currentTokenStart = bufStart;
	bufStart = getTokenEnd();
	return tok;
      }
      catch (EmptyTokenException e) {
	if (!fill())
	  throw e;
      }
      catch (PartialTokenException e) {
	if (!fill()) {
	  currentTokenStart = bufStart;
	  bufStart = bufEnd;
	  fatal("UNCLOSED_TOKEN");
	}
      }
      catch (ExtensibleTokenException e) {
	if (!fill()) {
	  currentTokenStart = bufStart;
	  bufStart = bufEnd;
	  return e.getTokenType();
	}
      }
      catch (InvalidTokenException e) {
	bufStart = currentTokenStart = e.getOffset();
	reportInvalidToken(e);
      }
    }
  }

  private final void skipIgnoreSect() throws IOException {
    for (;;) {
      try {
	int sectStart = bufStart;
	bufStart = Tokenizer.skipIgnoreSect(buf, bufStart, bufEnd);
	addAtom(new Atom(Tokenizer.TOK_COND_SECT_CLOSE,
			 bufferString(sectStart, bufStart)));
	return;
      }
      catch (PartialTokenException e) {
	if (!fill()) {
	  currentTokenStart = bufStart;
	  fatal("UNCLOSED_CONDITIONAL_SECTION");
	}
      }
      catch (InvalidTokenException e) {
	currentTokenStart = e.getOffset();
	fatal("IGNORE_SECT_CHAR");
      }
    }
  }

  private Vector tokenizeOverriddenEntity(char[] text) {
    Vector v = new Vector();
    int level = 0;
    try {
      Token t = new Token();
      int start = 0;
      for (;;) {
	int tok;
	int tokenEnd;
	try {
	  tok = Tokenizer.tokenizeProlog(text, start, text.length, t);
	  tokenEnd = t.getTokenEnd();
	}
	catch (ExtensibleTokenException e) {
	  tok = e.getTokenType();
	  tokenEnd = text.length;
	}
	switch (tok) {
	case Tokenizer.TOK_COND_SECT_OPEN:
	case Tokenizer.TOK_OPEN_PAREN:
	case Tokenizer.TOK_OPEN_BRACKET:
	case Tokenizer.TOK_DECL_OPEN:
	  level++;
	  break;
	case Tokenizer.TOK_CLOSE_PAREN:
	case Tokenizer.TOK_CLOSE_PAREN_ASTERISK:
	case Tokenizer.TOK_CLOSE_PAREN_QUESTION:
	case Tokenizer.TOK_CLOSE_PAREN_PLUS:
	case Tokenizer.TOK_CLOSE_BRACKET:
	case Tokenizer.TOK_DECL_CLOSE:
	  if (--level < 0)
	    return null;
	  break;
	case Tokenizer.TOK_COND_SECT_CLOSE:
	  if ((level -= 2) < 0)
	    return null;
	  break;
	}
	v.addElement(new Atom(tok,
			      new String(text, start, tokenEnd - start)));
	
	start = tokenEnd;
      }
    }
    catch (EmptyTokenException e) {
      if (level != 0)
	return null;
      return v;
    }
    catch (EndOfPrologException e) { }
    catch (PartialTokenException e) { }
    catch (InvalidTokenException e) { }
    return null;
  }

  /* The size of the buffer is always a multiple of READSIZE.
     We do reads so that a complete read would end at the
     end of the buffer.  Unless there has been an incomplete
     read, we always read in multiples of READSIZE. */
  private boolean fill() throws IOException {
    if (in == null)
      return false;
    if (bufEnd == buf.length) {
      Tokenizer.movePosition(buf, posOff, bufStart, pos);
      /* The last read was complete. */
      int keep = bufEnd - bufStart;
      if (keep == 0)
	bufEnd = 0;
      else if (keep + READSIZE <= buf.length) {
	/*
	 * There is space in the buffer for at least READSIZE bytes.
	 * Choose bufEnd so that it is the least non-negative integer
	 * greater than or equal to <code>keep</code>, such
	 * <code>bufLength - keep</code> is a multiple of READSIZE.
	 */
	bufEnd = buf.length - (((buf.length - keep)/READSIZE) * READSIZE);
	for (int i = 0; i < keep; i++)
	  buf[bufEnd - keep + i] = buf[bufStart + i];
      }
      else {
	char newBuf[] = new char[buf.length << 1];
	bufEnd = buf.length;
	System.arraycopy(buf, bufStart, newBuf, bufEnd - keep, keep);
	buf = newBuf;
      }
      bufStart = bufEnd - keep;
      posOff = bufStart;
    }
    int nChars = in.read(buf, bufEnd, buf.length - bufEnd);
    if (nChars < 0) {
      in.close();
      in = null;
      return false;
    }
    bufEnd += nChars;
    bufEndStreamOffset += nChars;
    return true;
  }

  private void fatal(String key, String arg) throws ParseException {
    doFatal(localizer.message(key, arg));
  }
  
  private void fatal(String key) throws ParseException {
    doFatal(localizer.message(key));
  }

  private void doFatal(String message) throws ParseException {
    if (isInternal)
      parent.doFatal(message);
    if (posOff > currentTokenStart)
      throw new Error("positioning botch");
    Tokenizer.movePosition(buf, posOff, currentTokenStart, pos);
    posOff = currentTokenStart;
    throw new ParseException(localizer,
			     message,
			     location,
			     pos.getLineNumber(),
			     pos.getColumnNumber());
  }

  private void reportInvalidToken(InvalidTokenException e) throws ParseException {
    if (e.getType() == InvalidTokenException.XML_TARGET)
      fatal("XML_TARGET");
    else
      fatal("ILLEGAL_CHAR");
  }

  private void addAtom(Atom a) {
    atoms.addElement(a);
  }

  private void setLastAtomEntity(Entity e) {
    Atom a = (Atom)atoms.elementAt(atoms.size() - 1);
    atoms.setElementAt(new Atom(a.getTokenType(),
				a.getToken(),
				e),
		       atoms.size() - 1);
  }

  private final String bufferString(int start, int end) {
    return normalizeNewlines(new String(buf, start, end - start));
  }

  private final String normalizeNewlines(String str) {
    if (isInternal)
      return str;
    int i = str.indexOf('\r');
    if (i < 0)
      return str;
    StringBuffer buf = new StringBuffer();
    for (i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c == '\r') {
	buf.append('\n');
	if (i + 1 < str.length() && str.charAt(i + 1) == '\n')
	  i++;
      }
      else
	buf.append(c);
    }
    return buf.toString();
  }

  static class AttributeValueException extends Exception {
    final int offset;
    final String key;
    final String arg;

    AttributeValueException(String key, int offset) {
      this.key = key;
      this.arg = null;
      this.offset = offset;
    }

    AttributeValueException(String key, String arg, int offset) {
      this.key = key;
      this.arg = arg;
      this.offset = offset;
    }
  }

  private void normalizeAttributeValue(char[] b,
				       int start,
				       int end,
				       StringBuffer result)
    throws AttributeValueException {
    Token t = new Token();
    for (;;) {
      int tok;
      int nextStart;
      try {
	tok = Tokenizer.tokenizeAttributeValue(b, start, end, t);
	nextStart = t.getTokenEnd();
      }
      catch (PartialTokenException e) {
	throw new AttributeValueException("NOT_WELL_FORMED", end);
      }
      catch (InvalidTokenException e) {
	throw new AttributeValueException("ILLEGAL_CHAR", e.getOffset());
      }
      catch (EmptyTokenException e) {
	return;
      }
      catch (ExtensibleTokenException e) {
	tok = e.getTokenType();
	nextStart = end;
      }
      switch (tok) {
      case Tokenizer.TOK_DATA_NEWLINE:
	if (b == buf && !isInternal)
	  result.append(' ');
	else {
	  for (int i = start; i < nextStart; i++)
	    result.append(' ');
	}
	break;
      case Tokenizer.TOK_DATA_CHARS:
	result.append(b, start, nextStart - start);
	break;
      case Tokenizer.TOK_MAGIC_ENTITY_REF:
      case Tokenizer.TOK_CHAR_REF:
	result.append(t.getRefChar());
	break;
      case Tokenizer.TOK_CHAR_PAIR_REF:
	{
	  char[] pair = new char[2];
	  t.getRefCharPair(pair, 0);
	  result.append(pair);
	}
	break;
      case Tokenizer.TOK_ATTRIBUTE_VALUE_S:
	result.append(' ');
	break;
      case Tokenizer.TOK_ENTITY_REF:
	String name = new String(b, start + 1, nextStart - start - 2);
	Entity entity = db.lookupGeneralEntity(name);
	if (entity == null)
	  throw new AttributeValueException("UNDEF_REF", name, start);
	if (entity.systemId != null)
	  throw new AttributeValueException("EXTERN_REF_ATTVAL", name, start);
	try {
	  if (entity.open)
	    throw new AttributeValueException("RECURSION", start);
	  entity.open = true;
	  normalizeAttributeValue(entity.text,
				  0,
				  entity.text.length,
				  result);
	  entity.open = false;
	}
	catch (AttributeValueException e) {
	  throw new AttributeValueException(e.key, e.arg, start);
	}
	break;
      default:
	throw new Error("attribute value botch");
      }
      start = nextStart;
    }
  }

  private Atom makeAtom(int tok, int start, int end) {
    String token = null;
    if (end - start == 1) {
      switch (buf[start]) {
      case ' ':
	token = " ";
	break;
      case '\t':
	token = "\t";
	break;
      case '\n':
	token = "\n";
	break;
      case ',':
	token = ",";
	break;
      case '|':
	token = "|";
	break;
      case '(':
	token = "(";
	break;
      case ')':
	token = ")";
	break;
      }
    }
    else if (end - start == 2 && buf[start] == '\r' && buf[start + 1] == '\n')
      token = "\n";
    if (token == null)
      token = bufferString(start, end);
    Atom a = (Atom)atomTable.get(token);
    if (a == null) {
      a = new Atom(tok, token);
      atomTable.put(token, a);
    }
    return a;
  }

}
