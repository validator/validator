package com.thaiopensource.xml.dtd.om;

public class AttributeDefaultDef extends TopLevel {
  
  private final String name;
  private final AttributeDefault attributeDefault;

  public AttributeDefaultDef(String name, AttributeDefault attributeDefault) {
    this.name = name;
    this.attributeDefault = attributeDefault;
  }

  public int getType() {
    return ATTRIBUTE_DEFAULT_DEF;
  }
  
  public AttributeDefault getAttributeDefault() {
    return attributeDefault;
  }
  
  public String getName() {
    return name;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.attributeDefaultDef(name, attributeDefault);
  }
}
