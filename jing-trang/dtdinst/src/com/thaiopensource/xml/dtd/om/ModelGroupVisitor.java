package com.thaiopensource.xml.dtd.om;

public interface ModelGroupVisitor {
  void choice(ModelGroup[] members) throws Exception;
  void sequence(ModelGroup[] members) throws Exception;
  void oneOrMore(ModelGroup member) throws Exception;
  void zeroOrMore(ModelGroup member) throws Exception;
  void optional(ModelGroup member) throws Exception;
  void modelGroupRef(String name, ModelGroup modelGroup) throws Exception;
  void elementRef(NameSpec name) throws Exception;
  void pcdata() throws Exception;
  void any() throws Exception;
}

