package com.thaiopensource.validate.picl;

class NotAllowedPattern extends Pattern {
  boolean matchesElement(Path path, int rootDepth) {
    return false;
  }

  boolean matchesAttribute(Path path, String namespaceUri, String localName, int rootDepth) {
    return false;
  }

  public String toString() {
    return "(notAllowed)";
  }
}
