package com.thaiopensource.xml.dtd.om;

public class AttributeGroupDef extends Def {
  
  private final AttributeGroup attributeGroup;

  public AttributeGroupDef(String name, AttributeGroup attributeGroup) {
    super(name);
    this.attributeGroup = attributeGroup;
  }

  public int getType() {
    return ATTRIBUTE_GROUP_DEF;
  }
  
  public AttributeGroup getAttributeGroup() {
    return attributeGroup;
  }
  
  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.attributeGroupDef(getName(), attributeGroup);
  }
}
