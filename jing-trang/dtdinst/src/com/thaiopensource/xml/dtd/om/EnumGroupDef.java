package com.thaiopensource.xml.dtd.om;

public class EnumGroupDef extends Def {
  
  private final EnumGroup enumGroup;

  public EnumGroupDef(String name, EnumGroup enumGroup) {
    super(name);
    this.enumGroup = enumGroup;
  }

  public int getType() {
    return ENUM_GROUP_DEF;
  }
  
  public EnumGroup getEnumGroup() {
    return enumGroup;
  }
  
  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.enumGroupDef(getName(), enumGroup);
  }
}
