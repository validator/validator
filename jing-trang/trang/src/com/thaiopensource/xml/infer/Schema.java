package com.thaiopensource.xml.infer;

import com.thaiopensource.relaxng.output.common.Name;

import java.util.Map;
import java.util.HashMap;

public class Schema {
  private final Map elementDecls = new HashMap();
  private Particle start;
  private final Map prefixMap = new HashMap();

  public Map getElementDecls() {
    return elementDecls;
  }

  public Map getPrefixMap() {
    return prefixMap;
  }

  public ElementDecl getElementDecl(Name name) {
    return (ElementDecl)elementDecls.get(name);
  }

  public Particle getStart() {
    return start;
  }

  public void setStart(Particle start) {
    this.start = start;
  }
}
