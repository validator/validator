package com.thaiopensource.xml.dtd.om;

public abstract class Datatype {
  public static final int BASIC = 0;
  public static final int ENUM = 1;
  public static final int NOTATION = 2;
  public static final int DATATYPE_REF = 3;

  public abstract int getType();
  public abstract void accept(DatatypeVisitor visitor) throws Exception;
}
