package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;

abstract class Atom {
  boolean matchesString() {
    return false;
  }
  boolean matchesDatatypeValue(Datatype dt, Object obj) {
    return false;
  }
  boolean matchesDatatype(Datatype dt) {
    return false;
  }
  void setKey(Datatype dt, String name) { }
  void setKeyRef(Datatype dt, String name) { }
  boolean matchesList(PatternBuilder b, Pattern p) {
    return false;
  }
  boolean matchesAttribute(PatternBuilder b, NameClass nc, Pattern value) {
    return false;
  }
  boolean isAttribute() {
    return false;
  }
  boolean matchesElement(NameClass nc, Pattern content) {
    return false;
  }
}
