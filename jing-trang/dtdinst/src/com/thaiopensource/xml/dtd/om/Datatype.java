package com.thaiopensource.xml.dtd.om;

public abstract class Datatype {
  public static final int CDATA = 0;
  public static final int TOKENIZED = 1;
  public static final int ENUM = 2;
  public static final int NOTATION = 3;
  public static final int DATATYPE_REF = 4;

  public abstract int getType();
  public abstract void accept(DatatypeVisitor visitor) throws Exception;
  public Datatype deref() {
    return this;
  }
}
