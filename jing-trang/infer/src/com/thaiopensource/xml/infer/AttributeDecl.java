package com.thaiopensource.xml.infer;

import com.thaiopensource.relaxng.output.common.Name;

public class AttributeDecl {
  private final Name datatype;
  private final boolean optional;

  public AttributeDecl(Name datatype, boolean optional) {
    this.datatype = datatype;
    this.optional = optional;
  }

  /**
   * @return null for anything
   */
  public Name getDatatype() {
    return datatype;
  }

  public boolean isOptional() {
    return optional;
  }
}
