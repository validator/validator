package com.thaiopensource.xml.dtd;

public interface NameSpecVisitor {
  void name(String value) throws Exception;
  void nameSpecRef(String name, NameSpec nameSpec) throws Exception;
}
