package com.thaiopensource.relaxng;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;
import com.thaiopensource.datatype.DatatypeFactory;
import com.thaiopensource.datatype.DatatypeBuilder;
import com.thaiopensource.datatype.InvalidParamException;

public class PatternReader implements DatatypeContext {

  static final String relaxngURI = "http://relaxng.org/ns/structure/0.9";
  static final String xmlURI = "http://www.w3.org/XML/1998/namespace";

  XMLReader xr;
  XMLReaderCreator xrc;
  PatternBuilder patternBuilder;
  DatatypeFactory datatypeFactory;
  Pattern startPattern;
  Locator locator;
  PrefixMapping prefixMapping;
  boolean hadError = false;

  Hashtable patternTable;
  Hashtable nameClassTable;
  final OpenIncludes openIncludes;
  DatatypeBuilder stringDatatypeBuilder
    = new BuiltinDatatypeBuilder(new StringDatatype());
  DatatypeBuilder tokenDatatypeBuilder
    = new BuiltinDatatypeBuilder(new TokenDatatype());

  static class PrefixMapping {
    final String prefix;
    final String uri;
    final PrefixMapping next;

    PrefixMapping(String prefix, String uri, PrefixMapping next) {
      this.prefix = prefix;
      this.uri = uri;
      this.next = next;
    }
  }

  static class OpenIncludes {
    final String systemId;
    final OpenIncludes parent;

    OpenIncludes(String systemId, OpenIncludes parent) {
      this.systemId = systemId;
      this.parent = parent;
    }
  }

  abstract class State implements ContentHandler {
    State parent;
    String nsInherit;
    String ns;
    String datatypeLibrary;
    Grammar grammar;

    void set() {
      xr.setContentHandler(this);
    }

    abstract State create();
    abstract State createChildState(String localName) throws SAXException;

    public void setDocumentLocator(Locator loc) {
      locator = loc;
    }

    void setParent(State parent) {
      this.parent = parent;
      if (parent.ns != null)
	this.nsInherit = parent.ns;
      else
	this.nsInherit = parent.nsInherit;
      this.datatypeLibrary = parent.datatypeLibrary;
      this.grammar = parent.grammar;
    }

    public void startElement(String namespaceURI,
			     String localName,
			     String qName,
			     Attributes atts) throws SAXException {
      if (isPatternNamespaceURI(namespaceURI)) {
	State state = createChildState(localName);
	if (state == null) {
	  xr.setContentHandler(new Skipper(this));
	  return;
	}
	state.setParent(this);
	state.set();
	state.attributes(atts);
      }
      else
	xr.setContentHandler(new Skipper(this));
    }

    public void endElement(String namespaceURI,
			   String localName,
			   String qName) throws SAXException {
      parent.set();
      end();
    }

    void setName(String name) throws SAXException {
      error("illegal_name_attribute");
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      error("illegal_attribute_ignored", name);
    }

    void endAttributes() throws SAXException {
    }

    void attributes(Attributes atts) throws SAXException {
      int len = atts.getLength();
      for (int i = 0; i < len; i++)
	if (atts.getURI(i).length() == 0) {
	  String name = atts.getLocalName(i);
	  if (name.equals("name"))
	    // XXX check that ip is a name
	    setName(atts.getValue(i).trim());
	  else if (name.equals("ns"))
	    ns = atts.getValue(i);
	  else if (name.equals("datatypeLibrary"))
	    datatypeLibrary = atts.getValue(i);
	  else
	    setOtherAttribute(name, atts.getValue(i));
	}
      // XXX disallow attributes from relaxngURI namespace?
      endAttributes();
    }

    abstract void end() throws SAXException;

    void endChild(Pattern pattern) {
      // XXX cannot happen; throw exception
    }

    void endChild(NameClass nc) {
      // XXX cannot happen; throw exception
    }

    public void startDocument() { }
    public void endDocument() throws SAXException { }
    public void processingInstruction(String target, String date) { }
    public void skippedEntity(String name) { }
    public void ignorableWhitespace(char[] ch, int start, int len) { }

    public void characters(char[] ch, int start, int len) throws SAXException {
      for (int i = 0; i < len; i++) {
	switch(ch[start + i]) {
	case ' ':
	case '\r':
	case '\n':
	case '\t':
	  break;
	default:
	  error("illegal_characters_ignored");
	  break;
	}
      }
    }

