package com.thaiopensource.xml.dtd;

import java.io.Reader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/*
TODO  Need to ensure newline normalization is done properly.
Don't unexpand entities that are not properly nested.
*/

public class Parser extends Token {
  private Parser parent;
  private Reader in;
  private char[] buf;
  private int bufStart = 0;
  private int bufEnd;
  private int currentTokenStart = 0;
  // The offset in buffer corresponding to pos.
  private int posOff = 0;
  private long bufEndStreamOffset = 0;
  private Position pos = new Position();

  private static final int READSIZE = 1024*8;
  // Some temporary buffers
  private ReplacementTextBuffer valueBuf;
  private Hashtable paramEntityTable;
  private Vector atoms = new Vector();

  static class Atom {
    private int tokenType;
    private String token;
    private EntityImpl entity;

    Atom(EntityImpl entity) {
      this.entity = entity;
      this.tokenType = -1;
      this.token = null;
    }

    Atom(int tokenType, String token) {
      this.tokenType = tokenType;
      this.token = token;
    }

    final int getTokenType() {
      return tokenType;
    }

    final String getToken() {
      return token;
    }

    final EntityImpl getEntity() {
      return entity;
    }

    void setEntity(EntityImpl entity) {
      this.entity = entity;
    }

    public int hashCode() {
      return token.hashCode();
    }

    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof Atom))
	return false;
      Atom other = (Atom)obj;
      if (this.entity != null)
	return this.entity == other.entity;
      else
	return this.tokenType == other.tokenType && this.token.equals(other.token);
    }
  }

  static class EntityImpl {
    final String name;
    EntityImpl(String name) { this.name = name; }
    char[] text;
    // Which parts of text came from references?
    EntityReference[] references;
    boolean open;
    String notationName;
    Vector atoms;
    boolean mustReparse;
    int textIndexToAtomIndex(int ti) {
      int nAtoms = atoms.size();
      int len = 0;
      int atomIndex = 0;      
      for (;;) {
	if (len == ti)
	  return atomIndex;
	if (atomIndex >= nAtoms)
	  break;
	Atom a = (Atom)atoms.elementAt(atomIndex);
	len += a.getToken().length();
	if (len > ti)
	  break;
	atomIndex++;
      }
      return -1;
    }

    void unexpandEntities() {
      if (references == null || atoms == null)
	return;
      Vector newAtoms = null;
      int nCopiedAtoms = 0;
      for (int i = 0; i < references.length; i++) {
	int start = textIndexToAtomIndex(references[i].start);
	int end = textIndexToAtomIndex(references[i].end);
	if (start >= 0 && end >= 0) {
	  if (newAtoms == null)
	    newAtoms = new Vector();
	  appendSlice(newAtoms, atoms, nCopiedAtoms, start);
	  newAtoms.addElement(new Atom(references[i].entity));
	  if (references[i].entity.atoms == null) {
	    Vector tem = new Vector();
	    references[i].entity.atoms = tem;
	    appendSlice(tem, atoms, start, end);
	    references[i].entity.unexpandEntities();
	  }
	  nCopiedAtoms = end;
	}
	else {
	  System.err.println("Warning: could not preserve reference to entity \""
			     + references[i].entity.name
			     + "\" in entity \""
			     + this.name
			     + "\"");
	}
      }
      if (newAtoms == null)
	return;
      appendSlice(newAtoms, atoms, nCopiedAtoms, atoms.size());
      atoms = newAtoms;
      references = null;
    }

    static void appendSlice(Vector to, Vector from, int start, int end) {
      for (; start < end; start++)
	to.addElement(from.elementAt(start));
    }
  }

  static class DeclState {
    EntityImpl entity;
  }

  static class EntityReference {
    EntityReference(EntityImpl entity, int start, int end) {
      this.entity = entity;
      this.start = start;
      this.end = end;
    }
    EntityImpl entity;
    int start;
    int end;
  }

  static class ReplacementTextBuffer {
    private static final int INIT_SIZE = 64;
    private char[] buf = new char[INIT_SIZE];
    private int len;
    private boolean mustReparse = false;
    private EntityReference[] refs = new EntityReference[2];
    int nRefs;

    public void clear() {
      len = 0;
      mustReparse = false;
      nRefs = 0;
    }

    public void setMustReparse() {
      mustReparse = true;
    }

    public boolean getMustReparse() {
      return mustReparse;
    }

    public void appendReplacementText(EntityImpl entity) {
      appendEntityReference(new EntityReference(entity, len, len + entity.text.length));
      append(entity.text, 0, entity.text.length);
    }

    private void appendEntityReference(EntityReference r) {
      if (nRefs == refs.length) {
	EntityReference[] tem = refs;
	refs = new EntityReference[tem.length << 1];
	System.arraycopy(tem, 0, refs, 0, tem.length);
      }
      refs[nRefs++] = r;
    }

    public EntityReference[] getReferences() {
      if (nRefs == 0)
	return null;
      EntityReference[] r = new EntityReference[nRefs];
      System.arraycopy(refs, 0, r, 0, nRefs);
      return r;
    }

    public void append(char c) {
      need(1);
      buf[len++] = c;
    }

    public void appendRefCharPair(Token t) {
      need(2);
      t.getRefCharPair(buf, len);
      len += 2;
    }

    public void append(char[] cbuf, int start, int end) {
      need(end - start);
      for (int i = start; i < end; i++)
	buf[len++] = cbuf[i];
    }

    private void need(int n) {
      if (len + n <= buf.length)
	return;
      char[] tem = buf;
      if (n > tem.length)
	buf = new char[n * 2];
      else
	buf = new char[tem.length << 1];
      System.arraycopy(tem, 0, buf, 0, tem.length);
    }

    public char[] getChars() {
      char[] text = new char[len];
      System.arraycopy(buf, 0, text, 0, len);
      return text;
    }

    public String toString() {
      return new String(buf, 0, len);
    }

    public int length() {
      return len;
    }

    public char charAt(int i) {
      if (i >= len)
	throw new IndexOutOfBoundsException();
      return buf[i];
    }

    public void chop() {
      --len;
    }
  }

  public Parser(Reader in) {
    this.in = in;
    this.parent = null;
    this.buf = new char[READSIZE * 2];
    this.valueBuf = new ReplacementTextBuffer();
    this.bufEnd = 0;
    this.paramEntityTable = new Hashtable();
  }

  private Parser(char[] buf, String entityName, boolean isParameterEntity, Parser parent) {
    // this.internalEntityName = entityName;
    // this.isParameterEntity = isParameterEntity;
    this.buf = buf;
    this.parent = parent;
    //baseURL = parent.baseURL;
    //entityManager = parent.entityManager;
    this.bufEnd = buf.length;
    this.bufEndStreamOffset = buf.length;
    this.valueBuf = parent.valueBuf;
    this.paramEntityTable = parent.paramEntityTable;
  }

  public void parse() throws IOException {
    System.err.println("Parsing");
    parseDecls(false);
    System.err.println("Unexpanding entities");
    for (Enumeration e = paramEntityTable.elements();
	 e.hasMoreElements();)
      ((EntityImpl)e.nextElement()).unexpandEntities();
    System.err.println("Dumping");
    dumpEntity("#doc", atoms);
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
    addAtom(new Atom(tok, new String(buf,
				     currentTokenStart,
				     bufStart - currentTokenStart)));
    int action = pp.action(tok, buf, currentTokenStart, bufStart);
    switch (action) {
    case PrologParser.ACTION_IGNORE_SECT:
      skipIgnoreSect();
      break;
    case PrologParser.ACTION_GENERAL_ENTITY_NAME:
      declState.entity = null;
      break;
    case PrologParser.ACTION_PARAM_ENTITY_NAME:
      {
	String name = new String(buf,
				 currentTokenStart,
				 bufStart - currentTokenStart);
	declState.entity = createParamEntity(name);
	break;
      }
    case PrologParser.ACTION_ENTITY_VALUE_WITH_PEREFS:
      if (declState.entity != null) {
	makeReplacementText();
	declState.entity.text = valueBuf.getChars();
	declState.entity.mustReparse = valueBuf.getMustReparse();
	declState.entity.references = valueBuf.getReferences();
      }
      break;
    case PrologParser.ACTION_INNER_PARAM_ENTITY_REF:
    case PrologParser.ACTION_OUTER_PARAM_ENTITY_REF:
      {
	int nameStart = currentTokenStart + 1;
	String name = new String(buf, nameStart, getNameEnd() - nameStart);
	EntityImpl entity = lookupParamEntity(name);
	if (entity == null) {
	  fatal("UNDEF_PEREF", name);
	  break;
	}
	Parser parser = makeParserForEntity(entity, name, true);
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
    }
  }

  void parseInnerParamEntity(PrologParser pp, DeclState declState) throws IOException {
    int groupLevel = pp.getGroupLevel();
    try {
      for (;;) {
	int tok = tokenizeProlog();
	prologAction(tok, pp, declState);
	if (tok == Tokenizer.TOK_DECL_CLOSE)
	  fatal("PE_DECL_NESTING");
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


  private Parser makeParserForEntity(EntityImpl entity, String name, boolean isParameter) throws IOException {
    if (entity.open)
      fatal("RECURSION");
    if (entity.notationName != null)
      fatal("UNPARSED_REF");
    if (entity.text != null)
      return new Parser(entity.text, name, isParameter, this);
    // XXX
    return null;

    //OpenEntity openEntity
    //  = entityManager.open(entity.systemId, entity.baseURL, entity.publicId);
    //if (openEntity == null)
    //  return null;
    //return new EntityParser(openEntity, entityManager, app, locale, this);
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
    case Tokenizer.TOK_DATA_NEWLINE:
      value.append('\n');
      break;
    case Tokenizer.TOK_PARAM_ENTITY_REF:
      String name = new String(buf, start + 1, end - start - 2);
      EntityImpl entity = lookupParamEntity(name);
      if (entity == null) {
	fatal("UNDEF_PEREF", name);
	break;
      }
      if (entity.text != null && !entity.mustReparse)
	value.appendReplacementText(entity);
      else {
	System.err.println("Warning: reparsed reference to entity \""
			   + name
			   + "\"");
	Parser parser = makeParserForEntity(entity, name, true);
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
			 new String(buf, sectStart, bufStart - sectStart)));
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

  private EntityImpl lookupParamEntity(String name) {
    return (EntityImpl)paramEntityTable.get(name);
  }

  private EntityImpl createParamEntity(String name) {
    EntityImpl e = (EntityImpl)paramEntityTable.get(name);
    if (e != null)
      return null;
    e = new EntityImpl(name);
    paramEntityTable.put(name, e);
    return e;
  }

  private void fatal(String s, String arg) throws IOException {
    throw new IOException(s + ": " + arg);
  }
  
  private void fatal(String s) throws IOException {
    // XXX
    throw new IOException(s);
  }

  private void reportInvalidToken(InvalidTokenException e) throws IOException {
    // XXX
    fatal("INVALID_TOKEN");
  }

  private void addAtom(Atom a) {
    atoms.addElement(a);
  }

  private void setLastAtomEntity(EntityImpl e) {
    ((Atom)atoms.elementAt(atoms.size() - 1)).setEntity(e);
  }

  private void dumpEntity(String name, Vector atoms) {
    System.out.println("<e name=\"" + name + "\">");
    dumpAtoms(atoms);
    System.out.println("</e>");
  }

  private void dumpAtoms(Vector v) {
    int n = v.size();
    for (int i = 0; i < n; i++) {
      Atom a = (Atom)v.elementAt(i);
      EntityImpl e = a.getEntity();
      if (e != null)
	dumpEntity(e.name, e.atoms);
      else if (a.getTokenType() != Tokenizer.TOK_PROLOG_S) {
	System.out.print("<t>");
	dumpString(a.getToken());
	System.out.println("</t>");
      }
    }
  }
  
  private void dumpString(String s) {
    int n = s.length();
    for (int i = 0; i < n; i++)
      switch (s.charAt(i)) {
      case '<':
	System.out.print("&lt;");
	break;
      case '>':
	System.out.print("&gt;");
	break;
      case '&':
	System.out.print("&amp;");
	break;
      default:
	System.out.print(s.charAt(i));
	break;
      }
  }

}
