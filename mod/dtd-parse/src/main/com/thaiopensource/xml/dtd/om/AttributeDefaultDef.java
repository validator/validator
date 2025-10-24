package com.thaiopensource.xml.dtd.om;

public class AttributeDefaultDef extends Def {
  
  private final AttributeDefault attributeDefault;

  public AttributeDefaultDef(String name, AttributeDefault attributeDefault) {
    super(name);
    this.attributeDefault = attributeDefault;
  }

  public int getType() {
    return ATTRIBUTE_DEFAULT_DEF;
  }
  
  public AttributeDefault getAttributeDefault() {
    return attributeDefault;
  }
  
  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.attributeDefaultDef(getName(), attributeDefault);
  }
}
