package com.thaiopensource.xml.dtd.om;

public class NameSpecDef extends Def {
  private final NameSpec nameSpec;

  public NameSpecDef(String name, NameSpec nameSpec) {
    super(name);
    this.nameSpec = nameSpec;
  }

  public int getType() {
    return NAME_SPEC_DEF;
  }

  public NameSpec getNameSpec() {
    return nameSpec;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.nameSpecDef(getName(), nameSpec);
  }
}