    public void startPrefixMapping(String prefix, String uri) {
      prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
    }

    public void endPrefixMapping(String prefix) {
      prefixMapping = prefixMapping.next;
    }

  }

  class Skipper extends DefaultHandler {
    int level = 1;
    State nextState;

    Skipper(State nextState) {
      this.nextState = nextState;
    }
    
    public void startElement(String namespaceURI,
			     String localName,
			     String qName,
			     Attributes atts) throws SAXException {
      ++level;
    }

    public void endElement(String namespaceURI,
			   String localName,
			   String qName) throws SAXException {
      if (--level == 0)
	nextState.set();
    }

  }

  abstract class EmptyContentState extends State {

    State createChildState(String localName) throws SAXException {
      error("expected_empty", localName);
      return null;
    }

    abstract Pattern makePattern() throws SAXException;

    void end() throws SAXException {
      parent.endChild(makePattern());
    }
  }

  abstract class PatternContainerState extends State {
    Pattern containedPattern;

    State createChildState(String localName) throws SAXException {
      State state = (State)patternTable.get(localName);
      if (state == null) {
	error("expected_pattern", localName);
	return null;
      }
      return state.create();
    }

    Pattern combinePattern(Pattern p1, Pattern p2) {
      return patternBuilder.makeSequence(p1, p2);
    }

    Pattern wrapPattern(Pattern p) throws SAXException {
      return p;
    }

    void endChild(Pattern pattern) {
      if (containedPattern == null)
	containedPattern = pattern;
      else
	containedPattern = combinePattern(containedPattern, pattern);
    }

    void end() throws SAXException {
      if (containedPattern == null) {
	error("missing_children");
	containedPattern = patternBuilder.makeError();
      }
      sendPatternToParent(wrapPattern(containedPattern));
    }

    void sendPatternToParent(Pattern p) {
      parent.endChild(p);
    }
  }

  class GroupState extends PatternContainerState {
    State create() {
      return new GroupState();
    }
  }

  class ZeroOrMoreState extends PatternContainerState {
    State create() {
      return new ZeroOrMoreState();
    }
    Pattern wrapPattern(Pattern p) {
      return patternBuilder.makeZeroOrMore(p);
    }
  }

  class OneOrMoreState extends PatternContainerState {
    State create() {
      return new OneOrMoreState();
    }
    Pattern wrapPattern(Pattern p) {
      return patternBuilder.makeOneOrMore(p);
    }
  }

  class OptionalState extends PatternContainerState {
    State create() {
      return new OptionalState();
    }
    Pattern wrapPattern(Pattern p) {
      return patternBuilder.makeOptional(p);
    }
  }

  class ListState extends PatternContainerState {
    State create() {
      return new ListState();
    }
    Pattern wrapPattern(Pattern p) {
      return patternBuilder.makeList(p, copyLocator());
    }
  }

  class ChoiceState extends PatternContainerState {
    State create() {
      return new ChoiceState();
    }
    Pattern combinePattern(Pattern p1, Pattern p2) {
      return patternBuilder.makeChoice(p1, p2);
    }
  }

  class InterleaveState extends PatternContainerState {
    State create() {
      return new InterleaveState();
    }
    Pattern combinePattern(Pattern p1, Pattern p2) {
      return patternBuilder.makeInterleave(p1, p2);
    }
  }

  class MixedState extends PatternContainerState {
    State create() {
      return new MixedState();
    }
    Pattern wrapPattern(Pattern p) {
      return patternBuilder.makeInterleave(patternBuilder.makeText(), p);
    }
  }

  static interface NameClassRef {
    void setNameClass(NameClass nc);
  }

  class ElementState extends PatternContainerState implements NameClassRef {
    NameClass nameClass;
    String name;

    void setName(String name) throws SAXException {
      this.name = name;
    }

    public void setNameClass(NameClass nc) {
      nameClass = nc;
    }

    void endAttributes() throws SAXException {
      if (name != null)
	nameClass = expandName(name, ns != null ? ns : nsInherit);
      else
	new NameClassChildState(this, this).set();
    }

