package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.util.Equal;

public class OptionalAttribute extends SingleAttributeUse {
  private final Attribute attribute;
  private final String defaultValue;

  public OptionalAttribute(SourceLocation location, Annotation annotation, Attribute attribute, String defaultValue) {
    super(location, annotation);
    this.attribute = attribute;
    this.defaultValue = defaultValue;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public Object accept(AttributeUseVisitor visitor) {
    return visitor.visitOptionalAttribute(this);
  }

  public Name getName() {
    return attribute.getName();
  }

  public SimpleType getType() {
    return attribute.getType();
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public boolean isOptional() {
    return true;
  }

  public boolean equals(Object obj) {
    return (super.equals(obj)
            && ((OptionalAttribute)obj).attribute.equals(attribute)
            && Equal.equal(defaultValue, ((OptionalAttribute)obj).defaultValue));
  }

  public int hashCode() {
    int hc = super.hashCode() ^ attribute.hashCode();
    if (defaultValue != null)
      hc ^= defaultValue.hashCode();
    return hc;
  }
}
