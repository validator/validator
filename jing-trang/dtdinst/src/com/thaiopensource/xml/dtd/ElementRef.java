package com.thaiopensource.xml.dtd;

public class ElementRef extends ModelGroup {
  
  private final NameSpec nameSpec;

  public ElementRef(NameSpec nameSpec) {
    this.nameSpec = nameSpec;
  }

  public int getType() {
    return ELEMENT_REF;
  }

  public NameSpec getNameSpec() {
    return nameSpec;
  }

  public void accept(ModelGroupVisitor visitor) throws Exception {
    visitor.elementRef(nameSpec);
  }
  
}
