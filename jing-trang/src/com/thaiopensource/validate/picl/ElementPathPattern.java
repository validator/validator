package com.thaiopensource.validate.picl;

class ElementPathPattern extends PathPattern {
  ElementPathPattern(String[] names, boolean[] descendantsOrSelf) {
    super(names, descendantsOrSelf);
  }

  boolean isAttribute() {
    return false;
  }
}
