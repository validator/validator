package com.thaiopensource.relaxng.input.parse;

import com.thaiopensource.relaxng.input.CommentTrimmer;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.edit.Comment;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.Param;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.ElementAnnotation;
import com.thaiopensource.relaxng.edit.TextAnnotation;
import com.thaiopensource.relaxng.edit.Combine;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.SchemaDocument;
import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.DataPatternBuilder;
import com.thaiopensource.relaxng.parse.Div;
import com.thaiopensource.relaxng.parse.ElementAnnotationBuilder;
import com.thaiopensource.relaxng.parse.Grammar;
import com.thaiopensource.relaxng.parse.GrammarSection;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Include;
import com.thaiopensource.relaxng.parse.IncludedGrammar;
import com.thaiopensource.relaxng.parse.Location;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.ParsedElementAnnotation;
import com.thaiopensource.relaxng.parse.ParsedNameClass;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.parse.Context;
import com.thaiopensource.relaxng.parse.CommentList;
import com.thaiopensource.relaxng.parse.SubParser;
import com.thaiopensource.util.Localizer;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.DatatypeBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

class SchemaBuilderImpl implements SchemaBuilder {
  private final SubParser subParser;
  private final ErrorHandler eh;
  private final Map schemas;
  private final DatatypeLibraryFactory dlf;
  private final boolean commentsNeedTrimming;
  private boolean hadError = false;
  static private final Localizer localizer = new Localizer(SchemaBuilderImpl.class);

  private SchemaBuilderImpl(SubParser subParser, ErrorHandler eh, Map schemas, DatatypeLibraryFactory dlf, boolean commentsNeedTrimming) {
    this.subParser = subParser;
    this.eh = eh;
    this.schemas = schemas;
    this.dlf = dlf;
    this.commentsNeedTrimming = commentsNeedTrimming;
  }

  public ParsedPattern makeChoice(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws BuildException  {
    return makeComposite(new ChoicePattern(), patterns, nPatterns, loc, anno);
  }

  private static ParsedPattern makeComposite(CompositePattern p, ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws BuildException {
    List children = p.getChildren();
    for (int i = 0; i < nPatterns; i++)
      children.add(patterns[i]);
    return finishPattern(p, loc, anno);
  }

  public ParsedPattern makeGroup(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws BuildException {
    return makeComposite(new GroupPattern(), patterns, nPatterns, loc, anno);
  }

  public ParsedPattern makeInterleave(ParsedPattern[] patterns, int nPatterns, Location loc, Annotations anno) throws BuildException {
    return makeComposite(new InterleavePattern(), patterns, nPatterns, loc, anno);
  }

  public ParsedPattern makeOneOrMore(ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new OneOrMorePattern((Pattern)p), loc, anno);
  }

  public ParsedPattern makeZeroOrMore(ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new ZeroOrMorePattern((Pattern)p), loc, anno);
  }

  public ParsedPattern makeOptional(ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new OptionalPattern((Pattern)p), loc, anno);
  }

