package com.thaiopensource.xml.dtd.om;

public class AttributeGroup {
  private final AttributeGroupMember[] members;

  public AttributeGroup(AttributeGroupMember[] members) {
    this.members = members;
  }

  public AttributeGroupMember[] getMembers() {
    AttributeGroupMember[] tem = new AttributeGroupMember[members.length];
    System.arraycopy(members, 0, tem, 0, members.length);
    return tem;
  }
  
  public void accept(AttributeGroupVisitor visitor) throws Exception {
    for (int i = 0; i < members.length; i++)
      members[i].accept(visitor);
  }
}
