package com.thaiopensource.xml.dtd;

public class DatatypeDef extends TopLevel {
  
  private final String name;
  private final Datatype datatype;

  public DatatypeDef(String name, Datatype datatype) {
    this.name = name;
    this.datatype = datatype;
  }

  public int getType() {
    return DATATYPE_DEF;
  }
  
  public Datatype getDatatype() {
    return datatype;
  }
  
  public String getName() {
    return name;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.datatypeDef(name, datatype);
  }
}
