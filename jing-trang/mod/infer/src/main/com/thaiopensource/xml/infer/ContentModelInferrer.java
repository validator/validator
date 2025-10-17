package com.thaiopensource.xml.infer;

import com.thaiopensource.xml.util.Name;

import java.util.Set;

public abstract class ContentModelInferrer {
  public abstract void addElement(Name elementName);

  public abstract void endSequence();

  public abstract Particle inferContentModel();

  public abstract Set<Name> getElementNames();

  public static ContentModelInferrer createContentModelInferrer() {
    return new ContentModelInferrerImpl();
  }
}
