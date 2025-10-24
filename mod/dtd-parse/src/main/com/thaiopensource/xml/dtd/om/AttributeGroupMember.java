package com.thaiopensource.xml.dtd.om;

public abstract class AttributeGroupMember {
  public static final int ATTRIBUTE = 0;
  public static final int ATTRIBUTE_GROUP_REF = 1;

  public abstract int getType();
  public abstract void accept(AttributeGroupVisitor visitor) throws Exception;
}
