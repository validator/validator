package com.thaiopensource.xml.dtd.om;

public interface FlagVisitor {
  void include() throws Exception;
  void ignore() throws Exception;
  void flagRef(String name, Flag flag) throws Exception;
}
