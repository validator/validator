package com.thaiopensource.relaxng.edit;

import com.thaiopensource.util.VoidValue;

public class AbstractVisitor extends AbstractPatternVisitor<VoidValue> implements ComponentVisitor<VoidValue>, NameClassVisitor<VoidValue>,
        AnnotationChildVisitor<VoidValue>, AttributeAnnotationVisitor<VoidValue> {

  public VoidValue visitPattern(Pattern p) {
    return VoidValue.VOID;
  }

  public VoidValue visitDefine(DefineComponent c) {
    return visitComponent(c);
  }

  public VoidValue visitDiv(DivComponent c) {
    return visitComponent(c);
  }

  public VoidValue visitInclude(IncludeComponent c) {
    return visitComponent(c);
  }

  public VoidValue visitComponent(Component c) {
    return VoidValue.VOID;
  }

  public VoidValue visitChoice(ChoiceNameClass nc) {
    return visitNameClass(nc);
  }

  public VoidValue visitAnyName(AnyNameNameClass nc) {
    return visitNameClass(nc);
  }

  public VoidValue visitNsName(NsNameNameClass nc) {
    return visitNameClass(nc);
  }

  public VoidValue visitName(NameNameClass nc) {
    return visitNameClass(nc);
  }

  public VoidValue visitNameClass(NameClass nc) {
    return VoidValue.VOID;
  }

  public VoidValue visitText(TextAnnotation ta) {
    return visitAnnotationChild(ta);
  }

  public VoidValue visitComment(Comment c) {
    return visitAnnotationChild(c);
  }

  public VoidValue visitElement(ElementAnnotation ea) {
    return visitAnnotationChild(ea);
  }

  public VoidValue visitAnnotationChild(AnnotationChild ac) {
    return VoidValue.VOID;
  }

  public VoidValue visitAttribute(AttributeAnnotation a) {
    return VoidValue.VOID;
  }
}
