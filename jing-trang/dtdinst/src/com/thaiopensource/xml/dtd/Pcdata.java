package com.thaiopensource.xml.dtd;

public class Pcdata extends ModelGroup {
  
  public Pcdata() { }

  public int getType() {
    return PCDATA;
  }

  public void accept(ModelGroupVisitor visitor) throws VisitException {
    try {
      visitor.pcdata();
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }
}
