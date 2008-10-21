package com.thaiopensource.xml.dtd.om;

public abstract class TopLevel {
  public static final int ELEMENT_DECL = 1;
  public static final int ATTLIST_DECL = 2;
  public static final int PROCESSING_INSTRUCTION = 3;
  public static final int COMMENT = 4;
  public static final int NOTATION_DECL = 5;
  public static final int MODEL_GROUP_DEF = 6;
  public static final int ATTRIBUTE_GROUP_DEF = 7;
  public static final int DATATYPE_DEF = 8;
  public static final int ENUM_GROUP_DEF = 9;
  public static final int FLAG_DEF = 10;
  public static final int INCLUDED_SECTION = 11;
  public static final int IGNORED_SECTION = 12;
  public static final int INTERNAL_ENTITY_DECL = 13;
  public static final int EXTERNAL_ENTITY_DECL = 14;
  public static final int NAME_SPEC_DEF = 15;
  public static final int OVERRIDDEN_DEF = 16;
  public static final int EXTERNAL_ID_DEF = 17;
  public static final int EXTERNAL_ID_REF = 18;
  public static final int PARAM_DEF = 19;
  public static final int ATTRIBUTE_DEFAULT_DEF = 20;

  public abstract int getType();
  public abstract void accept(TopLevelVisitor visitor) throws Exception;
}
