package com.thaiopensource.relaxng.edit;

public interface NameClassVisitor {
  Object visitChoice(ChoiceNameClass nc);
  Object visitAnyName(AnyNameNameClass nc);
  Object visitNsName(NsNameNameClass nc);
  Object visitName(NameNameClass nc);
}
