package com.thaiopensource.validate.picl;

class AttributePathPattern extends PathPattern {
  AttributePathPattern(String[] names, boolean[] descendantsOrSelf) {
    super(names, descendantsOrSelf);
  }

  boolean isAttribute() {
    return true;
  }

}
