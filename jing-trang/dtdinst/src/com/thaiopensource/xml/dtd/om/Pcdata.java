package com.thaiopensource.xml.dtd.om;

public class Pcdata extends ModelGroup {
  
  public Pcdata() { }

  public int getType() {
    return PCDATA;
  }

  public void accept(ModelGroupVisitor visitor) throws Exception {
    visitor.pcdata();
  }
}
