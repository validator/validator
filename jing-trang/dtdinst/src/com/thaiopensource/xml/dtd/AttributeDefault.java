package com.thaiopensource.xml.dtd;

public abstract class AttributeDefault {
  public static final int DEFAULT_VALUE = 0;
  public static final int FIXED_VALUE = 1;
  public static final int IMPLIED_VALUE = 2;
  public static final int REQUIRED_VALUE = 3;

  public abstract int getType();
  public abstract void accept(AttributeDefaultVisitor visitor) throws Exception;
}