  public ParsedPattern makeList(ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new ListPattern((Pattern)p), loc, anno);
  }

  public ParsedPattern makeMixed(ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new MixedPattern((Pattern)p), loc, anno);
  }

  public ParsedPattern makeEmpty(Location loc, Annotations anno) {
    return finishPattern(new EmptyPattern(), loc, anno);
  }

  public ParsedPattern makeNotAllowed(Location loc, Annotations anno) {
    return finishPattern(new NotAllowedPattern(), loc, anno);
  }

  public ParsedPattern makeText(Location loc, Annotations anno) {
    return finishPattern(new TextPattern(), loc, anno);
  }

  public ParsedPattern makeAttribute(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new AttributePattern((NameClass)nc, (Pattern)p), loc, anno);
  }

  public ParsedPattern makeElement(ParsedNameClass nc, ParsedPattern p, Location loc, Annotations anno) throws BuildException {
    return finishPattern(new ElementPattern((NameClass)nc, (Pattern)p), loc, anno);
  }

  private static class TraceValidationContext implements ValidationContext {
    private final Map map;
    private final ValidationContext vc;
    private final String ns;
    TraceValidationContext(Map map, ValidationContext vc, String ns) {
      this.map = map;
      this.vc = vc;
      this.ns = ns.length() == 0 ? null : ns;
    }

    public String resolveNamespacePrefix(String prefix) {
      String result;
      if (prefix.length() == 0)
        result = ns;
      else {
        result = vc.resolveNamespacePrefix(prefix);
        if (result == SchemaBuilder.INHERIT_NS)
          return null;
      }
      if (result != null)
        map.put(prefix, result);
      return result;
    }

    public String getBaseUri() {
      return vc.getBaseUri();
    }

    public boolean isUnparsedEntity(String entityName) {
      return vc.isUnparsedEntity(entityName);
    }

    public boolean isNotation(String notationName) {
      return vc.isNotation(notationName);
    }
  }

  public ParsedPattern makeValue(String datatypeLibrary, String type, String value, Context context,
                                 String ns, Location loc, Annotations anno) throws BuildException {
    ValuePattern p = new ValuePattern(datatypeLibrary, type, value);
    DatatypeLibrary dl = dlf.createDatatypeLibrary(datatypeLibrary);
    if (dl != null) {
      try {
        DatatypeBuilder dtb = dl.createDatatypeBuilder(type);
        try {
          Datatype dt = dtb.createDatatype();
          try {
            ValidationContext vc = dt.isContextDependent() ? new TraceValidationContext(p.getPrefixMap(), context, ns) : null;
            // use createValue rather than isValid so that default namespace gets used with QName
            if (dt.createValue(value, vc) == null)
              dt.checkValid(value, vc);
          }
          catch (DatatypeException e) {
            diagnoseDatatypeException("invalid_value_detail", "invalid_value", e, loc);
          }
        }
        catch (DatatypeException e) {
          diagnoseDatatypeException("invalid_params_detail", "invalid_params", e, loc);
        }
      }
      catch (DatatypeException e) {
        diagnoseDatatypeException("unsupported_datatype_detail", "unknown_datatype", e, loc);
      }
    }
    return finishPattern(p, loc, anno);
  }

  public ParsedPattern makeExternalRef(String uri, String ns, Scope scope,
                                       Location loc, Annotations anno) throws BuildException, IllegalSchemaException {
    ExternalRefPattern erp = new ExternalRefPattern(uri);
    erp.setNs(mapInheritNs(ns));
    finishPattern(erp, loc, anno);
    if (schemas.get(uri) == null) {
      schemas.put(uri, new Object()); // avoid possibility of infinite loop
      schemas.put(uri, new SchemaDocument((Pattern)subParser.parseExternal(uri, this, scope)));
    }
    return erp;
  }

  static private ParsedPattern finishPattern(Pattern p, Location loc, Annotations anno) {
    finishAnnotated(p, loc, anno);
    return p;
  }

  public ParsedNameClass makeChoice(ParsedNameClass[] nameClasses, int nNameClasses, Location loc, Annotations anno) {
    ChoiceNameClass nc = new ChoiceNameClass();
    List children = nc.getChildren();
    for (int i = 0; i < nNameClasses; i++)
      children.add(nameClasses[i]);
    return finishNameClass(nc, loc, anno);
  }

  public ParsedNameClass makeName(String ns, String localName, String prefix, Location loc, Annotations anno) {
    NameNameClass nc = new NameNameClass(mapInheritNs(ns), localName);
    nc.setPrefix(prefix);
    return finishNameClass(nc, loc, anno);
  }

  public ParsedNameClass makeNsName(String ns, Location loc, Annotations anno) {
    return finishNameClass(new NsNameNameClass(mapInheritNs(ns)), loc, anno);
  }

  public ParsedNameClass makeNsName(String ns, ParsedNameClass except, Location loc, Annotations anno) {
    return finishNameClass(new NsNameNameClass(mapInheritNs(ns), (NameClass)except), loc, anno);
  }

  public ParsedNameClass makeAnyName(Location loc, Annotations anno) {
    return finishNameClass(new AnyNameNameClass(), loc, anno);
  }

  public ParsedNameClass makeAnyName(ParsedNameClass except, Location loc, Annotations anno) {
    return finishNameClass(new AnyNameNameClass((NameClass)except), loc, anno);
  }

  private static class ScopeImpl implements Scope {
    public ParsedPattern makeRef(String name, Location loc, Annotations anno) throws BuildException {
      return finishPattern(new RefPattern(name), loc, anno);
    }
    public ParsedPattern makeParentRef(String name, Location loc, Annotations anno) throws BuildException {
      return finishPattern(new ParentRefPattern(name), loc, anno);
    }
  }

  private class GrammarSectionImpl extends ScopeImpl implements Grammar, Div, Include, IncludedGrammar {
    private final Annotated subject;
    private final List components;
    Component lastComponent;

    private GrammarSectionImpl(Annotated subject, Container container) {
      this.subject = subject;
      this.components = container.getComponents();
    }

    public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
            throws BuildException {
      if (name == GrammarSection.START)
        name = DefineComponent.START;
      DefineComponent dc = new DefineComponent(name, (Pattern)pattern);
      if (combine != null)
        dc.setCombine(mapCombine(combine));
      finishAnnotated(dc, loc, anno);
      add(dc);
    }

    public Div makeDiv() {
      DivComponent dc = new DivComponent();
      add(dc);
      return new GrammarSectionImpl(dc, dc);
    }

    public Include makeInclude() {
      IncludeComponent ic = new IncludeComponent();
      add(ic);
      return new GrammarSectionImpl(ic, ic);
    }

    public void topLevelAnnotation(ParsedElementAnnotation ea) throws BuildException {
      if (lastComponent == null)
        ((ElementAnnotationBuilderImpl)ea).addTo(subject.getChildElementAnnotations());
      else
        addAfterAnnotation(lastComponent, ea);
    }

    public void topLevelComment(CommentList comments) throws BuildException {
      if (comments != null) {
        if (lastComponent == null)
          subject.getChildElementAnnotations().addAll(((CommentListImpl)comments).list);
        else
          addAfterComment(lastComponent, comments);
      }
    }

    private void add(Component c) {
      components.add(c);
      lastComponent = c;
    }

    public void endDiv(Location loc, Annotations anno) throws BuildException {
      finishAnnotated(subject, loc, anno);
    }

    public void endInclude(String uri, String ns,
                           Location loc, Annotations anno) throws BuildException, IllegalSchemaException {
      IncludeComponent ic = (IncludeComponent)subject;
      ic.setHref(uri);
      ic.setNs(mapInheritNs(ns));
      finishAnnotated(ic, loc, anno);
      if (schemas.get(uri) == null) {
        schemas.put(uri, new Object()); // avoid possibility of infinite loop
        GrammarPattern g = new GrammarPattern();
        try {
          ParsedPattern pattern = subParser.parseInclude(uri, SchemaBuilderImpl.this, new GrammarSectionImpl(g, g));
          schemas.put(uri, new SchemaDocument((Pattern)pattern));
        }
        catch (IllegalSchemaException e) {
          schemas.remove(uri);
          hadError = true;
          throw e;
        }
      }
    }

    public ParsedPattern endGrammar(Location loc, Annotations anno) throws BuildException {
      finishAnnotated(subject, loc, anno);
      return (ParsedPattern)subject;
    }

    public ParsedPattern endIncludedGrammar(Location loc, Annotations anno) throws BuildException {
      finishAnnotated(subject, loc, anno);
      return (ParsedPattern)subject;
    }
  }

  public Grammar makeGrammar(Scope parent) {
    GrammarPattern g = new GrammarPattern();
    return new GrammarSectionImpl(g, g);
  }

  private static ParsedNameClass finishNameClass(NameClass nc, Location loc, Annotations anno) {
    finishAnnotated(nc, loc, anno);
    return nc;
  }

  private static void finishAnnotated(Annotated a, Location loc, Annotations anno) {
    a.setSourceLocation((SourceLocation)loc);
    if (anno != null)
      ((AnnotationsImpl)anno).apply(a);
  }

  public ParsedNameClass annotate(ParsedNameClass nc, Annotations anno) throws BuildException {
    if (anno != null)
      ((AnnotationsImpl)anno).apply((Annotated)nc);
    return nc;
  }

  public ParsedPattern annotate(ParsedPattern p, Annotations anno) throws BuildException {
    if (anno != null)
      ((AnnotationsImpl)anno).apply((Annotated)p);
    return p;
  }

  public ParsedPattern annotateAfter(ParsedPattern p, ParsedElementAnnotation e) throws BuildException {
    addAfterAnnotation((Pattern)p, e);
    return p;
  }

  public ParsedNameClass annotateAfter(ParsedNameClass nc, ParsedElementAnnotation e) throws BuildException {
    addAfterAnnotation((NameClass)nc, e);
    return nc;
  }

  static private void addAfterAnnotation(Annotated a, ParsedElementAnnotation e) {
    ((ElementAnnotationBuilderImpl)e).addTo(a.getFollowingElementAnnotations());
  }

  public ParsedPattern commentAfter(ParsedPattern p, CommentList comments) throws BuildException {
    addAfterComment((Pattern)p, comments);
    return p;
  }

  public ParsedNameClass commentAfter(ParsedNameClass nc, CommentList comments) throws BuildException {
    addAfterComment((NameClass)nc, comments);
    return nc;
  }

  static private void addAfterComment(Annotated a, CommentList comments) {
    if (comments != null)
      a.getFollowingElementAnnotations().addAll(((CommentListImpl)comments).list);
  }

  public Location makeLocation(String systemId, int lineNumber, int columnNumber) {
    return new SourceLocation(systemId, lineNumber, columnNumber);
  }

  static class CommentListImpl implements CommentList {
    private final List list = new Vector();
    public void addComment(String value, Location loc) throws BuildException {
      Comment comment = new Comment(value);
      comment.setSourceLocation((SourceLocation)loc);
      list.add(comment);
    }
    void add(CommentListImpl comments) {
      list.addAll(comments.list);
    }
  }

  static class TrimmingCommentListImpl extends CommentListImpl {
    public void addComment(String value, Location loc) throws BuildException {
      super.addComment(CommentTrimmer.trimComment(value), loc);
    }
  }

  public CommentList makeCommentList() {
    if (commentsNeedTrimming)
      return new TrimmingCommentListImpl();
    else
      return new CommentListImpl();
  }

  private class DataPatternBuilderImpl implements DataPatternBuilder {
    private final DataPattern p;
    private DatatypeBuilder dtb = null;

    DataPatternBuilderImpl(DataPattern p) throws BuildException {
      this.p = p;
      DatatypeLibrary dl = dlf.createDatatypeLibrary(p.getDatatypeLibrary());
      if (dl != null) {
        try {
          dtb = dl.createDatatypeBuilder(p.getType());
        }
        catch (DatatypeException e) {
          String datatypeLibrary = p.getDatatypeLibrary();
          String type = p.getType();
          SourceLocation loc = p.getSourceLocation();
          String detail = e.getMessage();
          if (detail != null)
            error("unsupported_datatype_detail", datatypeLibrary, type, detail, loc);
          else
            error("unknown_datatype", datatypeLibrary, type, loc);
        }
      }
    }

    public void addParam(String name, String value, Context context, String ns, Location loc, Annotations anno)
            throws BuildException {
      Param param = new Param(name, value);
      param.setContext(context.copy());
      finishAnnotated(param, loc, anno);
      p.getParams().add(param);
      if (dtb != null) {
        try {
          dtb.addParameter(name, value, context);
        }
        catch (DatatypeException e) {
          diagnoseDatatypeException("invalid_param_detail", "invalid_param", e, loc);
        }
      }
    }

    public void annotation(ParsedElementAnnotation ea) {
      List params = p.getParams();
      ((ElementAnnotationBuilderImpl)ea).addTo(params.isEmpty()
                                               ? p.getChildElementAnnotations()
                                               : ((Param)params.get(params.size() - 1)).getFollowingElementAnnotations());
    }

    public ParsedPattern makePattern(Location loc, Annotations anno)
            throws BuildException {
      if (dtb != null) {
        try {
          dtb.createDatatype();
        }
        catch (DatatypeException e){
          diagnoseDatatypeException("invalid_params_detail", "invalid_params", e, loc);
        }
      }
      return finishPattern(p, loc, anno);
    }

    public ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
            throws BuildException {
      p.setExcept((Pattern)except);
      return finishPattern(p, loc, anno);
    }
  }

  public DataPatternBuilder makeDataPatternBuilder(String datatypeLibrary, String type, Location loc) throws BuildException {
    DataPattern pattern = new DataPattern(datatypeLibrary, type);
    pattern.setSourceLocation((SourceLocation)loc);
    return new DataPatternBuilderImpl(pattern);
  }

  public ParsedPattern makeErrorPattern() {
    return null;
  }

  public ParsedNameClass makeErrorNameClass() {
    return null;
  }

  private static class AnnotationsImpl implements Annotations {
    private CommentList comments;
    private final List attributes = new Vector();
    private final List elements = new Vector();
    private final Context context;

    AnnotationsImpl(CommentList comments, Context context) {
      this.comments = comments;
      this.context = context;
    }

    public void addAttribute(String ns, String localName, String prefix, String value, Location loc)
            throws BuildException {
      AttributeAnnotation att = new AttributeAnnotation(ns, localName, value);
      att.setPrefix(prefix);
      att.setSourceLocation((SourceLocation)loc);
      attributes.add(att);
    }

    public void addElement(ParsedElementAnnotation ea) throws BuildException {
      ((ElementAnnotationBuilderImpl)ea).addTo(elements);
    }

    public void addComment(CommentList comments) throws BuildException {
      if (comments != null)
        elements.addAll(((CommentListImpl)comments).list);
    }

    public void addLeadingComment(CommentList comments) throws BuildException {
      if (this.comments == null)
        this.comments = comments;
      else if (comments != null)
        ((CommentListImpl)this.comments).add((CommentListImpl)comments);
    }

    void apply(Annotated subject) {
      subject.setContext(context.copy());
      if (comments != null)
        subject.getLeadingComments().addAll(((CommentListImpl)comments).list);
      subject.getAttributeAnnotations().addAll(attributes);
      List list;
      if (subject.mayContainText())
        list = subject.getFollowingElementAnnotations();
      else
        list = subject.getChildElementAnnotations();
      list.addAll(elements);
    }
  }

  public Annotations makeAnnotations(CommentList comments, Context context) {
    return new AnnotationsImpl(comments, context);
  }

  private static class ElementAnnotationBuilderImpl implements ElementAnnotationBuilder, ParsedElementAnnotation {
    private final ElementAnnotation element;
    private CommentList comments;

    ElementAnnotationBuilderImpl(CommentList comments, ElementAnnotation element) {
      this.comments = comments;
      this.element = element;
    }

    public void addText(String value, Location loc, CommentList comments) throws BuildException {
      TextAnnotation t = new TextAnnotation(value);
      t.setSourceLocation((SourceLocation)loc);
      if (comments != null)
        element.getChildren().addAll(((CommentListImpl)comments).list);
      element.getChildren().add(t);
    }

    public void addAttribute(String ns, String localName, String prefix, String value, Location loc)
            throws BuildException {
      AttributeAnnotation att = new AttributeAnnotation(ns, localName, value);
      att.setPrefix(prefix);
      att.setSourceLocation((SourceLocation)loc);
      element.getAttributes().add(att);
    }

    public ParsedElementAnnotation makeElementAnnotation() throws BuildException {
      return this;
    }

    public void addElement(ParsedElementAnnotation ea) throws BuildException {
      ((ElementAnnotationBuilderImpl)ea).addTo(element.getChildren());
    }

    public void addComment(CommentList comments) throws BuildException {
      if (comments != null)
        element.getChildren().addAll(((CommentListImpl)comments).list);
    }

    public void addLeadingComment(CommentList comments) throws BuildException {
      if (this.comments == null)
        this.comments = comments;
      else if (comments != null)
        ((CommentListImpl)this.comments).add((CommentListImpl)comments);
    }

    void addTo(List elementList) {
      if (comments != null)
        elementList.addAll(((CommentListImpl)comments).list);
      elementList.add(element);
    }
  }

  public ElementAnnotationBuilder makeElementAnnotationBuilder(String ns, String localName, String prefix, Location loc,
                                                               CommentList comments, Context context) {
    ElementAnnotation element = new ElementAnnotation(ns, localName);
    element.setPrefix(prefix);
    element.setSourceLocation((SourceLocation)loc);
    element.setContext(context.copy());
    return new ElementAnnotationBuilderImpl(comments, element);
  }

  public boolean usesComments() {
    return true;
  }

  private static Combine mapCombine(GrammarSection.Combine combine) {
    if (combine == null)
      return null;
    return combine == GrammarSection.COMBINE_CHOICE ? Combine.CHOICE : Combine.INTERLEAVE;
  }

  private static String mapInheritNs(String ns) {
    // noop since we represent INHERIT_NS by the same object
    return ns;
  }

  private void parse(Parseable parseable, String uri) throws IllegalSchemaException {
    schemas.put(uri, new SchemaDocument((Pattern)parseable.parse(this, new ScopeImpl())));
  }

  static SchemaCollection parse(Parseable parseable, String uri, ErrorHandler eh, DatatypeLibraryFactory dlf, boolean commentsNeedTrimming)
          throws IllegalSchemaException, IOException, SAXException {
    try {
      SchemaCollection sc = new SchemaCollection();
      SchemaBuilderImpl sb = new SchemaBuilderImpl(parseable, eh, sc.getSchemaDocumentMap(), dlf, commentsNeedTrimming);
      sc.setMainUri(uri);
      sb.parse(parseable, uri);
      if (sb.hadError)
        throw new IllegalSchemaException();
      return sc;
    }
    catch (BuildException e) {
      Throwable t = e.getCause();
      if (t instanceof IOException)
        throw (IOException)t;
      if (t instanceof RuntimeException)
        throw (RuntimeException)t;
      if (t instanceof SAXException)
        throw (SAXException)t;
      if (t instanceof Exception)
        throw new SAXException((Exception)t);
      throw new SAXException(t.getClass().getName() + " thrown");
    }
  }

  private void error(SAXParseException message) throws BuildException {
    hadError = true;
    try {
      if (eh != null)
        eh.error(message);
    }
    catch (SAXException e) {
      throw new BuildException(e);
    }
  }

  private void diagnoseDatatypeException(String detailKey, String noDetailKey, DatatypeException e, Location loc)
    throws BuildException {
    String detail = e.getMessage();
    if (detail != null)
      error(detailKey, detail, (SourceLocation)loc);
    else
      error(noDetailKey, (SourceLocation)loc);
  }

  static private Locator makeLocator(final SourceLocation loc) {
    return new Locator() {
      public String getPublicId() {
        return null;
      }

      public int getColumnNumber() {
        if (loc == null)
          return -1;
        return loc.getColumnNumber();
      }

      public String getSystemId() {
        if (loc == null)
          return null;
        return loc.getUri();
      }

      public int getLineNumber() {
        if (loc == null)
          return -1;
        return loc.getLineNumber();
      }
    };
  }

  private void error(String key, SourceLocation loc) throws BuildException {
    error(new SAXParseException(localizer.message(key), makeLocator(loc)));
  }

  private void error(String key, String arg, SourceLocation loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, arg), makeLocator(loc)));
  }

  private void error(String key, String arg1, String arg2, SourceLocation loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, arg1, arg2), makeLocator(loc)));
  }

  private void error(String key, String arg1, String arg2, String arg3, SourceLocation loc) throws BuildException {
    error(new SAXParseException(localizer.message(key, new Object[]{arg1, arg2, arg3}), makeLocator(loc)));
  }
}
