package com.thaiopensource.relaxng.output.dtd;

class AttributeType {
  private AttributeType() { }
  static final AttributeType EMPTY = new AttributeType();
  static final AttributeType SINGLE = new AttributeType();
  static final AttributeType MULTI = new AttributeType();

  static AttributeType group(AttributeType at1, AttributeType at2) {
    if (at1 == EMPTY)
      return at2;
    if (at2 == EMPTY)
      return at1;
    return MULTI;
  }
}
