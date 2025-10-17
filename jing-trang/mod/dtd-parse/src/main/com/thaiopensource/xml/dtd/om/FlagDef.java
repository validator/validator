package com.thaiopensource.xml.dtd.om;

public class FlagDef extends Def {
  
  private final Flag flag;

  public FlagDef(String name, Flag flag) {
    super(name);
    this.flag = flag;
  }

  public int getType() {
    return FLAG_DEF;
  }
  
  public Flag getFlag() {
    return flag;
  }
  
  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.flagDef(getName(), flag);
  }
}
