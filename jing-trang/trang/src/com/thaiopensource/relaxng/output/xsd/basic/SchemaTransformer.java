package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;
import java.util.Vector;

public class SchemaTransformer implements SchemaVisitor, ParticleVisitor, ComplexTypeVisitor, AttributeUseVisitor, SimpleTypeVisitor {
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
    def.setParticle((Particle)def.getParticle().accept(this));
  }

  public void visitAttributeGroup(AttributeGroupDefinition def) {
    def.setAttributeUses((AttributeUse)def.getAttributeUses().accept(this));
  }

  public void visitSimpleType(SimpleTypeDefinition def) {
    def.setSimpleType((SimpleType)def.getSimpleType().accept(this));
  }

  public void visitRoot(RootDeclaration decl) {
    decl.setParticle((Particle)decl.getParticle().accept(this));
  }

  public void visitInclude(Include include) {
    include.getIncludedSchema().accept(this);
  }

  public void visitComment(Comment comment) {
  }

  public Object visitRepeat(ParticleRepeat p) {
    Particle child = (Particle)p.getChild().accept(this);
    if (child == p.getChild())
      return p;
    return new ParticleRepeat(p.getLocation(), p.getAnnotation(), child, p.getOccurs());
  }

  public Object visitGroupRef(GroupRef p) {
    return p;
  }

  public Object visitElement(Element p) {
    ComplexType ct = (ComplexType)p.getComplexType().accept(this);
    if (ct == p.getComplexType())
      return p;
    return new Element(p.getLocation(), p.getAnnotation(), p.getName(), ct);
  }

  public Object visitWildcardElement(WildcardElement p) {
    return p;
  }

  public Object visitSequence(ParticleSequence p) {
    List children = transformParticleList(p.getChildren());
    if (children == p.getChildren())
      return p;
    if (children.size() == 1)
      return children.get(0);
    if (children.size() == 0)
      return null;
    return new ParticleSequence(p.getLocation(), p.getAnnotation(), children);
  }

  public Object visitChoice(ParticleChoice p) {
    List children = transformParticleList(p.getChildren());
    if (children == p.getChildren())
      return p;
    return new ParticleChoice(p.getLocation(), p.getAnnotation(), children);
  }

  public Object visitAll(ParticleAll p) {
    List children = transformParticleList(p.getChildren());
    if (children == p.getChildren())
      return p;
    return new ParticleAll(p.getLocation(), p.getAnnotation(), children);
  }

  public Object visitComplexContent(ComplexTypeComplexContent t) {
    Particle particle = t.getParticle();
    AttributeUse attributeUses = (AttributeUse)t.getAttributeUses().accept(this);
    if (particle != null)
      particle = (Particle)particle.accept(this);
    if (particle == t.getParticle() && attributeUses == t.getAttributeUses())
      return t;
    return new ComplexTypeComplexContent(attributeUses, particle, t.isMixed());
  }

  public Object visitSimpleContent(ComplexTypeSimpleContent t) {
    SimpleType simpleType = (SimpleType)t.getSimpleType().accept(this);
    AttributeUse attributeUses = (AttributeUse)t.getAttributeUses().accept(this);
    if (simpleType == t.getSimpleType() && attributeUses == t.getAttributeUses())
      return t;
    return new ComplexTypeSimpleContent(attributeUses, simpleType);
  }

  public Object visitNotAllowedContent(ComplexTypeNotAllowedContent t) {
    return t;
  }

  public Object visitAttribute(Attribute a) {
    SimpleType type = a.getType();
    if (type != null) {
      type = (SimpleType)type.accept(this);
      if (type == null || type != a.getType())
        return new Attribute(a.getLocation(), a.getAnnotation(), a.getName(), type);
    }
    return a;
  }

  public Object visitWildcardAttribute(WildcardAttribute a) {
    return a;
  }

  public Object visitAttributeGroupRef(AttributeGroupRef a) {
    return a;
  }

  public Object visitOptionalAttribute(OptionalAttribute a) {
    Attribute attribute = (Attribute)a.getAttribute().accept(this);
    if (attribute == a.getAttribute())
      return a;
    return new OptionalAttribute(a.getLocation(), a.getAnnotation(), attribute, a.getDefaultValue());
  }

  public Object visitAttributeGroup(AttributeGroup a) {
    List children = transformAttributeUseList(a.getChildren());
    if (children == a.getChildren())
      return a;
    return new AttributeGroup(a.getLocation(), a.getAnnotation(), children);
  }

  public Object visitAttributeUseChoice(AttributeUseChoice a) {
    List children = transformAttributeUseList(a.getChildren());
    if (children == a.getChildren())
      return a;
    return new AttributeUseChoice(a.getLocation(), a.getAnnotation(), children);
  }

  public Object visitRestriction(SimpleTypeRestriction t) {
    return t;
  }

  public Object visitUnion(SimpleTypeUnion t) {
    List children = transformSimpleTypeList(t.getChildren());
    if (children == t.getChildren())
      return t;
    return new SimpleTypeUnion(t.getLocation(), t.getAnnotation(), children);
  }

  public Object visitList(SimpleTypeList t) {
    SimpleType itemType = (SimpleType)t.getItemType().accept(this);
    if (itemType == t.getItemType())
      return t;
    return new SimpleTypeList(t.getLocation(), t.getAnnotation(), itemType, t.getOccurs());
  }

  public Object visitRef(SimpleTypeRef t) {
    return t;
  }

  public List transformAttributeUseList(List list) {
    List transformed = null;
    for (int i = 0, len = list.size(); i < len; i++) {
      Object obj = ((AttributeUse)list.get(i)).accept(this);
      if (transformed != null)
        transformed.add(obj);
      else if (obj != list.get(i)) {
        transformed = new Vector();
        for (int j = 0; j < i; j++)
          transformed.add(list.get(j));
        if (!obj.equals(AttributeGroup.EMPTY))
          transformed.add(obj);
      }
    }
    if (transformed == null)
      return list;
    return transformed;
  }

  public List transformParticleList(List list) {
    List transformed = null;
    for (int i = 0, len = list.size(); i < len; i++) {
      Object obj = ((Particle)list.get(i)).accept(this);
      if (transformed != null) {
        if (obj != null)
          transformed.add(obj);
      }
      else if (obj != list.get(i)) {
        transformed = new Vector();
        for (int j = 0; j < i; j++)
          transformed.add(list.get(j));
        if (obj != null)
          transformed.add(obj);
      }
    }
    if (transformed == null)
      return list;
    return transformed;
  }

  public List transformSimpleTypeList(List list) {
    List transformed = null;
    for (int i = 0, len = list.size(); i < len; i++) {
      Object obj = ((SimpleType)list.get(i)).accept(this);
      if (transformed != null)
        transformed.add(obj);
      else if (obj != list.get(i)) {
        transformed = new Vector();
        for (int j = 0; j < i; j++)
          transformed.add(list.get(j));
        transformed.add(obj);
      }
    }
    if (transformed == null)
      return list;
    return transformed;
  }
}
