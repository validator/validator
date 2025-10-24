package com.thaiopensource.xml.infer;

import com.thaiopensource.xml.util.Name;

import java.util.Map;
import java.util.HashMap;

public class Schema {
  private final Map<Name, ElementDecl> elementDecls = new HashMap<Name, ElementDecl>();
  private Particle start;
  private final Map<String, String> prefixMap = new HashMap<String, String>();

  public Map<Name, ElementDecl> getElementDecls() {
    return elementDecls;
  }

  public Map<String, String> getPrefixMap() {
    return prefixMap;
  }

  public ElementDecl getElementDecl(Name name) {
    return elementDecls.get(name);
  }

  public Particle getStart() {
    return start;
  }

  public void setStart(Particle start) {
    this.start = start;
  }
}
