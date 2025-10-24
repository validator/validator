package com.thaiopensource.xml.dtd.om;

public class FlagRef extends Flag {
  private final String name;
  private final Flag flag;

  public FlagRef(String name, Flag flag) {
    this.name = name;
    this.flag = flag;
  }

  public int getType() {
    return FLAG_REF;
  }
  
  public Flag getFlag() {
    return flag;
  }
  
  public String getName() {
    return name;
  }

  public void accept(FlagVisitor visitor) throws Exception {
    visitor.flagRef(name, flag);
  }
}
