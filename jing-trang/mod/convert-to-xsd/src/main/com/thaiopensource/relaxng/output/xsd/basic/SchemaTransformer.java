package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;
import java.util.Vector;

public class SchemaTransformer implements SchemaVisitor, ParticleVisitor<Particle>,
        ComplexTypeVisitor<ComplexType>, AttributeUseVisitor<AttributeUse>, SimpleTypeVisitor<SimpleType> {
  private final Schema schema;

  public SchemaTransformer(Schema schema) {
    this.schema = schema;
  }

  public Schema getSchema() {
    return schema;
  }

  public void transform() {
    schema.accept(this);
  }

  public void visitGroup(GroupDefinition def) {
    def.setParticle(def.getParticle().accept(this));
  }

  public void visitAttributeGroup(AttributeGroupDefinition def) {
    def.setAttributeUses(def.getAttributeUses().accept(this));
  }

  public void visitSimpleType(SimpleTypeDefinition def) {
    def.setSimpleType(def.getSimpleType().accept(this));
  }

  public void visitRoot(RootDeclaration decl) {
    decl.setParticle(decl.getParticle().accept(this));
  }

  public void visitInclude(Include include) {
    include.getIncludedSchema().accept(this);
  }

  public void visitComment(Comment comment) {
  }

  public Particle visitRepeat(ParticleRepeat p) {
    Particle child = p.getChild().accept(this);
    if (child == p.getChild())
      return p;
    return new ParticleRepeat(p.getLocation(), p.getAnnotation(), child, p.getOccurs());
  }

  public Particle visitGroupRef(GroupRef p) {
    return p;
  }

  public Particle visitElement(Element p) {
    ComplexType ct = p.getComplexType().accept(this);
    if (ct == p.getComplexType())
      return p;
    return new Element(p.getLocation(), p.getAnnotation(), p.getName(), ct);
  }

  public Particle visitWildcardElement(WildcardElement p) {
    return p;
  }

  public Particle visitSequence(ParticleSequence p) {
    List<Particle> children = transformParticleList(p.getChildren());
    if (children == p.getChildren())
      return p;
    if (children.size() == 1)
      return children.get(0);
    if (children.size() == 0)
      return null;
    return new ParticleSequence(p.getLocation(), p.getAnnotation(), children);
  }

  public Particle visitChoice(ParticleChoice p) {
    List<Particle> children = transformParticleList(p.getChildren());
    if (children == p.getChildren())
      return p;
    return new ParticleChoice(p.getLocation(), p.getAnnotation(), children);
  }

  public Particle visitAll(ParticleAll p) {
    List<Particle> children = transformParticleList(p.getChildren());
    if (children == p.getChildren())
      return p;
    return new ParticleAll(p.getLocation(), p.getAnnotation(), children);
  }

  public ComplexType visitComplexContent(ComplexTypeComplexContent t) {
    Particle particle = t.getParticle();
    AttributeUse attributeUses = t.getAttributeUses().accept(this);
    if (particle != null)
      particle = particle.accept(this);
    if (particle == t.getParticle() && attributeUses == t.getAttributeUses())
      return t;
    return new ComplexTypeComplexContent(attributeUses, particle, t.isMixed());
  }

  public ComplexType visitSimpleContent(ComplexTypeSimpleContent t) {
    SimpleType simpleType = t.getSimpleType().accept(this);
    AttributeUse attributeUses = t.getAttributeUses().accept(this);
    if (simpleType == t.getSimpleType() && attributeUses == t.getAttributeUses())
      return t;
    return new ComplexTypeSimpleContent(attributeUses, simpleType);
  }

  public ComplexType visitNotAllowedContent(ComplexTypeNotAllowedContent t) {
    return t;
  }

  public AttributeUse visitAttribute(Attribute a) {
    SimpleType type = a.getType();
    if (type != null) {
      type = type.accept(this);
      if (type == null || type != a.getType())
        return new Attribute(a.getLocation(), a.getAnnotation(), a.getName(), type);
    }
    return a;
  }

  public AttributeUse visitWildcardAttribute(WildcardAttribute a) {
    return a;
  }

  public AttributeUse visitAttributeGroupRef(AttributeGroupRef a) {
    return a;
  }

  public AttributeUse visitOptionalAttribute(OptionalAttribute a) {
    Attribute attribute = (Attribute)a.getAttribute().accept(this);
    if (attribute == a.getAttribute())
      return a;
    return new OptionalAttribute(a.getLocation(), a.getAnnotation(), attribute, a.getDefaultValue());
  }

  public AttributeUse visitAttributeGroup(AttributeGroup a) {
    List<AttributeUse> children = transformAttributeUseList(a.getChildren());
    if (children == a.getChildren())
      return a;
    return new AttributeGroup(a.getLocation(), a.getAnnotation(), children);
  }

  public AttributeUse visitAttributeUseChoice(AttributeUseChoice a) {
    List<AttributeUse> children = transformAttributeUseList(a.getChildren());
    if (children == a.getChildren())
      return a;
    return new AttributeUseChoice(a.getLocation(), a.getAnnotation(), children);
  }

  public SimpleType visitRestriction(SimpleTypeRestriction t) {
    return t;
  }

  public SimpleType visitUnion(SimpleTypeUnion t) {
    List<SimpleType> children = transformSimpleTypeList(t.getChildren());
    if (children == t.getChildren())
      return t;
    return new SimpleTypeUnion(t.getLocation(), t.getAnnotation(), children);
  }

  public SimpleType visitList(SimpleTypeList t) {
    SimpleType itemType = t.getItemType().accept(this);
    if (itemType == t.getItemType())
      return t;
    return new SimpleTypeList(t.getLocation(), t.getAnnotation(), itemType, t.getOccurs());
  }

  public SimpleType visitRef(SimpleTypeRef t) {
    return t;
  }

  public List<AttributeUse> transformAttributeUseList(List<AttributeUse> list) {
    List<AttributeUse> transformed = null;
    for (int i = 0, len = list.size(); i < len; i++) {
      AttributeUse use = list.get(i).accept(this);
      if (transformed != null)
        transformed.add(use);
      else if (use != list.get(i)) {
        transformed = new Vector<AttributeUse>();
        for (int j = 0; j < i; j++)
          transformed.add(list.get(j));
        if (!use.equals(AttributeGroup.EMPTY))
          transformed.add(use);
      }
    }
    if (transformed == null)
      return list;
    return transformed;
  }

  public List<Particle> transformParticleList(List<Particle> list) {
    List<Particle> transformed = null;
    for (int i = 0, len = list.size(); i < len; i++) {
      Particle p = list.get(i).accept(this);
      if (transformed != null) {
        if (p != null)
          transformed.add(p);
      }
      else if (p != list.get(i)) {
        transformed = new Vector<Particle>();
        for (int j = 0; j < i; j++)
          transformed.add(list.get(j));
        if (p != null)
          transformed.add(p);
      }
    }
    if (transformed == null)
      return list;
    return transformed;
  }

  public List<SimpleType> transformSimpleTypeList(List<SimpleType> list) {
    List<SimpleType> transformed = null;
    for (int i = 0, len = list.size(); i < len; i++) {
      SimpleType st = list.get(i).accept(this);
      if (transformed != null)
        transformed.add(st);
      else if (st != list.get(i)) {
        transformed = new Vector<SimpleType>();
        for (int j = 0; j < i; j++)
          transformed.add(list.get(j));
        transformed.add(st);
      }
    }
    if (transformed == null)
      return list;
    return transformed;
  }
}
