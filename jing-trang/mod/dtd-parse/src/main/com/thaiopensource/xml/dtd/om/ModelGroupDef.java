package com.thaiopensource.xml.dtd.om;

public class ModelGroupDef extends Def {
  
  private final ModelGroup modelGroup;

  public ModelGroupDef(String name, ModelGroup modelGroup) {
    super(name);
    this.modelGroup = modelGroup;
  }

  public int getType() {
    return MODEL_GROUP_DEF;
  }
  
  public ModelGroup getModelGroup() {
    return modelGroup;
  }
  
  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.modelGroupDef(getName(), modelGroup);
  }
}
