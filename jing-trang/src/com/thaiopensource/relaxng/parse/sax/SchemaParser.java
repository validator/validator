package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.DataPatternBuilder;
import com.thaiopensource.relaxng.parse.Grammar;
import com.thaiopensource.relaxng.parse.GrammarSection;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Include;
import com.thaiopensource.relaxng.parse.IncludedGrammar;
import com.thaiopensource.relaxng.parse.Location;
import com.thaiopensource.relaxng.parse.ParsedNameClass;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.relaxng.parse.Context;
import com.thaiopensource.relaxng.parse.CommentList;
import com.thaiopensource.util.Uri;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.xml.util.Naming;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/*
Deal with comments
Deal with element annotations
*/
class SchemaParser extends DtdContext implements Context {

  static final String relaxngURIPrefix = "http://relaxng.org/ns/structure/";
  static final String relaxng10URI = relaxngURIPrefix + "1.0";
  static final String xmlURI = "http://www.w3.org/XML/1998/namespace";
  static final String xsdURI = "http://www.w3.org/2001/XMLSchema-datatypes";
  static final Localizer localizer = new Localizer(SchemaParser.class);

  String relaxngURI;
  XMLReader xr;
  ErrorHandler eh;
  SchemaBuilder schemaBuilder;
  ParsedPattern startPattern;
  Locator locator;
  PrefixMapping prefixMapping;
  XmlBaseHandler xmlBaseHandler = new XmlBaseHandler();
  private boolean inDtd = false;

  boolean hadError = false;

  Hashtable patternTable;
  Hashtable nameClassTable;

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

  static interface CommentHandler {
    void comment(String value);
  }

  abstract class State implements ContentHandler, CommentHandler {
    State parent;
    String nsInherit;
    String ns;
    String datatypeLibrary;
    Scope scope;
    Location startLocation;
    Annotations annotations;
    CommentList comments;

    void set() {
      xr.setContentHandler(this);
    }

    abstract State create();
    abstract State createChildState(String localName) throws SAXException;

    public void setDocumentLocator(Locator loc) {
      locator = loc;
      xmlBaseHandler.setLocator(loc);
    }

    void setParent(State parent) {
      this.parent = parent;
      this.nsInherit = parent.getNs();
      this.datatypeLibrary = parent.datatypeLibrary;
      this.scope = parent.scope;
      this.startLocation = makeLocation();
      if (parent.comments != null) {
        annotations = schemaBuilder.makeAnnotations(parent.comments, SchemaParser.this);
        parent.comments = null;
      }
    }

    String getNs() {
      return ns == null ? nsInherit : ns;
    }

    boolean isRelaxNGElement(String uri) throws SAXException {
      return uri.equals(relaxngURI);
    }

    public void startElement(String namespaceURI,
			     String localName,
			     String qName,
			     Attributes atts) throws SAXException {
      xmlBaseHandler.startElement();
      if (isRelaxNGElement(namespaceURI)) {
	State state = createChildState(localName);
	if (state == null) {
	  xr.setContentHandler(new Skipper(this));
	  return;
	}
	state.setParent(this);
	state.set();
	state.attributes(atts);
      }
      else {
	checkForeignElement();
	xr.setContentHandler(new Skipper(this));
      }
    }

