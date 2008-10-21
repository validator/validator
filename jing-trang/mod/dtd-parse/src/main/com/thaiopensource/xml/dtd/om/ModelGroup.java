package com.thaiopensource.xml.dtd.om;

public abstract class ModelGroup {
  public static final int CHOICE = 1;
  public static final int SEQUENCE = 2;
  public static final int ONE_OR_MORE = 3;
  public static final int ZERO_OR_MORE = 4;
  public static final int OPTIONAL = 5;
  public static final int MODEL_GROUP_REF = 6;
  public static final int ELEMENT_REF = 7;
  public static final int PCDATA = 8;
  public static final int ANY = 9;

  public abstract int getType();
  public abstract void accept(ModelGroupVisitor visitor) throws Exception;
}
