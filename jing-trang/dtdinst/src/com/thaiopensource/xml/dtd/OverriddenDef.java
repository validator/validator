package com.thaiopensource.xml.dtd;

public class OverriddenDef extends TopLevel {
  private final String name;
  private final String value;

  public OverriddenDef(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public int getType() {
    return OVERRIDDEN_DEF;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.overriddenDef(name, value);
  }

}
