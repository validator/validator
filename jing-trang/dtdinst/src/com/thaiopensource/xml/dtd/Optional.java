package com.thaiopensource.xml.dtd;

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

  public void accept(ModelGroupVisitor visitor) throws VisitException {
    try {
      visitor.optional(member);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }
}
