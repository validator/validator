package com.thaiopensource.relaxng.output.rnc;

import com.thaiopensource.relaxng.edit.*;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Context;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import com.thaiopensource.xml.out.CharRepertoire;
import com.thaiopensource.util.Utf16;

import java.io.IOException;
import java.util.*;

class Output {
  private final Prettyprinter pp;
  private final CharRepertoire cr;
  private final String indent;
  private final String sourceUri;
  private final OutputDirectory od;
  private final ErrorReporter er;
  private final NamespaceManager.NamespaceBindings nsb;
  private final Map datatypeLibraryMap = new HashMap();
  private final ComplexityCache complexityCache = new ComplexityCache();
  private final NameClassVisitor nameClassOutput = new NameClassOutput(true);
  private final NameClassVisitor noParenNameClassOutput = new NameClassOutput(false);
  private final PatternVisitor noParenPatternOutput = new PatternOutput(false);
  private final PatternVisitor patternOutput = new PatternOutput(true);
  private final ComponentVisitor componentOutput = new ComponentOutput();
  private final AnnotationChildVisitor annotationChildOutput = new AnnotationChildOutput();
  private final AnnotationChildVisitor followingAnnotationChildOutput = new FollowingAnnotationChildOutput();
  private boolean isAttributeNameClass;
  private final StringBuffer encodeBuf = new StringBuffer();


  static private final String[] keywords = {
    "attribute", "default", "datatypes", "div", "element", "empty", "external",
    "grammar", "include", "inherit", "list", "mixed", "namespace", "notAllowed",
    "parent", "start", "string", "text", "token"
  };

  static private final Set keywordSet = new HashSet();

  static {
    for (int i = 0; i < keywords.length; i++)
      keywordSet.add(keywords[i]);
  }

  static void output(Pattern p, String encoding, String sourceUri, OutputDirectory od, ErrorReporter er) throws IOException {
    try {
      new Output(sourceUri, encoding, od, er, NamespaceVisitor.createBindings(p)).topLevel(p);
    }
    catch (Prettyprinter.WrappedException e) {
      throw e.getIOException();
    }
  }

  private Output(String sourceUri, String encoding, OutputDirectory od, ErrorReporter er,
                 NamespaceManager.NamespaceBindings nsb) throws IOException {
    this.sourceUri = sourceUri;
    this.od = od;
    this.er = er;
    // Only preserve the input encoding if it's one that can be auto-detected.
    if (encoding != null
        && !encoding.equalsIgnoreCase("UTF-8")
        && !encoding.equalsIgnoreCase("UTF-16")
        && !encoding.equalsIgnoreCase("US-ASCII"))
      encoding = null;
    OutputDirectory.Stream stream = od.open(sourceUri, encoding);
    this.cr = stream.getCharRepertoire();
    this.pp = new StreamingPrettyprinter(od.getLineLength(), od.getLineSeparator(), stream.getWriter());
    this.nsb = nsb;
    char[] tem = new char[od.getIndent()];
    for (int i = 0; i < tem.length; i++)
      tem[i] = ' ';
    this.indent = new String(tem);
  }

  private void topLevel(Pattern p) {
    p.accept(new TextAnnotationMerger());
    boolean implicitGrammar = p instanceof GrammarPattern && p.getAttributeAnnotations().isEmpty();
    if (implicitGrammar && !p.getLeadingComments().isEmpty()) {
      leadingComments(p);
      pp.hardNewline();
    }
    outputNamespaceDeclarations();
    outputDatatypeLibraryDeclarations(p);
    if (implicitGrammar) {
      for (Iterator iter = p.getChildElementAnnotations().iterator(); iter.hasNext();) {
        ((AnnotationChild)iter.next()).accept(annotationChildOutput);
        pp.hardNewline();
      }
      innerBody(((GrammarPattern)p).getComponents());
      // This deals with trailing comments
      for (Iterator iter = p.getFollowingElementAnnotations().iterator(); iter.hasNext();) {
        pp.hardNewline();
        ((AnnotationChild)iter.next()).accept(annotationChildOutput);
      }
    }
    else
      p.accept(patternOutput);
    // The pretty printer ensures that we have a terminating newline.
    pp.close();
  }

