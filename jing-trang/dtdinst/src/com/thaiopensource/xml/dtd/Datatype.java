package com.thaiopensource.xml.dtd;

public abstract class Datatype {
  public static final int CDATA = 0;
  public static final int NMTOKEN = 1;
  public static final int NMTOKENS = 2;
  public static final int ID = 3;
  public static final int IDREF = 4;
  public static final int IDREFS = 5;
  public static final int ENTITY = 6;
  public static final int ENTITIES = 7;
  public static final int ENUM = 8;
  public static final int NOTATION = 9;
  public static final int DATATYPE_REF = 10;

  public abstract int getType();
  public abstract void accept(DatatypeVisitor visitor) throws VisitException;
}
