package com.thaiopensource.xml.dtd.om;

public class AttributeDefaultRef extends AttributeDefault {
  
  private final String name;
  private final AttributeDefault attributeDefault;

  public AttributeDefaultRef(String name, AttributeDefault attributeDefault) {
    this.name = name;
    this.attributeDefault = attributeDefault;
  }

  public int getType() {
    return ATTRIBUTE_DEFAULT_REF;
  }
  
  public AttributeDefault getAttributeDefault() {
    return attributeDefault;
  }
  
  public String getName() {
    return name;
  }

  public void accept(AttributeDefaultVisitor visitor) throws Exception {
    visitor.attributeDefaultRef(name, attributeDefault);
  }

  public boolean isRequired() {
    return attributeDefault.isRequired();
  }

  public String getDefaultValue() {
    return attributeDefault.getDefaultValue();
  }

  public String getFixedValue() {
    return attributeDefault.getFixedValue();
  }
}
