package com.thaiopensource.xml.dtd.om;

public class NameSpecDef extends TopLevel {
  private final String name;
  private final NameSpec nameSpec;

  public NameSpecDef(String name, NameSpec nameSpec) {
    this.name = name;
    this.nameSpec = nameSpec;
  }

  public int getType() {
    return NAME_SPEC_DEF;
  }

  public String getName() {
    return name;
  }

  public NameSpec getNameSpec() {
    return nameSpec;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.nameSpecDef(name, nameSpec);
  }
}