    public void endElement(String namespaceURI,
			   String localName,
			   String qName) throws SAXException {
      xmlBaseHandler.endElement();
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

    void checkForeignElement() throws SAXException {
    }

    void attributes(Attributes atts) throws SAXException {
      int len = atts.getLength();
      for (int i = 0; i < len; i++) {
	String uri = atts.getURI(i);
	if (uri.length() == 0) {
	  String name = atts.getLocalName(i);
	  if (name.equals("name"))
	    setName(atts.getValue(i).trim());
	  else if (name.equals("ns"))
	    ns = atts.getValue(i);
	  else if (name.equals("datatypeLibrary")) {
	    datatypeLibrary = atts.getValue(i);
	    checkUri(datatypeLibrary);
	    if (!datatypeLibrary.equals("")
		&& !Uri.isAbsolute(datatypeLibrary))
	      error("relative_datatype_library");
	    if (Uri.hasFragmentId(datatypeLibrary))
	      error("fragment_identifier_datatype_library");
	    datatypeLibrary = Uri.escapeDisallowedChars(datatypeLibrary);
	  }
	  else
	    setOtherAttribute(name, atts.getValue(i));
	}
	else if (uri.equals(relaxngURI))
	  error("qualified_attribute", atts.getLocalName(i));
	else if (uri.equals(xmlURI)
		 && atts.getLocalName(i).equals("base"))
	  xmlBaseHandler.xmlBaseAttribute(atts.getValue(i));
        else {
          if (annotations == null)
            annotations = schemaBuilder.makeAnnotations(null, SchemaParser.this);
          String qName = atts.getQName(i);
          String prefix = null;
          if (qName.equals("")) {
            for (PrefixMapping p = prefixMapping; p != null; p = p.next)
              if (p.uri.equals(uri)) {
                prefix = p.prefix;
                break;
              }
          }
          else
            prefix = qName.substring(0, qName.indexOf(':'));
          annotations.addAttribute(uri, atts.getLocalName(i), prefix, atts.getValue(i), startLocation);
        }
      }
      endAttributes();
    }

    abstract void end() throws SAXException;

    void endChild(ParsedPattern pattern) {
      // XXX cannot happen; throw exception
    }

    void endChild(ParsedNameClass nc) {
      // XXX cannot happen; throw exception
    }

    public void startDocument() { }
    public void endDocument() {
      if (comments != null && startPattern != null) {
        startPattern = schemaBuilder.commentAfter(startPattern, comments);
        comments = null;
      }
    }
    public void processingInstruction(String target, String date) { }
    public void skippedEntity(String name) { }
    public void ignorableWhitespace(char[] ch, int start, int len) { }

    public void comment(String value) {
      if (comments == null)
        comments = schemaBuilder.makeCommentList();
      comments.addComment(value, makeLocation());
    }

    CommentList getComments() {
      CommentList tem = comments;
      comments = null;
      return tem;
    }

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

    boolean isPatternNamespaceURI(String s) {
      return s.equals(relaxngURI);
    }

  }

  class Skipper extends DefaultHandler implements CommentHandler {
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

    public void comment(String value) {
    }
  }

  abstract class EmptyContentState extends State {

    State createChildState(String localName) throws SAXException {
      error("expected_empty", localName);
      return null;
    }

    abstract ParsedPattern makePattern() throws SAXException;

    void end() throws SAXException {
      if (comments != null) {
        if (annotations == null)
          annotations = schemaBuilder.makeAnnotations(null, SchemaParser.this);
        annotations.addComment(comments);
        comments = null;
      }
      parent.endChild(makePattern());
    }
  }

  static private final int INIT_CHILD_ALLOC = 5;

  abstract class PatternContainerState extends State {
    ParsedPattern[] childPatterns;
    int nChildPatterns = 0;

    State createChildState(String localName) throws SAXException {
      State state = (State)patternTable.get(localName);
      if (state == null) {
	error("expected_pattern", localName);
	return null;
      }
      return state.create();
    }

