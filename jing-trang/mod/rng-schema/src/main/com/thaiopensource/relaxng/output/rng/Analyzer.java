package com.thaiopensource.relaxng.output.rng;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.AnnotationChild;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementAnnotation;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NameClassedPattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.Param;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.relaxng.parse.Context;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Analyzer extends AbstractVisitor {

  private VoidValue visitAnnotated(Annotated anno) {
    if (anno.getAttributeAnnotations().size() > 0
        || anno.getChildElementAnnotations().size() > 0
        || anno.getFollowingElementAnnotations().size() > 0)
      noteContext(anno.getContext());
    visitAnnotationAttributes(anno.getAttributeAnnotations());
    visitAnnotationChildren(anno.getChildElementAnnotations());
    visitAnnotationChildren(anno.getFollowingElementAnnotations());
    return VoidValue.VOID;
  }

  private void visitAnnotationAttributes(List<AttributeAnnotation> list) {
    for (int i = 0, len = list.size(); i < len; i++) {
      AttributeAnnotation att = list.get(i);
      if (att.getNamespaceUri().length() != 0)
        noteNs(att.getPrefix(), att.getNamespaceUri());
    }
  }

  private void visitAnnotationChildren(List<AnnotationChild> list) {
    for (int i = 0, len = list.size(); i < len; i++) {
      AnnotationChild ac = list.get(i);
      if (ac instanceof ElementAnnotation) {
        ElementAnnotation elem = (ElementAnnotation)ac;
        if (elem.getPrefix() != null)
          noteNs(elem.getPrefix(), elem.getNamespaceUri());
        visitAnnotationAttributes(elem.getAttributes());
        visitAnnotationChildren(elem.getChildren());
      }
    }
  }

  public VoidValue visitPattern(Pattern p) {
    return visitAnnotated(p);
  }

  public VoidValue visitDefine(DefineComponent c) {
    visitAnnotated(c);
    return c.getBody().accept(this);
  }

  public VoidValue visitDiv(DivComponent c) {
    visitAnnotated(c);
    return visitContainer(c);
  }

  public VoidValue visitInclude(IncludeComponent c) {
    visitAnnotated(c);
    noteInheritNs(c.getNs());
    return visitContainer(c);
  }

  public VoidValue visitGrammar(GrammarPattern p) {
    visitAnnotated(p);
    return visitContainer(p);
  }

  private VoidValue visitContainer(Container c) {
    List<Component> list = c.getComponents();
    for (int i = 0, len = list.size(); i < len; i++)
      (list.get(i)).accept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitUnary(UnaryPattern p) {
    visitAnnotated(p);
    return p.getChild().accept(this);
  }

  public VoidValue visitComposite(CompositePattern p) {
    visitAnnotated(p);
    List<Pattern> list = p.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      (list.get(i)).accept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitNameClassed(NameClassedPattern p) {
    p.getNameClass().accept(this);
    return visitUnary(p);
  }

  public VoidValue visitAttribute(AttributePattern p) {
    NameClass nc = p.getNameClass();
    if (nc instanceof NameNameClass
        && ((NameNameClass)nc).getNamespaceUri().equals(""))
      return visitUnary(p);
    return visitNameClassed(p);
  }

  public VoidValue visitChoice(ChoiceNameClass nc) {
    visitAnnotated(nc);
    List<NameClass> list = nc.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      (list.get(i)).accept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitValue(ValuePattern p) {
    visitAnnotated(p);
    if (!p.getType().equals("token") || !p.getDatatypeLibrary().equals(""))
      noteDatatypeLibrary(p.getDatatypeLibrary());
    for (Map.Entry<String, String> entry : p.getPrefixMap().entrySet()) {
      noteNs(entry.getKey(), entry.getValue());
    }
    return VoidValue.VOID;
  }

  public VoidValue visitData(DataPattern p) {
    visitAnnotated(p);
    noteDatatypeLibrary(p.getDatatypeLibrary());
    Pattern except = p.getExcept();
    if (except != null)
      except.accept(this);
    for (Param param : p.getParams())
      visitAnnotated(param);
    return VoidValue.VOID;
  }

  public VoidValue visitExternalRef(ExternalRefPattern p) {
    visitAnnotated(p);
    noteInheritNs(p.getNs());
    return VoidValue.VOID;
  }

  public VoidValue visitName(NameNameClass nc) {
    visitAnnotated(nc);
    noteNs(nc.getPrefix(), nc.getNamespaceUri());
    return VoidValue.VOID;
  }

  public VoidValue visitAnyName(AnyNameNameClass nc) {
    visitAnnotated(nc);
    NameClass except = nc.getExcept();
    if (except != null)
      except.accept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitNsName(NsNameNameClass nc) {
    visitAnnotated(nc);
    noteInheritNs(nc.getNs());
    NameClass except = nc.getExcept();
    if (except != null)
      except.accept(this);
    return VoidValue.VOID;
  }

  private String datatypeLibrary = null;
  private final Map<String, String> prefixMap = new HashMap<String, String>();
  private boolean haveInherit = false;
  private Context lastContext = null;
  private String noPrefixNs = null;

  private void noteDatatypeLibrary(String uri) {
    if (datatypeLibrary == null || datatypeLibrary.length() == 0)
      datatypeLibrary = uri;
  }

  private void noteInheritNs(String ns) {
    if (ns == NameClass.INHERIT_NS)
      haveInherit = true;
    else
      noPrefixNs = ns;
  }

  private void noteNs(String prefix, String ns) {
    if (ns == NameClass.INHERIT_NS) {
      haveInherit = true;
      return;
    }
    if (prefix == null)
      prefix = "";
    if (ns == null || (ns.length() == 0 && prefix.length() != 0) || prefixMap.containsKey(prefix))
      return;
    prefixMap.put(prefix, ns);
  }

  private void noteContext(Context context) {
    if (context == null || context == lastContext)
      return;
    lastContext = context;
    for (Enumeration e = context.prefixes(); e.hasMoreElements();) {
      String prefix = (String)e.nextElement();
      noteNs(prefix, context.resolveNamespacePrefix(prefix));
    }
  }

  Map<String, String> getPrefixMap() {
    if (haveInherit)
      prefixMap.remove("");
    else if (noPrefixNs != null && !prefixMap.containsKey(""))
      prefixMap.put("", noPrefixNs);
    prefixMap.put("xml", WellKnownNamespaces.XML);
    return prefixMap;
  }

  String getDatatypeLibrary() {
    return datatypeLibrary;
  }

}
