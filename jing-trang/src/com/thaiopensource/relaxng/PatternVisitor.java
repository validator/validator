package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;

interface PatternVisitor {
  void visitEmptySequence();
  void visitEmptyChoice();
  void visitError();
  void visitSequence(Pattern p1, Pattern p2);
  void visitInterleave(Pattern p1, Pattern p2);
  void visitChoice(Pattern p1, Pattern p2);
  void visitOneOrMore(Pattern p);
  void visitElement(NameClass nc, Pattern content);
  void visitAttribute(NameClass ns, Pattern value);
  void visitDatatype(Datatype dt);
  void visitKey(String name, Pattern p);
  void visitKeyRef(String name, Pattern p);
  void visitValue(Datatype dt, Object obj);
  void visitText();
  void visitList(Pattern p);
}
