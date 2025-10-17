package com.thaiopensource.relaxng.edit;

public class AnyNameNameClass extends OpenNameClass {
  public AnyNameNameClass() {
  }

  public AnyNameNameClass(NameClass except) {
    super(except);
  }

  public <T> T accept(NameClassVisitor<T> visitor) {
    return visitor.visitAnyName(this);
  }
}
