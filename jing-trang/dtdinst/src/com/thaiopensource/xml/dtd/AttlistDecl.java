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

  public void accept(TopLevelVisitor visitor) throws VisitException {
    try {
      visitor.attlistDecl(elementName, attributeGroup);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }

}
