package com.thaiopensource.relaxng.output.rnc;

import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.NameClassVisitor;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.NameClassedPattern;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.Combine;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NullVisitor;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Collections;
import java.util.Map;

/*

Datatype declarations


Namespace declarations

Avoid top-level grammar element

Annotations
Comments
Encoding

Avoid lines with excessive complexity

Make use of ##

Make long literals pretty

Take advantage of
  default namespace x = "..."
*/
class Output {
  private final Prettyprinter pp;
  private final String sourceUri;
  private final OutputDirectory od;
  private final ErrorReporter er;
  private final NamespaceManager.NamespaceBindings nsb;
  private final NameClassVisitor nameClassOutput = new NameClassOutput();
  private final NameClassVisitor noParenNameClassOutput = new NoParenNameClassOutput();
  private final PatternVisitor noParenPatternOutput = new NoParenPatternOutput();
  private final PatternVisitor patternOutput = new PatternOutput();
  private final ComponentVisitor componentOutput = new ComponentOutput();
  private boolean isAttributeNameClass;

  static private final String indent = "  ";

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

  static void output(Pattern p, String sourceUri, OutputDirectory od, ErrorReporter er) throws IOException {
    try {
      new Output(sourceUri, od, er, NamespaceVisitor.createBindings(p)).topLevel(p);
    }
    catch (Prettyprinter.WrappedException e) {
      throw e.getIOException();
    }
  }

  private Output(String sourceUri, OutputDirectory od, ErrorReporter er, NamespaceManager.NamespaceBindings nsb) throws IOException {
    this.sourceUri = sourceUri;
    this.od = od;
    this.er = er;
    this.pp = new StreamingPrettyprinter(od.getLineLength(), od.getLineSeparator(), od.open(sourceUri));
    this.nsb = nsb;
  }

  private void topLevel(Pattern p) {
    outputNamespaceDeclarations();
    p.accept(patternOutput);
    pp.hardNewline();
    pp.close();
  }

  private void outputNamespaceDeclarations() {
    List prefixes = new Vector();
    prefixes.addAll(nsb.getPrefixes());
    Collections.sort(prefixes);

    for (Iterator iter = prefixes.iterator(); iter.hasNext();) {
      String prefix = (String)iter.next();
      String ns = nsb.getNamespaceUri(prefix);
      if (prefix.length() == 0) {
        if (!ns.equals(SchemaBuilder.INHERIT_NS)) {
          pp.text("default namespace = ");
          literal(ns);
          pp.hardNewline();
        }
      }
      else if (!prefix.equals("xml")) {
        pp.text("namespace ");
        pp.text(prefix);
        pp.text(" = ");
        if (ns.equals(SchemaBuilder.INHERIT_NS))
          pp.text("inherit");
        else
          literal(ns);
        pp.hardNewline();
      }
    }
  }

  static class NamespaceVisitor extends NullVisitor {
    private NamespaceManager nsm = new NamespaceManager();
    private boolean isAttribute;

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

    public void nullVisitValue(ValuePattern p) {
      super.nullVisitValue(p);
      for (Iterator iter = p.getPrefixMap().entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        nsm.requireBinding((String)entry.getKey(), (String)entry.getValue());
      }
    }

    static NamespaceManager.NamespaceBindings createBindings(Pattern p) {
      NamespaceVisitor nsv = new NamespaceVisitor();
      p.accept(nsv);
      return nsv.nsm.createBindings();
    }
  }

  class ComponentOutput implements ComponentVisitor {
    public Object visitDefine(DefineComponent c) {
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
      return null;
    }

    public Object visitDiv(DivComponent c) {
      pp.text("div");
      body(c);
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      pp.text("include ");
      literal(od.reference(sourceUri, c.getHref()));
      List components = c.getComponents();
      if (!components.isEmpty())
        body(components);
      return null;
    }
  }

  class NoParenPatternOutput implements PatternVisitor {
    public Object visitGrammar(GrammarPattern p) {
      pp.text("grammar");
      body(p);
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
      pp.text(key);
      pp.startNest(key);
      p.getNameClass().accept(noParenNameClassOutput);
      pp.endNest();
      pp.startGroup();
      pp.text(" {");
      pp.startNest(indent);
      pp.softNewline(" ");
      p.getChild().accept(noParenPatternOutput);
      pp.endNest();
      pp.softNewline(" ");
      pp.text("}");
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
      p.getChild().accept(patternOutput);
      pp.text(op);
    }

    public Object visitRef(RefPattern p) {
      identifier(p.getName());
      return null;
    }

