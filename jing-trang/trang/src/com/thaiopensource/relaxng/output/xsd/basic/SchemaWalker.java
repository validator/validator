package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;
import java.util.Iterator;

public abstract class SchemaWalker implements
        ParticleVisitor, SimpleTypeVisitor, SchemaVisitor, ComplexTypeVisitor, AttributeUseVisitor {
  public Object visitElement(Element p) {
    return p.getComplexType().accept(this);
  }

  public Object visitWildcardElement(WildcardElement p) {
    return null;
  }

  public Object visitRepeat(ParticleRepeat p) {
    return p.getChild().accept(this);
  }

  public Object visitSequence(ParticleSequence p) {
    return visitGroup(p);
  }

  public Object visitChoice(ParticleChoice p) {
    return visitGroup(p);
  }

  public Object visitAll(ParticleAll p) {
    return visitGroup(p);
  }

  public Object visitGroup(ParticleGroup p) {
    for (Iterator iter = p.getChildren().iterator(); iter.hasNext();)
      ((Particle)iter.next()).accept(this);
    return null;
  }

  public Object visitGroupRef(GroupRef p) {
    return null;
  }

  public Object visitRestriction(SimpleTypeRestriction t) {
    return null;
  }

  public Object visitUnion(SimpleTypeUnion t) {
    for (Iterator iter = t.getChildren().iterator(); iter.hasNext();)
      ((SimpleType)iter.next()).accept(this);
    return null;
  }

  public Object visitList(SimpleTypeList t) {
    return t.getItemType().accept(this);
  }

  public Object visitRef(SimpleTypeRef t) {
    return null;
  }

  public void visitGroup(GroupDefinition def) {
    def.getParticle().accept(this);
  }

  public void visitAttributeGroup(AttributeGroupDefinition def) {
    def.getAttributeUses().accept(this);
  }

  public Object visitAttribute(Attribute a) {
    if (a.getType() == null)
      return null;
    return a.getType().accept(this);
  }

  public Object visitWildcardAttribute(WildcardAttribute a) {
    return null;
  }

  public Object visitOptionalAttribute(OptionalAttribute a) {
    return a.getAttribute().accept(this);
  }

  public Object visitAttributeGroupRef(AttributeGroupRef a) {
    return null;
  }

  public Object visitAttributeGroup(AttributeGroup a) {
    for (Iterator iter = a.getChildren().iterator(); iter.hasNext();)
      ((AttributeUse)iter.next()).accept(this);
    return null;
  }

  public Object visitAttributeUseChoice(AttributeUseChoice a) {
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

  public Object visitComplexContent(ComplexTypeComplexContent t) {
    t.getAttributeUses().accept(this);
    if (t.getParticle() == null)
      return null;
    return t.getParticle().accept(this);
  }

  public Object visitSimpleContent(ComplexTypeSimpleContent t) {
    t.getAttributeUses().accept(this);
    return t.getSimpleType().accept(this);
  }

  public Object visitNotAllowedContent(ComplexTypeNotAllowedContent t) {
    return null;
  }
}
