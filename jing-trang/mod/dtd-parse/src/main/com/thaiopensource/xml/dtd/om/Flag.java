package com.thaiopensource.xml.dtd.om;

public abstract class Flag {
  public static final int INCLUDE = 0;
  public static final int IGNORE = 0;
  public static final int FLAG_REF = 0;
  
  public abstract int getType();
  public abstract void accept(FlagVisitor visitor) throws Exception;
}
