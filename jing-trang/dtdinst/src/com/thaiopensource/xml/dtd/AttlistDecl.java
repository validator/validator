package com.thaiopensource.xml.dtd;

public class AttlistDecl extends TopLevel {
  
  private final String elementName;
  private final AttributeGroup attributeGroup;

  public AttlistDecl(String elementName, AttributeGroup attributeGroup) {
    this.elementName = elementName;
    this.attributeGroup = attributeGroup;
  }

  public int getType() {
    return ATTLIST_DECL;
  }

  public String getElementName() {
    return elementName;
  }
  
  public AttributeGroup getAttributeGroup() {
    return attributeGroup;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.attlistDecl(elementName, attributeGroup);
  }

}