  private void outputNamespaceDeclarations() {
    List prefixes = new Vector();
    prefixes.addAll(nsb.getPrefixes());
    Collections.sort(prefixes);

    boolean needNewline = false;

    String defaultPrefix = null;
    String defaultNamespace = nsb.getNamespaceUri("");
    if (defaultNamespace != null && !defaultNamespace.equals(SchemaBuilder.INHERIT_NS))
      defaultPrefix = nsb.getNonEmptyPrefix(defaultNamespace);
    for (Iterator iter = prefixes.iterator(); iter.hasNext();) {
      String prefix = (String)iter.next();
      String ns = nsb.getNamespaceUri(prefix);
      if (prefix.length() == 0) {
        if (defaultPrefix == null && !ns.equals(SchemaBuilder.INHERIT_NS)) {
          pp.startGroup();
          pp.text("default namespace =");
          pp.startNest(indent);
          pp.softNewline(" ");
          literal(ns);
          pp.endNest();
          pp.endGroup();
          pp.hardNewline();
          needNewline = true;
        }
      }
      else if (!prefix.equals("xml")) {
        pp.startGroup();
        if (prefix.equals(defaultPrefix))
          pp.text("default namespace ");
        else
          pp.text("namespace ");
        encodedText(prefix);
        pp.text(" =");
        pp.startNest(indent);
        pp.softNewline(" ");
        if (ns.equals(SchemaBuilder.INHERIT_NS))
          pp.text("inherit");
        else
          literal(ns);
        pp.endNest();
        pp.endGroup();
        pp.hardNewline();
        needNewline = true;
      }
    }

    if (needNewline)
      pp.hardNewline();
  }


  private void outputDatatypeLibraryDeclarations(Pattern p) {
    datatypeLibraryMap.put(WellKnownNamespaces.XML_SCHEMA_DATATYPES, "xsd");
    List datatypeLibraries = new Vector();
    datatypeLibraries.addAll(DatatypeLibraryVisitor.findDatatypeLibraries(p));
    if (datatypeLibraries.isEmpty())
      return;
    Collections.sort(datatypeLibraries);
    for (int i = 0, len = datatypeLibraries.size(); i < len; i++) {
      String prefix = "d";
      if (len > 1)
        prefix += Integer.toString(i + 1);
      String uri = (String)datatypeLibraries.get(i);
      datatypeLibraryMap.put(uri, prefix);
      pp.startGroup();
      pp.text("datatypes ");
      encodedText(prefix);
      pp.text(" =");
      pp.startNest(indent);
      pp.softNewline(" ");
      literal(uri);
      pp.endNest();
      pp.endGroup();
      pp.hardNewline();
    }
    pp.hardNewline();
  }

  static class TextAnnotationMerger extends NullVisitor {
    public void nullVisitElement(ElementAnnotation ea) {
      TextAnnotation prevText = null;
      for (Iterator iter = ea.getChildren().iterator(); iter.hasNext();) {
        AnnotationChild child = (AnnotationChild)iter.next();
        if (child instanceof TextAnnotation) {
          if (prevText == null)
            prevText = (TextAnnotation)child;
          else {
            prevText.setValue(prevText.getValue() + ((TextAnnotation)child).getValue());
            iter.remove();
          }
        }
        else {
          prevText = null;
          child.accept(this);
        }
      }
    }
  }

  static class DatatypeLibraryVisitor extends NullVisitor {
    private final Set datatypeLibraries = new HashSet();

    public void nullVisitValue(ValuePattern p) {
      noteDatatypeLibrary(p.getDatatypeLibrary());
      super.nullVisitValue(p);
    }

    public void nullVisitData(DataPattern p) {
      noteDatatypeLibrary(p.getDatatypeLibrary());
      super.nullVisitData(p);
    }

    private void noteDatatypeLibrary(String uri) {
      if (!uri.equals("") && !uri.equals(WellKnownNamespaces.XML_SCHEMA_DATATYPES))
        datatypeLibraries.add(uri);
    }

    static Set findDatatypeLibraries(Pattern p) {
      DatatypeLibraryVisitor dlv = new DatatypeLibraryVisitor();
      p.accept(dlv);
      return dlv.datatypeLibraries;
    }
  }

  static class NamespaceVisitor extends NullVisitor {
    private final NamespaceManager nsm = new NamespaceManager();
    private boolean isAttribute;

