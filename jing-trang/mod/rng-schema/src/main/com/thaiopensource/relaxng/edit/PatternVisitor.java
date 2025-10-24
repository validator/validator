package com.thaiopensource.relaxng.edit;

public interface PatternVisitor<T> {
  T visitElement(ElementPattern p);
  T visitAttribute(AttributePattern p);
  T visitOneOrMore(OneOrMorePattern p);
  T visitZeroOrMore(ZeroOrMorePattern p);
  T visitOptional(OptionalPattern p);
  T visitInterleave(InterleavePattern p);
  T visitGroup(GroupPattern p);
  T visitChoice(ChoicePattern p);
  T visitGrammar(GrammarPattern p);
  T visitExternalRef(ExternalRefPattern p);
  T visitRef(RefPattern p);
  T visitParentRef(ParentRefPattern p);
  T visitValue(ValuePattern p);
  T visitData(DataPattern p);
  T visitMixed(MixedPattern p);
  T visitList(ListPattern p);
  T visitText(TextPattern p);
  T visitEmpty(EmptyPattern p);
  T visitNotAllowed(NotAllowedPattern p);
}
