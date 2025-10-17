package com.thaiopensource.xml.dtd.om;

public class DatatypeDef extends Def {
  
  private final Datatype datatype;

  public DatatypeDef(String name, Datatype datatype) {
    super(name);
    this.datatype = datatype;
  }

  public int getType() {
    return DATATYPE_DEF;
  }
  
  public Datatype getDatatype() {
    return datatype;
  }
  
  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.datatypeDef(getName(), datatype);
  }
}