    public void nullVisitInclude(IncludeComponent c) {
      super.nullVisitInclude(c);
      nsm.requireNamespace(c.getNs(), true);
    }

    public void nullVisitExternalRef(ExternalRefPattern p) {
      super.nullVisitExternalRef(p);
      nsm.requireNamespace(p.getNs(), true);
    }

    public void nullVisitElement(ElementPattern p) {
      isAttribute = false;
      super.nullVisitElement(p);
    }

    public void nullVisitAttribute(AttributePattern p) {
      isAttribute = true;
      super.nullVisitAttribute(p);
    }

    public void nullVisitName(NameNameClass nc) {
      super.nullVisitName(nc);
      if (!isAttribute || nc.getNamespaceUri().length() != 0)
        nsm.requireNamespace(nc.getNamespaceUri(), !isAttribute);
      if (nc.getPrefix() == null) {
        if (!isAttribute)
          nsm.preferBinding("", nc.getNamespaceUri());
      }
      else
        nsm.preferBinding(nc.getPrefix(), nc.getNamespaceUri());
    }

    public void nullVisitNsName(NsNameNameClass nc) {
      super.nullVisitNsName(nc);
      nsm.requireNamespace(nc.getNs(), false);
    }

    public void nullVisitValue(ValuePattern p) {
      super.nullVisitValue(p);
      for (Iterator iter = p.getPrefixMap().entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        nsm.requireBinding((String)entry.getKey(), (String)entry.getValue());
      }
    }

    public void nullVisitElement(ElementAnnotation ea) {
      super.nullVisitElement(ea);
      noteAnnotationBinding(ea.getPrefix(), ea.getNamespaceUri());
      noteContext(ea.getContext(), true);
    }

    private void noteContext(Context context, boolean required) {
      if (context == null)
        return;
      for (Enumeration e = context.prefixes(); e.hasMoreElements();) {
        String prefix = (String)e.nextElement();
        // Default namespace is not relevant to annotations
        if (!prefix.equals("")) {
          String ns = context.resolveNamespacePrefix(prefix);
          if (ns != null && !ns.equals(SchemaBuilder.INHERIT_NS)) {
            if (required)
              nsm.requireBinding(prefix, ns);
            else
              nsm.preferBinding(prefix, ns);
          }
        }
      }
    }

    public void nullVisitAttribute(AttributeAnnotation a) {
      super.nullVisitAttribute(a);
      noteAnnotationBinding(a.getPrefix(), a.getNamespaceUri());
    }

    private void noteAnnotationBinding(String prefix, String ns) {
      if (ns.length() != 0)
        nsm.requireNamespace(ns, false);
      if (prefix != null)
        nsm.preferBinding(prefix, ns);
    }

    public void nullVisitAnnotated(Annotated p) {
      p.leadingCommentsAccept(this);
      noteContext(p.getContext(), !p.getAttributeAnnotations().isEmpty());
      p.attributeAnnotationsAccept(this);
      List before = (p.mayContainText()
                     ? p.getFollowingElementAnnotations()
                     : p.getChildElementAnnotations());
      // Avoid unnecessary prefix for documentation
      int state = 0;
      for (Iterator iter = before.iterator(); iter.hasNext();) {
        AnnotationChild child = (AnnotationChild)iter.next();
        if (state < 2 && documentationString(child) != null)
          state = 1;
        else if (state != 1 || !(child instanceof Comment))
          state = 2;
        if (state == 2)
          child.accept(this);
      }
      if (!p.mayContainText())
        p.followingElementAnnotationsAccept(this);
    }

    static NamespaceManager.NamespaceBindings createBindings(Pattern p) {
      NamespaceVisitor nsv = new NamespaceVisitor();
      p.accept(nsv);
      return nsv.nsm.createBindings();
    }
  }

  class ComponentOutput implements ComponentVisitor {
    public Object visitDefine(DefineComponent c) {
      startAnnotations(c);
      pp.startGroup();
      String name = c.getName();
      if (name == DefineComponent.START)
        pp.text("start");
      else
        identifier(name);
      Combine combine = c.getCombine();
      String op;
      if (combine == null)
        op = " =";
      else if (combine == Combine.CHOICE)
        op = " |=";
      else
        op = " &=";
      pp.text(op);
      pp.startNest(indent);
      pp.softNewline(" ");
      c.getBody().accept(noParenPatternOutput);
      pp.endNest();
      pp.endGroup();
      endAnnotations(c);
      return null;
    }

