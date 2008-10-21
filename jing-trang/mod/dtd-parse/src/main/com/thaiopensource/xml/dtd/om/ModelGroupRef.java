package com.thaiopensource.xml.dtd.om;

public class ModelGroupRef extends ModelGroup {
  
  private final String name;
  private final ModelGroup modelGroup;

  public ModelGroupRef(String name, ModelGroup modelGroup) {
    this.name = name;
    this.modelGroup = modelGroup;
  }

  public int getType() {
    return MODEL_GROUP_REF;
  }
  
  public ModelGroup getModelGroup() {
    return modelGroup;
  }
  
  public String getName() {
    return name;
  }

  public void accept(ModelGroupVisitor visitor) throws Exception {
    visitor.modelGroupRef(name, modelGroup);
  }
}
