package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.ValidationContext;

class TokenizedDatatype extends DatatypeBase {
  private DatatypeBase base;
  
  TokenizedDatatype(DatatypeBase base) {
    super(WHITE_SPACE_PRESERVE);
    this.base = base;
  }

  boolean lexicallyAllows(String str) {
    return base.lexicallyAllows(base.normalizeWhiteSpace(str));
  }

  boolean allowsValue(String str, ValidationContext vc) {
    return base.allowsValue(base.normalizeWhiteSpace(str), vc);
  }

  OrderRelation getOrderRelation() {
    return base.getOrderRelation();
  }

  Object getValue(String str, ValidationContext vc) {
    return base.getValue(base.normalizeWhiteSpace(str), vc);
  }
}
