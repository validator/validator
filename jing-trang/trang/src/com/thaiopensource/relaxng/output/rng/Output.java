package com.thaiopensource.relaxng.output.rng;

import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.NameClassVisitor;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.AbstractRefPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.Param;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.OpenNameClass;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.AnnotationChild;
import com.thaiopensource.relaxng.edit.ElementAnnotation;
import com.thaiopensource.relaxng.edit.TextAnnotation;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.Comment;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.common.XmlWriter;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

class Output implements PatternVisitor, NameClassVisitor, ComponentVisitor {
  private final String sourceUri;
  private final OutputDirectory od;
  private final XmlWriter xw;
  private final String datatypeLibrary;
  private final Map prefixMap;

  static public void output(Pattern p, String encoding, String sourceUri, OutputDirectory od, String datatypeLibrary, Map prefixMap) throws IOException {
    try {
      Output out = new Output(sourceUri, encoding, od, datatypeLibrary, prefixMap);
      p.accept(out);
      out.xw.close();
    }
    catch (XmlWriter.WrappedException e) {
      throw e.getIOException();
    }
  }

  private Output(String sourceUri, String encoding, OutputDirectory od, String datatypeLibrary, Map prefixMap) throws IOException {
    this.sourceUri = sourceUri;
    this.od = od;
    this.datatypeLibrary = datatypeLibrary;
    this.prefixMap = prefixMap;
    OutputDirectory.Stream stream = od.open(sourceUri, encoding);
    this.xw = new XmlWriter(stream.getWriter(), stream.getEncoding(), stream.getCharRepertoire(),
                            od.getLineSeparator(), od.getIndent(), getTopLevelAttributes());
  }

  private String[] getTopLevelAttributes() {
    int nAtts = prefixMap.size();
    if (datatypeLibrary != null)
      nAtts += 1;
    String[] atts = new String[nAtts * 2];
    int i = 0;
    for (Iterator iter = prefixMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      String prefix = (String)entry.getKey();
      if (!prefix.equals("xml")) {
        if (prefix.equals(""))
          atts[i++] = "ns";
        else
          atts[i++] = "xmlns:" + prefix;
        atts[i++] = (String)entry.getValue();
      }
    }
    atts[i++] = "xmlns";
    atts[i++] = WellKnownNamespaces.RELAX_NG;
    if (datatypeLibrary != null) {
      atts[i++] = "datatypeLibrary";
      atts[i++] = datatypeLibrary;
    }
    return atts;
  }

  public Object visitElement(ElementPattern p) {
    leadingAnnotations(p);
    xw.startElement("element");
    boolean usedNameAtt = tryNameAttribute(p.getNameClass(), false);
    innerAnnotations(p);
    if (!usedNameAtt)
      p.getNameClass().accept(this);
    implicitGroup(p.getChild());
    end(p);
    return null;
  }

  public Object visitAttribute(AttributePattern p) {
    leadingAnnotations(p);
    xw.startElement("attribute");
    boolean usedNameAtt = tryNameAttribute(p.getNameClass(), true);
    innerAnnotations(p);
    if (!usedNameAtt)
      p.getNameClass().accept(this);
    Pattern child = p.getChild();
    if (!(child instanceof TextPattern) || hasAnnotations(child))
      child.accept(this);
    end(p);
    return null;
  }

  private boolean tryNameAttribute(NameClass nc, boolean isAttribute) {
    if (hasAnnotations(nc))
      return false;
    if (!(nc instanceof NameNameClass))
      return false;
    NameNameClass nnc = (NameNameClass)nc;
    String ns = nnc.getNamespaceUri();
    if (ns == NameClass.INHERIT_NS) {
      if (isAttribute)
        return false;
      xw.attribute("name", nnc.getLocalName());
      return true;
    }
    if (ns.length() == 0) {
      if (!isAttribute && !"".equals(prefixMap.get("")))
        return false;
      xw.attribute("name", nnc.getLocalName());
      return true;
    }
    String prefix = nnc.getPrefix();
    if (prefix == null) {
      if (!ns.equals(prefixMap.get("")))
        return false;
      xw.attribute("name", nnc.getLocalName());
    }
    else {
      if (!ns.equals(prefixMap.get(prefix)))
        xw.attribute("xmlns:" + prefix, ns);
      xw.attribute("name", prefix + ":" + nnc.getLocalName());
    }
    return true;
  }

  public Object visitOneOrMore(OneOrMorePattern p) {
    return visitUnary("oneOrMore", p);
  }

  public Object visitZeroOrMore(ZeroOrMorePattern p) {
    return visitUnary("zeroOrMore", p);
  }

  public Object visitOptional(OptionalPattern p) {
    return visitUnary("optional", p);
  }

  public Object visitInterleave(InterleavePattern p) {
    return visitComposite("interleave", p);
  }

  public Object visitGroup(GroupPattern p) {
    return visitComposite("group", p);
  }

  public Object visitChoice(ChoicePattern p) {
    return visitComposite("choice", p);
  }

  public Object visitGrammar(GrammarPattern p) {
    leadingAnnotations(p);
    xw.startElement("grammar");
    finishContainer(p, p);
    return null;
  }

