package com.thaiopensource.xml.dtd;

public interface DatatypeVisitor {
  void basicDatatype(int type) throws Exception;
  void enumDatatype(EnumGroup enumGroup) throws Exception;
  void notationDatatype(EnumGroup enumGroup) throws Exception;
  void datatypeRef(String name, Datatype datatype) throws Exception;
}