    State create() {
      return new ElementState();
    }

    Pattern wrapPattern(Pattern p) {
      return patternBuilder.makeElement(nameClass, p, copyLocator());
    }
  }

  class RootState extends PatternContainerState {
    RootState() {
    }
    
    RootState(Grammar grammar, String ns) {
      this.grammar = grammar;
      this.nsInherit = ns;
      this.datatypeLibrary = "";
    }

    State create() {
      return new RootState();
    }

    State createChildState(String localName) throws SAXException {
      if (grammar == null)
	return super.createChildState(localName);
      if (localName.equals("grammar"))
	return new MergeGrammarState();
      error("expected_grammar", localName);
      return null;
    }

    public void endDocument() throws SAXException {
      if (!hadError)
	startPattern = containedPattern;
    }

  }

  class NotAllowedState extends EmptyContentState {
    State create() {
      return new NotAllowedState();
    }

    Pattern makePattern() {
      return patternBuilder.makeNotAllowed();
    }
  }

  class EmptyState extends EmptyContentState {
    State create() {
      return new EmptyState();
    }

    Pattern makePattern() {
      return patternBuilder.makeEmptySequence();
    }
  }

  class TextState extends EmptyContentState {
    State create() {
      return new TextState();
    }

    Pattern makePattern() {
      return patternBuilder.makeText();
    }
  }

  class ValueState extends EmptyContentState {
    StringBuffer buf = new StringBuffer();
    String type;

    State create() {
      return new ValueState();
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("type"))
	type = value.trim();
      else
	super.setOtherAttribute(name, value);
    }

    public void characters(char[] ch, int start, int len) {
      buf.append(ch, start, len);
    }

