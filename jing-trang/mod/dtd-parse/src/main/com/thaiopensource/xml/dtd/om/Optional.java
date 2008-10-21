package com.thaiopensource.xml.dtd.om;

public class Optional extends ModelGroup {
  
  private final ModelGroup member;

  public Optional(ModelGroup member) {
    this.member = member;
  }

  public int getType() {
    return OPTIONAL;
  }
  
  public ModelGroup getMember() {
    return member;
  }

  public void accept(ModelGroupVisitor visitor) throws Exception {
    visitor.optional(member);
  }
}
