package com.thaiopensource.xml.infer;

import java.util.Map;
import java.util.HashMap;

public class ElementDecl {
  private Particle contentModel;
  private final Map attributeDecls = new HashMap();
  private boolean start = false;

  public Map getAttributeDecls() {
    return attributeDecls;
  }

  public Particle getContentModel() {
    return contentModel;
  }

  public void setContentModel(Particle contentModel) {
    this.contentModel = contentModel;
  }

  public boolean isStart() {
    return start;
  }

  public void setStart(boolean start) {
    this.start = start;
  }
}
