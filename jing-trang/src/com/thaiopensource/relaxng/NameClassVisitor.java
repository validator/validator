package com.thaiopensource.relaxng;

interface NameClassVisitor {
  void visitChoice(NameClass nc1, NameClass nc2);
  void visitNsName(String ns);
  void visitNsNameExcept(String ns, NameClass nc);
  void visitAnyName();
  void visitAnyNameExcept(NameClass nc);
  void visitName(Name name);
  void visitNull();
  void visitError();
}
