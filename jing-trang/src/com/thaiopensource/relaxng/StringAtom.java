package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;

class StringAtom extends Atom {
  private String str;
  private DatatypeContext dc;

  StringAtom(String str, DatatypeContext dc) {
    this.str = str;
    this.dc = dc;
  }
 
  boolean matchesString() {
    return true;
  }

  boolean matchesDatatypeValue(Datatype dt, Object obj) {
    return obj.equals(dt.createValue(str, dc));
  }

  boolean matchesDatatype(Datatype dt) {
    return dt.allows(str, dc);
  }
  
  String getStringValue() {
    return str;
  }
  
  DatatypeContext getDatatypeContext() {
    return dc;
  }

  String getString() {
    return str;
  }
}
