package com.thaiopensource.xml.dtd;

public class ModelGroupDef extends TopLevel {
  
  private final String name;
  private final ModelGroup modelGroup;

  public ModelGroupDef(String name, ModelGroup modelGroup) {
    this.name = name;
    this.modelGroup = modelGroup;
  }

  public int getType() {
    return MODEL_GROUP_DEF;
  }
  
  public ModelGroup getModelGroup() {
    return modelGroup;
  }
  
  public String getName() {
    return name;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.modelGroupDef(name, modelGroup);
  }
}