  public Object visitExternalRef(ExternalRefPattern p) {
    leadingAnnotations(p);
    xw.startElement("externalRef");
    xw.attribute("href", od.reference(sourceUri, p.getHref()));
    if (p.getNs() != NameClass.INHERIT_NS
        && !p.getNs().equals(prefixMap.get("")))
      xw.attribute("ns", p.getNs());
    innerAnnotations(p);
    end(p);
    return null;
  }

  public Object visitRef(RefPattern p) {
    return visitAbstractRef("ref", p);
  }

  public Object visitParentRef(ParentRefPattern p) {
    return visitAbstractRef("parentRef", p);
  }

  private Object visitAbstractRef(String name, AbstractRefPattern p) {
    leadingAnnotations(p);
    xw.startElement(name);
    xw.attribute("name", p.getName());
    innerAnnotations(p);
    end(p);
    return null;
  }

  public Object visitValue(ValuePattern p) {
    leadingAnnotations(p);
    xw.startElement("value");
    if (!p.getType().equals("token")
        || !p.getDatatypeLibrary().equals("")) {
      xw.attribute("type", p.getType());
      if (!p.getDatatypeLibrary().equals(datatypeLibrary))
        xw.attribute("datatypeLibrary", p.getDatatypeLibrary());
      for (Iterator iter = p.getPrefixMap().entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String prefix = (String)entry.getKey();
        String ns = (String)entry.getValue();
        if (ns != NameClass.INHERIT_NS && !ns.equals(prefixMap.get(prefix)))
          xw.attribute(prefix.length() == 0 ? "ns" : "xmlns:" + prefix,
                       ns);
      }
    }
    innerAnnotations(p);
    xw.text(p.getValue());
    end(p);
    return null;
  }

  public Object visitData(DataPattern p) {
    leadingAnnotations(p);
    xw.startElement("data");
    xw.attribute("type", p.getType());
    if (!p.getDatatypeLibrary().equals(datatypeLibrary))
      xw.attribute("datatypeLibrary", p.getDatatypeLibrary());
    innerAnnotations(p);
    List list = p.getParams();
    for (int i = 0, len = list.size(); i < len; i++) {
      Param param = (Param)list.get(i);
      leadingAnnotations(param);
      xw.startElement("param");
      xw.attribute("name", param.getName());
      innerAnnotations(param);
      xw.text(param.getValue());
      end(param);
    }
    Pattern except = p.getExcept();
    if (except != null) {
      xw.startElement("except");
      implicitChoice(except);
      xw.endElement();
    }
    end(p);
    return null;
  }

  public Object visitMixed(MixedPattern p) {
    return visitUnary("mixed", p);
  }

  public Object visitList(ListPattern p) {
    return visitUnary("list", p);
  }

  public Object visitText(TextPattern p) {
    return visitNullary("text", p);
  }

  public Object visitEmpty(EmptyPattern p) {
    return visitNullary("empty", p);
  }

  public Object visitNotAllowed(NotAllowedPattern p) {
    return visitNullary("notAllowed", p);
  }

  private Object visitNullary(String name, Pattern p) {
    leadingAnnotations(p);
    xw.startElement(name);
    innerAnnotations(p);
    end(p);
    return null;
  }

  private Object visitUnary(String name, UnaryPattern p) {
    leadingAnnotations(p);
    xw.startElement(name);
    innerAnnotations(p);
    implicitGroup(p.getChild());
    end(p);
    return null;
  }

  private Object visitComposite(String name, CompositePattern p) {
    leadingAnnotations(p);
    xw.startElement(name);
    innerAnnotations(p);
    List list = p.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      ((Pattern)list.get(i)).accept(this);
    end(p);
    return null;
  }

  public Object visitChoice(ChoiceNameClass nc) {
    leadingAnnotations(nc);
    xw.startElement("choice");
    innerAnnotations(nc);
    List list = nc.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      ((NameClass)list.get(i)).accept(this);
    end(nc);
    return null;
  }

  public Object visitAnyName(AnyNameNameClass nc) {
    leadingAnnotations(nc);
    xw.startElement("anyName");
    innerAnnotations(nc);
    visitExcept(nc);
    end(nc);
    return null;
  }

  public Object visitNsName(NsNameNameClass nc) {
    leadingAnnotations(nc);
    xw.startElement("nsName");
    if (nc.getNs() != NameClass.INHERIT_NS
        && !nc.getNs().equals(prefixMap.get("")))
      xw.attribute("ns", nc.getNs());
    innerAnnotations(nc);
    visitExcept(nc);
    end(nc);
    return null;
  }

  private void visitExcept(OpenNameClass onc) {
    NameClass except = onc.getExcept();
    if (except == null)
      return;
    xw.startElement("except");
    implicitChoice(except);
    xw.endElement();
  }

