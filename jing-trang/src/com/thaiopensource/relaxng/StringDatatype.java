package com.thaiopensource.relaxng;

import com.thaiopensource.datatype.Datatype;
import com.thaiopensource.datatype.DatatypeContext;

class StringDatatype implements Datatype {
  public boolean allows(String str, DatatypeContext dc) {
    return true;
  }

  public Object createValue(String str, DatatypeContext context) {
    return str;
  }

  public boolean isContextDependent() {
    return false;
  }

  public int getIdType() {
    return ID_TYPE_NULL;
  }
}
