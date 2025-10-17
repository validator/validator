package com.thaiopensource.xml.dtd.om;

public class OneOrMore extends ModelGroup {
  
  private final ModelGroup member;

  public OneOrMore(ModelGroup member) {
    this.member = member;
  }

  public int getType() {
    return ONE_OR_MORE;
  }
  
  public ModelGroup getMember() {
    return member;
  }

  public void accept(ModelGroupVisitor visitor) throws Exception {
    visitor.oneOrMore(member);
  }
}
