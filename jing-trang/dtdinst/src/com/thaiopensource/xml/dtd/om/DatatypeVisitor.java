package com.thaiopensource.xml.dtd.om;

public interface DatatypeVisitor {
  void basicDatatype(String typeName) throws Exception;
  void enumDatatype(EnumGroup enumGroup) throws Exception;
  void notationDatatype(EnumGroup enumGroup) throws Exception;
  void datatypeRef(String name, Datatype datatype) throws Exception;
}