    public Object visitDiv(DivComponent c) {
      startAnnotations(c);
      pp.text("div");
      body(c);
      endAnnotations(c);
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      startAnnotations(c);
      pp.startGroup();
      pp.text("include ");
      pp.startNest("include ");
      literal(od.reference(sourceUri, c.getHref()));
      inherit(c.getNs());
      pp.endNest();
      pp.endGroup();
      List components = c.getComponents();
      if (!components.isEmpty())
        body(components);
      endAnnotations(c);
      return null;
    }
  }

  class PatternOutput implements PatternVisitor {
    private final boolean alwaysUseParens;

    PatternOutput(boolean alwaysUseParens) {
      this.alwaysUseParens = alwaysUseParens;
    }

    public Object visitGrammar(GrammarPattern p) {
      startAnnotations(p);
      pp.text("grammar");
      body(p);
      endAnnotations(p);
      return null;
    }

    public Object visitElement(ElementPattern p) {
      isAttributeNameClass = false;
      nameClassed(p, "element ");
      return null;
    }

    public Object visitAttribute(AttributePattern p) {
      isAttributeNameClass = true;
      nameClassed(p, "attribute ");
      return null;
    }

    private void nameClassed(NameClassedPattern p, String key) {
      startAnnotations(p);
      pp.text(key);
      pp.startNest(key);
      p.getNameClass().accept(noParenNameClassOutput);
      pp.endNest();
      braceChild(p);
      endAnnotations(p);
    }

    private void braceChild(UnaryPattern p) {
      Pattern child = p.getChild();
      boolean isSimple = !complexityCache.isComplex(child);
      if (isSimple)
        pp.startGroup();
      pp.text(" {");
      pp.startNest(indent);
      if (isSimple)
        pp.softNewline(" ");
      else
        pp.hardNewline();
      child.accept(noParenPatternOutput);
      pp.endNest();
      if (isSimple)
        pp.softNewline(" ");
      else
        pp.hardNewline();
      pp.text("}");
      if (isSimple)
        pp.endGroup();
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      postfix(p, "+");
      return null;
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      postfix(p, "*");
      return null;
    }

    public Object visitOptional(OptionalPattern p) {
      postfix(p, "?");
      return null;
    }

    private void postfix(UnaryPattern p, String op) {
      if (!startAnnotations(p)) {
        p.getChild().accept(patternOutput);
        pp.text(op);
      }
      else {
        pp.text("(");
        pp.startNest("(");
        p.getChild().accept(patternOutput);
        pp.endNest();
        pp.text(op);
        pp.text(")");
      }
      endAnnotations(p);
    }

    public Object visitRef(RefPattern p) {
      startAnnotations(p);
      identifier(p.getName());
      endAnnotations(p);
      return null;
    }

    public Object visitParentRef(ParentRefPattern p) {
      startAnnotations(p);
      pp.text("parent ");
      identifier(p.getName());
      endAnnotations(p);
      return null;
    }

    public Object visitExternalRef(ExternalRefPattern p) {
      startAnnotations(p);
      pp.startGroup();
      pp.text("external ");
      pp.startNest("external ");
      literal(od.reference(sourceUri, p.getHref()));
      inherit(p.getNs());
      pp.endNest();
      pp.endGroup();
      endAnnotations(p);
      return null;
    }

    public Object visitText(TextPattern p) {
      startAnnotations(p);
      pp.text("text");
      endAnnotations(p);
      return null;
    }

    public Object visitEmpty(EmptyPattern p) {
      startAnnotations(p);
      pp.text("empty");
      endAnnotations(p);
      return null;
    }

    public Object visitNotAllowed(NotAllowedPattern p) {
      startAnnotations(p);
      pp.text("notAllowed");
      endAnnotations(p);
      return null;
    }

    public Object visitList(ListPattern p) {
      prefix(p, "list");
      return null;
    }

    public Object visitMixed(MixedPattern p) {
      prefix(p, "mixed");
      return null;
    }

    private void prefix(UnaryPattern p, String key) {
      startAnnotations(p);
      pp.text(key);
      braceChild(p);
      endAnnotations(p);
    }

