package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.DatatypeContext;

class TokenizedDatatype extends DatatypeBase {
  private DatatypeBase base;
  
  TokenizedDatatype(DatatypeBase base) {
    super(WHITE_SPACE_PRESERVE);
    this.base = base;
  }

  boolean lexicallyAllows(String str) {
    return base.lexicallyAllows(base.normalizeWhiteSpace(str));
  }

  boolean allowsValue(String str, DatatypeContext dc) {
    return base.allowsValue(base.normalizeWhiteSpace(str), dc);
  }

  OrderRelation getOrderRelation() {
    return base.getOrderRelation();
  }

  Object getValue(String str, DatatypeContext dc) {
    return base.getValue(base.normalizeWhiteSpace(str), dc);
  }
}
