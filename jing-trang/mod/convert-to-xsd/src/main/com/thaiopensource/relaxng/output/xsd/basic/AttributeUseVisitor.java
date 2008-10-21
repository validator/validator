package com.thaiopensource.relaxng.output.xsd.basic;

public interface AttributeUseVisitor<T> {
  T visitAttribute(Attribute a);
  T visitOptionalAttribute(OptionalAttribute a);
  T visitAttributeGroupRef(AttributeGroupRef a);
  T visitAttributeGroup(AttributeGroup a);
  T visitAttributeUseChoice(AttributeUseChoice a);
  T visitWildcardAttribute(WildcardAttribute a);
}
