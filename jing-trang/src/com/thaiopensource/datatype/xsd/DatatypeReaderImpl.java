package com.thaiopensource.datatype.xsd;

import java.util.StringTokenizer;
import java.util.Vector;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;
import com.thaiopensource.datatype.DatatypeReader;

class DatatypeReaderImpl implements DatatypeReader, DatatypeContext {

  static final String xmlURI = "http://www.w3.org/XML/1998/namespace";
  static private final String xsdns = "http://www.w3.org/2000/10/XMLSchema";
  Datatype datatype = null;
  XMLReader xr;
  Locator locator;
  PrefixMapping prefixMapping;
  DatatypeFactoryImpl factory;
  DatatypeContext context;

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

  abstract class State implements ContentHandler {
    State parent;
    boolean allowsAnnotation = true;

    // If illegal, give an error and return null.

    abstract State createChildState(String localName) throws SAXException;
    abstract void end() throws SAXException;

    void set() {
      xr.setContentHandler(this);
    }

    void setParent(State state) {
      this.parent = state;
    }

    public void setDocumentLocator(Locator loc) {
      locator = loc;
    }

    public void startElement(String namespaceURI,
			     String localName,
			     String qName,
			     Attributes atts) throws SAXException {
      if (xsdns.equals(namespaceURI)) {
	State childState;
	if (localName.equals("annotation")) {
	  if (!allowsAnnotation)
	    error("annotation_not_allowed");
	  childState = new AnnotationState();
	}
	else {
	  childState = createChildState(localName);
	  if (childState == null) {
	    giveUp();
	    return;
	  }
	}
	allowsAnnotation = false;
	childState.setParent(this);
	childState.set();
	childState.attributes(atts);
      }
      else {
	if (namespaceURI.length() == 0)
	  error("element_missing_namespace", localName);
	else
	  error("element_bad_namespace", namespaceURI, localName);
	giveUp();
      }
    }

    void setAttribute(String name, String value) throws SAXException {
      if (!name.equals("id"))
	error("illegal_attribute_ignored", name);
    }

    void setGlobalAttribute(String ns, String name, String value) throws SAXException { }

    void endAttributes() throws SAXException { }

    void attributes(Attributes atts) throws SAXException {
      int len = atts.getLength();
      for (int i = 0; i < len; i++) {
	String ns = atts.getURI(i);
        String name = atts.getLocalName(i);
        String value =  atts.getValue(i);
	if (ns.length() == 0)
	  setAttribute(name, value);
	else if (ns.equals(xsdns))
	  error("illegal_global_attribute_ignored", ns, name);
	else
	  setGlobalAttribute(ns, name, value);
      }
      endAttributes();
    }

    public void endElement(String namespaceURI,
			   String localName,
			   String qName) throws SAXException {
      parent.set();
      end();
    }

    public void startDocument() throws SAXException { }
    public void endDocument() throws SAXException { }

    public void startPrefixMapping(String prefix, String uri) {
      prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
    }

    public void endPrefixMapping(String prefix) {
      prefixMapping = prefixMapping.next;
    }
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

  }

  class AnnotationState extends State {
    AnnotationState() {
      allowsAnnotation = false;
    }
    State createChildState(String localName) throws SAXException {
      if (localName.equals("documentation"))
      	return new DocumentationState();
      if (localName.equals("appinfo"))
      	return new AppinfoState();
      error("expected_documentation_appinfo", localName);
      return null;
    }
    void end() { }
  }

  class AppinfoState extends State {
    int level = 0;
    public void characters(char[] ch, int start, int len) { }
    public void startElement(String namespaceURI,
			     String localName,
			     String qName,
			     Attributes atts) throws SAXException {
      ++level;
    }
    public void endElement(String namespaceURI,
			   String localName,
			   String qName) throws SAXException {
      if (level == 0)
	super.endElement(namespaceURI, localName, qName);
      else
	--level;
    }

    void end() { }

    void setAttribute(String name, String value) throws SAXException {
      if (!name.equals("source"))
	error("illegal_attribute_ignored", name);
    }

    void setGlobalAttribute(String ns, String name, String value) throws SAXException {
      error("illegal_global_attribute_ignored", ns, name);
    }

    State createChildState(String localName) throws SAXException {
      throw new Error("shouldn't happen");
    }
  }

  class DocumentationState extends AppinfoState {
    void setGlobalAttribute(String ns, String name, String value) throws SAXException {
      if (!ns.equals(xmlURI) || !name.equals("lang"))
	super.setGlobalAttribute(ns, name, value);
    }
  }

  abstract class DatatypeContainerState extends State {
    abstract void addDatatype(DatatypeBase dt);
  }

