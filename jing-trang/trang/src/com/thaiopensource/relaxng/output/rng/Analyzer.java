package com.thaiopensource.relaxng.output.rng;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.NameClassedPattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.AnnotationChild;
import com.thaiopensource.relaxng.edit.ElementAnnotation;
import com.thaiopensource.relaxng.edit.Param;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.parse.Context;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

class Analyzer extends AbstractVisitor {

  private Object visitAnnotated(Annotated anno) {
    if (anno.getAttributeAnnotations().size() > 0
        || anno.getChildElementAnnotations().size() > 0
        || anno.getFollowingElementAnnotations().size() > 0)
      noteContext(anno.getContext());
    visitAnnotationAttributes(anno.getAttributeAnnotations());
    visitAnnotationChildren(anno.getChildElementAnnotations());
    visitAnnotationChildren(anno.getFollowingElementAnnotations());
    return null;
  }

  private void visitAnnotationAttributes(List list) {
    for (int i = 0, len = list.size(); i < len; i++) {
      AttributeAnnotation att = (AttributeAnnotation)list.get(i);
      if (att.getNamespaceUri().length() != 0)
        noteNs(att.getPrefix(), att.getNamespaceUri());
    }
  }

  private void visitAnnotationChildren(List list) {
    for (int i = 0, len = list.size(); i < len; i++) {
      AnnotationChild ac = (AnnotationChild)list.get(i);
      if (ac instanceof ElementAnnotation) {
        ElementAnnotation elem = (ElementAnnotation)ac;
        if (elem.getPrefix() != null)
          noteNs(elem.getPrefix(), elem.getNamespaceUri());
        visitAnnotationAttributes(elem.getAttributes());
        visitAnnotationChildren(elem.getChildren());
      }
    }
  }

  public Object visitPattern(Pattern p) {
    return visitAnnotated(p);
  }

  public Object visitDefine(DefineComponent c) {
    visitAnnotated(c);
    return c.getBody().accept(this);
  }

  public Object visitDiv(DivComponent c) {
    visitAnnotated(c);
    return visitContainer(c);
  }

  public Object visitInclude(IncludeComponent c) {
    visitAnnotated(c);
    return visitContainer(c);
  }

  public Object visitGrammar(GrammarPattern p) {
    visitAnnotated(p);
    return visitContainer(p);
  }

  private Object visitContainer(Container c) {
    List list = c.getComponents();
    for (int i = 0, len = list.size(); i < len; i++)
      ((Component)list.get(i)).accept(this);
    return null;
  }

  public Object visitUnary(UnaryPattern p) {
    visitAnnotated(p);
    return p.getChild().accept(this);
  }

  public Object visitComposite(CompositePattern p) {
    visitAnnotated(p);
    List list = p.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      ((Pattern)list.get(i)).accept(this);
    return null;
  }

  public Object visitNameClassed(NameClassedPattern p) {
    p.getNameClass().accept(this);
    return visitUnary(p);
  }

  public Object visitAttribute(AttributePattern p) {
    NameClass nc = p.getNameClass();
    if (nc instanceof NameNameClass
        && ((NameNameClass)nc).getNamespaceUri().equals(""))
      return visitUnary(p);
    return visitNameClassed(p);
  }

  public Object visitChoice(ChoiceNameClass nc) {
    visitAnnotated(nc);
    List list = nc.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      ((NameClass)list.get(i)).accept(this);
    return null;
  }

  public Object visitValue(ValuePattern p) {
    visitAnnotated(p);
    if (!p.getType().equals("token") || !p.getDatatypeLibrary().equals(""))
      noteDatatypeLibrary(p.getDatatypeLibrary());
    for (Iterator iter = p.getPrefixMap().entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      noteNs((String)entry.getKey(), (String)entry.getValue());
    }
    return null;
  }

  public Object visitData(DataPattern p) {
    visitAnnotated(p);
    noteDatatypeLibrary(p.getDatatypeLibrary());
    Pattern except = p.getExcept();
    if (except != null)
      except.accept(this);
    for (Iterator iter = p.getParams().iterator(); iter.hasNext();)
      visitAnnotated((Param)iter.next());      
    return null;
  }

  public Object visitName(NameNameClass nc) {
    visitAnnotated(nc);
    noteNs(nc.getPrefix(), nc.getNamespaceUri());
    return null;
  }

  public Object visitAnyName(AnyNameNameClass nc) {
    visitAnnotated(nc);
    NameClass except = nc.getExcept();
    if (except != null)
      except.accept(this);
    return null;
  }

  public Object visitNsName(NsNameNameClass nc) {
    visitAnnotated(nc);
    noteInheritNs(nc.getNs());
    NameClass except = nc.getExcept();
    if (except != null)
      except.accept(this);
    return null;
  }

  private String datatypeLibrary = null;
  private final Map prefixMap = new HashMap();
  private boolean haveInherit = false;
  private Context lastContext = null;

  private void noteDatatypeLibrary(String uri) {
    if (datatypeLibrary == null || datatypeLibrary.length() == 0)
      datatypeLibrary = uri;
  }

  private void noteInheritNs(String ns) {
    if (ns == NameClass.INHERIT_NS)
      haveInherit = true;
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
    for (Enumeration enum = context.prefixes(); enum.hasMoreElements();) {
      String prefix = (String)enum.nextElement();
      noteNs(prefix, context.resolveNamespacePrefix(prefix));
    }
  }

  Map getPrefixMap() {
    if (haveInherit)
      prefixMap.remove("");
    prefixMap.put("xml", WellKnownNamespaces.XML);
    return prefixMap;
  }

  String getDatatypeLibrary() {
    return datatypeLibrary;
  }

}
