package com.thaiopensource.validate.picl;

abstract class Path {
  abstract int length();
  abstract String getLocalName(int i);
  abstract String getNamespace(int i);
}
