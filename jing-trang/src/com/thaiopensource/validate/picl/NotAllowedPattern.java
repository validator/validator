package com.thaiopensource.validate.picl;

class NotAllowedPattern extends Pattern {
  boolean matches(Path path, int rootDepth) {
    return false;
  }

  public String toString() {
    return "(notAllowed)";
  }
}