    public Object visitParentRef(ParentRefPattern p) {
      pp.text("parent ");
      identifier(p.getName());
      return null;
    }

    public Object visitExternalRef(ExternalRefPattern p) {
      pp.text("external ");
      literal(od.reference(sourceUri, p.getHref()));
      return null;
    }

    public Object visitText(TextPattern p) {
      pp.text("text");
      return null;
    }

    public Object visitEmpty(EmptyPattern p) {
      pp.text("empty");
      return null;
    }

    public Object visitNotAllowed(NotAllowedPattern p) {
      pp.text("notAllowed");
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
      pp.text(key);
      pp.text(" {");
      pp.startNest(indent);
      pp.softNewline(" ");
      p.getChild().accept(noParenPatternOutput);
      pp.endNest();
      pp.softNewline(" ");
      pp.text("}");
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
      pp.startGroup();
      boolean first = true;
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();) {
        if (!first) {
          if (sepBeforeNewline)
            pp.text(sep);
          pp.softNewline(" ");
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
      pp.endGroup();
    }

    public Object visitData(DataPattern p) {
      String lib = p.getDatatypeLibrary();
      String prefix;
      if (lib.equals(WellKnownNamespaces.XML_SCHEMA_DATATYPES))
        prefix = "xsd:";
      else if (lib.equals(""))
        prefix = "";
      else
        // XXX
        prefix = "xxx:";
      pp.text(prefix);
      pp.text(p.getType());
      // XXX params
      // XXX except
      return null;
    }

    public Object visitValue(ValuePattern p) {
      // XXX datatype
      literal(p.getValue());
      return null;
    }

  }

  class PatternOutput extends NoParenPatternOutput {
    void composite(CompositePattern p, String sep, boolean sepBeforeNewline) {
      pp.text("(");
      pp.startNest("(");
      super.composite(p, sep, sepBeforeNewline);
      pp.endNest();
      pp.text(")");
    }
  }

  class NoParenNameClassOutput implements NameClassVisitor {
    public Object visitAnyName(AnyNameNameClass nc) {
      pp.text("*");
      // XXX except
      return null;
    }

    public Object visitNsName(NsNameNameClass nc) {
      pp.text(nsb.getNonEmptyPrefix(nc.getNs()));
      pp.text(":*");
      // XXX except
      return null;
    }

    public Object visitName(NameNameClass nc) {
      pp.text(qualifyName(nc.getNamespaceUri(), nc.getPrefix(), nc.getLocalName(), isAttributeNameClass));
      return null;
    }

    public Object visitChoice(ChoiceNameClass nc) {
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
      return null;
    }
  }

  class NameClassOutput extends NoParenNameClassOutput {
    public Object visitChoice(ChoiceNameClass nc) {
      pp.text("(");
      pp.startNest("(");
      super.visitChoice(nc);
      pp.endNest();
      pp.text(")");
      return null;
    }
  }

  private void body(Container container) {
    body(container.getComponents());
  }

  private void body(List components) {
    pp.text(" {");
    pp.startNest(indent);
    for (Iterator iter = components.iterator(); iter.hasNext();) {
      pp.hardNewline();
      ((Component)iter.next()).accept(componentOutput);
    }
    pp.endNest();
    pp.hardNewline();
    pp.text("}");
  }

  private void identifier(String name) {
    if (keywordSet.contains(name))
      pp.text("\\");
    pp.text(name);
  }

  static final String[] delims = { "\"", "'", "\"\"\"", "'''" };

  private void literal(String str) {
    for (int i = 0, len = str.length();;) {
      // Find the delimiter that gives the longest segment
      String bestDelim = null;
      int bestEnd = -1;
      for (int j = 0; j < delims.length; j++) {
        int end = (str + delims[j]).indexOf(delims[j], i);
        if (end > bestEnd) {
          bestDelim = delims[j];
          bestEnd = end;
          if (end == len)
            break;
        }
      }
      if (i != 0)
        pp.text(" ~ ");
      pp.text(bestDelim);
      // XXX need to deal NL, CR, \x{
      pp.text(str.substring(i, bestEnd));
      pp.text(bestDelim);
      i = bestEnd;
      if (i == len)
        break;
    }
  }

  /**
   * null means no prefix
   */
  private String qualifyName(String ns, String prefix, String localName, boolean isAttribute) {
    prefix = choosePrefix(ns, prefix, isAttribute);
    if (prefix == null)
      return localName;
    StringBuffer buf = new StringBuffer(prefix);
    buf.append(':');
    buf.append(localName);
    return buf.toString();
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

}
