package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.util.Equal;

public class Annotation {
  private final String documentation;

  public Annotation(String documentation) {
    this.documentation = documentation;
  }

  public String getDocumentation() {
    return documentation;
  }

  public boolean equals(Object obj) {
    return obj instanceof Annotation && Equal.equal(documentation, ((Annotation)obj).documentation);
  }

  public int hashCode() {
    if (documentation != null)
      return documentation.hashCode();
    return Annotation.class.hashCode();
  }
}
