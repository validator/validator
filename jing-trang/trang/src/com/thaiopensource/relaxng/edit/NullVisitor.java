package com.thaiopensource.relaxng.edit;

import java.util.Iterator;

public class NullVisitor implements PatternVisitor, NameClassVisitor, ComponentVisitor,
        AnnotationChildVisitor, AttributeAnnotationVisitor {
  public final Object visitElement(ElementPattern p) {
    nullVisitElement(p);
    return null;
  }

  public void nullVisitElement(ElementPattern p) {
    nullVisitPattern(p);
    p.getNameClass().accept(this);
    p.getChild().accept(this);
  }

  public void nullVisitPattern(Pattern p) {
    nullVisitAnnotated(p);
  }

  public void nullVisitAnnotated(Annotated p) {
    p.leadingCommentsAccept(this);
    p.attributeAnnotationsAccept(this);
    p.childElementAnnotationsAccept(this);
    p.followingElementAnnotationsAccept(this);
  }

  public final Object visitChoice(ChoiceNameClass nc) {
    nullVisitChoice(nc);
    return null;
  }

  public void nullVisitChoice(ChoiceNameClass nc) {
    nullVisitNameClass(nc);
    nc.childrenAccept(this);
  }

  public void nullVisitNameClass(NameClass nc) {
    nullVisitAnnotated(nc);
  }

  public final Object visitDiv(DivComponent c) {
    nullVisitDiv(c);
    return null;
  }

  public void nullVisitDiv(DivComponent c) {
    nullVisitComponent(c);
    c.componentsAccept(this);
  }

  public void nullVisitComponent(Component c) {
    nullVisitAnnotated(c);
  }

  public final Object visitAttribute(AttributePattern p) {
    nullVisitAttribute(p);
    return null;
  }

  public void nullVisitAttribute(AttributePattern p) {
    nullVisitPattern(p);
    p.getNameClass().accept(this);
    p.getChild().accept(this);
  }

  public final Object visitAnyName(AnyNameNameClass nc) {
    nullVisitAnyName(nc);
    return null;
  }

  public void nullVisitAnyName(AnyNameNameClass nc) {
    nullVisitNameClass(nc);
    NameClass e = nc.getExcept();
    if (e != null)
      e.accept(this);
  }

  public final Object visitInclude(IncludeComponent c) {
    nullVisitInclude(c);
    return null;
  }

  public void nullVisitInclude(IncludeComponent c) {
    nullVisitComponent(c);
    c.componentsAccept(this);
  }

  public final Object visitOneOrMore(OneOrMorePattern p) {
    nullVisitOneOrMore(p);
    return null;
  }

  public void nullVisitOneOrMore(OneOrMorePattern p) {
    nullVisitPattern(p);
    p.getChild().accept(this);
  }

  public final Object visitNsName(NsNameNameClass nc) {
    nullVisitNsName(nc);
    return null;
  }

  public void nullVisitNsName(NsNameNameClass nc) {
    nullVisitNameClass(nc);
    NameClass e = nc.getExcept();
    if (e != null)
      e.accept(this);
  }

  public final Object visitDefine(DefineComponent c) {
    nullVisitDefine(c);
    return null;
  }

  public void nullVisitDefine(DefineComponent c) {
    nullVisitComponent(c);
    c.getBody().accept(this);
  }

  public final Object visitZeroOrMore(ZeroOrMorePattern p) {
    nullVisitPattern(p);
    p.getChild().accept(this);
    return null;
  }

  public final Object visitName(NameNameClass nc) {
    nullVisitName(nc);
    return null;
  }

  public void nullVisitName(NameNameClass nc) {
    nullVisitNameClass(nc);
  }

  public final Object visitOptional(OptionalPattern p) {
    nullVisitOptional(p);
    return null;
  }

  public void nullVisitOptional(OptionalPattern p) {
    nullVisitPattern(p);
    p.getChild().accept(this);
  }

  public final Object visitInterleave(InterleavePattern p) {
    nullVisitInterleave(p);
    return null;
  }

  public void nullVisitInterleave(InterleavePattern p) {
    nullVisitPattern(p);
    p.childrenAccept(this);
  }

  public final Object visitGroup(GroupPattern p) {
    nullVisitGroup(p);
    return null;
  }

  public void nullVisitGroup(GroupPattern p) {
    nullVisitPattern(p);
    p.childrenAccept(this);
  }

  public final Object visitChoice(ChoicePattern p) {
    nullVisitChoice(p);
    return null;
  }

  public void nullVisitChoice(ChoicePattern p) {
    nullVisitPattern(p);
    p.childrenAccept(this);
  }

  public final Object visitGrammar(GrammarPattern p) {
    nullVisitGrammar(p);
    return null;
  }

  public void nullVisitGrammar(GrammarPattern p) {
    nullVisitPattern(p);
    p.componentsAccept(this);
  }

  public final Object visitExternalRef(ExternalRefPattern p) {
    nullVisitExternalRef(p);
    return null;
  }

  public void nullVisitExternalRef(ExternalRefPattern p) {
    nullVisitPattern(p);
  }

  public final Object visitRef(RefPattern p) {
    nullVisitRef(p);
    return null;
  }

  public void nullVisitRef(RefPattern p) {
    nullVisitPattern(p);
  }

  public final Object visitParentRef(ParentRefPattern p) {
    nullVisitParentRef(p);
    return null;
  }

  public void nullVisitParentRef(ParentRefPattern p) {
    nullVisitPattern(p);
  }

  public final Object visitValue(ValuePattern p) {
    nullVisitValue(p);
    return null;
  }

  public void nullVisitValue(ValuePattern p) {
    nullVisitPattern(p);
  }

  public final Object visitData(DataPattern p) {
    nullVisitData(p);
    return null;
  }

  public void nullVisitData(DataPattern p) {
    nullVisitPattern(p);
    Pattern e = p.getExcept();
    if (e != null)
      e.accept(this);
    for (Iterator iter = p.getParams().iterator(); iter.hasNext();)
      nullVisitAnnotated((Param)iter.next());
  }

  public final Object visitMixed(MixedPattern p) {
    nullVisitMixed(p);
    return null;
  }

  public void nullVisitMixed(MixedPattern p) {
    nullVisitPattern(p);
    p.getChild().accept(this);
  }

  public final Object visitList(ListPattern p) {
    nullVisitList(p);
    return null;
  }

  public void nullVisitList(ListPattern p) {
    nullVisitPattern(p);
    p.getChild().accept(this);
  }

  public final Object visitText(TextPattern p) {
    nullVisitText(p);
    return null;
  }

  public void nullVisitText(TextPattern p) {
    nullVisitPattern(p);
  }

  public final Object visitEmpty(EmptyPattern p) {
    nullVisitEmpty(p);
    return null;
  }

  public void nullVisitEmpty(EmptyPattern p) {
    nullVisitPattern(p);
  }

  public final Object visitNotAllowed(NotAllowedPattern p) {
    nullVisitNotAllowed(p);
    return null;
  }

  public void nullVisitNotAllowed(NotAllowedPattern p) {
    nullVisitPattern(p);
  }

  public final Object visitText(TextAnnotation ta) {
    nullVisitText(ta);
    return null;
  }

  public void nullVisitText(TextAnnotation ta) {
    nullVisitAnnotationChild(ta);
  }

  public final Object visitComment(Comment c) {
    nullVisitComment(c);
    return null;
  }

  public void nullVisitComment(Comment c) {
    nullVisitAnnotationChild(c);
  }

  public final Object visitElement(ElementAnnotation ea) {
    nullVisitElement(ea);
    return null;
  }

  public void nullVisitElement(ElementAnnotation ea) {
    nullVisitAnnotationChild(ea);
    ea.attributesAccept(this);
    ea.childrenAccept(this);
  }

  public void nullVisitAnnotationChild(AnnotationChild ac) {
  }

  public final Object visitAttribute(AttributeAnnotation a) {
    nullVisitAttribute(a);
    return null;
  }

  public void nullVisitAttribute(AttributeAnnotation a) {
  }
}