  public Object visitName(NameNameClass nc) {
    leadingAnnotations(nc);
    xw.startElement("name");
    String ns = nc.getNamespaceUri();
    if (ns == NameClass.INHERIT_NS) {
      innerAnnotations(nc);
      xw.text(nc.getLocalName());
    }
    else {
      String prefix = nc.getPrefix();
      if (prefix == null || ns.length() == 0) {
        if (!ns.equals(prefixMap.get("")))
          xw.attribute("ns", ns);
        innerAnnotations(nc);
        xw.text(nc.getLocalName());
      }
      else {
        if (!ns.equals(prefixMap.get(prefix)))
          xw.attribute("xmlns:" + prefix, ns);
        innerAnnotations(nc);
        xw.text(prefix + ":" + nc.getLocalName());
      }
    }
    end(nc);
    return null;
  }

  public Object visitDefine(DefineComponent c) {
    leadingAnnotations(c);
    String name = c.getName();
    if (name == c.START)
      xw.startElement("start");
    else {
      xw.startElement("define");
      xw.attribute("name", name);
    }
    if (c.getCombine() != null)
      xw.attribute("combine", c.getCombine().toString());
    innerAnnotations(c);
    implicitGroup(c.getBody());
    end(c);
    return null;
  }

  public Object visitDiv(DivComponent c) {
    leadingAnnotations(c);
    xw.startElement("div");
    finishContainer(c, c);
    return null;
  }

  public Object visitInclude(IncludeComponent c) {
    leadingAnnotations(c);
    xw.startElement("include");
    xw.attribute("href", od.reference(sourceUri, c.getHref()));
    finishContainer(c, c);
    return null;
  }

  private void finishContainer(Annotated subject, Container container) {
    innerAnnotations(subject);
    List list = container.getComponents();
    for (int i = 0, len = list.size(); i < len; i++)
      ((Component)list.get(i)).accept(this);
    end(subject);
  }

  private void leadingAnnotations(Annotated subject) {
    annotationChildren(subject.getLeadingComments(), true);
  }

  private void innerAnnotations(Annotated subject) {
    annotationAttributes(subject.getAttributeAnnotations());
    annotationChildren(subject.getChildElementAnnotations(), true);
  }

  private void outerAnnotations(Annotated subject) {
    annotationChildren(subject.getFollowingElementAnnotations(), true);
  }

  private void annotationAttributes(List list) {
    for (int i = 0, len = list.size(); i < len; i++) {
      AttributeAnnotation att = (AttributeAnnotation)list.get(i);
      String name = att.getLocalName();
      String prefix = att.getPrefix();
      xw.attribute(prefix == null ? name : prefix + ":" + name,
                   att.getValue());
    }
  }

  private void annotationChildren(List list, boolean haveDefaultNamespace) {
    for (int i = 0, len = list.size(); i < len; i++) {
      AnnotationChild child = (AnnotationChild)list.get(i);
      if (child instanceof ElementAnnotation) {
        ElementAnnotation elem = (ElementAnnotation)child;
        String name = elem.getLocalName();
        String prefix = elem.getPrefix();
        if (prefix == null) {
          xw.startElement(name);
          if (haveDefaultNamespace) {
            xw.attribute("xmlns", "");
            haveDefaultNamespace = false;
          }
        }
        else
          xw.startElement(prefix + ":" + name);
        annotationAttributes(elem.getAttributes());
        annotationChildren(elem.getChildren(), haveDefaultNamespace);
        xw.endElement();
      }
      else if (child instanceof TextAnnotation)
        xw.text(((TextAnnotation)child).getValue());
      else if (child instanceof Comment)
        xw.comment(fixupComment(((Comment)child).getValue()));
    }
  }

  static private String fixupComment(String comment) {
    int i = 0;
    for (;;) {
      int j = comment.indexOf('-', i);
      if (j < 0)
        break;
      if (j == comment.length() - 1)
        return comment + " ";
      if (comment.charAt(j + 1) == '-')
        return comment.substring(0, j) + "- " + fixupComment(comment.substring(j + 1));
      i = j + 1;
    }
    return comment;
  }

  private void end(Annotated subject) {
    xw.endElement();
    outerAnnotations(subject);
  }

  private void implicitGroup(Pattern p) {
    if (!hasAnnotations(p) && p instanceof GroupPattern) {
      List list = ((GroupPattern)p).getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Pattern)list.get(i)).accept(this);
    }
    else
      p.accept(this);
  }

  private void implicitChoice(Pattern p) {
    if (!hasAnnotations(p) && p instanceof ChoicePattern) {
      List list = ((ChoicePattern)p).getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Pattern)list.get(i)).accept(this);
    }
    else
      p.accept(this);
  }

  private void implicitChoice(NameClass nc) {
    if (!hasAnnotations(nc) && nc instanceof ChoiceNameClass) {
      List list = ((ChoiceNameClass)nc).getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        ((NameClass)list.get(i)).accept(this);
    }
    else
      nc.accept(this);
  }

  private static boolean hasAnnotations(Annotated subject) {
    return (!subject.getLeadingComments().isEmpty()
            || !subject.getAttributeAnnotations().isEmpty()
            || !subject.getChildElementAnnotations().isEmpty()
            || !subject.getFollowingElementAnnotations().isEmpty());
  }
}
