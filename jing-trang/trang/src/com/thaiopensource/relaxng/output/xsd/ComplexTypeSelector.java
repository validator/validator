package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroup;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeComplexContent;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeSimpleContent;
import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.xsd.basic.GroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.GroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleAll;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleChoice;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleRepeat;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleSequence;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.RootDeclaration;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.xsd.basic.SchemaTransformer;
import com.thaiopensource.relaxng.output.xsd.basic.SchemaWalker;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeList;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRef;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeUnion;
import com.thaiopensource.relaxng.output.xsd.basic.WildcardElement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class ComplexTypeSelector extends SchemaWalker {
  static class Refs {
    final Set referencingElements = new HashSet();
    final Set referencingDefinitions = new HashSet();
    boolean nonTypeReference = false;
    boolean desirable = false;
  }

  static class NamedComplexType {
    private final boolean mixed;

    NamedComplexType(boolean mixed) {
      this.mixed = mixed;
    }
  }

  private final Map groupMap = new HashMap();
  private final Map attributeGroupMap = new HashMap();
  private final Map simpleTypeMap = new HashMap();
  private String parentDefinition;
  private Element parentElement;
  private int nonTypeReference = 0;
  private int undesirable = 0;
  private final Map complexTypeMap = new HashMap();
  private final Schema schema;
  private final Transformer transformer;
  private final ParticleVisitor baseFinder = new BaseFinder();

  class Transformer extends SchemaTransformer {
    Transformer(Schema schema) {
      super(schema);
    }

    public Object visitAttributeGroupRef(AttributeGroupRef a) {
      if (complexTypeMap.get(a.getName()) != null)
        return AttributeGroup.EMPTY;
      return a;
    }

    public Object visitGroupRef(GroupRef p) {
      if (complexTypeMap.get(p.getName()) != null)
        return null;
      return p;
    }

    public Object visitElement(Element p) {
      return p;
    }

    public Object visitAttribute(Attribute a) {
      return a;
    }
  }

  class BaseFinder implements ParticleVisitor {
    public Object visitGroupRef(GroupRef p) {
      if (complexTypeMap.get(p.getName()) != null)
        return p.getName();
      return null;
    }

    public Object visitSequence(ParticleSequence p) {
      return ((Particle)p.getChildren().get(0)).accept(this);
    }

    public Object visitElement(Element p) {
      return null;
    }

    public Object visitWildcardElement(WildcardElement p) {
      return null;
    }

    public Object visitRepeat(ParticleRepeat p) {
      return null;
    }

    public Object visitChoice(ParticleChoice p) {
      return null;
    }

    public Object visitAll(ParticleAll p) {
      return null;
    }
  }

  ComplexTypeSelector(Schema schema) {
    this.schema = schema;
    transformer = new Transformer(schema);
    schema.accept(this);
    chooseComplexTypes(groupMap);
    chooseComplexTypes(simpleTypeMap);
  }

  public void visitGroup(GroupDefinition def) {
    parentDefinition = def.getName();
    def.getParticle().accept(this);
    parentDefinition = null;
  }

  public void visitSimpleType(SimpleTypeDefinition def) {
    parentDefinition = def.getName();
    def.getSimpleType().accept(this);
    parentDefinition = null;
  }

  public void visitAttributeGroup(AttributeGroupDefinition def) {
    parentDefinition = def.getName();
    def.getAttributeUses().accept(this);
    parentDefinition = null;
  }

  public void visitRoot(RootDeclaration decl) {
    undesirable++;
    decl.getParticle().accept(this);
    undesirable--;
  }

  public Object visitElement(Element p) {
    Element oldParentElement = parentElement;
    int oldNonTypeReference = nonTypeReference;
    int oldExtensionReference = undesirable;
    parentElement = p;
    nonTypeReference = 0;
    undesirable = 0;
    p.getComplexType().accept(this);
    undesirable = oldExtensionReference;
    nonTypeReference = oldNonTypeReference;
    parentElement = oldParentElement;
    return null;
  }

  public Object visitSequence(ParticleSequence p) {
    Iterator iter = p.getChildren().iterator();
    undesirable++;
    ((Particle)iter.next()).accept(this);
    undesirable--;
    nonTypeReference++;
    while (iter.hasNext())
      ((Particle)iter.next()).accept(this);
    nonTypeReference--;
    return null;
  }

  public Object visitChoice(ParticleChoice p) {
    nonTypeReference++;
    super.visitChoice(p);
    nonTypeReference--;
    return null;
  }

  public Object visitAll(ParticleAll p) {
    nonTypeReference++;
    super.visitAll(p);
    nonTypeReference--;
    return null;
  }

  public Object visitRepeat(ParticleRepeat p) {
    nonTypeReference++;
    super.visitRepeat(p);
    nonTypeReference--;
    return null;
  }

  public Object visitAttribute(Attribute a) {
    nonTypeReference++;
    SimpleType t = a.getType();
    if (t != null)
      t.accept(this);
    nonTypeReference--;
    return null;
  }

  public Object visitComplexContent(ComplexTypeComplexContent t) {
    super.visitComplexContent(t);
    return null;
  }

  public Object visitSimpleContent(ComplexTypeSimpleContent t) {
    super.visitSimpleContent(t);
    return null;
  }

  public Object visitUnion(SimpleTypeUnion t) {
    nonTypeReference++;
    super.visitUnion(t);
    nonTypeReference--;
    return null;
  }

  public Object visitList(SimpleTypeList t) {
    nonTypeReference++;
    super.visitList(t);
    nonTypeReference--;
    return null;
  }

  public Object visitGroupRef(GroupRef p) {
    noteRef(groupMap, p.getName());
    return null;
  }

  public Object visitAttributeGroupRef(AttributeGroupRef a) {
    noteRef(attributeGroupMap, a.getName());
    return null;
  }

  public Object visitRef(SimpleTypeRef t) {
    // Don't make it a complex type unless there are attributes
    undesirable++;
    noteRef(simpleTypeMap, t.getName());
    undesirable--;
    return null;
  }

  private void noteRef(Map map, String name) {
    Refs refs = lookupRefs(map, name);
    if (nonTypeReference > 0)
      refs.nonTypeReference = true;
    else if (parentElement != null)
      refs.referencingElements.add(parentElement);
    else if (parentDefinition != null)
      refs.referencingDefinitions.add(parentDefinition);
    if (undesirable == 0)
      refs.desirable = true;
  }

  static private Refs lookupRefs(Map map, String name) {
    Refs refs = (Refs)map.get(name);
    if (refs == null) {
      refs = new Refs();
      map.put(name, refs);
    }
    return refs;
  }

  private void chooseComplexTypes(Map definitionMap) {
    for (;;) {
      boolean foundOne = false;
      for (Iterator iter = definitionMap.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String name = (String)entry.getKey();
        if (createComplexType(name,
                              (Refs)entry.getValue(),
                              (Refs)attributeGroupMap.get(name)))
          foundOne = true;
      }
      if (!foundOne)
        break;
    }
  }

  private boolean createComplexType(String name, Refs childRefs, Refs attributeGroupRefs) {
    if (complexTypeMap.get(name) != null)
      return false;
    if (childRefs.nonTypeReference)
      return false;
    if (attributeGroupRefs == null) {
      if (!childRefs.desirable)
        return false;
    }
    else if (!attributeGroupRefs.referencingDefinitions.equals(childRefs.referencingDefinitions)
             || !attributeGroupRefs.referencingElements.equals(childRefs.referencingElements))
      return false;
    boolean mixed = false;
    boolean hadReference = false;
    for (Iterator iter = childRefs.referencingElements.iterator(); iter.hasNext();) {
      boolean m = ((Element)iter.next()).getComplexType().isMixed();
      if (m != mixed) {
        if (hadReference)
          return false;
        mixed = m;
      }
      hadReference = true;
    }
    for (Iterator iter = childRefs.referencingDefinitions.iterator(); iter.hasNext();) {
      NamedComplexType ct = (NamedComplexType)complexTypeMap.get(iter.next());
      if (ct == null)
        return false;
      if (ct.mixed != mixed) {
        if (hadReference)
          return false;
        mixed = ct.mixed;
      }
      hadReference = true;
    }
    complexTypeMap.put(name, new NamedComplexType(mixed));
    return true;
  }


  private Particle transformParticle(Particle particle) {
    if (particle == null)
      return particle;
    return (Particle)particle.accept(transformer);
  }

  private AttributeUse transformAttributeUses(AttributeUse atts) {
    return (AttributeUse)atts.accept(transformer);
  }

  String particleBase(Particle particle) {
    if (particle == null)
      return null;
    return (String)particle.accept(baseFinder);
  }

  ComplexTypeComplexContentExtension transformComplexContent(ComplexTypeComplexContent ct) {
    String base = particleBase(ct.getParticle());
    if (base != null) {
      Particle particle = transformParticle(ct.getParticle());
      return new ComplexTypeComplexContentExtension(transformAttributeUses(ct.getAttributeUses()),
                                                    particle,
                                                    particle != null && ct.isMixed(),
                                                    base);
    }
    return new ComplexTypeComplexContentExtension(ct);
  }


  ComplexTypeSimpleContentExtension transformSimpleContent(ComplexTypeSimpleContent ct) {
    SimpleType st = ct.getSimpleType();
    if (st instanceof SimpleTypeRef) {
      String name = ((SimpleTypeRef)st).getName();
      NamedComplexType nct = (NamedComplexType)complexTypeMap.get(name);
      if (nct != null)
        return new ComplexTypeSimpleContentExtension(transformAttributeUses(ct.getAttributeUses()), null, name);
    }
    return new ComplexTypeSimpleContentExtension(ct);
  }

  ComplexTypeComplexContentExtension createComplexTypeForGroup(String name, NamespaceManager nsm) {
    NamedComplexType ct = (NamedComplexType)complexTypeMap.get(name);
    if (ct == null)
      return null;
    AttributeGroupDefinition attDef = schema.getAttributeGroup(name);
    AttributeUse att = attDef == null ? AttributeGroup.EMPTY : attDef.getAttributeUses();
    GroupDefinition def = schema.getGroup(name);
    if (nsm.getGroupDefinitionAbstractElementName(def) != null)
      return new ComplexTypeComplexContentExtension(att,
                                                    new GroupRef(def.getParticle().getLocation(), null, name),
                                                    ct.mixed,
                                                    null);
    return transformComplexContent(new ComplexTypeComplexContent(att,
                                                                 def.getParticle(),
                                                                 ct.mixed));
  }

  ComplexTypeSimpleContentExtension createComplexTypeForSimpleType(String name) {
    NamedComplexType ct = (NamedComplexType)complexTypeMap.get(name);
    if (ct == null)
      return null;
    AttributeGroupDefinition attDef = schema.getAttributeGroup(name);
    AttributeUse att = attDef == null ? AttributeGroup.EMPTY : attDef.getAttributeUses();
    return transformSimpleContent(new ComplexTypeSimpleContent(att,
                                                               schema.getSimpleType(name).getSimpleType()));
  }

  boolean isComplexType(String name) {
    return complexTypeMap.get(name) != null;
  }
}
