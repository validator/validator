package com.thaiopensource.relaxng.edit;

public interface PatternVisitor {
  Object visitElement(ElementPattern p);
  Object visitAttribute(AttributePattern p);
  Object visitOneOrMore(OneOrMorePattern p);
  Object visitZeroOrMore(ZeroOrMorePattern p);
  Object visitOptional(OptionalPattern p);
  Object visitInterleave(InterleavePattern p);
  Object visitGroup(GroupPattern p);
  Object visitChoice(ChoicePattern p);
  Object visitGrammar(GrammarPattern p);
  Object visitExternalRef(ExternalRefPattern p);
  Object visitRef(RefPattern p);
  Object visitParentRef(ParentRefPattern p);
  Object visitValue(ValuePattern p);
  Object visitData(DataPattern p);
  Object visitMixed(MixedPattern p);
  Object visitList(ListPattern p);
  Object visitText(TextPattern p);
  Object visitEmpty(EmptyPattern p);
  Object visitNotAllowed(NotAllowedPattern p);
}
