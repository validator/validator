package com.thaiopensource.xml.dtd.om;

public interface EnumGroupVisitor {
  void enumValue(String value) throws Exception;
  void enumGroupRef(String name, EnumGroup enumGroup) throws Exception;
}
