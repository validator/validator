package com.thaiopensource.validate.picl;

abstract class NamespaceContext {
  abstract String getNamespaceUri(String string);
  abstract String defaultPrefix();
}
