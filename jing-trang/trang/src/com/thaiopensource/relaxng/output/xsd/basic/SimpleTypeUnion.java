package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;
import java.util.Collections;

public class SimpleTypeUnion extends SimpleType {
  private final List children;

  public SimpleTypeUnion(SourceLocation location, Annotation annotation, List children) {
    super(location, annotation);
    this.children = Collections.unmodifiableList(children);
  }

  public List getChildren() {
    return children;
  }

  public Object accept(SimpleTypeVisitor visitor) {
    return visitor.visitUnion(this);
  }

  public boolean equals(Object obj) {
    return super.equals(obj) && children.equals(((SimpleTypeUnion)obj).children);
  }

  public int hashCode() {
    return super.hashCode() ^ children.hashCode();
  }
}
