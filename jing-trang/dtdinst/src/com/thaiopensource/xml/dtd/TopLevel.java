package com.thaiopensource.xml.dtd;

public abstract class TopLevel {
  public static final int ELEMENT_DECL = 1;
  public static final int ATTLIST_DECL = 2;
  public static final int PROCESSING_INSTRUCTION = 3;
  public static final int COMMENT = 4;
  public static final int MODEL_GROUP_DEF = 6;
  public static final int ATTRIBUTE_GROUP_DEF = 7;
  public static final int DATATYPE_DEF = 8;
  public static final int ENUM_GROUP_DEF = 9;
  public static final int FLAG_DEF = 10;
  public static final int INCLUDED_SECTION = 11;
  public static final int IGNORED_SECTION = 12;
  public static final int INTERNAL_ENTITY_DECL = 13;
  public static final int EXTERNAL_ENTITY_DECL = 14;

  public abstract int getType();
  public abstract void accept(TopLevelVisitor visitor) throws Exception;
}
