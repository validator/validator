package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;

class StringAtom extends Atom {
  private String str;
  private String normStr;
  private DatatypeContext dc;
  private Object assignmentClass = null;

  StringAtom(String str, DatatypeContext dc) {
    this.str = str;
    this.dc = dc;
  }
 
  boolean matchesString() {
    return true;
  }

  boolean matchesDatatypeValue(Datatype dt, String s) {
    if (normStr == null)
      normStr = StringNormalizer.normalize(str);
    return normStr.equals(s);
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
