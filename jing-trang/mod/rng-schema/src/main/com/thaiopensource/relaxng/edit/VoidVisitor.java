package com.thaiopensource.relaxng.edit;

import com.thaiopensource.util.VoidValue;

public class VoidVisitor implements PatternVisitor<VoidValue>, NameClassVisitor<VoidValue>, ComponentVisitor<VoidValue>,
        AnnotationChildVisitor<VoidValue>, AttributeAnnotationVisitor<VoidValue> {
  public final VoidValue visitElement(ElementPattern p) {
    voidVisitElement(p);
    return VoidValue.VOID;
  }

  public void voidVisitElement(ElementPattern p) {
    voidVisitPattern(p);
    p.getNameClass().accept(this);
    p.getChild().accept(this);
  }

  public void voidVisitPattern(Pattern p) {
    voidVisitAnnotated(p);
  }

  public void voidVisitAnnotated(Annotated p) {
    p.leadingCommentsAccept(this);
    p.attributeAnnotationsAccept(this);
    p.childElementAnnotationsAccept(this);
    p.followingElementAnnotationsAccept(this);
  }

  public final VoidValue visitChoice(ChoiceNameClass nc) {
    voidVisitChoice(nc);
    return VoidValue.VOID;
  }

  public void voidVisitChoice(ChoiceNameClass nc) {
    voidVisitNameClass(nc);
    nc.childrenAccept(this);
  }

  public void voidVisitNameClass(NameClass nc) {
    voidVisitAnnotated(nc);
  }

  public final VoidValue visitDiv(DivComponent c) {
    voidVisitDiv(c);
    return VoidValue.VOID;
  }

  public void voidVisitDiv(DivComponent c) {
    voidVisitComponent(c);
    c.componentsAccept(this);
  }

  public void voidVisitComponent(Component c) {
    voidVisitAnnotated(c);
  }

  public final VoidValue visitAttribute(AttributePattern p) {
    voidVisitAttribute(p);
    return VoidValue.VOID;
  }

  public void voidVisitAttribute(AttributePattern p) {
    voidVisitPattern(p);
    p.getNameClass().accept(this);
    p.getChild().accept(this);
  }

  public final VoidValue visitAnyName(AnyNameNameClass nc) {
    voidVisitAnyName(nc);
    return VoidValue.VOID;
  }

  public void voidVisitAnyName(AnyNameNameClass nc) {
    voidVisitNameClass(nc);
    NameClass e = nc.getExcept();
    if (e != null)
      e.accept(this);
  }

  public final VoidValue visitInclude(IncludeComponent c) {
    voidVisitInclude(c);
    return VoidValue.VOID;
  }

  public void voidVisitInclude(IncludeComponent c) {
    voidVisitComponent(c);
    c.componentsAccept(this);
  }

  public final VoidValue visitOneOrMore(OneOrMorePattern p) {
    voidVisitOneOrMore(p);
    return VoidValue.VOID;
  }

  public void voidVisitOneOrMore(OneOrMorePattern p) {
    voidVisitPattern(p);
    p.getChild().accept(this);
  }

  public final VoidValue visitNsName(NsNameNameClass nc) {
    voidVisitNsName(nc);
    return VoidValue.VOID;
  }

  public void voidVisitNsName(NsNameNameClass nc) {
    voidVisitNameClass(nc);
    NameClass e = nc.getExcept();
    if (e != null)
      e.accept(this);
  }

  public final VoidValue visitDefine(DefineComponent c) {
    voidVisitDefine(c);
    return VoidValue.VOID;
  }

  public void voidVisitDefine(DefineComponent c) {
    voidVisitComponent(c);
    c.getBody().accept(this);
  }

  public final VoidValue visitZeroOrMore(ZeroOrMorePattern p) {
    voidVisitPattern(p);
    p.getChild().accept(this);
    return VoidValue.VOID;
  }

  public final VoidValue visitName(NameNameClass nc) {
    voidVisitName(nc);
    return VoidValue.VOID;
  }

  public void voidVisitName(NameNameClass nc) {
    voidVisitNameClass(nc);
  }

  public final VoidValue visitOptional(OptionalPattern p) {
    voidVisitOptional(p);
    return VoidValue.VOID;
  }

  public void voidVisitOptional(OptionalPattern p) {
    voidVisitPattern(p);
    p.getChild().accept(this);
  }

  public final VoidValue visitInterleave(InterleavePattern p) {
    voidVisitInterleave(p);
    return VoidValue.VOID;
  }

  public void voidVisitInterleave(InterleavePattern p) {
    voidVisitPattern(p);
    p.childrenAccept(this);
  }

  public final VoidValue visitGroup(GroupPattern p) {
    voidVisitGroup(p);
    return VoidValue.VOID;
  }

  public void voidVisitGroup(GroupPattern p) {
    voidVisitPattern(p);
    p.childrenAccept(this);
  }

  public final VoidValue visitChoice(ChoicePattern p) {
    voidVisitChoice(p);
    return VoidValue.VOID;
  }

  public void voidVisitChoice(ChoicePattern p) {
    voidVisitPattern(p);
    p.childrenAccept(this);
  }

  public final VoidValue visitGrammar(GrammarPattern p) {
    voidVisitGrammar(p);
    return VoidValue.VOID;
  }

  public void voidVisitGrammar(GrammarPattern p) {
    voidVisitPattern(p);
    p.componentsAccept(this);
  }

  public final VoidValue visitExternalRef(ExternalRefPattern p) {
    voidVisitExternalRef(p);
    return VoidValue.VOID;
  }

  public void voidVisitExternalRef(ExternalRefPattern p) {
    voidVisitPattern(p);
  }

  public final VoidValue visitRef(RefPattern p) {
    voidVisitRef(p);
    return VoidValue.VOID;
  }

  public void voidVisitRef(RefPattern p) {
    voidVisitPattern(p);
  }

  public final VoidValue visitParentRef(ParentRefPattern p) {
    voidVisitParentRef(p);
    return VoidValue.VOID;
  }

  public void voidVisitParentRef(ParentRefPattern p) {
    voidVisitPattern(p);
  }

  public final VoidValue visitValue(ValuePattern p) {
    voidVisitValue(p);
    return VoidValue.VOID;
  }

  public void voidVisitValue(ValuePattern p) {
    voidVisitPattern(p);
  }

  public final VoidValue visitData(DataPattern p) {
    voidVisitData(p);
    return VoidValue.VOID;
  }

  public void voidVisitData(DataPattern p) {
    voidVisitPattern(p);
    Pattern e = p.getExcept();
    if (e != null)
      e.accept(this);
    for (Param param : p.getParams())
      voidVisitAnnotated(param);
  }

  public final VoidValue visitMixed(MixedPattern p) {
    voidVisitMixed(p);
    return VoidValue.VOID;
  }

  public void voidVisitMixed(MixedPattern p) {
    voidVisitPattern(p);
    p.getChild().accept(this);
  }

  public final VoidValue visitList(ListPattern p) {
    voidVisitList(p);
    return VoidValue.VOID;
  }

  public void voidVisitList(ListPattern p) {
    voidVisitPattern(p);
    p.getChild().accept(this);
  }

  public final VoidValue visitText(TextPattern p) {
    voidVisitText(p);
    return VoidValue.VOID;
  }

  public void voidVisitText(TextPattern p) {
    voidVisitPattern(p);
  }

  public final VoidValue visitEmpty(EmptyPattern p) {
    voidVisitEmpty(p);
    return VoidValue.VOID;
  }

  public void voidVisitEmpty(EmptyPattern p) {
    voidVisitPattern(p);
  }

  public final VoidValue visitNotAllowed(NotAllowedPattern p) {
    voidVisitNotAllowed(p);
    return VoidValue.VOID;
  }

  public void voidVisitNotAllowed(NotAllowedPattern p) {
    voidVisitPattern(p);
  }

  public final VoidValue visitText(TextAnnotation ta) {
    voidVisitText(ta);
    return VoidValue.VOID;
  }

  public void voidVisitText(TextAnnotation ta) {
    voidVisitAnnotationChild(ta);
  }

  public final VoidValue visitComment(Comment c) {
    voidVisitComment(c);
    return VoidValue.VOID;
  }

  public void voidVisitComment(Comment c) {
    voidVisitAnnotationChild(c);
  }

  public final VoidValue visitElement(ElementAnnotation ea) {
    voidVisitElement(ea);
    return VoidValue.VOID;
  }

  public void voidVisitElement(ElementAnnotation ea) {
    voidVisitAnnotationChild(ea);
    ea.attributesAccept(this);
    ea.childrenAccept(this);
  }

  public void voidVisitAnnotationChild(AnnotationChild ac) {
  }

  public final VoidValue visitAttribute(AttributeAnnotation a) {
    voidVisitAttribute(a);
    return VoidValue.VOID;
  }

  public void voidVisitAttribute(AttributeAnnotation a) {
  }
}
