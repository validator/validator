package com.thaiopensource.xml.dtd.om;

public class ZeroOrMore extends ModelGroup {
  
  private final ModelGroup member;

  public ZeroOrMore(ModelGroup member) {
    this.member = member;
  }

  public int getType() {
    return ZERO_OR_MORE;
  }
  
  public ModelGroup getMember() {
    return member;
  }

  public void accept(ModelGroupVisitor visitor) throws Exception {
    visitor.zeroOrMore(member);
  }
}
