package com.thaiopensource.xml.dtd.om;

public class ParamDef extends Def {
  private final String value;

  public ParamDef(String name, String value) {
    super(name);
    this.value = value;
  }

  public int getType() {
    return PARAM_DEF;
  }

  public String getValue() {
    return value;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.paramDef(getName(), value);
  }

}
