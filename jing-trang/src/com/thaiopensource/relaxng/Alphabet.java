package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;

abstract class Alphabet {
  void addElement(NameClass nc) { }
  void addValue(Datatype dt, Object obj) throws RestrictionViolationException { }
  void addData() throws RestrictionViolationException { }
  abstract boolean isEmpty();
  abstract void addAlphabet(Alphabet a) throws RestrictionViolationException;
  abstract void checkOverlap(Alphabet a) throws RestrictionViolationException;
}
