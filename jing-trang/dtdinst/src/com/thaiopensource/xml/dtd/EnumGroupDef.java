package com.thaiopensource.xml.dtd;

public class EnumGroupDef extends TopLevel {
  
  private final String name;
  private final EnumGroup enumGroup;

  public EnumGroupDef(String name, EnumGroup enumGroup) {
    this.name = name;
    this.enumGroup = enumGroup;
  }

  public int getType() {
    return ENUM_GROUP_DEF;
  }
  
  public EnumGroup getEnumGroup() {
    return enumGroup;
  }
  
  public String getName() {
    return name;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.enumGroupDef(name, enumGroup);
  }
}
