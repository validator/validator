package com.thaiopensource.xml.dtd;

public class Attribute extends AttributeGroupMember {
  private final String name;
  private final boolean optional;
  private final Datatype datatype;
  private final String defaultValue;

  public Attribute(String name,
		   boolean optional,
		   Datatype datatype,
		   String defaultValue) {
    this.name = name;
    this.optional = optional;
    this.datatype = datatype;
    this.defaultValue = defaultValue;
  }

  
  public String getName() {
    return name;
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
    visitor.attribute(name, optional, datatype, defaultValue);
  }

  public int getType() {
    return ATTRIBUTE;
  }
  
}
