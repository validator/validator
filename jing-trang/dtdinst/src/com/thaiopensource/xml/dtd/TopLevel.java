package com.thaiopensource.xml.dtd;

public abstract class TopLevel {
  public static final int ELEMENT_DECL = 1;
  public static final int ATTLIST_DECL = 2;
  public static final int PROCESSING_INSTRUCTION = 3;
  public static final int COMMENT = 4;
  public static final int IGNORED_SECTION = 5;
  public static final int MODEL_GROUP_DEF = 6;
  public static final int ATTRIBUTE_GROUP_DEF = 7;
  public static final int DATATYPE_DEF = 8;
  public static final int ENUM_GROUP_DEF = 9;

  public abstract int getType();
  public abstract void accept(TopLevelVisitor visitor) throws VisitException;
}