    public Object visitChoice(ChoicePattern p) {
      composite(p, "| ", false);
      return null;
    }

    public Object visitInterleave(InterleavePattern p) {
      composite(p, "& ", false);
      return null;
    }

    public Object visitGroup(GroupPattern p) {
      composite(p, ",", true);
      return null;
    }

    void composite(CompositePattern p, String sep, boolean sepBeforeNewline) {
      boolean useParens = alwaysUseParens;
      if (startAnnotations(p))
        useParens = true;
      boolean isSimple = !complexityCache.isComplex(p);
      if (isSimple)
        pp.startGroup();
      if (useParens) {
        pp.text("(");
        pp.startNest("(");
      }

      boolean first = true;
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();) {
        if (!first) {
          if (sepBeforeNewline)
            pp.text(sep);
          if (isSimple)
            pp.softNewline(" ");
          else
            pp.hardNewline();
          if (!sepBeforeNewline) {
            pp.text(sep);
            pp.startNest(sep);
          }
        }
        ((Pattern)iter.next()).accept(patternOutput);
        if (first)
          first = false;
        else if (!sepBeforeNewline)
          pp.endNest();
      }
      if (useParens) {
        pp.endNest();
        pp.text(")");
      }
      if (isSimple)
        pp.endGroup();
      endAnnotations(p);
    }

    public Object visitData(DataPattern p) {
      startAnnotations(p);
      String lib = p.getDatatypeLibrary();
      String qn;
      if (!lib.equals(""))
        qn = (String)datatypeLibraryMap.get(lib) + ":" + p.getType();
      else
        qn = p.getType();
      qn = encode(qn);
      pp.text(qn);
      List params = p.getParams();
      if (params.size() > 0) {
        pp.startGroup();
        pp.text(" {");
        pp.startNest(indent);
        for (Iterator iter = params.iterator(); iter.hasNext();) {
          pp.softNewline(" ");
          Param param = (Param)iter.next();
          startAnnotations(param);
          pp.startGroup();
          encodedText(param.getName());
          pp.text(" =");
          pp.startNest(indent);
          pp.softNewline(" ");
          literal(param.getValue());
          pp.endNest();
          pp.endGroup();
          endAnnotations(param);
        }
        pp.endNest();
        pp.softNewline(" ");
        pp.text("}");
        pp.endGroup();
      }
      Pattern e = p.getExcept();
      if (e != null) {
        boolean useParen = (!e.mayContainText()
                            && !e.getFollowingElementAnnotations().isEmpty());
        String s;
        if (params.isEmpty())
          s = " - ";
        else {
          pp.startGroup();
          pp.softNewline(" ");
          s = "- ";
        }
        if (useParen)
          s += "(";
        pp.text(s);
        pp.startNest(params.isEmpty() ? qn + s : s);
        e.accept(useParen ? noParenPatternOutput : patternOutput);
        pp.endNest();
        if (useParen)
          pp.text(")");
        if (!params.isEmpty())
          pp.endGroup();
      }
      endAnnotations(p);
      return null;
    }

