package com.thaiopensource.xml.dtd;

public class EnumGroupRef extends EnumGroupMember {
  
  private final String name;
  private final EnumGroup enumGroup;

  public EnumGroupRef(String name, EnumGroup enumGroup) {
    this.name = name;
    this.enumGroup = enumGroup;
  }

  public int getType() {
    return ENUM_GROUP_REF;
  }
  
  public EnumGroup getEnumGroup() {
    return enumGroup;
  }
  
  public String getName() {
    return name;
  }

  public void accept(EnumGroupVisitor visitor) throws VisitException {
    try {
      visitor.enumGroupRef(name, enumGroup);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }
}
