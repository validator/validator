package com.thaiopensource.xml.dtd.om;

public class Any extends ModelGroup {
  
  public Any() { }

  public int getType() {
    return ANY;
  }

  public void accept(ModelGroupVisitor visitor) throws Exception {
    visitor.any();
  }
}
