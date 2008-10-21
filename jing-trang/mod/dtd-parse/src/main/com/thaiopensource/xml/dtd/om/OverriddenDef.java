package com.thaiopensource.xml.dtd.om;

public class OverriddenDef extends TopLevel {
  private final boolean duplicate;
  private final Def def;

  public OverriddenDef(Def def, boolean duplicate) {
    this.def = def;
    this.duplicate = duplicate;
  }

  public int getType() {
    return OVERRIDDEN_DEF;
  }

  public Def getDef() {
    return def;
  }

  public boolean isDuplicate() {
    return duplicate;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.overriddenDef(def, duplicate);
  }

}
