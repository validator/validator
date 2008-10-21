package com.thaiopensource.relaxng.edit;

public abstract class AbstractPatternVisitor<T> implements PatternVisitor<T> {
  public T visitElement(ElementPattern p) {
    return visitNameClassed(p);
  }

  public T visitAttribute(AttributePattern p) {
    return visitNameClassed(p);
  }

  public T visitOneOrMore(OneOrMorePattern p) {
    return visitUnary(p);
  }

  public T visitZeroOrMore(ZeroOrMorePattern p) {
    return visitUnary(p);
  }

  public T visitOptional(OptionalPattern p) {
    return visitUnary(p);
  }

  public T visitInterleave(InterleavePattern p) {
    return visitComposite(p);
  }

  public T visitGroup(GroupPattern p) {
    return visitComposite(p);
  }

  public T visitChoice(ChoicePattern p) {
    return visitComposite(p);
  }

  public T visitGrammar(GrammarPattern p) {
    return visitPattern(p);
  }

  public T visitExternalRef(ExternalRefPattern p) {
    return visitPattern(p);
  }

  public T visitRef(RefPattern p) {
    return visitPattern(p);
  }

  public T visitParentRef(ParentRefPattern p) {
    return visitPattern(p);
  }

  public T visitValue(ValuePattern p) {
    return visitPattern(p);
  }

  public T visitData(DataPattern p) {
    return visitPattern(p);
  }

  public T visitMixed(MixedPattern p) {
    return visitUnary(p);
  }

  public T visitList(ListPattern p) {
    return visitUnary(p);
  }

  public T visitText(TextPattern p) {
    return visitPattern(p);
  }

  public T visitEmpty(EmptyPattern p) {
    return visitPattern(p);
  }

  public T visitNotAllowed(NotAllowedPattern p) {
    return visitPattern(p);
  }

  public T visitNameClassed(NameClassedPattern p) {
    return visitUnary(p);
  }

  public T visitUnary(UnaryPattern p) {
    return visitPattern(p);
  }

  public T visitComposite(CompositePattern p) {
    return visitPattern(p);
  }

  public abstract T visitPattern(Pattern p);
}
