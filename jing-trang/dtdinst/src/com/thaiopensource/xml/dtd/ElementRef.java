package com.thaiopensource.xml.dtd;

public class ElementRef extends ModelGroup {
  
  private final String name;

  public ElementRef(String name) {
    this.name = name;
  }

  public int getType() {
    return ELEMENT_REF;
  }

  public String getName() {
    return name;
  }

  public void accept(ModelGroupVisitor visitor) throws VisitException {
    try {
      visitor.elementRef(name);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }
  
}
