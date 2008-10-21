package com.thaiopensource.xml.dtd.om;

public class ElementDecl extends TopLevel {
  
  private final NameSpec nameSpec;
  private final ModelGroup modelGroup;

  public ElementDecl(NameSpec nameSpec, ModelGroup modelGroup) {
    this.nameSpec = nameSpec;
    this.modelGroup = modelGroup;
  }

  public int getType() {
    return ELEMENT_DECL;
  }

  public NameSpec getNameSpec() {
    return nameSpec;
  }
  
  public ModelGroup getModelGroup() {
    return modelGroup;
  }

  public void accept(TopLevelVisitor visitor) throws Exception {
    visitor.elementDecl(nameSpec, modelGroup);
  }

}