    ParsedPattern buildPattern(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws SAXException {
      if (nPatterns == 1 && anno == null)
        return patterns[0];
      return schemaBuilder.makeGroup(patterns, nPatterns, loc, anno);
    }

    void endChild(ParsedPattern pattern) {
      if (childPatterns == null)
        childPatterns = new ParsedPattern[INIT_CHILD_ALLOC];
      else if (nChildPatterns >= childPatterns.length) {
        ParsedPattern[] newChildPatterns = new ParsedPattern[childPatterns.length * 2];
        System.arraycopy(childPatterns, 0, newChildPatterns, 0, childPatterns.length);
        childPatterns = newChildPatterns;
      }
      childPatterns[nChildPatterns++] = pattern;
    }

    void end() throws SAXException {
      if (nChildPatterns == 0) {
	error("missing_children");
	endChild(schemaBuilder.makeErrorPattern());
      }
      if (comments != null) {
        childPatterns[nChildPatterns - 1] = schemaBuilder.commentAfter(childPatterns[nChildPatterns - 1], comments);
        comments = null;
      }
      sendPatternToParent(buildPattern(childPatterns, nChildPatterns, startLocation, annotations));
    }

    void sendPatternToParent(ParsedPattern p) {
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

    ParsedPattern buildPattern(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws SAXException {
      return schemaBuilder.makeZeroOrMore(super.buildPattern(patterns, nPatterns, loc, null), loc, anno);
    }
  }

  class OneOrMoreState extends PatternContainerState {
    State create() {
      return new OneOrMoreState();
    }
    ParsedPattern buildPattern(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws SAXException {
      return schemaBuilder.makeOneOrMore(super.buildPattern(patterns, nPatterns, loc, null), loc, anno);
    }
  }

  class OptionalState extends PatternContainerState {
    State create() {
      return new OptionalState();
    }
    ParsedPattern buildPattern(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws SAXException {
      return schemaBuilder.makeOptional(super.buildPattern(patterns, nPatterns, loc, null), loc, anno);
    }
  }

  class ListState extends PatternContainerState {
    State create() {
      return new ListState();
    }
    ParsedPattern buildPattern(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws SAXException {
      return schemaBuilder.makeList(super.buildPattern(patterns, nPatterns, loc, null), loc, anno);
    }
  }

  class ChoiceState extends PatternContainerState {
    State create() {
      return new ChoiceState();
    }
    ParsedPattern buildPattern(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws SAXException {
      return schemaBuilder.makeChoice(patterns, nPatterns, loc, anno);
    }
  }

  class InterleaveState extends PatternContainerState {
    State create() {
      return new InterleaveState();
    }
    ParsedPattern buildPattern(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) {
      return schemaBuilder.makeInterleave(patterns, nPatterns, loc, anno);
    }
  }

  class MixedState extends PatternContainerState {
    State create() {
      return new MixedState();
    }
    ParsedPattern buildPattern(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws SAXException {
      return schemaBuilder.makeMixed(super.buildPattern(patterns, nPatterns, loc, null), loc, anno);
    }
  }

  static interface NameClassRef {
    void setNameClass(ParsedNameClass nc);
  }

  class ElementState extends PatternContainerState implements NameClassRef {
    ParsedNameClass nameClass;
    String name;

    void setName(String name) {
      this.name = name;
    }

    public void setNameClass(ParsedNameClass nc) {
      nameClass = nc;
    }

    void endAttributes() throws SAXException {
      if (name != null)
	nameClass = expandName(name, getNs());
      else
	new NameClassChildState(this, this).set();
    }

    State create() {
      return new ElementState();
    }

    ParsedPattern buildPattern(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws SAXException {
      return schemaBuilder.makeElement(nameClass, super.buildPattern(patterns, nPatterns, loc, null), loc, anno);
    }

  }

  class RootState extends PatternContainerState {
    IncludedGrammar grammar;

    RootState() {
    }

    RootState(IncludedGrammar grammar, Scope scope, String ns) {
      this.grammar = grammar;
      this.scope = scope;
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
	return new MergeGrammarState(grammar);
      error("expected_grammar", localName);
      return null;
    }

    void checkForeignElement() throws SAXException {
      error("root_bad_namespace_uri", relaxng10URI);
    }

    void endChild(ParsedPattern pattern) {
      startPattern = pattern;
    }

    boolean isRelaxNGElement(String uri) throws SAXException {
      if (!uri.startsWith(relaxngURIPrefix))
	return false;
      if (!uri.equals(relaxng10URI))
	warning("wrong_uri_version",
		relaxng10URI.substring(relaxngURIPrefix.length()),
		uri.substring(relaxngURIPrefix.length()));
      relaxngURI = uri;
      return true;
    }

  }

  class NotAllowedState extends EmptyContentState {
    State create() {
      return new NotAllowedState();
    }

    ParsedPattern makePattern() {
      return schemaBuilder.makeNotAllowed(startLocation, annotations);
    }
  }

  class EmptyState extends EmptyContentState {
    State create() {
      return new EmptyState();
    }

    ParsedPattern makePattern() {
      return schemaBuilder.makeEmpty(startLocation, annotations);
    }
  }

  class TextState extends EmptyContentState {
    State create() {
      return new TextState();
    }

    ParsedPattern makePattern() {
      return schemaBuilder.makeText(startLocation, annotations);
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
	type = checkNCName(value.trim());
      else
	super.setOtherAttribute(name, value);
    }

    public void characters(char[] ch, int start, int len) {
      buf.append(ch, start, len);
    }

    void checkForeignElement() throws SAXException {
      error("value_contains_foreign_element");
    }

    ParsedPattern makePattern() throws SAXException {
      if (type == null)
        return makePattern("", "token");
      else
        return makePattern(datatypeLibrary, type);
    }

    ParsedPattern makePattern(String datatypeLibrary, String type) {
      return schemaBuilder.makeValue(datatypeLibrary,
                                     type,
                                     buf.toString(),
                                     SchemaParser.this,
                                     getNs(),
                                     startLocation,
                                     annotations);
    }

  }

  class DataState extends State {
    String type;
    ParsedPattern except = null;
    DataPatternBuilder dpb = null;

    State create() {
      return new DataState();
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("param")) {
	if (except != null)
	  error("param_after_except");
	return new ParamState(dpb);
      }
      if (localName.equals("except")) {
	if (except != null)
	  error("multiple_except");
	return new ChoiceState();
      }
      error("expected_param_except", localName);
      return null;
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("type"))
	type = checkNCName(value.trim());
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (type == null)
	error("missing_type_attribute");
      else
	dpb = schemaBuilder.makeDataPatternBuilder(datatypeLibrary, type, startLocation);
    }

    void end() throws SAXException {
      ParsedPattern p;
      if (dpb != null) {
        if (except != null)
          p = dpb.makePattern(except, startLocation, annotations);
        else
          p = dpb.makePattern(startLocation, annotations);
      }
      else
        p = schemaBuilder.makeErrorPattern();
      // XXX need to capture comments
      parent.endChild(p);
    }

    void endChild(ParsedPattern pattern) {
      except = pattern;
    }

  }

  class ParamState extends State {
    private StringBuffer buf = new StringBuffer();
    private DataPatternBuilder dpb;
    private String name;

    ParamState(DataPatternBuilder dpb) {
      this.dpb = dpb;
    }

    State create() {
      return new ParamState(null);
    }

    void setName(String name) throws SAXException {
      this.name = checkNCName(name);
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

    void checkForeignElement() throws SAXException {
      error("param_contains_foreign_element");
    }

    void end() throws SAXException {
      if (name == null)
	return;
      if (dpb != null)
	dpb.addParam(name, buf.toString(), SchemaParser.this, getNs(), startLocation, annotations);
    }
  }

  class AttributeState extends PatternContainerState implements NameClassRef {
    ParsedNameClass nameClass;
    String name;

    State create() {
      return new AttributeState();
    }

    void setName(String name) {
      this.name = name;
    }

    public void setNameClass(ParsedNameClass nc) {
      nameClass = nc;
    }

    void endAttributes() throws SAXException {
      if (name != null) {
	String nsUse;
	if (ns != null)
	  nsUse = ns;
	else
	  nsUse = "";
	nameClass = expandName(name, nsUse);
      }
      else
	new NameClassChildState(this, this).set();
    }

    void end() throws SAXException {
      if (nChildPatterns == 0)
	endChild(schemaBuilder.makeText(startLocation, null));
      super.end();
    }

    ParsedPattern buildPattern(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws SAXException {
      return schemaBuilder.makeAttribute(nameClass, super.buildPattern(patterns, nPatterns, loc, null), loc, anno);
    }

    State createChildState(String localName) throws SAXException {
      State tem = super.createChildState(localName);
      if (tem != null && nChildPatterns != 0)
	error("attribute_multi_pattern");
      return tem;
    }

  }

  abstract class SinglePatternContainerState extends PatternContainerState {
    State createChildState(String localName) throws SAXException {
      if (nChildPatterns == 0)
	return super.createChildState(localName);
      error("too_many_children");
      return null;
    }
  }

  class DivState extends State {
    GrammarSection section;

    DivState() { }

    DivState(GrammarSection section) {
      this.section = section;
    }

    State create() {
      return new DivState(null);
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("define"))
	return new DefineState(section);
      if (localName.equals("start"))
	return new StartState(section);
      if (localName.equals("include")) {
	Include include = section.makeInclude();
	if (include != null)
	  return new IncludeState(include);
      }
      if (localName.equals("div"))
	return new DivState(section.makeDiv());
      error("expected_define", localName);
      // XXX better errors
      return null;
    }

    void end() throws SAXException {
      if (comments != null) {
        section.topLevelComment(comments);
        comments = null;
      }
    }
  }

  class IncludeState extends DivState {
    String href;
    Include include;

    IncludeState(Include include) {
      super(include);
      this.include = include;
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("href")) {
	href = value;
	checkUri(href);
      }
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (href == null)
	error("missing_href_attribute");
      else
        href = resolve(href);
    }

    void end() throws SAXException {
      super.end();
      if (href != null) {
        try {
          include.endInclude(href, getNs(), startLocation, annotations);
        }
        catch (IllegalSchemaException e) {
        }
      }
    }
  }

  class MergeGrammarState extends DivState {
    IncludedGrammar grammar;
    MergeGrammarState(IncludedGrammar grammar) {
      super(grammar);
      this.grammar = grammar;
    }

    void end() throws SAXException {
      super.end();
      parent.endChild(grammar.endIncludedGrammar(startLocation, annotations));
    }
  }

  class GrammarState extends DivState {
    Grammar grammar;

    void setParent(State parent) {
      super.setParent(parent);
      grammar = schemaBuilder.makeGrammar(scope);
      section = grammar;
      scope = grammar;
    }

    State create() {
      return new GrammarState();
    }

    void end() throws SAXException {
      super.end();
      parent.endChild(grammar.endGrammar(startLocation, annotations));
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
    }

    void setName(String name) throws SAXException {
      this.name = checkNCName(name);
    }

    ParsedPattern makePattern() {
      return makePattern(scope);
    }

    ParsedPattern makePattern(Scope scope) {
      return scope.makeRef(name, startLocation, annotations);
    }
  }

  class ParentRefState extends RefState {
    State create() {
      return new ParentRefState();
    }

    void endAttributes() throws SAXException {
      super.endAttributes();
    }

    ParsedPattern makePattern() {
      return scope.makeParentRef(name, startLocation, annotations);
    }
  }

  class ExternalRefState extends EmptyContentState {
    String href;
    ParsedPattern includedPattern;

    State create() {
      return new ExternalRefState();
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("href")) {
	href = value;
	checkUri(href);
      }
      else
	super.setOtherAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (href == null)
	error("missing_href_attribute");
      else
        href = resolve(href);
    }

    ParsedPattern makePattern() {
      if (href != null) {
        try {
          return schemaBuilder.makeExternalRef(href,
                                               getNs(),
                                               scope,
                                               startLocation,
                                               annotations);
        }
        catch (IllegalSchemaException e) { }
      }
      return schemaBuilder.makeErrorPattern();
    }
  }

  abstract class DefinitionState extends PatternContainerState {
    GrammarSection.Combine combine = null;
    GrammarSection section;

    DefinitionState(GrammarSection section) {
      this.section = section;
    }

    void setOtherAttribute(String name, String value) throws SAXException {
      if (name.equals("combine")) {
	value = value.trim();
	if (value.equals("choice"))
	  combine = GrammarSection.COMBINE_CHOICE;
	else if (value.equals("interleave"))
	  combine = GrammarSection.COMBINE_INTERLEAVE;
	else
	  error("combine_attribute_bad_value", value);
      }
      else
	super.setOtherAttribute(name, value);
    }
  }

  class DefineState extends DefinitionState {
    String name;

    DefineState(GrammarSection section) {
      super(section);
    }

    State create() {
      return new DefineState(null);
    }

    void setName(String name) throws SAXException {
      this.name = checkNCName(name);
    }

    void endAttributes() throws SAXException {
      if (name == null)
	error("missing_name_attribute");
    }

    void sendPatternToParent(ParsedPattern p) {
      if (name != null)
	section.define(name, combine, p, startLocation, annotations);
    }

  }

  class StartState extends DefinitionState {

    StartState(GrammarSection section) {
      super(section);
    }

    State create() {
      return new StartState(null);
    }

    void sendPatternToParent(ParsedPattern p) {
      section.define(GrammarSection.START, combine, p, startLocation, annotations);
    }

    State createChildState(String localName) throws SAXException {
      State tem = super.createChildState(localName);
      if (tem != null && nChildPatterns != 0)
	error("start_multi_pattern");
      return tem;
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
      this.ns = prevState.ns;
    }

    void endChild(ParsedNameClass nameClass) {
      nameClassRef.setNameClass(nameClass);
      prevState.set();
    }

    void end() throws SAXException {
      nameClassRef.setNameClass(schemaBuilder.makeErrorNameClass());
      error("missing_name_class");
      prevState.set();
      prevState.end();
    }
  }

  abstract class NameClassBaseState extends State {

    abstract ParsedNameClass makeNameClass() throws SAXException;

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

    void checkForeignElement() throws SAXException {
      error("name_contains_foreign_element");
    }

    ParsedNameClass makeNameClass() throws SAXException {
      return expandName(buf.toString().trim(), getNs());
    }

  }

  private static final int PATTERN_CONTEXT = 0;
  private static final int ANY_NAME_CONTEXT = 1;
  private static final int NS_NAME_CONTEXT = 2;

  class AnyNameState extends NameClassBaseState {
    ParsedNameClass except = null;

    State create() {
      return new AnyNameState();
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("except")) {
	if (except != null)
	  error("multiple_except");
	return new NameClassChoiceState(getContext());
      }
      error("expected_except", localName);
      return null;
    }

    int getContext() {
      return ANY_NAME_CONTEXT;
    }

    ParsedNameClass makeNameClass() {
      if (except == null)
	return makeNameClassNoExcept();
      else
	return makeNameClassExcept(except);
    }

    ParsedNameClass makeNameClassNoExcept() {
      return schemaBuilder.makeAnyName(startLocation, annotations);
    }

    ParsedNameClass makeNameClassExcept(ParsedNameClass except) {
      return schemaBuilder.makeAnyName(except, startLocation, annotations);
    }

    void endChild(ParsedNameClass nameClass) {
      except = nameClass;
    }

  }

  class NsNameState extends AnyNameState {
    State create() {
      return new NsNameState();
    }

    ParsedNameClass makeNameClassNoExcept() {
      return schemaBuilder.makeNsName(getNs(), null, null);
    }

    ParsedNameClass makeNameClassExcept(ParsedNameClass except) {
      return schemaBuilder.makeNsName(getNs(), except, null, null);
    }

    int getContext() {
      return NS_NAME_CONTEXT;
    }

  }

  class NameClassChoiceState extends NameClassContainerState {
    private ParsedNameClass[] nameClasses;
    private int nNameClasses;
    private int context;

    NameClassChoiceState() {
      this.context = PATTERN_CONTEXT;
    }

    NameClassChoiceState(int context) {
      this.context = context;
    }

    void setParent(State parent) {
      super.setParent(parent);
      if (parent instanceof NameClassChoiceState)
	this.context = ((NameClassChoiceState)parent).context;
    }

    State create() {
      return new NameClassChoiceState();
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("anyName")) {
	if (context >= ANY_NAME_CONTEXT) {
	  error(context == ANY_NAME_CONTEXT
		? "any_name_except_contains_any_name"
		: "ns_name_except_contains_any_name");
	  return null;
	}
      }
      else if (localName.equals("nsName")) {
	if (context == NS_NAME_CONTEXT) {
	  error("ns_name_except_contains_ns_name");
	  return null;
	}
      }
      return super.createChildState(localName);
    }

    void endChild(ParsedNameClass nc) {
      if (nameClasses == null)
        nameClasses = new ParsedNameClass[INIT_CHILD_ALLOC];
      else if (nNameClasses >= nameClasses.length) {
        ParsedNameClass[] newNameClasses = new ParsedNameClass[nameClasses.length * 2];
        System.arraycopy(nameClasses, 0, newNameClasses, 0, nameClasses.length);
        nameClasses = newNameClasses;
      }
      nameClasses[nNameClasses++] = nc;
    }

    void end() throws SAXException {
      if (nNameClasses == 0) {
	error("missing_name_class");
	parent.endChild(schemaBuilder.makeErrorNameClass());
	return;
      }
      if (comments != null) {
        nameClasses[nNameClasses - 1] = schemaBuilder.commentAfter(nameClasses[nNameClasses - 1], comments);
        comments = null;
      }
      parent.endChild(schemaBuilder.makeChoice(nameClasses, nNameClasses, startLocation, annotations));
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
    nameClassTable.put("choice", new NameClassChoiceState());
  }

  ParsedPattern getStartPattern() throws IllegalSchemaException {
    if (hadError)
      throw new IllegalSchemaException();
    return startPattern;
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
    error(new SAXParseException(localizer.message(key), loc));
  }

  void error(String key, String arg, Locator loc) throws SAXException {
    error(new SAXParseException(localizer.message(key, arg), loc));
  }

  void error(String key, String arg1, String arg2, Locator loc)
    throws SAXException {
    error(new SAXParseException(localizer.message(key, arg1, arg2), loc));
  }

  void error(SAXParseException e) throws SAXException {
    hadError = true;
    if (eh != null)
      eh.error(e);
  }

  void warning(String key) throws SAXException {
    warning(key, locator);
  }

  void warning(String key, String arg) throws SAXException {
    warning(key, arg, locator);
  }

  void warning(String key, String arg1, String arg2) throws SAXException {
    warning(key, arg1, arg2, locator);
  }

  void warning(String key, Locator loc) throws SAXException {
    warning(new SAXParseException(localizer.message(key), loc));
  }

  void warning(String key, String arg, Locator loc) throws SAXException {
    warning(new SAXParseException(localizer.message(key, arg), loc));
  }

  void warning(String key, String arg1, String arg2, Locator loc)
    throws SAXException {
    warning(new SAXParseException(localizer.message(key, arg1, arg2), loc));
  }

  void warning(SAXParseException e) throws SAXException {
    if (eh != null)
      eh.warning(e);
  }

  SchemaParser(XMLReader xr,
               ErrorHandler eh,
               SchemaBuilder schemaBuilder,
               IncludedGrammar grammar,
               Scope scope) throws SAXException {
    this.xr = xr;
    this.eh = eh;
    this.schemaBuilder = schemaBuilder;
    if (eh != null)
      xr.setErrorHandler(eh);
    xr.setDTDHandler(this);
    if (schemaBuilder.usesComments()) {
      try {
        xr.setProperty("http://xml.org/sax/properties/lexical-handler", new LexicalHandlerImpl());
      }
      catch (SAXNotRecognizedException e) {
        warning("no_comment_support", xr.getClass().getName());
      }
      catch (SAXNotSupportedException e) {
        warning("no_comment_support", xr.getClass().getName());
      }
    }
    initPatternTable();
    initNameClassTable();
    prefixMapping = new PrefixMapping("xml", xmlURI, null);
    new RootState(grammar, scope, SchemaBuilder.INHERIT_NS).set();
  }

  public void startDTD(String s, String s1, String s2) throws SAXException {
    inDtd = true;
  }

  public void endDTD() throws SAXException {
    inDtd = false;
  }


  public String resolveNamespacePrefix(String prefix) {
    for (PrefixMapping p = prefixMapping; p != null; p = p.next)
      if (p.prefix.equals(prefix))
        return p.uri;
    return null;
  }

  public Enumeration prefixes() {
    Vector v = new Vector();
    for (PrefixMapping p = prefixMapping; p != null; p = p.next) {
      if (!v.contains(p.prefix))
        v.addElement(p.prefix);
    }
    return v.elements();
  }

  public String getBaseUri() {
    return xmlBaseHandler.getBaseUri();
  }

  class LexicalHandlerImpl extends AbstractLexicalHandler {
    public void comment(char[] chars, int start, int length) throws SAXException {
      if (!inDtd) {
        if (length > 0 && (chars[start] == ' ' || chars[start] == '\n')) {
          length--;
          start++;
        }
        while (length > 0) {
          char c = chars[start + length - 1];
          if (c != ' ' && c != '\n')
            break;
          length--;
        }
        ((CommentHandler)xr.getContentHandler()).comment(new String(chars, start, length));
      }
    }
  }

  ParsedNameClass expandName(String name, String ns) throws SAXException {
    int ic = name.indexOf(':');
    if (ic == -1)
      return schemaBuilder.makeName(ns, checkNCName(name), null, null, null);
    String prefix = checkNCName(name.substring(0, ic));
    String localName = checkNCName(name.substring(ic + 1));
    for (PrefixMapping tem = prefixMapping; tem != null; tem = tem.next)
      if (tem.prefix.equals(prefix))
	return schemaBuilder.makeName(tem.uri, localName, prefix, null, null);
    error("undefined_prefix", prefix);
    return schemaBuilder.makeName("", localName, null, null, null);
  }

  String checkNCName(String str) throws SAXException {
    if (!Naming.isNcname(str))
      error("invalid_ncname", str);
    return str;
  }

  String resolve(String systemId) throws SAXException {
    if (Uri.hasFragmentId(systemId))
      error("href_fragment_id");
    systemId = Uri.escapeDisallowedChars(systemId);
    return Uri.resolve(xmlBaseHandler.getBaseUri(), systemId);
  }

  Location makeLocation() {
    if (locator == null)
      return null;
    return schemaBuilder.makeLocation(locator.getSystemId(),
				      locator.getLineNumber(),
				      locator.getColumnNumber());
  }

  void checkUri(String s) throws SAXException {
    if (!Uri.isValid(s))
      error("invalid_uri", s);
  }
}
