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
import com.thaiopensource.util.VoidValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class ComplexTypeSelector extends SchemaWalker {
  static class Refs {
    final Set<Element> referencingElements = new HashSet<Element>();
    final Set<String> referencingDefinitions = new HashSet<String>();
    boolean nonTypeReference = false;
    boolean desirable = false;
  }

  static class NamedComplexType {
    private final boolean mixed;

    NamedComplexType(boolean mixed) {
      this.mixed = mixed;
    }
  }

  private final Map<String, Refs> groupMap = new HashMap<String, Refs>();
  private final Map<String, Refs> attributeGroupMap = new HashMap<String, Refs>();
  private final Map<String, Refs> simpleTypeMap = new HashMap<String, Refs>();
  private String parentDefinition;
  private Element parentElement;
  private int nonTypeReference = 0;
  private int undesirable = 0;
  private final Map<String, NamedComplexType> complexTypeMap = new HashMap<String, NamedComplexType>();
  private final Schema schema;
  private final Transformer transformer;
  private final ParticleVisitor<String> baseFinder = new BaseFinder();

  class Transformer extends SchemaTransformer {
    Transformer(Schema schema) {
      super(schema);
    }

    public AttributeUse visitAttributeGroupRef(AttributeGroupRef a) {
      if (complexTypeMap.get(a.getName()) != null)
        return AttributeGroup.EMPTY;
      return a;
    }

    public Particle visitGroupRef(GroupRef p) {
      if (complexTypeMap.get(p.getName()) != null)
        return null;
      return p;
    }

    public Particle visitElement(Element p) {
      return p;
    }

    public AttributeUse visitAttribute(Attribute a) {
      return a;
    }
  }

  class BaseFinder implements ParticleVisitor<String> {
    public String visitGroupRef(GroupRef p) {
      if (complexTypeMap.get(p.getName()) != null)
        return p.getName();
      return null;
    }

    public String visitSequence(ParticleSequence p) {
      return p.getChildren().get(0).accept(this);
    }

    public String visitElement(Element p) {
      return null;
    }

    public String visitWildcardElement(WildcardElement p) {
      return null;
    }

    public String visitRepeat(ParticleRepeat p) {
      return null;
    }

    public String visitChoice(ParticleChoice p) {
      return null;
    }

    public String visitAll(ParticleAll p) {
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

  public VoidValue visitElement(Element p) {
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
    return VoidValue.VOID;
  }

  public VoidValue visitSequence(ParticleSequence p) {
    Iterator<Particle> iter = p.getChildren().iterator();
    undesirable++;
    (iter.next()).accept(this);
    undesirable--;
    nonTypeReference++;
    while (iter.hasNext())
      (iter.next()).accept(this);
    nonTypeReference--;
    return VoidValue.VOID;
  }

  public VoidValue visitChoice(ParticleChoice p) {
    nonTypeReference++;
    super.visitChoice(p);
    nonTypeReference--;
    return VoidValue.VOID;
  }

  public VoidValue visitAll(ParticleAll p) {
    nonTypeReference++;
    super.visitAll(p);
    nonTypeReference--;
    return VoidValue.VOID;
  }

  public VoidValue visitRepeat(ParticleRepeat p) {
    nonTypeReference++;
    super.visitRepeat(p);
    nonTypeReference--;
    return VoidValue.VOID;
  }

  public VoidValue visitAttribute(Attribute a) {
    nonTypeReference++;
    SimpleType t = a.getType();
    if (t != null)
      t.accept(this);
    nonTypeReference--;
    return VoidValue.VOID;
  }

  public VoidValue visitComplexContent(ComplexTypeComplexContent t) {
    super.visitComplexContent(t);
    return VoidValue.VOID;
  }

  public VoidValue visitSimpleContent(ComplexTypeSimpleContent t) {
    super.visitSimpleContent(t);
    return VoidValue.VOID;
  }

  public VoidValue visitUnion(SimpleTypeUnion t) {
    nonTypeReference++;
    super.visitUnion(t);
    nonTypeReference--;
    return VoidValue.VOID;
  }

  public VoidValue visitList(SimpleTypeList t) {
    nonTypeReference++;
    super.visitList(t);
    nonTypeReference--;
    return VoidValue.VOID;
  }

  public VoidValue visitGroupRef(GroupRef p) {
    noteRef(groupMap, p.getName());
    return VoidValue.VOID;
  }

  public VoidValue visitAttributeGroupRef(AttributeGroupRef a) {
    noteRef(attributeGroupMap, a.getName());
    return VoidValue.VOID;
  }

  public VoidValue visitRef(SimpleTypeRef t) {
    // Don't make it a complex type unless there are attributes
    undesirable++;
    noteRef(simpleTypeMap, t.getName());
    undesirable--;
    return VoidValue.VOID;
  }

  private void noteRef(Map<String, Refs> map, String name) {
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

  static private Refs lookupRefs(Map<String, Refs> map, String name) {
    Refs refs = map.get(name);
    if (refs == null) {
      refs = new Refs();
      map.put(name, refs);
    }
    return refs;
  }

  private void chooseComplexTypes(Map<String, Refs> definitionMap) {
    for (;;) {
      boolean foundOne = false;
      for (Map.Entry<String, Refs> entry : definitionMap.entrySet()) {
        String name = entry.getKey();
        if (createComplexType(name,
                              entry.getValue(),
                              attributeGroupMap.get(name)))
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
    for (Element elem : childRefs.referencingElements) {
      boolean m = elem.getComplexType().isMixed();
      if (m != mixed) {
        if (hadReference)
          return false;
        mixed = m;
      }
      hadReference = true;
    }
    for (String def : childRefs.referencingDefinitions) {
      NamedComplexType ct = complexTypeMap.get(def);
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
    return particle.accept(transformer);
  }

  private AttributeUse transformAttributeUses(AttributeUse atts) {
    return atts.accept(transformer);
  }

  String particleBase(Particle particle) {
    if (particle == null)
      return null;
    return particle.accept(baseFinder);
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
      NamedComplexType nct = complexTypeMap.get(name);
      if (nct != null)
        return new ComplexTypeSimpleContentExtension(transformAttributeUses(ct.getAttributeUses()), null, name);
    }
    return new ComplexTypeSimpleContentExtension(ct);
  }

  ComplexTypeComplexContentExtension createComplexTypeForGroup(String name, NamespaceManager nsm) {
    NamedComplexType ct = complexTypeMap.get(name);
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
    NamedComplexType ct = complexTypeMap.get(name);
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
