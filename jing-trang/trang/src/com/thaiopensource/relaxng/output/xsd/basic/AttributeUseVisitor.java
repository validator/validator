package com.thaiopensource.relaxng.output.xsd.basic;

public interface AttributeUseVisitor {
  Object visitAttribute(Attribute a);
  Object visitOptionalAttribute(OptionalAttribute a);
  Object visitAttributeGroupRef(AttributeGroupRef a);
  Object visitAttributeGroup(AttributeGroup a);
  Object visitAttributeUseChoice(AttributeUseChoice a);
  Object visitWildcardAttribute(WildcardAttribute a);
}
