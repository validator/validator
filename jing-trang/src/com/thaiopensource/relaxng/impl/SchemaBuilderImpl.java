package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.DataPatternBuilder;
import com.thaiopensource.relaxng.parse.Div;
import com.thaiopensource.relaxng.parse.ElementAnnotation;
import com.thaiopensource.relaxng.parse.Grammar;
import com.thaiopensource.relaxng.parse.GrammarSection;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Include;
import com.thaiopensource.relaxng.parse.IncludedGrammar;
import com.thaiopensource.relaxng.parse.Location;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.ParsedNameClass;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.DatatypeBuilder;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.Enumeration;
import java.util.Hashtable;

class SchemaBuilderImpl implements SchemaBuilder, Annotations {
  private final SchemaBuilderImpl parent;
  private boolean hadError = false;
  private final Parseable parseable;
  private final SchemaPatternBuilder pb;
  private final DatatypeLibraryFactory datatypeLibraryFactory;
  private final String inheritNs;
  private final ErrorHandler eh;
  private final OpenIncludes openIncludes;

  static class OpenIncludes {
    final String uri;
    final OpenIncludes parent;

    OpenIncludes(String uri, OpenIncludes parent) {
      this.uri = uri;
      this.parent = parent;
    }
  }

  static public Pattern parse(Parseable parseable,
                              ErrorHandler eh,
                              DatatypeLibraryFactory datatypeLibraryFactory,
                              SchemaPatternBuilder pb) throws IllegalSchemaException, BuildException {
    SchemaBuilderImpl sb = new SchemaBuilderImpl(parseable,
                                                 eh,
                                                 datatypeLibraryFactory,
                                                 pb);
    return sb.expandPattern((Pattern)parseable.parse(sb));
  }

  private Pattern expandPattern(Pattern pattern) throws IllegalSchemaException, BuildException {
    if (!hadError) {
      try {
        pattern.checkRecursion(0);
        pattern = pattern.expand(pb);
        pattern.checkRestrictions(Pattern.START_CONTEXT, null, null);
        if (!hadError)
          return pattern;
      }
      catch (SAXParseException e) {
        error(e);
      }
      catch (SAXException e) {
        throw new BuildException(e);
      }
      catch (RestrictionViolationException e) {
        error(e.getMessageId(), e.getLocator());
      }
    }
    throw new IllegalSchemaException();
  }

  private SchemaBuilderImpl(Parseable parseable,
                            ErrorHandler eh,
                            DatatypeLibraryFactory datatypeLibraryFactory,
                            SchemaPatternBuilder pb) {
    this.parent = null;
    this.parseable = parseable;
    this.eh = eh;
    this.datatypeLibraryFactory = datatypeLibraryFactory;
    this.pb = pb;
    this.inheritNs = "";
    this.openIncludes = null;
  }

  private SchemaBuilderImpl(String inheritNs,
                            String uri,
                            SchemaBuilderImpl parent) {
    this.parent = parent;
    this.parseable = parent.parseable;
    this.eh = parent.eh;
    this.datatypeLibraryFactory = parent.datatypeLibraryFactory;
    this.pb = parent.pb;
    this.inheritNs = inheritNs;
    this.openIncludes = new OpenIncludes(uri, parent.openIncludes);
  }

  public ParsedPattern makeChoice(ParsedPattern p1, ParsedPattern p2, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeChoice((Pattern)p1, (Pattern)p2);
  }

  public ParsedPattern makeInterleave(ParsedPattern p1, ParsedPattern p2, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeInterleave((Pattern)p1, (Pattern)p2);
  }

  public ParsedPattern makeGroup(ParsedPattern p1, ParsedPattern p2, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeGroup((Pattern)p1, (Pattern)p2);
  }

  public ParsedPattern makeOneOrMore(ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeOneOrMore((Pattern)p);
  }

  public ParsedPattern makeZeroOrMore(ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeZeroOrMore((Pattern)p);
  }

  public ParsedPattern makeOptional(ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeOptional((Pattern)p);
  }

  public ParsedPattern makeList(ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeList((Pattern)p, (Locator)loc);
  }

  public ParsedPattern makeMixed(ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeMixed((Pattern)p);
  }

  public ParsedPattern makeEmpty(Location loc, Annotations anno) {
    return pb.makeEmpty();
  }

  public ParsedPattern makeNotAllowed(Location loc, Annotations anno) {
    return pb.makeNotAllowed();
  }

  public ParsedPattern makeText(Location loc, Annotations anno) {
    return pb.makeText();
  }

  public ParsedPattern makeAttribute(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeAttribute((NameClass)nc, (Pattern)p, (Locator)loc);
  }

