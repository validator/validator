package com.thaiopensource.xml.dtd;

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

  public void accept(AttributeGroupVisitor visitor) throws VisitException {
    try {
      visitor.attributeGroupRef(name, attributeGroup);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }
}
