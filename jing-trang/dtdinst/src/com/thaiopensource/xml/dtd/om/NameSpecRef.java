package com.thaiopensource.xml.dtd.om;

public class NameSpecRef extends NameSpec {
  private final String name;
  private final NameSpec nameSpec;

  public NameSpecRef(String name, NameSpec nameSpec) {
    this.name = name;
    this.nameSpec = nameSpec;
  }

  public int getType() {
    return NAME_SPEC_REF;
  }

  public String getName() {
    return name;
  }

  public NameSpec getNameSpec() {
    return nameSpec;
  }

  public void accept(NameSpecVisitor visitor) throws Exception {
    visitor.nameSpecRef(name, nameSpec);
  }

  public String getValue() {
    return nameSpec.getValue();
  }
}