    public Object visitValue(ValuePattern p) {
      for (Iterator iter = p.getPrefixMap().entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String prefix = (String)entry.getKey();
        String uri = (String)entry.getValue();
        if (!uri.equals(nsb.getNamespaceUri(prefix))) {
          if (prefix.equals(""))
            er.error("value_inconsistent_default_binding", uri, p.getSourceLocation());
          else
            er.error("value_inconsistent_binding", prefix, uri, p.getSourceLocation());
        }
      }
      startAnnotations(p);
      String lib = p.getDatatypeLibrary();
      pp.startGroup();
      String str = null;
      if (lib.equals("")) {
        if (!p.getType().equals("token"))
          str = p.getType() + " ";
      }
      else
        str = (String)datatypeLibraryMap.get(lib) + ":" + p.getType() + " ";
      if (str != null) {
        String encoded = encode(str);
        pp.text(encoded);
        pp.startNest(encoded);
      }
      literal(p.getValue());
      if (str != null)
        pp.endNest();
      pp.endGroup();
      endAnnotations(p);
      return null;
    }

  }

  class NameClassOutput implements NameClassVisitor {
    private final boolean alwaysUseParens;

    NameClassOutput(boolean alwaysUseParens) {
      this.alwaysUseParens = alwaysUseParens;
    }

    public Object visitAnyName(AnyNameNameClass nc) {
      NameClass e = nc.getExcept();
      if (e == null) {
        startAnnotations(nc);
        pp.text("*");
      }
      else {
        boolean useParens = startAnnotations(nc) || alwaysUseParens;
        String s = useParens ?  "(* - " : "* - ";
        pp.text(s);
        pp.startNest(s);
        e.accept(nameClassOutput);
        if (useParens)
          pp.text(")");
        pp.endNest();
      }
      endAnnotations(nc);
      return null;
    }

    public Object visitNsName(NsNameNameClass nc) {
      NameClass e = nc.getExcept();
      String prefix = nsb.getNonEmptyPrefix(nc.getNs());
      if (e == null) {
        startAnnotations(nc);
        encodedText(prefix);
        pp.text(":*");
      }
      else {
        boolean useParens = startAnnotations(nc) || alwaysUseParens;
        String s = useParens ? "(" : "";
        s += encode(prefix);
        s += ":* - ";
        pp.text(s);
        pp.startNest(s);
        e.accept(nameClassOutput);
        pp.endNest();
        if (useParens)
          pp.text(")");
      }
      endAnnotations(nc);
      return null;
    }

    public Object visitName(NameNameClass nc) {
      startAnnotations(nc);
      qualifiedName(nc.getNamespaceUri(), nc.getPrefix(), nc.getLocalName(), isAttributeNameClass);
      endAnnotations(nc);
      return null;
    }

    public Object visitChoice(ChoiceNameClass nc) {
      boolean useParens = alwaysUseParens;
      if (startAnnotations(nc))
        useParens = true;
      else if (nc.getChildren().size() == 1)
        useParens = false;
      if (useParens) {
        pp.text("(");
        pp.startNest("(");
      }
      pp.startGroup();
      boolean first = true;
      for (Iterator iter = nc.getChildren().iterator(); iter.hasNext();) {
        if (first)
          first = false;
        else {
          pp.softNewline(" ");
          pp.text("| ");
        }
        ((NameClass)iter.next()).accept(nameClassOutput);
      }
      pp.endGroup();
      if (useParens) {
        pp.endNest();
        pp.text(")");
      }
      endAnnotations(nc);
      return null;
    }
  }

  class AnnotationChildOutput implements AnnotationChildVisitor {
    public Object visitText(TextAnnotation ta) {
      literal(ta.getValue());
      return null;
    }

    public Object visitComment(Comment c) {
      comment("#", c.getValue());
      return null;
    }

    public Object visitElement(ElementAnnotation elem) {
      checkContext(elem.getContext(), elem.getSourceLocation());
      qualifiedName(elem.getNamespaceUri(), elem.getPrefix(), elem.getLocalName(),
                    // unqualified annotation element names have "" namespace
                    true);
      pp.text(" ");
      annotationBody(elem.getAttributes(), elem.getChildren());
      return null;
    }
  }

  class FollowingAnnotationChildOutput extends AnnotationChildOutput {
    public Object visitElement(ElementAnnotation elem) {
      pp.text(">> ");
      pp.startNest(">> ");
      super.visitElement(elem);
      pp.endNest();
      return null;
    }
  }

  private static boolean hasAnnotations(Annotated annotated) {
    return (!annotated.getChildElementAnnotations().isEmpty()
            || !annotated.getAttributeAnnotations().isEmpty()
            || !annotated.getFollowingElementAnnotations().isEmpty());
  }

  private boolean startAnnotations(Annotated annotated) {
    if (!annotated.getLeadingComments().isEmpty()) {
      leadingComments(annotated);
      if (!hasAnnotations(annotated))
        return false;
    }
    else if (!hasAnnotations(annotated))
      return false;
    List before = (annotated.mayContainText()
                   ? annotated.getFollowingElementAnnotations()
                   : annotated.getChildElementAnnotations());
    int i = 0;
    int len = before.size();
    for (; i < len; i++) {
      int j = i;
      if (i != 0) {
        do {
          if (!(before.get(j) instanceof Comment))
            break;
        } while (++j < len);
        if (j >= len)
          break;
      }
      String doc = documentationString((AnnotationChild)before.get(j));
      if (doc == null)
        break;
      if (j == i)
        pp.hardNewline();
      else {
        for (;;) {
          ((Comment)before.get(i)).accept(annotationChildOutput);
          if (++i == j)
            break;
          pp.hardNewline();
        }
      }
      comment("##", doc);
    }
    if (i > 0)
      before = before.subList(i, len);
    pp.startGroup();
    if (!annotated.getAttributeAnnotations().isEmpty()
        || !before.isEmpty()) {
      if (!annotated.getAttributeAnnotations().isEmpty())
        checkContext(annotated.getContext(), annotated.getSourceLocation());
      annotationBody(annotated.getAttributeAnnotations(), before);
      pp.softNewline(" ");
    }
    return true;
  }

  private static String documentationString(AnnotationChild child) {
    if (!(child instanceof ElementAnnotation))
     return null;
    ElementAnnotation elem = (ElementAnnotation)child;
    if (!elem.getLocalName().equals("documentation"))
      return null;
    if (!elem.getNamespaceUri().equals(WellKnownNamespaces.RELAX_NG_COMPATIBILITY_ANNOTATIONS))
      return null;
    if (!elem.getAttributes().isEmpty())
      return null;
    StringBuffer buf = new StringBuffer();
    for (Iterator iter = elem.getChildren().iterator(); iter.hasNext();) {
      Object obj = iter.next();
      if (!(obj instanceof TextAnnotation))
        return null;
      buf.append(((TextAnnotation)obj).getValue());
    }
    return buf.toString();
  }

  private void endAnnotations(Annotated annotated) {
    if (!annotated.mayContainText()) {
      for (Iterator iter = annotated.getFollowingElementAnnotations().iterator(); iter.hasNext();) {
        if (annotated instanceof Component)
          pp.hardNewline();
        else
          pp.softNewline(" ");
        AnnotationChildVisitor output = (annotated instanceof Component
                                         ? annotationChildOutput
                                         : followingAnnotationChildOutput);
        ((AnnotationChild)iter.next()).accept(output);
      }
    }
    if (hasAnnotations(annotated))
      pp.endGroup();
  }

  private void leadingComments(Annotated annotated) {
    boolean first = true;
    for (Iterator iter = annotated.getLeadingComments().iterator(); iter.hasNext();) {
      if (!first)
        pp.hardNewline();
      else
        first = false;
      ((Comment)iter.next()).accept(annotationChildOutput);
    }
  }

  private void annotationBody(List attributes, List children) {
    pp.startGroup();
    pp.text("[");
    pp.startNest(indent);
    for (Iterator iter = attributes.iterator(); iter.hasNext();) {
      AttributeAnnotation att = (AttributeAnnotation)iter.next();
      pp.softNewline(" ");
      pp.startGroup();
      qualifiedName(att.getNamespaceUri(), att.getPrefix(), att.getLocalName(), true);
      pp.text(" =");
      pp.startNest(indent);
      pp.softNewline(" ");
      literal(att.getValue());
      pp.endNest();
      pp.endGroup();
    }
    for (Iterator iter = children.iterator(); iter.hasNext();) {
      pp.softNewline(" ");
      ((AnnotationChild)iter.next()).accept(annotationChildOutput);
    }
    pp.endNest();
    pp.softNewline(" ");
    pp.text("]");
    pp.endGroup();
  }

  private void body(Container container) {
    body(container.getComponents());
  }

  private void body(List components) {
    if (components.size() == 0)
      pp.text(" { }");
    else {
      pp.text(" {");
      pp.startNest(indent);
      pp.hardNewline();
      innerBody(components);
      pp.endNest();
      pp.hardNewline();
      pp.text("}");
    }
  }

  private void innerBody(List components) {
    boolean first = true;
    for (Iterator iter = components.iterator(); iter.hasNext();) {
      if (first)
        first = false;
      else
        pp.hardNewline();
      ((Component)iter.next()).accept(componentOutput);
    }
  }

  private void inherit(String ns) {
    if (ns.equals(nsb.getNamespaceUri("")))
      return;
    pp.softNewline(" ");
    pp.text("inherit = ");
    encodedText(nsb.getNonEmptyPrefix(ns));
  }

  private void identifier(String name) {
    if (keywordSet.contains(name))
      pp.text("\\");
    encodedText(name);
  }

  private static final String[] delims = { "\"", "'", "\"\"\"", "'''" };

  private void literal(String str) {
    for (int i = 0, len = str.length();;) {
      // Find the delimiter that gives the longest segment
      String bestDelim = null;
      int bestEnd = -1;
      int lim = str.indexOf('\n', i);
      if (lim < 0)
        lim = len;
      else
        ++lim;
      for (int j = 0; j < delims.length; j++) {
        int end = (str + delims[j]).indexOf(delims[j], i);
        if (end > bestEnd) {
          bestDelim = delims[j];
          bestEnd = end;
          if (end >= lim) {
            bestEnd = lim;
            break;
          }
        }
      }
      if (i != 0) {
        pp.text(" ~");
        pp.softNewline(" ");
      }
      pp.text(bestDelim);
      encodedText(str.substring(i, bestEnd));
      pp.text(bestDelim);
      i = bestEnd;
      if (i == len)
        break;
    }
  }

  private void encodedText(String str) {
    pp.text(encode(str));
  }

  private String encode(String str) {
    int start = 0;
    int len = str.length();
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      switch (c) {
      case '\\':
        if (!startsWithEscapeOpen(str, i))
          break;
        // fall through
      case '\r':
      case '\n':
        if (start < i)
          encodeBuf.append(str.substring(start, i));
        escape(c);
        start = i + 1;
        break;
      default:
        if (Utf16.isSurrogate(c)) {
          if (!cr.contains(c, str.charAt(i + 1))) {
            if (start < i)
              encodeBuf.append(str.substring(start, i));
            escape(Utf16.scalarValue(c, str.charAt(i + 1)));
            start = i + 2;
          }
          ++i;
        }
        else if (!cr.contains(c)) {
          if (start < i)
            encodeBuf.append(str.substring(start, i));
          escape(c);
          start = i + 1;
        }
        break;
      }
    }
    if (start == 0)
      return str;
    if (start != len)
      encodeBuf.append(str.substring(start, len));
    str = encodeBuf.toString();
    encodeBuf.setLength(0);
    return str;
  }

  private void escape(int n) {
    encodeBuf.append("\\x{");
    encodeBuf.append(Integer.toHexString(n));
    encodeBuf.append("}");
  }

  static private boolean startsWithEscapeOpen(String str, int off) {
    if (!str.startsWith("\\x", off))
      return false;
    for (off += 2; str.startsWith("x", off); off++)
      ;
    return str.startsWith("{", off);
  }

  /**
   * null means no prefix
   */
  private void qualifiedName(String ns, String prefix, String localName, boolean isAttribute) {
    prefix = choosePrefix(ns, prefix, isAttribute);
    if (prefix == null)
      encodedText(localName);
    else {
      encodedText(prefix);
      pp.text(":");
      encodedText(localName);
    }
  }

  /**
   * null means no prefix
   */
  private String choosePrefix(String ns, String prefix, boolean isAttribute) {
    if (prefix != null && ns.equals(nsb.getNamespaceUri(prefix)))
      return prefix;
    if (isAttribute) {
      if (ns.length() == 0)
        return null;
    }
    else {
      if (ns.equals(nsb.getNamespaceUri("")))
        return null;
    }
    return nsb.getNonEmptyPrefix(ns);
  }

  private void comment(String delim, String value) {
    int i = 0;
    for (;;) {
      pp.text(delim);
      if (i < value.length() && value.charAt(i) != '\t')
        pp.text(" ");
      int j = value.indexOf('\n', i);
      String tem = j < 0 ? value.substring(i) : value.substring(i, j);
      encodedText(tem);
      pp.hardNewline();
      if (j < 0)
        break;
      i = j + 1;
    }
  }

  private void checkContext(Context context, SourceLocation loc) {
    if (context == null)
      return;
    for (Enumeration e = context.prefixes(); e.hasMoreElements();) {
      String prefix = (String)e.nextElement();
      // Default namespace is not relevant to annotations
      if (!prefix.equals("")) {
        String ns = context.resolveNamespacePrefix(prefix);
        if (ns != null && !ns.equals(SchemaBuilder.INHERIT_NS)
            && !nsb.getNamespaceUri(prefix).equals(ns))
          er.warning("annotation_inconsistent_binding", prefix, ns, loc);
      }
    }
  }
}
