package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

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
