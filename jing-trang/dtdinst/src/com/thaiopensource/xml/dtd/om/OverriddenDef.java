package com.thaiopensource.xml.dtd.om;

public class OverriddenDef extends TopLevel {
  private final String name;
  private final String value;
  private final boolean duplicate;

  public OverriddenDef(String name, String value, boolean duplicate) {
    this.name = name;
    this.value = value;
    this.duplicate = duplicate;
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

  public boolean isDuplicate() {
    return duplicate;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.overriddenDef(name, value, duplicate);
  }

}
