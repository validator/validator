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
    if (dt.allows(str, dc)) {
      Object tem = dt.getAssignmentClass();
      if (tem != null) {
	if (assignmentClass == null)
	  assignmentClass = tem;
	else if (tem != assignmentClass)
	  assignmentClass = AMBIGUOUS_ASSIGNMENT;
      }
      return true;
    }
    return false;
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

  Object getAssignmentClass() {
    return assignmentClass;
  }
}
