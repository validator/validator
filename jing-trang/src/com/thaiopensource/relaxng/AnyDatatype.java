package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;

class AnyDatatype implements Datatype {
  public boolean allows(String str, DatatypeContext dc) {
    return true;
  }
}
