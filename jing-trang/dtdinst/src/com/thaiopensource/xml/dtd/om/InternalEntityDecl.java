package com.thaiopensource.xml.dtd.om;

public class InternalEntityDecl extends TopLevel {
  
  private final String name;
  private final String value;

  public InternalEntityDecl(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public int getType() {
    return INTERNAL_ENTITY_DECL;
  }
  
  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.internalEntityDecl(name, value);
  }
}
