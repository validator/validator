package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.xsd.basic.SchemaWalker;
import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.xsd.basic.GroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleChoice;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleRepeat;
import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleGroup;
import com.thaiopensource.relaxng.output.xsd.basic.GroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRef;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleAll;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeComplexContent;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleSequence;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeUnion;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeList;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeSimpleContent;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroup;
import com.thaiopensource.relaxng.output.xsd.basic.SchemaTransformer;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.RootDeclaration;
import com.thaiopensource.relaxng.output.xsd.basic.WildcardElement;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexType;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeNotAllowedContent;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRestriction;
import com.thaiopensource.relaxng.output.common.Name;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

class ComplexTypeSelector extends SchemaWalker {
  static class Refs {
    Set referencingElements = new HashSet();
    Set referencingDefinitions = new HashSet();
    boolean nonTypeReference = false;
    boolean desirable = false;
    boolean someReferencingElementsNotMixed = false;
  }

  static class NamedComplexType {
    private final boolean complex;
    private final boolean elementOnly;

    NamedComplexType(boolean complex, boolean elementOnly) {
      this.complex = complex;
      this.elementOnly = elementOnly;
    }
  }

  private final Map groupMap = new HashMap();
  private final Map attributeGroupMap = new HashMap();
  private final Map simpleTypeMap = new HashMap();
  private String parentDefinition;
  private Element parentElement;
  private int nonTypeReference = 0;
  private int undesirable = 0;
  private boolean mixed = false;
  private Map complexTypeMap = new HashMap();
  private final Schema schema;
  private final Transformer transformer;
  private final ParticleVisitor baseFinder = new BaseFinder();
  private final Map abstractElementComplexTypeMap = new HashMap();
  private final ComplexType urType = new ComplexTypeNotAllowedContent();

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
    chooseComplexTypes(true, groupMap);
    chooseComplexTypes(false, simpleTypeMap);
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
    boolean oldMixed = mixed;
    mixed = t.isMixed();
    super.visitComplexContent(t);
    mixed = oldMixed;
    return null;
  }

  public Object visitSimpleContent(ComplexTypeSimpleContent t) {
    boolean oldMixed = mixed;
    mixed = false;
    super.visitSimpleContent(t);
    mixed = oldMixed;
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
    else if (parentElement != null) {
      refs.referencingElements.add(parentElement);
      if (!mixed)
        refs.someReferencingElementsNotMixed = true;
    }
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

  void chooseComplexTypes(boolean complex, Map definitionMap) {
    for (;;) {
      boolean foundOne = false;
      for (Iterator iter = definitionMap.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String name = (String)entry.getKey();
        if (createComplexType(name,
                              complex,
                              (Refs)entry.getValue(),
                              (Refs)attributeGroupMap.get(name)))
          foundOne = true;
      }
      if (!foundOne)
        break;
    }
  }

  private boolean createComplexType(String name, boolean complex, Refs childRefs, Refs attributeGroupRefs) {
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
    boolean elementOnly = childRefs.someReferencingElementsNotMixed;
    for (Iterator iter = childRefs.referencingDefinitions.iterator(); iter.hasNext();) {
      NamedComplexType ct = (NamedComplexType)complexTypeMap.get(iter.next());
      if (ct == null)
        return false;
      if (ct.elementOnly)
        elementOnly = true;
    }
    complexTypeMap.put(name, new NamedComplexType(complex, elementOnly));
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

  private String particleBase(Particle particle) {
    if (particle == null)
      return null;
    return (String)particle.accept(baseFinder);
  }

  ComplexTypeComplexContentExtension transformComplexContent(ComplexTypeComplexContent ct) {
    String base = particleBase(ct.getParticle());
    if (base != null) {
      NamedComplexType nct = (NamedComplexType)complexTypeMap.get(base);
      return new ComplexTypeComplexContentExtension(transformAttributeUses(ct.getAttributeUses()),
                                                    transformParticle(ct.getParticle()),
                                                    ct.isMixed() && nct.elementOnly,
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
                                                    !ct.elementOnly,
                                                    null);
    return transformComplexContent(new ComplexTypeComplexContent(att,
                                                                 def.getParticle(),
                                                                 !ct.elementOnly));
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

  boolean attributeGroupBelongsToComplexType(String name) {
    return complexTypeMap.get(name) != null;
  }

  ComplexType getAbstractElementComplexType(Name name, NamespaceManager nsm) {
    ComplexType ct = (ComplexType)abstractElementComplexTypeMap.get(name);
    if (ct == null) {
      ct = computeAbstractElementComplexType(name, nsm);
      if (ct == null)
        ct = urType;
      abstractElementComplexTypeMap.put(name, ct);
    }
    if (ct == urType)
      return null;
    return ct;
  }

  ComplexType computeAbstractElementComplexType(Name name, NamespaceManager nsm) {
    List members = nsm.getAbstractElementSubstitutionGroupMembers(name);
    if (members == null)
      return null;
    ComplexType commonType = null;
    for (Iterator iter = members.iterator(); iter.hasNext();) {
      ComplexType ct = getElementComplexType((Name)iter.next(), nsm);
      if (ct == null)
        return null;
      if (commonType == null)
        commonType = ct;
      else {
        commonType = commonBaseType(commonType, ct);
        if (commonType == null)
          return null;
      }
    }
    return commonType;
  }

  private ComplexType commonBaseType(ComplexType ct1, ComplexType ct2) {
    if (ct1.equals(ct2))
      return ct1;
    if (hasBaseType(ct1, ct2))
      return ct2;
    if (hasBaseType(ct2, ct1))
      return ct1;
    return null;
  }

  private boolean hasBaseType(ComplexType ct1, ComplexType ct2) {
    if (ct1 instanceof ComplexTypeComplexContent && ct2 instanceof ComplexTypeComplexContent)
      return hasBaseTypeComplexContent((ComplexTypeComplexContent)ct1, (ComplexTypeComplexContent)ct2);
    if (ct1 instanceof ComplexTypeSimpleContent && ct2 instanceof ComplexTypeSimpleContent)
      return hasBaseTypeSimpleContent((ComplexTypeSimpleContent)ct1, (ComplexTypeSimpleContent)ct2);
    return false;
  }

  private boolean hasBaseTypeComplexContent(ComplexTypeComplexContent ct1, ComplexTypeComplexContent ct2) {
    ComplexTypeComplexContentExtension ex = transformComplexContent(ct2);
    String base = ex.getBase();
    if (base == null || ex.getParticle() != null || !ex.getAttributeUses().equals(AttributeGroup.EMPTY) || ex.isMixed())
      return false;
    Particle particle = ct1.getParticle();
    for (;;) {
      String tem = particleBase(particle);
      if (base.equals(tem))
        return true;
      if (tem == null)
        break;
      if (complexTypeMap.get(tem) == null)
        break;
      particle = schema.getGroup(tem).getParticle();
    }
    return false;
  }

  private boolean hasBaseTypeSimpleContent(ComplexTypeSimpleContent ct1, ComplexTypeSimpleContent ct2) {
    ComplexTypeSimpleContentExtension ex = transformSimpleContent(ct2);
    if (!ex.getAttributeUses().equals(AttributeGroup.EMPTY))
      return false;
    String base = ex.getBase();
    String builtinBase = null;
    if (base == null) {
      if (ex.getSimpleType() instanceof SimpleTypeRestriction) {
        SimpleTypeRestriction restriction = (SimpleTypeRestriction)ex.getSimpleType();
        if (restriction.getFacets().size() > 0
                || restriction.getAnnotation() != null)
          return false;
        builtinBase = restriction.getName();
      }
      else if (ex.getSimpleType() instanceof SimpleTypeRef)
        base = ((SimpleTypeRef)ex.getSimpleType()).getName();
      else
        return false;
    }
    SimpleType st = ct1.getSimpleType();
    for (;;) {
      if (!(st instanceof SimpleTypeRef))
        break;
      String tem = ((SimpleTypeRef)st).getName();
      if (tem.equals(base))
        return true;
      if (complexTypeMap.get(tem) == null)
        return false;
      st = schema.getSimpleType(tem).getSimpleType();
    }
    if (!(st instanceof SimpleTypeRestriction))
      return false;
    String builtinType = ((SimpleTypeRestriction)st).getName();
    do {
      if (builtinType.equals(builtinBase))
        return true;
      builtinType = BuiltinSimpleTypeHierarchy.getParentType(builtinType);
    } while (builtinType != null);
    return false;
  }

  private ComplexType getElementComplexType(Name name, NamespaceManager nsm) {
    Element element = nsm.getGlobalElement(name);
    if (element != null)
      return element.getComplexType();
    return getAbstractElementComplexType(name, nsm);
  }
}
