package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.DatatypeContext;

class ErrorDatatype extends DatatypeBase implements OrderRelation {
  public boolean lexicallyAllows(String str) {
    return true;
  }
  static class Error { }

  Object getValue(String str, DatatypeContext dc) {
    return new Error();
  }

  OrderRelation getOrderRelation() {
    return this;
  }

  public int compareValue(Object obj1, Object obj2) {
    return COMPARE_EQUAL;
  }

}
