package com.thaiopensource.xml.dtd.om;

public interface Dtd {
  String getEncoding();
  TopLevel[] getAllTopLevel();
  void accept(TopLevelVisitor visitor) throws Exception;
}
