package com.thaiopensource.xml.dtd;

public class Attribute extends AttributeGroupMember {
  private final NameSpec nameSpec;
  private final boolean optional;
  private final Datatype datatype;
  private final String defaultValue;

  public Attribute(NameSpec nameSpec,
		   boolean optional,
		   Datatype datatype,
		   String defaultValue) {
    this.nameSpec = nameSpec;
    this.optional = optional;
    this.datatype = datatype;
    this.defaultValue = defaultValue;
  }

  
  public NameSpec getNameSpec() {
    return nameSpec;
  }

  public boolean isOptional() {
    return optional;
  }

  public Datatype getDatatype() {
    return datatype;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void accept(AttributeGroupVisitor visitor) throws Exception {
    visitor.attribute(nameSpec, optional, datatype, defaultValue);
  }

  public int getType() {
    return ATTRIBUTE;
  }
  
}
