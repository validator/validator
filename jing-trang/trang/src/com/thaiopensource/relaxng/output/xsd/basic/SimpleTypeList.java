package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class SimpleTypeList extends SimpleType {
  private final SimpleType itemType;
  private final Occurs occurs;

  public SimpleTypeList(SourceLocation location, Annotation annotation, SimpleType itemType, Occurs occurs) {
    super(location, annotation);
    this.itemType = itemType;
    this.occurs = occurs;
  }

  public SimpleType getItemType() {
    return itemType;
  }

  public Occurs getOccurs() {
    return occurs;
  }

  public Object accept(SimpleTypeVisitor visitor) {
    return visitor.visitList(this);
  }

  public boolean equals(Object obj) {
    if (!super.equals(obj))
      return false;
    SimpleTypeList other = (SimpleTypeList)obj;
    return this.itemType.equals(other.itemType) && this.occurs.equals(other.occurs);
  }

  public int hashCode() {
    return super.hashCode() ^ itemType.hashCode() ^ occurs.hashCode();
  }
}
