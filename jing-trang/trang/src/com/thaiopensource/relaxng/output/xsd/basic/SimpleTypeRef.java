package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class SimpleTypeRef extends SimpleType {
  private final String name;

  public SimpleTypeRef(SourceLocation location, Annotation annotation, String name) {
    super(location, annotation);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Object accept(SimpleTypeVisitor visitor) {
    return visitor.visitRef(this);
  }

  public boolean equals(Object obj) {
    return super.equals(obj) && ((SimpleTypeRef)obj).name.equals(name);
  }

  public int hashCode() {
    return super.hashCode() ^ name.hashCode();
  }
}
