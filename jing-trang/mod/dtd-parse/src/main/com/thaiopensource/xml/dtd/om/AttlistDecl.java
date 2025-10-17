package com.thaiopensource.xml.dtd.om;

public class AttlistDecl extends TopLevel {
  
  private final NameSpec elementNameSpec;
  private final AttributeGroup attributeGroup;

  public AttlistDecl(NameSpec elementNameSpec, AttributeGroup attributeGroup) {
    this.elementNameSpec = elementNameSpec;
    this.attributeGroup = attributeGroup;
  }

  public int getType() {
    return ATTLIST_DECL;
  }

  public NameSpec getElementNameSpec() {
    return elementNameSpec;
  }
  
  public AttributeGroup getAttributeGroup() {
    return attributeGroup;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.attlistDecl(elementNameSpec, attributeGroup);
  }

}
