package com.thaiopensource.datatype;

public interface DatatypeContext {
  /**
   * The default namespace (as set by xmlns attribute) is specified using "" for prefix
   * Returns null is the prefix is undefined.
   */
  String getNamespaceURI(String prefix);
}
