package com.thaiopensource.validate.picl;

abstract class Pattern {
  abstract boolean matchesElement(Path path, int rootDepth);
  abstract boolean matchesAttribute(Path path, String namespaceUri, String localName, int rootDepth);
}