  class RootState extends DatatypeContainerState {
    RootState() {
      allowsAnnotation = false;
    }
    void addDatatype(DatatypeBase dt) {
      datatype = dt;
    }
    State createChildState(String localName) throws SAXException {
      if (localName.equals("simpleType"))
	return new SimpleTypeState();
      State state = lookupDeriveState(localName);
      if (state != null)
	return state;
      error("expected_simple_type_restriction_list_union", localName);
      return null;
    }
    void end() { }
  }

  class SimpleTypeState extends DatatypeContainerState {
    DatatypeBase dt;
    boolean hadChild = false;
    State createChildState(String localName) throws SAXException {
      if (hadChild)
	error("simple_type_too_many_children");
      else {
	hadChild = true;
	State state = lookupDeriveState(localName);
	if (state != null)
	  return state;
	error("expected_restriction_list_union", localName);
      }
      return null;
    }
    void addDatatype(DatatypeBase dt) {
      this.dt = dt;
    }
    void end() throws SAXException {
      if (!hadChild)
	error("simple_type_no_children");
      if (dt == null)
	dt = new ErrorDatatype();
      ((DatatypeContainerState)parent).addDatatype(dt);
    }
  }

  class ListState extends DatatypeContainerState {
    DatatypeBase itemType;
    void setAttribute(String name, String value) throws SAXException {
      if (name.equals("itemType"))
	itemType = lookupDatatype(value.trim());
      else
	super.setAttribute(name, value);
    }
    State createChildState(String localName) throws SAXException {
      if (itemType == null) {
	if (localName.equals("simpleType"))
	  return new SimpleTypeState();
	error("expected_simple_type", localName);
      }
      else {
	if (localName.equals("simpleType"))
	  error("duplicate_item");
	else
	  error("list_unexpected", localName);
      }
      return null;
    }

    void addDatatype(DatatypeBase dt) {
      itemType = dt;
    }

    void end() throws SAXException {
      if (itemType == null) {
	error("missing_item_type");
	itemType = new ErrorDatatype();
      }
      ((DatatypeContainerState)parent).addDatatype(new ListDatatype(itemType));
    }
  }

  class UnionState extends DatatypeContainerState {
    DatatypeBase type;
    void setAttribute(String name, String value) throws SAXException {
      if (name.equals("memberTypes")) {
	for (StringTokenizer e = new StringTokenizer(value);
	     e.hasMoreElements();)
	  addDatatype(lookupDatatype((String)e.nextElement()));
      }
      else
	super.setAttribute(name, value);
    }

    State createChildState(String localName) throws SAXException {
      if (localName.equals("simpleType"))
	return new SimpleTypeState();
      error("expected_simple_type", localName);
      return null;
    }

    void addDatatype(DatatypeBase dt) {
      if (type == null)
	type = dt;
      else
	type = new UnionDatatype(type, dt);
    }

    void end() throws SAXException {
      if (type == null) {
	error("missing_member_type");
	type = new ErrorDatatype();
      }
      ((DatatypeContainerState)parent).addDatatype(type);
    }
  }

  class RestrictionState extends DatatypeContainerState {
    DatatypeBase base;
    DatatypeBase restricted;
    Vector enumerations;

    void setAttribute(String name, String value) throws SAXException {
      if (name.equals("base")) {
	base = lookupDatatype(value.trim());
	restricted = base;
      }
      else
	super.setAttribute(name, value);
    }

    void addDatatype(DatatypeBase dt) {
      base = dt;
      restricted = dt;
    }

    void addEnumeration(Object value) {
      if (enumerations == null)
	enumerations = new Vector();
      enumerations.addElement(value);
    }

    State createChildState(String localName) throws SAXException {
      boolean givenError = false;
      if (base == null) {
	if (localName.equals("simpleType"))
	  return new SimpleTypeState();
	error("expected_simple_type", localName);
	givenError = true;
	base = new ErrorDatatype();
	restricted = base;
      }
      if (localName.equals("enumeration"))
	return new EnumerationState();
      if (localName.equals("pattern"))
	return new PatternState();
      if (localName.equals("minInclusive"))
	return new MinInclusiveState();
      if (localName.equals("maxInclusive"))
	return new MaxInclusiveState();
      if (localName.equals("minExclusive"))
	return new MinExclusiveState();
      if (localName.equals("maxExclusive"))
	return new MaxExclusiveState();
      if (localName.equals("length"))
	return new LengthState();
      if (localName.equals("minLength"))
	return new MinLengthState();
      if (localName.equals("maxLength"))
	return new MaxLengthState();
      if (localName.equals("whiteSpace"))
	return new WhiteSpaceState();
      if (localName.equals("scale"))
	return new ScaleState();
      if (localName.equals("precision"))
	return new PrecisionState();
      if (!givenError)
	error("expected_facet", localName);
      return null;
    }

