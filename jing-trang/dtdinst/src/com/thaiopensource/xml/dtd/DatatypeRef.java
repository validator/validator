package com.thaiopensource.xml.dtd;

public class DatatypeRef extends Datatype {
  
  private final String name;
  private final Datatype datatype;

  public DatatypeRef(String name, Datatype datatype) {
    this.name = name;
    this.datatype = datatype;
  }

  public int getType() {
    return DATATYPE_REF;
  }
  
  public Datatype getDatatype() {
    return datatype;
  }
  
  public String getName() {
    return name;
  }

  public void accept(DatatypeVisitor visitor) throws Exception {
    visitor.datatypeRef(name, datatype);
  }
}