  public ParsedPattern makeElement(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno)
          throws BuildException {
    return pb.makeElement((NameClass)nc, (Pattern)p, (Locator)loc);
  }

  private class DummyDataPatternBuilder implements DataPatternBuilder {
    public void addParam(String name, String value, ValidationContext vc, Location loc, Annotations anno)
            throws BuildException {
    }

    public ParsedPattern makePattern(Location loc, Annotations anno)
            throws BuildException {
      return pb.makeError();
    }

    public ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
            throws BuildException {
      return pb.makeError();
    }
  }

  private class DataPatternBuilderImpl implements DataPatternBuilder {
    private DatatypeBuilder dtb;
    DataPatternBuilderImpl(DatatypeBuilder dtb) {
      this.dtb = dtb;
    }
    public void addParam(String name, String value, ValidationContext vc, Location loc, Annotations anno)
            throws BuildException {
      try {
        dtb.addParameter(name, value, vc);
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
	if (detail != null)
	  error("invalid_param_detail", detail, (Locator)loc);
	else
	  error("invalid_param", (Locator)loc);
      }
    }

    public ParsedPattern makePattern(Location loc, Annotations anno)
            throws BuildException {
      try {
        return pb.makeData(dtb.createDatatype());
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
	if (detail != null)
	  error("invalid_params_detail", detail, (Locator)loc);
	else
	  error("invalid_params", (Locator)loc);
        return pb.makeError();
      }
    }

    public ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
            throws BuildException {
      try {
        return pb.makeDataExcept(dtb.createDatatype(), (Pattern)except, (Locator)loc);
      }
      catch (DatatypeException e) {
	String detail = e.getMessage();
	if (detail != null)
	  error("invalid_params_detail", detail, (Locator)loc);
	else
	  error("invalid_params", (Locator)loc);
        return pb.makeError();
      }
    }
  }

  public DataPatternBuilder makeDataPatternBuilder(String datatypeLibrary, String type, Location loc)
          throws BuildException {
    DatatypeLibrary dl = datatypeLibraryFactory.createDatatypeLibrary(datatypeLibrary);
    if (dl == null)
      error("unrecognized_datatype_library", datatypeLibrary, (Locator)loc);
    else {
      try {
        return new DataPatternBuilderImpl(dl.createDatatypeBuilder(type));
      }
      catch (DatatypeException e) {
        error("unrecognized_datatype", datatypeLibrary, type, (Locator)loc);
      }
    }
    return new DummyDataPatternBuilder();
  }

  public ParsedPattern makeValue(String datatypeLibrary, String type, String value, ValidationContext vc,
                                 Location loc, Annotations anno) throws BuildException {
    DatatypeLibrary dl = datatypeLibraryFactory.createDatatypeLibrary(datatypeLibrary);
    if (dl == null)
      error("unrecognized_datatype_library", datatypeLibrary, (Locator)loc);
    else {
      try {
        DatatypeBuilder dtb = dl.createDatatypeBuilder(type);
        try {
          Datatype dt = dtb.createDatatype();
          Object obj = dt.createValue(value, vc);
          if (obj != null)
            return pb.makeValue(dt, obj);
          error("invalid_value", value, (Locator)loc);
        }
        catch (DatatypeException e) {
          String detail = e.getMessage();
          if (detail != null)
            error("datatype_requires_param_detail", detail, (Locator)loc);
          else
            error("datatype_requires_param", (Locator)loc);
        }
      }
      catch (DatatypeException e) {
        error("unrecognized_datatype", datatypeLibrary, type, (Locator)loc);
      }
    }
    return pb.makeError();
  }

  class GrammarImpl implements Grammar, Div, IncludedGrammar {
    private final Hashtable defines;
    private final RefPattern startRef;
    private final Scope parent;

    private GrammarImpl(Scope parent) {
      this.parent = parent;
      this.defines = new Hashtable();
      this.startRef = new RefPattern(null);
    }

    protected GrammarImpl(GrammarImpl g) {
      parent = g.parent;
      startRef = g.startRef;
      defines = g.defines;
    }

    public ParsedPattern endGrammar(Location loc, Annotations anno) throws BuildException {
      for (Enumeration enum = defines.keys();
           enum.hasMoreElements();) {
        String name = (String)enum.nextElement();
        RefPattern rp = (RefPattern)defines.get(name);
        if (rp.getPattern() == null) {
          error("reference_to_undefined", name, rp.getRefLocator());
          rp.setPattern(pb.makeError());
        }
      }
      Pattern start = startRef.getPattern();
      if (start == null) {
        error("missing_start_element", (Locator)loc);
        start = pb.makeError();
      }
      return start;
    }