    void end() throws SAXException {
      if (base == null) {
	error("missing_base");
	restricted = new ErrorDatatype();
      }
      if (enumerations != null) {
	Object[] values = new Object[enumerations.size()];
	enumerations.copyInto(values);
	restricted = new EnumerationRestrictDatatype(restricted, values);
      }
      ((DatatypeContainerState)parent).addDatatype(restricted);
    }
  }

  abstract class FacetBaseState extends State {
    boolean fixed = false;
    String value = null;

    State createChildState(String localName) throws SAXException {
      error("facet_children", localName);
      return null;
    }

    void setAttribute(String name, String value) throws SAXException {
      if (name.equals("fixed")) {
	value = value.trim();
	if (value.equals("true"))
	  fixed = true;
	else if (!value.equals("false"))
	  error("fixed_boolean", value);
      }
      else if (name.equals("value"))
	this.value = value;
      else
	super.setAttribute(name, value);
    }

    void endAttributes() throws SAXException {
      if (value == null)
	error("missing_value_attribute");
      else
	setRestriction((RestrictionState)parent, value);
    }

    void end() { }
    abstract void setRestriction(RestrictionState parent, String value)
      throws SAXException;
  }

  class EnumerationState extends FacetBaseState {
    void setRestriction(RestrictionState parent, String str)
      throws SAXException {
      Object value = parent.base.getValue(str, DatatypeReaderImpl.this);
      if (value == null)
	error("invalid_enumeration", str);
      else
	parent.addEnumeration(value);
    }
  }

  abstract class FacetState extends FacetBaseState {
    void setRestriction(RestrictionState parent, String value)
      throws SAXException {
	DatatypeBase d = createRestriction(parent.base,
					   parent.restricted,
					   value);
	if (d != null)
	  parent.restricted = d;
    }

    abstract RestrictDatatype createRestriction(DatatypeBase base,
						DatatypeBase cur,
						String value) throws SAXException;
  }

  class PatternState extends FacetState {
    RestrictDatatype createRestriction(DatatypeBase base,
				       DatatypeBase cur,
				       String str) throws SAXException {
      try {
	RegexEngine engine = factory.getRegexEngine();
	return new PatternRestrictDatatype(cur,
					   engine.compile(str));
      }
      catch (InvalidRegexException e) {
	error("invalid_regex", e.getMessage());
        return null;
      }
    }
  }

  abstract class LimitState extends FacetState {
    abstract RestrictDatatype createLimit(DatatypeBase cur, Object limit);
    RestrictDatatype createRestriction(DatatypeBase base,
				       DatatypeBase cur,
				       String str) throws SAXException {
      if (base.getOrderRelation() == null) {
	error("not_ordered");
	return null;
      }
      Object value = base.getValue(str,
				   DatatypeReaderImpl.this);
      if (value == null) {
	error("invalid_limit", str);
	return null;
      }
      return createLimit(cur, value);
    }
  }

  class MinExclusiveState extends LimitState {
    RestrictDatatype createLimit(DatatypeBase cur, Object limit) {
      return new MinExclusiveRestrictDatatype(cur, limit);
    }
  }
  class MaxExclusiveState extends LimitState {
    RestrictDatatype createLimit(DatatypeBase cur, Object limit) {
      return new MaxExclusiveRestrictDatatype(cur, limit);
    }
  }
  class MinInclusiveState extends LimitState {
    RestrictDatatype createLimit(DatatypeBase cur, Object limit) {
      return new MinInclusiveRestrictDatatype(cur, limit);
    }
  }
  class MaxInclusiveState extends LimitState {
    RestrictDatatype createLimit(DatatypeBase cur, Object limit) {
      return new MaxInclusiveRestrictDatatype(cur, limit);
    }
  }

  class LengthState extends FacetState {
    RestrictDatatype createLength(DatatypeBase cur, int len) {
      return new LengthRestrictDatatype(cur, len);
    }
    RestrictDatatype createRestriction(DatatypeBase base,
				       DatatypeBase cur,
				       String str) throws SAXException {
      if (base.getMeasure() == null) {
	error("no_length");
	return null;
      }
      int len = convertNonNegativeInteger(str);
      if (len < 0) {
	error("value_not_non_negative_integer");
	return null;
      }
      return createLength(cur, len);
    }
  }

