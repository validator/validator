package com.thaiopensource.xml.dtd.om;

public interface Dtd {
  String getEncoding();
  String getUri();
  TopLevel[] getAllTopLevel();
  void accept(TopLevelVisitor visitor) throws Exception;
}
