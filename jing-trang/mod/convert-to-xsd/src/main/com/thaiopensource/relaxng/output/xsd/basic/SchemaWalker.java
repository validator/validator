package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.util.VoidValue;

public abstract class SchemaWalker implements ParticleVisitor<VoidValue>, SimpleTypeVisitor<VoidValue>,
        SchemaVisitor, ComplexTypeVisitor<VoidValue>, AttributeUseVisitor<VoidValue> {
  public VoidValue visitElement(Element p) {
    return p.getComplexType().accept(this);
  }

  public VoidValue visitWildcardElement(WildcardElement p) {
    return VoidValue.VOID;
  }

  public VoidValue visitRepeat(ParticleRepeat p) {
    return p.getChild().accept(this);
  }

  public VoidValue visitSequence(ParticleSequence p) {
    return visitGroup(p);
  }

  public VoidValue visitChoice(ParticleChoice p) {
    return visitGroup(p);
  }

  public VoidValue visitAll(ParticleAll p) {
    return visitGroup(p);
  }

  public VoidValue visitGroup(ParticleGroup p) {
    for (Particle child : p.getChildren())
      child.accept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitGroupRef(GroupRef p) {
    return VoidValue.VOID;
  }

  public VoidValue visitRestriction(SimpleTypeRestriction t) {
    return VoidValue.VOID;
  }

  public VoidValue visitUnion(SimpleTypeUnion t) {
    for (SimpleType child : t.getChildren())
      child.accept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitList(SimpleTypeList t) {
    return t.getItemType().accept(this);
  }

  public VoidValue visitRef(SimpleTypeRef t) {
    return VoidValue.VOID;
  }

  public void visitGroup(GroupDefinition def) {
    def.getParticle().accept(this);
  }

  public void visitAttributeGroup(AttributeGroupDefinition def) {
    def.getAttributeUses().accept(this);
  }

  public VoidValue visitAttribute(Attribute a) {
    if (a.getType() == null)
      return VoidValue.VOID;
    return a.getType().accept(this);
  }

  public VoidValue visitWildcardAttribute(WildcardAttribute a) {
    return VoidValue.VOID;
  }

  public VoidValue visitOptionalAttribute(OptionalAttribute a) {
    return a.getAttribute().accept(this);
  }

  public VoidValue visitAttributeGroupRef(AttributeGroupRef a) {
    return VoidValue.VOID;
  }

  public VoidValue visitAttributeGroup(AttributeGroup a) {
    for (AttributeUse child : a.getChildren())
      child.accept(this);
    return VoidValue.VOID;
  }

  public VoidValue visitAttributeUseChoice(AttributeUseChoice a) {
    return visitAttributeGroup(a);
  }

  public void visitSimpleType(SimpleTypeDefinition def) {
    def.getSimpleType().accept(this);
  }

  public void visitRoot(RootDeclaration decl) {
    decl.getParticle().accept(this);
  }

  public void visitInclude(Include include) {
    include.getIncludedSchema().accept(this);
  }

  public void visitComment(Comment comment) {
  }

  public VoidValue visitComplexContent(ComplexTypeComplexContent t) {
    t.getAttributeUses().accept(this);
    if (t.getParticle() == null)
      return VoidValue.VOID;
    return t.getParticle().accept(this);
  }

  public VoidValue visitSimpleContent(ComplexTypeSimpleContent t) {
    t.getAttributeUses().accept(this);
    return t.getSimpleType().accept(this);
  }

  public VoidValue visitNotAllowedContent(ComplexTypeNotAllowedContent t) {
    return VoidValue.VOID;
  }
}