    public void endDiv(Location loc, Annotations anno) throws BuildException {
      // nothing to do
    }

    public void endIncludedGrammar(Location loc, Annotations anno) throws BuildException {
      // nothing to do
    }

    public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
            throws BuildException {
      define(lookup(name), combine, pattern, loc);
    }

    private void define(RefPattern rp, GrammarSection.Combine combine, ParsedPattern pattern, Location loc)
            throws BuildException {
      switch (rp.getReplacementStatus()) {
      case RefPattern.REPLACEMENT_KEEP:
        if (combine == null) {
          if (rp.isCombineImplicit()) {
            if (rp.getName() == null)
              error("duplicate_start", (Locator)loc);
            else
              error("duplicate_define", rp.getName(), (Locator)loc);
          }
          else
            rp.setCombineImplicit();
        }
        else {
          byte combineType = (combine == COMBINE_CHOICE ? RefPattern.COMBINE_CHOICE : RefPattern.COMBINE_INTERLEAVE);
          if (rp.getCombineType() != RefPattern.COMBINE_NONE
              && rp.getCombineType() != combineType) {
            if (rp.getName() == null)
              error("conflict_combine_start", (Locator)loc);
            else
              error("conflict_combine_define", rp.getName(), (Locator)loc);
          }
          rp.setCombineType(combineType);
        }
        Pattern p = (Pattern)pattern;
        if (rp.getPattern() == null)
          rp.setPattern(p);
        else if (rp.getCombineType() == RefPattern.COMBINE_INTERLEAVE)
          rp.setPattern(pb.makeInterleave(rp.getPattern(), p));
        else
          rp.setPattern(pb.makeChoice(rp.getPattern(), p));
        break;
      case RefPattern.REPLACEMENT_REQUIRE:
        rp.setReplacementStatus(RefPattern.REPLACEMENT_IGNORE);
        break;
      case RefPattern.REPLACEMENT_IGNORE:
        break;
      }
    }

    public Scope getParent() {
      return parent;
    }

    public void topLevelAnnotation(ElementAnnotation ea) throws BuildException {
    }

    private RefPattern lookup(String name) {
      if (name == START)
        return startRef;
      return lookup1(name);
    }

    private RefPattern lookup1(String name) {
      RefPattern p = (RefPattern)defines.get(name);
      if (p == null) {
        p = new RefPattern(name);
        defines.put(name, p);
      }
      return p;
    }

    public ParsedPattern makeRef(String name, Location loc, Annotations anno) throws BuildException {
      RefPattern p = lookup1(name);
      if (p.getRefLocator() == null && loc != null)
        p.setRefLocator((Locator)loc);
      return p;
    }

    public Div makeDiv() {
      return this;
    }

    public Include makeInclude() {
      return new IncludeImpl(this);
    }

  }

  static class Override {
    Override(RefPattern prp, Override next) {
      this.prp = prp;
      this.next = next;
    }

    RefPattern prp;
    Override next;
    byte replacementStatus;
  }


  private class IncludeImpl implements Include, Div {
    private Override overrides;
    private GrammarImpl grammar;

    private IncludeImpl(GrammarImpl g) {
      this.grammar = grammar;
    }

    public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
            throws BuildException {
      RefPattern rp = grammar.lookup(name);
      if (rp.getReplacementStatus() == RefPattern.REPLACEMENT_KEEP)
        overrides = new Override(rp, overrides);
      grammar.define(rp, combine, pattern, loc);
    }

    public void endDiv(Location loc, Annotations anno) throws BuildException {
      // nothing to do
    }

    public void topLevelAnnotation(ElementAnnotation ea) throws BuildException {
      // nothing to do
    }

    public Div makeDiv() {
      return this;
    }

    public void endInclude(String uri, String ns,
                           Location loc, Annotations anno) throws BuildException {
      for (OpenIncludes inc = openIncludes;
           inc != null;
           inc = inc.parent) {
        if (inc.uri.equals(uri)) {
          error("recursive_include", uri, (Locator)loc);
          return;
        }
      }

      for (Override o = overrides; o != null; o = o.next) {
        o.replacementStatus = o.prp.getReplacementStatus();
        o.prp.setReplacementStatus(RefPattern.REPLACEMENT_REQUIRE);
      }
      try {
        parseable.parseInclude(uri, new SchemaBuilderImpl(ns, uri, SchemaBuilderImpl.this), grammar);
        for (Override o = overrides; o != null; o = o.next) {
          if (o.prp.getReplacementStatus() == RefPattern.REPLACEMENT_REQUIRE) {
            if (o.prp.getName() == null)
              error("missing_start_replacement", (Locator)loc);
            else
              error("missing_define_replacement", o.prp.getName(), (Locator)loc);
          }
        }
      }
      catch (IllegalSchemaException e) {
        noteError();
      }
      finally {
        for (Override o = overrides; o != null; o = o.next)
          o.prp.setReplacementStatus(o.replacementStatus);
      }
    }

