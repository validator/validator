package com.thaiopensource.validate.picl;

class ElementPathPattern extends PathPattern {
  ElementPathPattern(String[] names, boolean[] descendantsOrSelf) {
    super(names, descendantsOrSelf);
  }

  boolean matchesAttribute(Path path, String namespaceUri, String localName, int rootDepth) {
    return false;
  }

  boolean matchesElement(Path path, int rootDepth) {
    return matchSegment(path, rootDepth, path.length() - rootDepth, 0, names.length >> 1, false);
  }

  public String toString() {
    return toString(false);
  }
}
