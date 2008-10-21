package com.thaiopensource.xml.dtd.om;

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

  public void accept(EnumGroupVisitor visitor) throws Exception {
    visitor.enumGroupRef(name, enumGroup);
  }
}
