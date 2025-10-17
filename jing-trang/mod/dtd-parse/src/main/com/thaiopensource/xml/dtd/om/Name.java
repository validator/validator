package com.thaiopensource.xml.dtd.om;

public class Name extends NameSpec {
  private final String value;

  public Name(String value) {
    this.value = value;
  }

  public int getType() {
    return NAME;
  }

  public String getValue() {
    return value;
  }

  public void accept(NameSpecVisitor visitor) throws Exception {
    visitor.name(value);
  }
}
