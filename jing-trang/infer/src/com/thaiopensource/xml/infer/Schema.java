package com.thaiopensource.xml.infer;

import com.thaiopensource.relaxng.output.common.Name;

import java.util.Map;
import java.util.HashMap;

public class Schema {
  private final Map elementDecls = new HashMap();

  public Map getElementDecls() {
    return elementDecls;
  }

  public ElementDecl getElementDecl(Name name) {
    return (ElementDecl)elementDecls.get(name);
  }
}
