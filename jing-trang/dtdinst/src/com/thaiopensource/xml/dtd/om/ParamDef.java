package com.thaiopensource.xml.dtd.om;

public class ParamDef extends TopLevel {
  private final String name;
  private final String value;

  public ParamDef(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public int getType() {
    return PARAM_DEF;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.paramDef(name, value);
  }

}