    Pattern makePattern() throws SAXException {
      DatatypeBuilder dtb;
      if (type == null)
	dtb = tokenDatatypeBuilder;
      else
	dtb = getDatatypeBuilder(datatypeLibrary, type);
      Datatype dt = dtb.finish();
      Object value = dt.createValue(buf.toString(), PatternReader.this);
      if (value == null) {
	error("invalid_value", buf.toString());
	return patternBuilder.makeData(dt);
      }
      return patternBuilder.makeValue(dt, value);
    }

  }

  class DataState extends State {
    String type;
    DatatypeBuilder dtb;

    State create() {
      return new DataState();
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("param"))
	return new ParamState(dtb);
      error("expected_param", localName);
      return null;
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("type"))
	type = value.trim();
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (type == null) {
	error("missing_type_attribute");
	dtb = stringDatatypeBuilder;
      }
      else
	dtb = getDatatypeBuilder(datatypeLibrary, type);
    }

    void end() {
      parent.endChild(patternBuilder.makeData(dtb.finish()));
    }
  }

  class ParamState extends State {
    private StringBuffer buf = new StringBuffer();
    private DatatypeBuilder dtb;
    private String name;

    ParamState(DatatypeBuilder dtb) {
      this.dtb = dtb;
    }

    State create() {
      return new ParamState(null);
    }

    void setName(String name) {
      this.name = name;
    }

    void endAttributes() throws SAXException {
      if (name == null)
	error("missing_name_attribute");
    }

    State createChildState(String localName) throws SAXException {
      error("expected_empty", localName);
      return null;
    }

    public void characters(char[] ch, int start, int len) {
      buf.append(ch, start, len);
    }
    
    void end() throws SAXException {
      if (name == null)
	return;
      try {
	dtb.addParam(name, buf.toString(), PatternReader.this);
      }
      catch (InvalidParamException e) {
	error("invalid_param", e.getMessage());
      }
    }
  }

  class AttributeState extends PatternContainerState implements NameClassRef {
    NameClass nameClass;
    String name;
    boolean global = false;

    State create() {
      return new AttributeState();
    }

    void setName(String name) {
      this.name = name;
    }

    public void setNameClass(NameClass nc) {
      nameClass = nc;
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("global")) {
	value = value.trim();
	if (value.equals("true"))
	  global = true;
	else if (!value.equals("false"))
	  error("global_attribute_bad_value", value);
      }
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (name != null) {
	String nsUse;
	if (ns != null)
	  nsUse = ns;
	else if (global)
	  nsUse = nsInherit;
	else
	  nsUse = "";
	nameClass = expandName(name, nsUse);
      }
      else
	new NameClassChildState(this, this).set();
    }

    void end() throws SAXException {
      if (containedPattern == null)
	containedPattern = patternBuilder.makeText();
      super.end();
    }

    Pattern wrapPattern(Pattern p) {
      return patternBuilder.makeAttribute(nameClass, p, copyLocator());
    }
  }

  abstract class SinglePatternContainerState extends PatternContainerState {
    State createChildState(String localName) throws SAXException {
      if (containedPattern == null)
	return super.createChildState(localName);
      error("too_many_children");
      return null;
    }
  }

  class DivState extends State {
    IncludeState include;

    DivState(IncludeState include) {
      this.include = include;
    }

    State create() {
      return new DivState(null);
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("define"))
	return new DefineState(include);
      if (localName.equals("start"))
	return new StartState(include);
      if (include == null && localName.equals("include"))
	return new IncludeState();
      if (localName.equals("div"))
	return new DivState(include);
      error("expected_define", localName);
      // XXX better errors
      return null;
    }

    void end() throws SAXException {
    }
  }

  static class Item {
    Item(PatternRefPattern prp, Item next) {
      this.prp = prp;
      this.next = next;
    }
    
    PatternRefPattern prp;
    Item next;
    byte replacementStatus;
  }

  class IncludeState extends DivState {
    String href;

    private Item items;

    IncludeState() {
      super(null);
      include = this;
    }

    void add(PatternRefPattern prp) {
      items = new Item(prp, items);
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("href"))
	href = value;
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (href == null)
	error("missing_href_attribute");
    }

    void end() throws SAXException {
      if (href == null)
	return;
      Item i;
      for (i = items; i != null; i = i.next) {
	i.replacementStatus = i.prp.getReplacementStatus();
	i.prp.setReplacementStatus(PatternRefPattern.REPLACEMENT_REQUIRE);
      }
      try {
	InputSource in = makeInputSource(href);
	String systemId = in.getSystemId();
	for (OpenIncludes inc = openIncludes;
	     inc != null;
	     inc = inc.parent)
	  if (inc.systemId.equals(systemId)) {
	    error("recursive_include", systemId);
	    return;
	  }
	if (readPattern(PatternReader.this,
			in,
			grammar,
			ns == null ? nsInherit : ns) == null)
	  hadError = true;
      }
      catch (IOException e) {
	throw new SAXException(e);
      }
      for (i = items; i != null; i = i.next) {
	if (i.prp.getReplacementStatus()
	    == PatternRefPattern.REPLACEMENT_REQUIRE) {
	  if (i.prp.getName() == null)
	    error("missing_start_replacement");
	  else
	    error("missing_define_replacement", i.prp.getName());
	}
	i.prp.setReplacementStatus(i.replacementStatus);
      }
    }

  }

  class MergeGrammarState extends DivState {
    MergeGrammarState() {
      super(null);
    }

    void end() throws SAXException {
      // need a non-null pattern to avoid error
      parent.endChild(patternBuilder.makeEmptySequence());
    }
  }

  class GrammarState extends MergeGrammarState {

    void setParent(State parent) {
      super.setParent(parent);
      grammar = new Grammar(grammar);
    }

    State create() {
      return new GrammarState();
    }

    void end() throws SAXException {
      for (Enumeration enum = grammar.patternNames();
	   enum.hasMoreElements();) {
	String name = (String)enum.nextElement();
	PatternRefPattern tr = (PatternRefPattern)grammar.makePatternRef(name);
	if (tr.getPattern() == null) {
	  error("reference_to_undefined", name, tr.getRefLocator());
	  tr.setPattern(patternBuilder.makeError());
	}
      }
      Pattern start = grammar.startPatternRef().getPattern();
      if (start == null) {
	error("missing_start_element");
	start = patternBuilder.makeError();
      }
      parent.endChild(start);
    }
  }

  class RefState extends EmptyContentState {
    String name;

    State create() {
      return new RefState();
    }

    void endAttributes() throws SAXException {
      if (name == null)
	error("missing_name_attribute");
      if (grammar == null)
	error("ref_outside_grammar");
    }

    void setName(String name) throws SAXException {
      this.name = name;
    }

    Pattern makePattern() {
      return makePattern(grammar);
    }

    Pattern makePattern(Grammar g) {
      if (g != null && name != null) {
	PatternRefPattern p = g.makePatternRef(name);
	if (p.getRefLocator() == null && locator != null)
	  p.setRefLocator(new LocatorImpl(locator));
	return p;
      }
      return patternBuilder.makeError();
    } 
  }

  class ParentRefState extends RefState {
    State create() {
      return new ParentRefState();
    }

    void endAttributes() throws SAXException {
      super.endAttributes();
      if (grammar.getParent() == null)
	error("parent_ref_outside_grammar");
    }

    Pattern makePattern() {
      return makePattern(grammar == null ? null : grammar.getParent());
    }
  }

  class ExternalRefState extends EmptyContentState {
    String href;
    Pattern includedPattern;
    
    State create() {
      return new ExternalRefState();
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("href"))
	href = value;
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (href == null)
	error("missing_href_attribute");
      else {
	try {
	  InputSource in = makeInputSource(href);
	  String systemId = in.getSystemId();
	  for (OpenIncludes inc = openIncludes;
	       inc != null;
	       inc = inc.parent)
	    if (inc.systemId.equals(systemId)) {
	      error("recursive_include", systemId);
	      return;
	    }
	  includedPattern = readPattern(PatternReader.this,
					in,
					null,
					ns == null ? nsInherit : ns);
	}
	catch (IOException e) {
	  throw new SAXException(e);
	}
      }
    }

    Pattern makePattern() {
      if (includedPattern == null) {
	hadError = true;
	return patternBuilder.makeError();
      }
      return includedPattern;
    }
  }

  class DefineState extends PatternContainerState {
    String name;
    private IncludeState include;

    byte combine = PatternRefPattern.COMBINE_NONE;

    DefineState(IncludeState include) {
      this.include = include;
    }

    State create() {
      return new DefineState(null);
    }

    void setName(String name) {
      this.name = name;
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("combine")) {
	value = value.trim();
	if (value.equals("choice"))
	  combine = PatternRefPattern.COMBINE_CHOICE;
	else if (value.equals("interleave"))
	  combine = PatternRefPattern.COMBINE_INTERLEAVE;
	else
	  error("combine_attribute_bad_value", value);
      }
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (name == null)
	error("missing_name_attribute");
      else 
	checkCombine(grammar.makePatternRef(name));
    }

    void checkCombine(PatternRefPattern prp) throws SAXException {
      if (prp.getReplacementStatus() != PatternRefPattern.REPLACEMENT_KEEP)
	return;
      switch (combine) {
      case PatternRefPattern.COMBINE_NONE:
	if (prp.isCombineImplicit()) {
	  if (prp.getName() == null)
	    error("duplicate_start");
	  else
	    error("duplicate_define", prp.getName());
	}
	else
	  prp.setCombineImplicit();
	break;
      case PatternRefPattern.COMBINE_CHOICE:
      case PatternRefPattern.COMBINE_INTERLEAVE:
	if (prp.getCombineType() != PatternRefPattern.COMBINE_NONE
	    && prp.getCombineType() != combine) {
	  if (prp.getName() == null)
	    error("conflict_combine_start");
	  else
	    error("conflict_combine_define", prp.getName());
	}
	prp.setCombineType(combine);
	break;
      }
    }

    void setPattern(PatternRefPattern prp, Pattern p) {
      switch (prp.getReplacementStatus()) {
      case PatternRefPattern.REPLACEMENT_KEEP:
	if (include != null)
	  include.add(prp);
	if (prp.getPattern() == null)
	  prp.setPattern(p);
	else if (prp.getCombineType()
		 == PatternRefPattern.COMBINE_INTERLEAVE)
	  prp.setPattern(patternBuilder.makeInterleave(prp.getPattern(),
						       p));
	else
	  prp.setPattern(patternBuilder.makeChoice(prp.getPattern(),
						   p));
	break;
      case PatternRefPattern.REPLACEMENT_REQUIRE:
	prp.setReplacementStatus(PatternRefPattern.REPLACEMENT_IGNORE);
	break;
      case PatternRefPattern.REPLACEMENT_IGNORE:
	break;
      }
    }

    void sendPatternToParent(Pattern p) {
      if (name != null)
	setPattern(grammar.makePatternRef(name), p);
    }

  }

  class StartState extends DefineState {

    StartState(IncludeState include) {
      super(include);
    }

    State create() {
      return new StartState(null);
    }

    void endAttributes() throws SAXException {
      if (name != null)
	checkCombine(grammar.makePatternRef(name));
      checkCombine(grammar.startPatternRef());
    }

    void setName(String name) {
      this.name = name;
    }

    void sendPatternToParent(Pattern p) {
      if (name != null)
	setPattern(grammar.makePatternRef(name), p);
      setPattern(grammar.startPatternRef(), p);
    }
  }

  abstract class NameClassContainerState extends State {
    State createChildState(String localName) throws SAXException {
      State state = (State)nameClassTable.get(localName);
      if (state == null) {
	error("expected_name_class", localName);
	return null;
      }
      return state.create();
    }
  }

  class NameClassChildState extends NameClassContainerState {
    State prevState;
    NameClassRef nameClassRef;

    State create() {
      return null;
    }

    NameClassChildState(State prevState, NameClassRef nameClassRef) {
      this.prevState = prevState;
      this.nameClassRef = nameClassRef;
      setParent(prevState.parent);
    }

    void endChild(NameClass nameClass) {
      nameClassRef.setNameClass(nameClass);
      prevState.set();
    }

    void end() throws SAXException {
      nameClassRef.setNameClass(new ErrorNameClass());
      error("missing_name_class");
      prevState.set();
      prevState.end();
    }
  }

  abstract class NameClassBaseState extends State {

    abstract NameClass makeNameClass() throws SAXException;

    void end() throws SAXException {
      parent.endChild(makeNameClass());
    }
  }

  class NameState extends NameClassBaseState {
    StringBuffer buf = new StringBuffer();

    State createChildState(String localName) throws SAXException {
      error("expected_name", localName);
      return null;
    }

    State create() {
      return new NameState();
    }

    public void characters(char[] ch, int start, int len) {
      buf.append(ch, start, len);
    }

    NameClass makeNameClass() throws SAXException {
      return expandName(buf.toString().trim(), 
			ns != null ? ns : nsInherit);
    }
    
  }

  class AnyNameState extends NameClassBaseState {
    NameClass except = null;

    State create() {
      return new AnyNameState();
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("except")) {
	if (except != null)
	  error("multiple_except");
	return new NameClassChoiceState();
      }
      error("expected_except", localName);
      return null;
    }

    NameClass makeNameClass() {
      NameClass nc = makeNameClassNoExcept();
      if (except == null)
	return nc;
      else
	return new DifferenceNameClass(nc, except);
    }

    NameClass makeNameClassNoExcept() {
      return new AnyNameClass();
    }

    void endChild(NameClass nameClass) {
      if (except != null)
	except = new ChoiceNameClass(except, nameClass);
      else
	except = nameClass;
    }

  }

  class NsNameState extends AnyNameState {
    State create() {
      return new NsNameState();
    }

    NameClass makeNameClassNoExcept() {
      return new NsNameClass(ns != null ? ns : nsInherit);
    }
  }

  class NameClassNotState extends NameClassContainerState {
    NameClass nameClass;

    void endChild(NameClass nc) {
      nameClass = nc;
    }

    State createChildState(String localName) throws SAXException {
      if (nameClass == null)
	return super.createChildState(localName);
      error("expected_one_name_class", localName);
      return null;
    }

    State create() {
      return new NameClassNotState();
    }

    void end() throws SAXException {
      if (nameClass == null) {
	error("missing_name_class");
	parent.endChild(new ErrorNameClass());
	return;
      }
      parent.endChild(new NotNameClass(nameClass));
    }

    void endAttributes() throws SAXException {
      warning("not_deprecated");
      super.endAttributes();
    }
  }

  abstract class NameClassMultiContainerState extends NameClassContainerState {
    NameClass nameClass;

    abstract NameClass combineNameClass(NameClass nc1, NameClass nc2);

    void endChild(NameClass nc) {
      if (nameClass == null)
	nameClass = nc;
      else
	nameClass = combineNameClass(nameClass, nc);
    }

    void end() throws SAXException {
      if (nameClass == null) {
	error("missing_name_class");
	parent.endChild(new ErrorNameClass());
	return;
      }
      parent.endChild(nameClass);
    }
  }

  class NameClassChoiceState extends NameClassMultiContainerState {
    State create() {
      return new NameClassChoiceState();
    }

    NameClass combineNameClass(NameClass nc1, NameClass nc2) {
      return new ChoiceNameClass(nc1, nc2);
    }
  }

  class NameClassDifferenceState extends NameClassMultiContainerState {
    State create() {
      return new NameClassDifferenceState();
    }

    void endAttributes() throws SAXException {
      warning("difference_deprecated");
      super.endAttributes();
    }

    NameClass combineNameClass(NameClass nc1, NameClass nc2) {
      return new DifferenceNameClass(nc1, nc2);
    }
  }

  private void initPatternTable() {
    patternTable = new Hashtable();
    patternTable.put("zeroOrMore", new ZeroOrMoreState());
    patternTable.put("oneOrMore", new OneOrMoreState());
    patternTable.put("optional", new OptionalState());
    patternTable.put("list", new ListState());
    patternTable.put("choice", new ChoiceState());
    patternTable.put("interleave", new InterleaveState());
    patternTable.put("group", new GroupState());
    patternTable.put("mixed", new MixedState());
    patternTable.put("element", new ElementState());
    patternTable.put("attribute", new AttributeState());
    patternTable.put("empty", new EmptyState());
    patternTable.put("text", new TextState());
    patternTable.put("value", new ValueState());
    patternTable.put("data", new DataState());
    patternTable.put("notAllowed", new NotAllowedState());
    patternTable.put("grammar", new GrammarState());
    patternTable.put("ref", new RefState());
    patternTable.put("parentRef", new ParentRefState());
    patternTable.put("externalRef", new ExternalRefState());
  }

  private void initNameClassTable() {
    nameClassTable = new Hashtable();
    nameClassTable.put("name", new NameState());
    nameClassTable.put("anyName", new AnyNameState());
    nameClassTable.put("nsName", new NsNameState());
    nameClassTable.put("not", new NameClassNotState());
    nameClassTable.put("choice", new NameClassChoiceState());
    nameClassTable.put("difference", new NameClassDifferenceState());
  }

  Pattern getStartPattern() {
    return startPattern;
  }

  Pattern expandPattern(Pattern pattern) throws SAXException {
    if (pattern != null) {
      try {
	pattern.checkRecursion(0);
      }
      catch (SAXParseException e) {
	error(e);
      }
      pattern = pattern.expand(patternBuilder);
      try {
	pattern.checkRestrictions(Pattern.START_CONTEXT);
	return pattern;
      }
      catch (RestrictionViolationException e) {
	error(e.getMessageId(), e.getLocator());
      }
    }
    return null;
  }

  void error(String key) throws SAXException {
    error(key, locator);
  }

  void error(String key, String arg) throws SAXException {
    error(key, arg, locator);
  }

  void error(String key, String arg1, String arg2) throws SAXException {
    error(key, arg1, arg2, locator);
  }

  void error(String key, Locator loc) throws SAXException {
    error(new SAXParseException(Localizer.message(key), loc));
  }

  void error(String key, String arg, Locator loc) throws SAXException {
    error(new SAXParseException(Localizer.message(key, arg), loc));
  }

  void error(String key, String arg1, String arg2, Locator loc)
    throws SAXException {
    error(new SAXParseException(Localizer.message(key, arg1, arg2), loc));
  }

  void error(SAXParseException e) throws SAXException {
    hadError = true;
    ErrorHandler eh = xr.getErrorHandler();
    if (eh != null)
      eh.error(e);
  }

  void warning(String key) throws SAXException {
    warning(key, locator);
  }

  void warning(String key, Locator loc) throws SAXException {
    warning(new SAXParseException(Localizer.message(key), loc));
  }

  void warning(SAXParseException e) throws SAXException {
    ErrorHandler eh = xr.getErrorHandler();
    if (eh != null)
      eh.error(e);
  }

  public PatternReader(XMLReaderCreator xrc,
		       XMLReader xr,
		       PatternBuilder patternBuilder,
		       DatatypeFactory datatypeFactory) {
    this.xrc = xrc;
    this.patternBuilder = patternBuilder;
    this.xr = xr;
    this.datatypeFactory = datatypeFactory;
    openIncludes = null;
    init(null, "");
  }

  PatternReader(PatternReader parent,
		String systemId,
		XMLReader xr,
		Grammar grammar,
		String ns) {
    this.xrc = parent.xrc;
    this.patternBuilder = parent.patternBuilder;
    this.datatypeFactory = parent.datatypeFactory;
    this.xr = xr;
    this.openIncludes = new OpenIncludes(systemId, parent.openIncludes);
    init(grammar, ns);
  }

  private void init(Grammar grammar, String ns) {
    initPatternTable();
    initNameClassTable();
    prefixMapping = new PrefixMapping("xml", xmlURI, null);
    new RootState(grammar, ns).set();
  }

  SimpleNameClass expandName(String name, String ns) throws SAXException {
    int ic = name.indexOf(':');
    if (ic == -1)
      return new SimpleNameClass(ns, name);
    String prefix = name.substring(0, ic);
    String localName = name.substring(ic + 1);
    for (PrefixMapping tem = prefixMapping; tem != null; tem = tem.next)
      if (tem.prefix.equals(prefix))
	return new SimpleNameClass(tem.uri, localName);
    error("undefined_prefix", prefix);
    return new SimpleNameClass("", localName);
  }

  InputSource makeInputSource(String systemId)
    throws IOException, SAXException {
    if (locator != null) {
      String base = locator.getSystemId();
      if (base != null) {
	try {
	  systemId = new URL(new URL(base), systemId).toString();
	}
	catch (MalformedURLException e) { }
      }
    }
    EntityResolver er = xr.getEntityResolver();
    if (er != null) {
      InputSource inputSource = er.resolveEntity(null, systemId);
      if (inputSource != null)
	return inputSource;
    }
    return new InputSource(systemId);
  }

  XMLReader createXMLReader() throws SAXException {
    XMLReader ixr = xrc.createXMLReader();
    EntityResolver er = xr.getEntityResolver();
    if (er != null)
      ixr.setEntityResolver(er);
    ErrorHandler eh = xr.getErrorHandler();
    if (eh != null)
      ixr.setErrorHandler(eh);
    return ixr;
  }

  public static Pattern readPattern(XMLReaderCreator xrc,
				    XMLReader xr,
				    PatternBuilder patternBuilder,
				    DatatypeFactory datatypeFactory,
				    InputSource in) throws SAXException, IOException {
    PatternReader pr = new PatternReader(xrc, xr, patternBuilder, datatypeFactory);
    xr.parse(in);
    return pr.expandPattern(pr.getStartPattern());
  }

  static Pattern readPattern(PatternReader parent,
			     InputSource in,
			     Grammar grammar,
			     String ns) throws SAXException, IOException {
    XMLReader xr = parent.createXMLReader();
    PatternReader pr = new PatternReader(parent,
					 in.getSystemId(),
					 xr,
					 grammar,
					 ns);
    xr.parse(in);
    return pr.getStartPattern();
  }

  public String getNamespaceURI(String prefix) {
    for (PrefixMapping p = prefixMapping; p != null; p = p.next)
      if (p.prefix.equals(prefix))
	return p.uri;
    return null;
  }

  boolean isPatternNamespaceURI(String s) {
    return s.equals(relaxngURI);
  }

  DatatypeBuilder getDatatypeBuilder(String datatypeLibrary, String type) throws SAXException {
    if ("".equals(datatypeLibrary)) {
      if (type.equals("string"))
	return stringDatatypeBuilder;
      if (type.equals("token"))
	return tokenDatatypeBuilder;
      error("unrecognized_builtin_datatype", type);
    }
    else {
      DatatypeBuilder dtb
	= datatypeFactory.createDatatypeBuilder(datatypeLibrary,
						type);
      if (dtb != null)
	return dtb;
      error("unrecognized_datatype", datatypeLibrary, type);
    }
    return stringDatatypeBuilder;
  }

  Locator copyLocator() {
    if (locator == null)
      return null;
    return new LocatorImpl(locator);
  }
}
