package com.thaiopensource.xml.dtd.om;

public interface NameSpecVisitor {
  void name(String value) throws Exception;
  void nameSpecRef(String name, NameSpec nameSpec) throws Exception;
}
