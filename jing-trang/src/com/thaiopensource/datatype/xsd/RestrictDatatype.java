package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.DatatypeContext;

class RestrictDatatype extends DatatypeBase {
  protected DatatypeBase base;
  
  RestrictDatatype(DatatypeBase base) {
    this(base, base.getWhiteSpace());
  }

  RestrictDatatype(DatatypeBase base, int whiteSpace) {
    super(whiteSpace);
    this.base = base;
  }

  boolean lexicallyAllows(String str) {
    return base.lexicallyAllows(str);
  }

  boolean allowsValue(String str, DatatypeContext dc) {
    return base.allowsValue(str, dc);
  }

  OrderRelation getOrderRelation() {
    return base.getOrderRelation();
  }

  Measure getMeasure() {
    return base.getMeasure();
  }

  Object getValue(String str, DatatypeContext dc) {
    return base.getValue(str, dc);
  }

  DatatypeBase getPrimitive() {
    return base.getPrimitive();
  }

  public Object getAssignmentClass() {
    return base.getAssignmentClass();
  }
}