  class MinLengthState extends LengthState {
    RestrictDatatype createLength(DatatypeBase cur, int len) {
      return new MinLengthRestrictDatatype(cur, len);
    }
  }

  class MaxLengthState extends LengthState {
    RestrictDatatype createLength(DatatypeBase cur, int len) {
      return new MaxLengthRestrictDatatype(cur, len);
    }
  }

  class WhiteSpaceState extends FacetState {
    RestrictDatatype createRestriction(DatatypeBase base,
				       DatatypeBase cur,
				       String str) throws SAXException {
      str = str.trim();
      int whiteSpace;
      if (str.equals("preserve"))
	whiteSpace = DatatypeBase.WHITE_SPACE_PRESERVE;
      else if (str.equals("replace"))
	whiteSpace = DatatypeBase.WHITE_SPACE_REPLACE;
      else if (str.equals("collapse"))
	whiteSpace = DatatypeBase.WHITE_SPACE_COLLAPSE;
      else {
	error("white_space_value", str);
	return null;
      }
      if (whiteSpace <= base.getWhiteSpace()) {
	if (whiteSpace != base.getWhiteSpace())
	  error("loosen_white_space");
	return null;
      }
      return new RestrictDatatype(cur, whiteSpace);
    }
  }

  class ScaleState extends FacetState {
    RestrictDatatype createRestriction(DatatypeBase base,
				       DatatypeBase cur,
				       String str) throws SAXException {
      if (!(base.getPrimitive() instanceof DecimalDatatype)) {
	error("scale_not_derived_from_decimal");
	return null;
      }
      int scale = convertNonNegativeInteger(str);
      if (scale < 0) {
	error("value_not_non_negative_integer");
	return null;
      }
      return new ScaleRestrictDatatype(cur, scale);
    }
  }

  class PrecisionState extends FacetState {
    RestrictDatatype createRestriction(DatatypeBase base,
				       DatatypeBase cur,
				       String str) throws SAXException {
      if (!(base.getPrimitive() instanceof DecimalDatatype)) {
	error("precision_not_derived_from_decimal");
	return null;
      }
      int scale = convertNonNegativeInteger(str);
      if (scale <= 0) {
	error("value_not_positive_integer");
	return null;
      }
      return new PrecisionRestrictDatatype(cur, scale);
    }
  }

  public void start(XMLReader xr) {
    this.xr = xr;
    new RootState().set();
  }

  public Datatype end() {
    return datatype;
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
    ErrorHandler eh = xr.getErrorHandler();
    if (eh != null)
      eh.error(e);
  }

  void giveUp() {
    xr.setContentHandler(new DefaultHandler());
  }
  
  DatatypeBase lookupDatatype(String qName) throws SAXException {
    int i = qName.indexOf(':');
    String prefix;
    String localName;
    if (i < 0) {
      prefix = "";
      localName = qName;
    }
    else {
      prefix = qName.substring(0, i);
      localName = qName.substring(i + 1);
    }
    String ns = getNamespaceURI(prefix);
    if (ns == null && prefix.length() != 0)
      error("undeclared_prefix", prefix);
    else {
      if (xsdns.equals(ns)) {
	DatatypeBase dt = factory.createXsdDatatype(localName);
	if (dt != null)
	  return dt;
      }
      error("unrecognized_datatype", qName);
    }
    return new ErrorDatatype();
  }

  State lookupDeriveState(String localName) {
    if (localName.equals("restriction"))
      return new RestrictionState();
    if (localName.equals("union"))
      return new UnionState();
    if (localName.equals("list"))
      return new ListState();
    return null;
  }

  DatatypeReaderImpl(DatatypeFactoryImpl factory, DatatypeContext context) {
    this.factory = factory;
    this.context = context;
  }

  public String getNamespaceURI(String prefix) {
    for (PrefixMapping p = prefixMapping; p != null; p = p.next)
      if (p.prefix.equals(prefix))
	return p.uri;
    return context.getNamespaceURI(prefix);
  }

  // Return -1 for anything that is not a nonNegativeInteger
  // Return Integer.MAX_VALUE for values that are too big

  int convertNonNegativeInteger(String str) {
    str = str.trim();
    DecimalDatatype decimal = new DecimalDatatype();
    if (!decimal.lexicallyAllows(str))
      return -1;
    // Canonicalize the value
    str = decimal.getValue(str, this).toString();
    // Reject negative and fractional numbers
    if (str.charAt(0) == '-' || str.indexOf('.') >= 0)
      return -1;
    try {
      return Integer.parseInt(str);
    }
    catch (NumberFormatException e) {
      // Map out of range integers to MAX_VALUE
      return Integer.MAX_VALUE;
    }
  }
}
