package com.thaiopensource.xml.dtd;

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

  public void accept(ModelGroupVisitor visitor) throws VisitException {
    try {
      visitor.modelGroupRef(name, modelGroup);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }
}
