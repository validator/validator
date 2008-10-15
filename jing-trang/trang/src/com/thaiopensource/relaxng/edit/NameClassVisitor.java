package com.thaiopensource.relaxng.edit;

public interface NameClassVisitor<T> {
  T visitChoice(ChoiceNameClass nc);
  T visitAnyName(AnyNameNameClass nc);
  T visitNsName(NsNameNameClass nc);
  T visitName(NameNameClass nc);
}
