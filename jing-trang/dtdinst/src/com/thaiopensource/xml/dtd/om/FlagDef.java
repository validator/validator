package com.thaiopensource.xml.dtd.om;

public class FlagDef extends TopLevel {
  
  private final String name;
  private final Flag flag;

  public FlagDef(String name, Flag flag) {
    this.name = name;
    this.flag = flag;
  }

  public int getType() {
    return FLAG_DEF;
  }
  
  public Flag getFlag() {
    return flag;
  }
  
  public String getName() {
    return name;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.flagDef(name, flag);
  }
}
