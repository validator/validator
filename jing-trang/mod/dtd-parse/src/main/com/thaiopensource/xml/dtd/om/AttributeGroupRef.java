package com.thaiopensource.xml.dtd.om;

public class AttributeGroupRef extends AttributeGroupMember {
  
  private final String name;
  private final AttributeGroup attributeGroup;

  public AttributeGroupRef(String name, AttributeGroup attributeGroup) {
    this.name = name;
    this.attributeGroup = attributeGroup;
  }

  public int getType() {
    return ATTRIBUTE_GROUP_REF;
  }
  
  public AttributeGroup getAttributeGroup() {
    return attributeGroup;
  }
  
  public String getName() {
    return name;
  }

  public void accept(AttributeGroupVisitor visitor) throws Exception {
    visitor.attributeGroupRef(name, attributeGroup);
  }
}
