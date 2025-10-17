package com.thaiopensource.xml.dtd.om;

public abstract class EnumGroupMember {
  public static final int ENUM_VALUE = 0;
  public static final int ENUM_GROUP_REF = 1;

  public abstract int getType();
  public abstract void accept(EnumGroupVisitor visitor) throws Exception;
}
