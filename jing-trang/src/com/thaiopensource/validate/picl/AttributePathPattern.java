package com.thaiopensource.validate.picl;

class AttributePathPattern extends PathPattern {
  AttributePathPattern(String[] names, boolean[] descendantsOrSelf) {
    super(names, descendantsOrSelf);
  }

  boolean matchesAttribute(Path path, String namespaceUri, String localName, int rootDepth) {
    if (!matchName(namespaceUri, names[names.length - 2])
        || !matchName(localName, names[names.length - 1]))
      return false;
    return matchSegment(path, rootDepth, path.length() - rootDepth, 0, (names.length >> 1) - 1, false);
  }

  boolean matchesElement(Path path, int rootDepth) {
    return false;
  }

  public String toString() {
    return toString(true);
  }

}
