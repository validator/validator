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
  String getStringValue() {
    return null;
  }
  DatatypeContext getDatatypeContext() {
    return null;
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