    public Include makeInclude() {
      return null;
    }
  }

  public Grammar makeGrammar(Scope parent) {
    return new GrammarImpl(parent);
  }

  public ParsedPattern annotateAfter(ParsedPattern p, ElementAnnotation e) throws BuildException {
    return p;
  }

  public ParsedNameClass annotateAfter(ParsedNameClass nc, ElementAnnotation e) throws BuildException {
    return nc;
  }

  public ParsedPattern makeExternalRef(String uri, String ns, Scope scope,
                                       Location loc, Annotations anno)
          throws BuildException {
    for (OpenIncludes inc = openIncludes;
         inc != null;
         inc = inc.parent) {
      if (inc.uri.equals(uri)) {
        error("recursive_include", uri, (Locator)loc);
        return pb.makeError();
      }
    }
    try {
      return parseable.parseExternal(uri, new SchemaBuilderImpl(ns, uri, this), scope);
    }
    catch (IllegalSchemaException e) {
      return pb.makeError();
    }
  }

  public ParsedNameClass makeChoice(ParsedNameClass nc1, ParsedNameClass nc2, Location loc, Annotations anno) {
    return new ChoiceNameClass((NameClass)nc1, (NameClass)nc2);
  }

  public ParsedNameClass makeName(String ns, String localName, String prefix, Location loc, Annotations anno) {
    return new SimpleNameClass(new Name(resolveInherit(ns), localName));
  }

  public ParsedNameClass makeNsName(String ns, Location loc, Annotations anno) {
    return new NsNameClass(resolveInherit(ns));
  }

  public ParsedNameClass makeNsName(String ns, ParsedNameClass except, Location loc, Annotations anno) {
    return new NsNameExceptNameClass(resolveInherit(ns), (NameClass)except);
  }

  public ParsedNameClass makeAnyName(Location loc, Annotations anno) {
    return new AnyNameClass();
  }

  public ParsedNameClass makeAnyName(ParsedNameClass except, Location loc, Annotations anno) {
    return new AnyNameExceptNameClass((NameClass)except);
  }

  private final String resolveInherit(String ns) {
    if (ns == INHERIT_NS)
      return inheritNs;
    return ns;
  }

  private class LocatorImpl implements Locator, Location {
    private final String systemId;
    private final int lineNumber;
    private final int columnNumber;

    private LocatorImpl(String systemId, int lineNumber, int columnNumber) {
      this.systemId = systemId;
      this.lineNumber = lineNumber;
      this.columnNumber = columnNumber;
    }

    public String getPublicId() {
      return null;
    }

    public String getSystemId() {
      return systemId;
    }

    public int getLineNumber() {
      return lineNumber;
    }

    public int getColumnNumber() {
      return columnNumber;
    }
  }

  public Location makeLocation(String systemId, int lineNumber, int columnNumber) {
    return new LocatorImpl(systemId, lineNumber, columnNumber);
  }

  public Annotations makeAnnotations() {
    return this;
  }

  public void addAttribute(String ns, String localName, String prefix, String value, Location loc) {
    // nothing needed
  }

  public void addElement(ElementAnnotation ea) {
    // nothing needed
  }

  private void error(SAXParseException message) throws BuildException {
    noteError();
    try {
      if (eh != null)
        eh.error(message);
    }
    catch (SAXException e) {
      throw new BuildException(e);
    }
  }

  private void warning(SAXParseException message) throws BuildException {
    try {
      if (eh != null)
        eh.warning(message);
    }
    catch (SAXException e) {
      throw new BuildException(e);
    }
  }

  private void error(String key, Locator loc) throws BuildException {
    error(new SAXParseException(Localizer.message(key), loc));
  }

  private void error(String key, String arg, Locator loc) throws BuildException {
    error(new SAXParseException(Localizer.message(key, arg), loc));
  }

  private void error(String key, String arg1, String arg2, Locator loc) throws BuildException {
     error(new SAXParseException(Localizer.message(key, arg1, arg2), loc));
   }

  private void noteError() {
    if (!hadError && parent != null)
      parent.noteError();
    hadError = true;
  }
}