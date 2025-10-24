package com.thaiopensource.xml.dtd.om;

public abstract class NameSpec {
  public static final int NAME = 0;
  public static final int NAME_SPEC_REF = 1;

  public abstract int getType();
  public abstract void accept(NameSpecVisitor visitor) throws Exception;
  public abstract String getValue();
}
