package com.thaiopensource.xml.dtd;

public class ElementDecl extends TopLevel {
  
  private final String name;
  private final ModelGroup modelGroup;

  public ElementDecl(String name, ModelGroup modelGroup) {
    this.name = name;
    this.modelGroup = modelGroup;
  }

  public int getType() {
    return ELEMENT_DECL;
  }

  public String getName() {
    return name;
  }
  
  public ModelGroup getModelGroup() {
    return modelGroup;
  }

  public void accept(TopLevelVisitor visitor) throws VisitException {
    try {
      visitor.elementDecl(name, modelGroup);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new VisitException(e);
    }
  }

}
