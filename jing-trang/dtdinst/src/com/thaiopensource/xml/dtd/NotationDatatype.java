package com.thaiopensource.xml.dtd;

public class NotationDatatype extends EnumDatatype {
  public NotationDatatype(EnumGroup enumGroup) {
    super(enumGroup);
  }

  public int getType() {
    return NOTATION;
  }

  public void accept(DatatypeVisitor visitor) throws VisitException {
    try {
      visitor.notationDatatype(getEnumGroup());
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }
}
