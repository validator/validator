package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;

interface PatternVisitor {
  void visitEmptySequence();
  void visitEmptyChoice();
  void visitError();
  void visitSequence(Pattern p1, Pattern p2);
  void visitInterleave(Pattern p1, Pattern p2);
  void visitConcur(Pattern p1, Pattern p2);
  void visitChoice(Pattern p1, Pattern p2);
  void visitOneOrMore(Pattern p);
  void visitElement(NameClass nc, Pattern content);
  void visitAttribute(NameClass ns, Pattern value);
  void visitDatatype(Datatype dt);
  void visitString(boolean normalizeWhiteSpace, String str);
  void visitAnyString();
}
