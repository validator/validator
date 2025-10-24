package com.thaiopensource.xml.dtd.om;

public class Choice extends ModelGroup {
  
  private final ModelGroup[] members;

  public Choice(ModelGroup[] members) {
    this.members = members;
  }

  public int getType() {
    return CHOICE;
  }
  
  public ModelGroup[] getMembers() {
    ModelGroup[] tem = new ModelGroup[members.length];
    System.arraycopy(members, 0, tem, 0, members.length);
    return tem;
  }

  public void accept(ModelGroupVisitor visitor) throws Exception {
    visitor.choice(getMembers());
  }
}
