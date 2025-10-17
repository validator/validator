package com.thaiopensource.xml.dtd.om;

public class Attribute extends AttributeGroupMember {
  private final NameSpec nameSpec;
  private final Datatype datatype;
  private final AttributeDefault attributeDefault;

  public Attribute(NameSpec nameSpec,
		   Datatype datatype,
		   AttributeDefault attributeDefault) {
    this.nameSpec = nameSpec;
    this.datatype = datatype;
    this.attributeDefault = attributeDefault;
  }

  public NameSpec getNameSpec() {
    return nameSpec;
  }

  public Datatype getDatatype() {
    return datatype;
  }

  public AttributeDefault getAttributeDefault() {
    return attributeDefault;
  }

  public void accept(AttributeGroupVisitor visitor) throws Exception {
    visitor.attribute(nameSpec, datatype, attributeDefault);
  }

  public int getType() {
    return ATTRIBUTE;
  }
  
}
