package com.thaiopensource.xml.dtd;

public class EnumDatatype extends Datatype {
  private final EnumGroup enumGroup;

  public EnumDatatype(EnumGroup enumGroup) {
    this.enumGroup = enumGroup;
  }

  public int getType() {
    return ENUM;
  }

  public EnumGroup getEnumGroup() {
    return enumGroup;
  }

  public void accept(DatatypeVisitor visitor) throws VisitException {
    try {
      visitor.enumDatatype(enumGroup);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }
}

