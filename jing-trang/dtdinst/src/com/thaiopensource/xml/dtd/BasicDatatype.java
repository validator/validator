package com.thaiopensource.xml.dtd;

public class BasicDatatype extends Datatype {
  private final int type;

  public BasicDatatype(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public void accept(DatatypeVisitor visitor) throws VisitException {
    try {
      visitor.basicDatatype(type);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }
}

