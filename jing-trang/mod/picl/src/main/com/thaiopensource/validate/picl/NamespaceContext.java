package com.thaiopensource.validate.picl;

interface NamespaceContext {
  String getNamespaceUri(String string);
  String defaultPrefix();
}
