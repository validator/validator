package com.thaiopensource.xml.infer;

import com.thaiopensource.relaxng.output.common.Name;

public class AttributeDecl {
  private final Name name;
  private final String datatype;
  private final boolean optional;

  public AttributeDecl(Name name, String datatype, boolean optional) {
    this.name = name;
    this.datatype = datatype;
    this.optional = optional;
  }

  public Name getName() {
    return name;
  }

  public String getDatatype() {
    return datatype;
  }

  public boolean isOptional() {
    return optional;
  }
}
