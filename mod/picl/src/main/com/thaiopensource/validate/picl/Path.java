package com.thaiopensource.validate.picl;

interface Path {
  int length();
  String getLocalName(int i);
  String getNamespaceUri(int i);
  boolean isAttribute();
}
