package com.thaiopensource.relaxng.edit;

public class AbstractVisitor implements ComponentVisitor, PatternVisitor, NameClassVisitor,
        AnnotationChildVisitor, AttributeAnnotationVisitor {
  public Object visitDefine(DefineComponent c) {
    return visitComponent(c);
  }

  public Object visitDiv(DivComponent c) {
    return visitComponent(c);
  }

  public Object visitInclude(IncludeComponent c) {
    return visitComponent(c);
  }

  public Object visitComponent(Component c) {
    return null;
  }

  public Object visitElement(ElementPattern p) {
    return visitNameClassed(p);
  }

  public Object visitAttribute(AttributePattern p) {
    return visitNameClassed(p);
  }

  public Object visitOneOrMore(OneOrMorePattern p) {
    return visitUnary(p);
  }

  public Object visitZeroOrMore(ZeroOrMorePattern p) {
    return visitUnary(p);
  }

  public Object visitOptional(OptionalPattern p) {
    return visitUnary(p);
  }

  public Object visitInterleave(InterleavePattern p) {
    return visitComposite(p);
  }

  public Object visitGroup(GroupPattern p) {
    return visitComposite(p);
  }

  public Object visitChoice(ChoicePattern p) {
    return visitComposite(p);
  }

  public Object visitGrammar(GrammarPattern p) {
    return visitPattern(p);
  }

  public Object visitExternalRef(ExternalRefPattern p) {
    return visitPattern(p);
  }

  public Object visitRef(RefPattern p) {
    return visitPattern(p);
  }

  public Object visitParentRef(ParentRefPattern p) {
    return visitPattern(p);
  }

  public Object visitValue(ValuePattern p) {
    return visitPattern(p);
  }

  public Object visitData(DataPattern p) {
    return visitPattern(p);
  }

  public Object visitMixed(MixedPattern p) {
    return visitUnary(p);
  }

  public Object visitList(ListPattern p) {
    return visitUnary(p);
  }

  public Object visitText(TextPattern p) {
    return visitPattern(p);
  }

  public Object visitEmpty(EmptyPattern p) {
    return visitPattern(p);
  }

  public Object visitNotAllowed(NotAllowedPattern p) {
    return visitPattern(p);
  }

  public Object visitNameClassed(NameClassedPattern p) {
    return visitUnary(p);
  }

  public Object visitUnary(UnaryPattern p) {
    return visitPattern(p);
  }

  public Object visitComposite(CompositePattern p) {
    return visitPattern(p);
  }

  public Object visitPattern(Pattern p) {
    return null;
  }

  public Object visitChoice(ChoiceNameClass nc) {
    return visitNameClass(nc);
  }

  public Object visitAnyName(AnyNameNameClass nc) {
    return visitNameClass(nc);
  }

  public Object visitNsName(NsNameNameClass nc) {
    return visitNameClass(nc);
  }

  public Object visitName(NameNameClass nc) {
    return visitNameClass(nc);
  }

  public Object visitNameClass(NameClass nc) {
    return null;
  }

  public Object visitText(TextAnnotation ta) {
    return visitAnnotationChild(ta);
  }

  public Object visitComment(Comment c) {
    return visitAnnotationChild(c);
  }

  public Object visitElement(ElementAnnotation ea) {
    return visitAnnotationChild(ea);
  }

  public Object visitAnnotationChild(AnnotationChild ac) {
    return null;
  }

  public Object visitAttribute(AttributeAnnotation a) {
    return null;
  }
}
