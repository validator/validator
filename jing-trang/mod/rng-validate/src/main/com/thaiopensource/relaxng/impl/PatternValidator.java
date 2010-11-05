package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.exceptions.BadAttributeValueException;
import com.thaiopensource.relaxng.exceptions.ImpossibleAttributeIgnoredException;
import com.thaiopensource.relaxng.exceptions.OnlyTextNotAllowedException;
import com.thaiopensource.relaxng.exceptions.OutOfContextElementException;
import com.thaiopensource.relaxng.exceptions.RequiredAttributesMissingException;
import com.thaiopensource.relaxng.exceptions.RequiredAttributesMissingOneOfException;
import com.thaiopensource.relaxng.exceptions.RequiredElementsMissingException;
import com.thaiopensource.relaxng.exceptions.RequiredElementsMissingOneOfException;
import com.thaiopensource.relaxng.exceptions.StringNotAllowedException;
import com.thaiopensource.relaxng.exceptions.TextNotAllowedException;
import com.thaiopensource.relaxng.exceptions.UnfinishedElementException;
import com.thaiopensource.relaxng.exceptions.UnfinishedElementOneOfException;
import com.thaiopensource.relaxng.exceptions.UnknownElementException;
import com.thaiopensource.relaxng.parse.sax.DtdContext;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.xml.util.Name;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.relaxng.datatype.Datatype;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext2;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PatternValidator extends DtdContext implements Validator, ContentHandler, DTDHandler, ValidationContext2 {
  private final ValidatorPatternBuilder builder;
  private final Pattern start;
  private final ErrorHandler eh;
  private Map recoverPatternTable;
  private PatternMemo memo;
  private boolean hadError;
  private boolean collectingCharacters;
  private final StringBuffer charBuf = new StringBuffer();
  private PrefixMapping prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);
  private Locator locator;
  private final Map datatypeErrors = new HashMap();
  private Name[] stack = null;
  private int stackLen = 0;
  private int suppressDepth = 0;

  private static final class PrefixMapping {
    private final String prefix;
    private final String namespaceURI;
    private final PrefixMapping previous;

    PrefixMapping(String prefix, String namespaceURI, PrefixMapping prev) {
      this.prefix = prefix;
      this.namespaceURI = namespaceURI;
      this.previous = prev;
    }

    PrefixMapping getPrevious() {
      return previous;
    }
  }

    /**
     * Record names of elements and attributes. This is a simplified version of
     * George Bina's ModelExtractorVisitor; cf. his original at:
     * http://code.google.com/p/jing-trang/issues/detail?id=35 While his
     * original extracts string representations of content models from the
     * current pattern, this simplified version just keeps a record of element
     * and attribute names from the current pattern.
     */
  private static class NameRecordingVisitor implements PatternVisitor,
    NameClassVisitor {

    /**
     * The string for recording element and attribute names.
     */
    private String nameRecord = null;

    /**
     * The set of attributes.
     */
    private Set<String> attributes = new TreeSet<String>();

    /**
     * The set of elements.
     */
    private Set<String> elements = new TreeSet<String>();

    /**
     * True if content model contains a choice.
     */
    private boolean visitedChoice;

    /**
     * Creates a model extractor visitor.
     */
    public NameRecordingVisitor() {
    }

    /**
     * Gets the detected attributes.
     * 
     * @return The attributes set.
     */
    public Set<String> getAttributes() {
      return attributes;
    }

    /**
     * Gets the detected elements.
     * 
     * @return The elements set.
     */
    public Set<String> getElements() {
      return elements;
    }

    /**
     * @return True if content model contains a choice.
     */
    public boolean hasChoice() {
      return visitedChoice;
    }

    /**
     * Got an empty pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitEmpty()
     */
    public void visitEmpty() {
    }

    /**
     * Got a notAllowed pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitNotAllowed()
     */
    public void visitNotAllowed() {
    }

    /**
     * Got an error pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitError()
     */
    public void visitError() {
    }

    /**
     * Got a group pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitGroup(com.thaiopensource.relaxng.impl.Pattern,
     *      com.thaiopensource.relaxng.impl.Pattern)
     */
    public void visitGroup(Pattern p1, Pattern p2) {
      if (p1 instanceof EmptyPattern && p2 instanceof EmptyPattern) {
       return;
      }
      if (p1 instanceof EmptyPattern) {
        p2.accept(this);
        return;
      }
      if (p2 instanceof EmptyPattern) {
        p1.accept(this);
        return;
      }
      p1.accept(this);
      p2.accept(this);
    }

    /**
     * Got an interleave pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitInterleave(com.thaiopensource.relaxng.impl.Pattern,
     *      com.thaiopensource.relaxng.impl.Pattern)
     */
    public void visitInterleave(Pattern p1, Pattern p2) {
      if (p1 instanceof EmptyPattern && p2 instanceof EmptyPattern) {
        return;
      }
      if (p1 instanceof EmptyPattern) {
        p2.accept(this);
        return;
      }
      if (p2 instanceof EmptyPattern) {
        p1.accept(this);
        return;
      }
      p1.accept(this);
      p2.accept(this);
    }

    /**
     * Got a choice pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitChoice(com.thaiopensource.relaxng.impl.Pattern,
     *      com.thaiopensource.relaxng.impl.Pattern)
     */
    public void visitChoice(Pattern p1, Pattern p2) {
      if (p1 instanceof EmptyPattern && p2 instanceof EmptyPattern) {
        return;
      }
      p1.accept(this);
      p2.accept(this);
      visitedChoice = true;
    }

    /**
     * Got an one or mode pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitOneOrMore(com.thaiopensource.relaxng.impl.Pattern)
     */
    public void visitOneOrMore(Pattern p) {
      if (!(p instanceof EmptyPattern)) {
        p.accept(this);
      }
    }

    /**
     * Got an element pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitElement(com.thaiopensource.relaxng.impl.NameClass,
     *      com.thaiopensource.relaxng.impl.Pattern)
     */
    public void visitElement(NameClass nc, Pattern content) {
      // just output the element name class, not the content.
      nc.accept(this);
      elements.add(nameRecord);
    }

    /**
     * Got an attribute pattern. Here it seems we get only attribute
     * wildcards patterns.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitAttribute(com.thaiopensource.relaxng.impl.NameClass,
     *      com.thaiopensource.relaxng.impl.Pattern)
     */
    public void visitAttribute(NameClass ns, Pattern value) {
      ns.accept(this);
      attributes.add(nameRecord);
    }

    /**
     * Got a data pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitData(org.relaxng.datatype.Datatype)
     */
    public void visitData(Datatype dt) {
    }

    /**
     * Got a data except pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitDataExcept(org.relaxng.datatype.Datatype,
     *      com.thaiopensource.relaxng.impl.Pattern)
     */
    public void visitDataExcept(Datatype dt, Pattern except) {
    }

    /**
     * Got a value pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitValue(org.relaxng.datatype.Datatype,
     *      java.lang.Object)
     */
    public void visitValue(Datatype dt, Object obj) {
    }

    /**
     * Got a text pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitText()
     */
    public void visitText() {
    }

    /**
     * Got a list pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.PatternVisitor#visitList(com.thaiopensource.relaxng.impl.Pattern)
     */
    public void visitList(Pattern p) {
      p.accept(this);
    }

    /**
     * Got a choice name class.
     * 
     * @see com.thaiopensource.relaxng.impl.NameClassVisitor#visitChoice(com.thaiopensource.relaxng.impl.NameClass,
     *      com.thaiopensource.relaxng.impl.NameClass)
     */
    public void visitChoice(NameClass nc1, NameClass nc2) {
      nc1.accept(this);
      nc2.accept(this);
    }

    /**
     * Got a nsName name class.
     * 
     * @see com.thaiopensource.relaxng.impl.NameClassVisitor#visitNsName(java.lang.String)
     */
    public void visitNsName(String ns) {
    }

    /**
     * Got a nsName except name class.
     * 
     * @see com.thaiopensource.relaxng.impl.NameClassVisitor#visitNsNameExcept(java.lang.String,
     *      com.thaiopensource.relaxng.impl.NameClass)
     */
    public void visitNsNameExcept(String ns, NameClass nc) {
      nc.accept(this);
    }

    /**
     * Got an anyName nameclass.
     * 
     * @see com.thaiopensource.relaxng.impl.NameClassVisitor#visitAnyName()
     */
    public void visitAnyName() {
    }

    /**
     * Got an anyName except nameclass.
     * 
     * @see com.thaiopensource.relaxng.impl.NameClassVisitor#visitAnyNameExcept(com.thaiopensource.relaxng.impl.NameClass)
     */
    public void visitAnyNameExcept(NameClass nc) {
      nc.accept(this);
    }

    /**
     * Got a name nameclass.
     * 
     * @see com.thaiopensource.relaxng.impl.NameClassVisitor#visitName(com.thaiopensource.xml.util.Name)
     */
    public void visitName(Name name) {
      nameRecord = name.getLocalName();
    }

    /**
     * Got a null pattern.
     * 
     * @see com.thaiopensource.relaxng.impl.NameClassVisitor#visitNull()
     */
    public void visitNull() {
    }

  }

  private void startCollectingCharacters() {
    if (!collectingCharacters) {
      collectingCharacters = true;
      charBuf.setLength(0);
    }
  }

  private void flushCharacters() throws SAXException {
    collectingCharacters = false;
    int len = charBuf.length();
    for (int i = 0; i < len; i++) {
      switch (charBuf.charAt(i)) {
      case ' ':
      case '\r':
      case '\t':
      case '\n':
	break;
      default:
	text();
	return;
      }
    }
  }

  public void startElement(String namespaceURI,
			   String localName,
			   String qName,
			   Attributes atts) throws SAXException {
    if (collectingCharacters)
      flushCharacters();
    if (suppressDepth > 0) {
      suppressDepth++;
    }
    
    Name name = new Name(namespaceURI, localName);
    if (!setMemo(memo.startTagOpenDeriv(name))) {
      PatternMemo next = memo.startTagOpenRecoverDeriv(name);
      if (!next.isNotAllowed()) {
        Pattern p = RequiredContent.getRequiredFrontierContent(builder, memo.getPattern());
        NameRecordingVisitor nrv = new NameRecordingVisitor();
        p.accept(nrv);
        if (nrv.hasChoice()) {
          error(new RequiredElementsMissingOneOfException(locator, name, nrv.getElements(), peek()));
        } else {
          for (String elementName : nrv.getElements()) {
            error(new RequiredElementsMissingException(locator, name, elementName, peek()));
          }
        }
      }
      else {
        next = builder.getPatternMemo(builder.makeAfter(findElement(name),
            memo.getPattern()));
        if (next.isNotAllowed()) {
          error(new UnknownElementException(locator, name, peek()));
        }
        else {
          error(new OutOfContextElementException(locator, name, peek()));
        }
        if (suppressDepth == 0) {
          suppressDepth = 1;
        }
      }
      memo = next;
    }
    int len = atts.getLength();
    for (int i = 0; i < len; i++) {
      Name attName = new Name(atts.getURI(i), atts.getLocalName(i));
      String value = atts.getValue(i);
      datatypeErrors.clear();

      if (!setMemo(memo.startAttributeDeriv(attName)))
        error(new ImpossibleAttributeIgnoredException(locator, name, peek(),
            attName));
      else if (!setMemo(memo.dataDeriv(value, this))) {
        error(new BadAttributeValueException(locator, name, peek(), attName,
            value, datatypeErrors));
        memo = memo.recoverAfter();
      }
    }
    if (!setMemo(memo.endAttributes())) {
      Pattern p = RequiredContent.getRequiredAttributes(builder, memo.getPattern());
      NameRecordingVisitor nrv = new NameRecordingVisitor();
      p.accept(nrv);
      if (nrv.hasChoice()) {
        error(new RequiredAttributesMissingOneOfException(locator, name, nrv.getAttributes(), peek()));
      } else {
        for (String attributeLocalName : nrv.getAttributes()) {
          error(new RequiredAttributesMissingException(locator, name, attributeLocalName, peek()));
        }
      }
      memo = memo.ignoreMissingAttributes();
    }
    if (memo.getPattern().getContentType() == Pattern.DATA_CONTENT_TYPE)
      startCollectingCharacters();
    push(name);
  }

  private PatternMemo fixAfter(PatternMemo p) {
    return builder.getPatternMemo(p.getPattern().applyForPattern(new ApplyAfterFunction(builder) {
      Pattern apply(Pattern p) {
        return builder.makeEmpty();
      }
    }));
  }

  public void endElement(String namespaceURI,
			 String localName,
			 String qName) throws SAXException {
    Name name = pop();
    // The tricky thing here is that the derivative that we compute may be notAllowed simply because the parent
    // is notAllowed; we don't want to give an error in this case.
    if (collectingCharacters) {
      collectingCharacters = false;
      if (!setMemo(memo.textOnly())) {
        error(new OnlyTextNotAllowedException(locator, name, peek()));
	memo = memo.recoverAfter();
	return;
      }
      final String data = charBuf.toString();
      if (!setMemo(memo.dataDeriv(data, this))) {
        PatternMemo next = memo.recoverAfter();
        datatypeErrors.clear();
        if (!memo.isNotAllowed()) {
          if (!next.isNotAllowed()
              || fixAfter(memo).dataDeriv(data, this).isNotAllowed())
            error(new StringNotAllowedException(locator, name, peek(), data, datatypeErrors));
        }
        memo = next;
      }
    }
    else if (!setMemo(memo.endTagDeriv())) {
      PatternMemo next = memo.recoverAfter();
      if (!memo.isNotAllowed()) {
        if (!next.isNotAllowed()
            || fixAfter(memo).endTagDeriv().isNotAllowed()) {
          Pattern p = RequiredContent.getRequiredContent(builder, memo.getPattern());
          NameRecordingVisitor nrv = new NameRecordingVisitor();
          p.accept(nrv);
        if (nrv.hasChoice()) {
          error(new UnfinishedElementOneOfException(locator, name, nrv.getElements(), peek()));
        } else {
          for (String elementName : nrv.getElements()) {
            error(new UnfinishedElementException(locator, name, elementName, peek()));
          }
        }
        }
        
      }
      memo = next;
    }
    if (suppressDepth > 0) {
      suppressDepth--;
    }
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    if (collectingCharacters) {
      charBuf.append(ch, start, length);
      return;
    }
    for (int i = 0; i < length; i++) {
      switch (ch[start + i]) {
      case ' ':
      case '\r':
      case '\t':
      case '\n':
	break;
      default:
	text();
	return;
      }
    }
  }

  private void text() throws SAXException {
    if (!setMemo(memo.mixedTextDeriv()))
      error(new TextNotAllowedException(locator, peek()));
  }

  public void endDocument() {
    // XXX maybe check that memo.isNullable if !hadError
  }

  public void setDocumentLocator(Locator loc) {
    locator = loc;
  }

  public void startDocument() throws SAXException {
    stack = new Name[48];
    stackLen = 0;
    suppressDepth = 0;
    if (memo.isNotAllowed())
      error("schema_allows_nothing");
  }
  public void processingInstruction(String target, String date) { }
  public void skippedEntity(String name) { }
  public void ignorableWhitespace(char[] ch, int start, int len) { }
  public void startPrefixMapping(String prefix, String uri) {
    prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
  }
  public void endPrefixMapping(String prefix) {
    prefixMapping = prefixMapping.getPrevious();
  }

  public PatternValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
    this.start = pattern;
    this.builder = builder;
    this.eh = eh;
    reset();
  }

  public void reset() {
    hadError = false;
    collectingCharacters = false;
    locator = null;
    memo = builder.getPatternMemo(start);
    prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);
    clearDtdContext();
    charBuf.setLength(0);
    datatypeErrors.clear();
    stack = null;
    recoverPatternTable = null;
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return this;
  }

  private void error(String key) throws SAXException {
    if ((suppressDepth > 0) || (hadError && memo.isNotAllowed()))
      return;
    hadError = true;
    eh.error(new SAXParseException(SchemaBuilderImpl.localizer.message(key), locator));
  }

  private void error(SAXParseException e) throws SAXException {
    if ((suppressDepth > 0) || (hadError && memo.isNotAllowed()))
      return;
    hadError = true;
    eh.error(e);
  }

  /* Return false if m is notAllowed. */
  private boolean setMemo(PatternMemo m) {
    if (m.isNotAllowed())
      return false;
    else {
      memo = m;
      return true;
    }
  }

  private Pattern findElement(Name name) {
    if (recoverPatternTable == null)
      recoverPatternTable = new HashMap();
    Pattern p = (Pattern)recoverPatternTable.get(name);
    if (p == null) {
      p = FindElementFunction.findElement(builder, name, start);
      recoverPatternTable.put(name, p);
    }
    return p;
  }

  public String resolveNamespacePrefix(String prefix) {
    PrefixMapping tem = prefixMapping;
    do {
      if (tem.prefix.equals(prefix))
        return tem.namespaceURI;
      tem = tem.previous;
    } while (tem != null);
    return null;
  }

  public String getBaseUri() {
    return null;
  }
  
  public final void addDatatypeError(String message, DatatypeException exception) {
    datatypeErrors.put(message, exception);
  }
  
  private final void push(Name name) {
      if (stackLen == stack.length) {
          Name[] newStack = new Name[stackLen + (stackLen >> 1)];
          System.arraycopy(stack, 0, newStack, 0, stackLen);
          stack = newStack;
      }
      stack[stackLen] = name;
      stackLen++;
  }
  
  private final Name pop() {
      stackLen--;
      return stack[stackLen];
  }
  
  private final Name peek() {
      if (stackLen == 0) {
          return null;
      } else {
          return stack[stackLen - 1];
      }
  }

  public Locator getLocator() {
    return locator;
  }
}
