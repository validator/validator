package com.thaiopensource.xml.dtd.om;

public abstract class AttributeDefault {
  public static final int DEFAULT_VALUE = 0;
  public static final int FIXED_VALUE = 1;
  public static final int IMPLIED_VALUE = 2;
  public static final int REQUIRED_VALUE = 3;
  public static final int ATTRIBUTE_DEFAULT_REF = 4;

  public abstract int getType();

  public abstract void accept(AttributeDefaultVisitor visitor) throws Exception;

  public boolean isRequired() {
    return false;
  }

  public String getDefaultValue() {
    return null;
  }

  public String getFixedValue() {
    return null;
  }
}

