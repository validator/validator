package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;
import java.util.Collections;

public class SimpleTypeRestriction extends SimpleType {
  private final String name;
  private final List facets;

  public SimpleTypeRestriction(SourceLocation location, Annotation annotation, String name, List facets) {
    super(location, annotation);
    this.name = name;
    this.facets = Collections.unmodifiableList(facets);
  }

  /**
   * Name is the name of a builtin simple type.
   * facets is a list of facets
   */

  public String getName() {
    return name;
  }

  public List getFacets() {
    return facets;
  }

  public Object accept(SimpleTypeVisitor visitor) {
    return visitor.visitRestriction(this);
  }

  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    SimpleTypeRestriction other = (SimpleTypeRestriction)obj;
    return this.name.equals(other.name) && this.facets.equals(other.facets);
  }

  public int hashCode() {
    return super.hashCode() ^ name.hashCode() ^ facets.hashCode();
  }
}
