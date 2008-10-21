package com.thaiopensource.relaxng.output.rng;

import com.thaiopensource.relaxng.edit.AbstractRefPattern;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.AnnotationChild;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Comment;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementAnnotation;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NameClassVisitor;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OpenNameClass;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Param;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.TextAnnotation;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.common.XmlWriter;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class Output implements PatternVisitor<VoidValue>, NameClassVisitor<VoidValue>, ComponentVisitor<VoidValue> {
  private final String sourceUri;
  private final OutputDirectory od;
  private final XmlWriter xw;
  private final String datatypeLibrary;
  private final Map<String, String> prefixMap;
  private String localNs = null;

  static public void output(Pattern p, String encoding, String sourceUri, OutputDirectory od, String datatypeLibrary, Map<String, String> prefixMap) throws IOException {
    try {
      Output out = new Output(sourceUri, encoding, od, datatypeLibrary, prefixMap);
      p.accept(out);
      out.xw.close();
    }
    catch (XmlWriter.WrappedException e) {
      throw e.getIOException();
    }
  }

  private Output(String sourceUri, String encoding, OutputDirectory od, String datatypeLibrary, Map<String, String> prefixMap) throws IOException {
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
    for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
      String prefix = entry.getKey();
      if (!prefix.equals("xml")) {
        if (prefix.equals(""))
          atts[i++] = "ns";
        else
          atts[i++] = "xmlns:" + prefix;
        atts[i++] = entry.getValue();
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

  public VoidValue visitElement(ElementPattern p) {
    leadingAnnotations(p);
    xw.startElement("element");
    boolean usedNameAtt = tryNameAttribute(p.getNameClass(), false);
    innerAnnotations(p);
    if (!usedNameAtt)
      p.getNameClass().accept(this);
    implicitGroup(p.getChild());
    end(p);
    return VoidValue.VOID;
  }

  public VoidValue visitAttribute(AttributePattern p) {
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
    return VoidValue.VOID;
  }

  private boolean tryNameAttribute(NameClass nc, boolean isAttribute) {
    if (hasAnnotations(nc))
      return false;
    if (!(nc instanceof NameNameClass))
      return false;
    NameNameClass nnc = (NameNameClass)nc;
    String ns = nnc.getNamespaceUri();
    if (ns == NameClass.INHERIT_NS) {
      if (isAttribute || lookupPrefix("") != null)
        return false;
      xw.attribute("name", nnc.getLocalName());
      return true;
    }
    if (ns.length() == 0) {
      if (!isAttribute && !"".equals(lookupPrefix("")))
        return false;
      xw.attribute("name", nnc.getLocalName());
      return true;
    }
    String prefix = nnc.getPrefix();
    if (prefix == null) {
      if (isAttribute || !ns.equals(lookupPrefix("")))
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

  public VoidValue visitOneOrMore(OneOrMorePattern p) {
    return visitUnary("oneOrMore", p);
  }

  public VoidValue visitZeroOrMore(ZeroOrMorePattern p) {
    return visitUnary("zeroOrMore", p);
  }

  public VoidValue visitOptional(OptionalPattern p) {
    return visitUnary("optional", p);
  }

  public VoidValue visitInterleave(InterleavePattern p) {
    return visitComposite("interleave", p);
  }

  public VoidValue visitGroup(GroupPattern p) {
    return visitComposite("group", p);
  }

  public VoidValue visitChoice(ChoicePattern p) {
    return visitComposite("choice", p);
  }

  public VoidValue visitGrammar(GrammarPattern p) {
    leadingAnnotations(p);
    xw.startElement("grammar");
    finishContainer(p, p);
    return VoidValue.VOID;
  }

  public VoidValue visitExternalRef(ExternalRefPattern p) {
    leadingAnnotations(p);
    xw.startElement("externalRef");
    xw.attribute("href", od.reference(sourceUri, p.getHref()));
    nsAttribute(p.getNs());
    innerAnnotations(p);
    end(p);
    return VoidValue.VOID;
  }

  public VoidValue visitRef(RefPattern p) {
    return visitAbstractRef("ref", p);
  }

  public VoidValue visitParentRef(ParentRefPattern p) {
    return visitAbstractRef("parentRef", p);
  }

  private VoidValue visitAbstractRef(String name, AbstractRefPattern p) {
    leadingAnnotations(p);
    xw.startElement(name);
    xw.attribute("name", p.getName());
    innerAnnotations(p);
    end(p);
    return VoidValue.VOID;
  }

  public VoidValue visitValue(ValuePattern p) {
    leadingAnnotations(p);
    xw.startElement("value");
    if (!p.getType().equals("token")
        || !p.getDatatypeLibrary().equals("")) {
      xw.attribute("type", p.getType());
      if (!p.getDatatypeLibrary().equals(datatypeLibrary))
        xw.attribute("datatypeLibrary", p.getDatatypeLibrary());
      for (Map.Entry<String, String> entry : p.getPrefixMap().entrySet()) {
        String prefix = entry.getKey();
        String ns = entry.getValue();
        if (prefix.length() == 0)
          nsAttribute(ns);
        else if (ns != NameClass.INHERIT_NS && !ns.equals(lookupPrefix(prefix)))
          xw.attribute("xmlns:" + prefix, ns);
      }
    }
    innerAnnotations(p);
    xw.text(p.getValue());
    end(p);
    return VoidValue.VOID;
  }

  public VoidValue visitData(DataPattern p) {
    leadingAnnotations(p);
    xw.startElement("data");
    xw.attribute("type", p.getType());
    if (!p.getDatatypeLibrary().equals(datatypeLibrary))
      xw.attribute("datatypeLibrary", p.getDatatypeLibrary());
    innerAnnotations(p);
    List<Param> list = p.getParams();
    for (int i = 0, len = list.size(); i < len; i++) {
      Param param = list.get(i);
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
    return VoidValue.VOID;
  }

  public VoidValue visitMixed(MixedPattern p) {
    return visitUnary("mixed", p);
  }

  public VoidValue visitList(ListPattern p) {
    return visitUnary("list", p);
  }

  public VoidValue visitText(TextPattern p) {
    return visitNullary("text", p);
  }

  public VoidValue visitEmpty(EmptyPattern p) {
    return visitNullary("empty", p);
  }

  public VoidValue visitNotAllowed(NotAllowedPattern p) {
    return visitNullary("notAllowed", p);
  }

  private VoidValue visitNullary(String name, Pattern p) {
    leadingAnnotations(p);
    xw.startElement(name);
    innerAnnotations(p);
    end(p);
    return VoidValue.VOID;
  }

  private VoidValue visitUnary(String name, UnaryPattern p) {
    leadingAnnotations(p);
    xw.startElement(name);
    innerAnnotations(p);
    implicitGroup(p.getChild());
    end(p);
    return VoidValue.VOID;
  }

  private VoidValue visitComposite(String name, CompositePattern p) {
    leadingAnnotations(p);
    xw.startElement(name);
    innerAnnotations(p);
    List<Pattern> list = p.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      (list.get(i)).accept(this);
    end(p);
    return VoidValue.VOID;
  }

  public VoidValue visitChoice(ChoiceNameClass nc) {
    leadingAnnotations(nc);
    xw.startElement("choice");
    innerAnnotations(nc);
    List<NameClass> list = nc.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      (list.get(i)).accept(this);
    end(nc);
    return VoidValue.VOID;
  }

  public VoidValue visitAnyName(AnyNameNameClass nc) {
    leadingAnnotations(nc);
    xw.startElement("anyName");
    innerAnnotations(nc);
    visitExcept(nc);
    end(nc);
    return VoidValue.VOID;
  }

  public VoidValue visitNsName(NsNameNameClass nc) {
    leadingAnnotations(nc);
    xw.startElement("nsName");
    String saveNs = localNs;
    localNs = nsAttribute(nc.getNs());
    innerAnnotations(nc);
    visitExcept(nc);
    localNs = saveNs;
    end(nc);
    return VoidValue.VOID;
  }

  private void visitExcept(OpenNameClass onc) {
    NameClass except = onc.getExcept();
    if (except == null)
      return;
    xw.startElement("except");
    implicitChoice(except);
    xw.endElement();
  }

  public VoidValue visitName(NameNameClass nc) {
    leadingAnnotations(nc);
    xw.startElement("name");
    String ns = nc.getNamespaceUri();
    if (ns == NameClass.INHERIT_NS) {
      nsAttribute(ns);
      innerAnnotations(nc);
      xw.text(nc.getLocalName());
    }
    else {
      String prefix = nc.getPrefix();
      if (prefix == null || ns.length() == 0) {
        nsAttribute(ns);
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
    return VoidValue.VOID;
  }

  public VoidValue visitDefine(DefineComponent c) {
    leadingAnnotations(c);
    String name = c.getName();
    if (name == DefineComponent.START)
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
    return VoidValue.VOID;
  }

  public VoidValue visitDiv(DivComponent c) {
    leadingAnnotations(c);
    xw.startElement("div");
    finishContainer(c, c);
    return VoidValue.VOID;
  }

  public VoidValue visitInclude(IncludeComponent c) {
    leadingAnnotations(c);
    xw.startElement("include");
    xw.attribute("href", od.reference(sourceUri, c.getHref()));
    String saveNs = localNs;
    localNs = nsAttribute(c.getNs());
    finishContainer(c, c);
    localNs = saveNs;
    return VoidValue.VOID;
  }

  private void finishContainer(Annotated subject, Container container) {
    innerAnnotations(subject);
    List<Component> list = container.getComponents();
    for (int i = 0, len = list.size(); i < len; i++)
      (list.get(i)).accept(this);
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

  private void annotationAttributes(List<AttributeAnnotation> list) {
    for (int i = 0, len = list.size(); i < len; i++) {
      AttributeAnnotation att = list.get(i);
      String name = att.getLocalName();
      String prefix = att.getPrefix();
      xw.attribute(prefix == null ? name : prefix + ":" + name,
                   att.getValue());
    }
  }

  private void annotationChildren(List<? extends AnnotationChild> list, boolean haveDefaultNamespace) {
    for (int i = 0, len = list.size(); i < len; i++) {
      AnnotationChild child = list.get(i);
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
      List<Pattern> list = ((GroupPattern)p).getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        (list.get(i)).accept(this);
    }
    else
      p.accept(this);
  }

  private void implicitChoice(Pattern p) {
    if (!hasAnnotations(p) && p instanceof ChoicePattern) {
      List<Pattern> list = ((ChoicePattern)p).getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        (list.get(i)).accept(this);
    }
    else
      p.accept(this);
  }

  private void implicitChoice(NameClass nc) {
    if (!hasAnnotations(nc) && nc instanceof ChoiceNameClass) {
      List<NameClass> list = ((ChoiceNameClass)nc).getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        (list.get(i)).accept(this);
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

  private String nsAttribute(String ns) {
    if (ns == NameClass.INHERIT_NS) {
      if (lookupPrefix("") != null) {
        // cannot do it exactly; this is the best approximation
        xw.attribute("ns", "");
        return "";
      }
    }
    else if (!ns.equals(lookupPrefix(""))) {
      xw.attribute("ns", ns);
      return ns;
    }
    return localNs;
  }

  private String lookupPrefix(String prefix) {
    if (prefix.equals("") && localNs != null)
      return localNs;
    return prefixMap.get(prefix);
  }
}
