package com.thaiopensource.relaxng;

interface NameClassVisitor {
  void visitChoice(NameClass nc1, NameClass nc2);
  void visitDifference(NameClass nc1, NameClass nc2);
  void visitNot(NameClass nc);
  void visitNsName(String ns);
  void visitAnyName();
  void visitName(String ns, String localName);
  void visitError();
}
