package com.thaiopensource.xml.infer;

import com.thaiopensource.relaxng.output.common.Name;

import java.util.Set;

public abstract class ContentModelInferrer {
  public abstract void addElement(Name elementName);

  public abstract void endSequence();

  public abstract Particle inferContentModel();

  public abstract Set getElementNames();

  public static ContentModelInferrer createContentModelInferrer() {
    return new ContentModelInferrerImpl();
  }
}
