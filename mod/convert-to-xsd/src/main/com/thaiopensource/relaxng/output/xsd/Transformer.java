package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.output.xsd.basic.AbstractAttributeUseVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroup;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUseChoice;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexType;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeComplexContent;
import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.xsd.basic.Facet;
import com.thaiopensource.relaxng.output.xsd.basic.GroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.Occurs;
import com.thaiopensource.relaxng.output.xsd.basic.OptionalAttribute;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleAll;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleChoice;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleRepeat;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleSequence;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.xsd.basic.SchemaTransformer;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRestriction;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeUnion;
import com.thaiopensource.relaxng.output.xsd.basic.SingleAttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.Wildcard;
import com.thaiopensource.relaxng.output.xsd.basic.WildcardAttribute;
import com.thaiopensource.relaxng.output.xsd.basic.WildcardElement;
import com.thaiopensource.util.Equal;
import com.thaiopensource.xml.util.Name;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

class Transformer extends SchemaTransformer {
  private final AttributeMapper attributeMapper = new AttributeMapper();
  private final Set<String> transformedAttributeGroups = new HashSet<String>();
  private final ErrorReporter er;
  private boolean preserveAllGroup = false;

  Transformer(Schema schema, ErrorReporter er) {
    super(schema);
    this.er = er;
  }

  public SimpleType visitUnion(SimpleTypeUnion t) {
    List<SimpleType> list = transformSimpleTypeList(t.getChildren());
    SimpleType combined = combineEnumeration(t, list);
    if (combined != null)
      return combined;
    return new SimpleTypeUnion(t.getLocation(), t.getAnnotation(), list);
  }

  private static SimpleType combineEnumeration(SimpleTypeUnion orig, List<SimpleType> transformedChildren) {
    if (transformedChildren.size() < 2)
      return null;
    SimpleType first = transformedChildren.get(0);
    if (!(first instanceof SimpleTypeRestriction))
      return null;
    String builtinTypeName = ((SimpleTypeRestriction)first).getName();
    List<Facet> facets = new Vector<Facet>();
    for (SimpleType child : transformedChildren) {
      if (!(child instanceof SimpleTypeRestriction))
        return null;
      SimpleTypeRestriction restriction = (SimpleTypeRestriction)child;
      if (!restriction.getName().equals(builtinTypeName))
        return null;
      if (restriction.getFacets().isEmpty())
        return null;
      for (Facet facet : restriction.getFacets()) {
        if (!facet.getName().equals("enumeration"))
          return null;
        facets.add(facet);
      }
    }
    return new SimpleTypeRestriction(orig.getLocation(), orig.getAnnotation(), builtinTypeName, facets);
  }

  class SequenceDetector implements ParticleVisitor<Boolean> {
    public Boolean visitElement(Element p) {
      return Boolean.FALSE;
    }

    public Boolean visitWildcardElement(WildcardElement p) {
      return Boolean.FALSE;
    }

    public Boolean visitSequence(ParticleSequence p) {
      return Boolean.TRUE;
    }

    public Boolean visitGroupRef(GroupRef p) {
      return getSchema().getGroup(p.getName()).getParticle().accept(this);
    }

    public Boolean visitAll(ParticleAll p) {
      return Boolean.FALSE;
    }

    public Boolean visitRepeat(ParticleRepeat p) {
      return p.getChild().accept(this);
    }

    public Boolean visitChoice(ParticleChoice p) {
      for (Particle child : p.getChildren())
        if (child.accept(this) == Boolean.TRUE)
          return Boolean.TRUE;
      return Boolean.FALSE;
    }
  }

  class AllBodyTransformer extends SchemaTransformer {
    public AllBodyTransformer(Schema schema) {
      super(schema);
    }

    public Particle visitGroupRef(GroupRef p) {
      if (new SequenceDetector().visitGroupRef(p) == Boolean.FALSE)
        return p;
      return getSchema().getGroup(p.getName()).getParticle().accept(this);
    }

    public Particle visitSequence(ParticleSequence p) {
      return new ParticleChoice(p.getLocation(), p.getAnnotation(), transformParticleList(p.getChildren()));
    }

    public Particle visitRepeat(ParticleRepeat p) {
      return p.getChild().accept(this);
    }

    public Particle visitElement(Element p) {
      return Transformer.this.visitElement(p);
    }
  }


  public Particle visitAll(ParticleAll p) {
    if (preserveAllGroup) {
      preserveAllGroup = false;
      return super.visitAll(p);
    }
    return new ParticleRepeat(p.getLocation(),
                              p.getAnnotation(),
                              new ParticleChoice(p.getLocation(),
                                                 null,
                                                 new AllBodyTransformer(getSchema()).transformParticleList(transformParticleList(p.getChildren()))),
                              Occurs.ZERO_OR_MORE);

  }

  public AttributeUse visitAttributeGroup(AttributeGroup a) {
    List<AttributeUse> children = transformAttributeUseList(a.getChildren());
    Wildcard wildcard = null;
    boolean[] removeWildcard = new boolean[children.size()];
    boolean multipleWildcards = false;
    int wildcardUseIndex = -1;
    for (int i = 0; i < removeWildcard.length; i++) {
      Wildcard wc = attributeMapper.getAttributeWildcard(children.get(i));
      if (wc != null) {
        if (wildcard == null) {
          wildcard = wc;
          wildcardUseIndex = i;
        }
        else {
          multipleWildcards = true;
          Wildcard union = Wildcard.union(wildcard, wc);
          if (union.equals(wildcard))
            removeWildcard[i] = true;
          else if (union.equals(wc)) {
            if (wildcardUseIndex >= 0)
              removeWildcard[wildcardUseIndex] = true;
            wildcardUseIndex = i;
            wildcard = wc;
          }
          else {
            removeWildcard[i] = true;
            if (wildcardUseIndex >= 0)
              removeWildcard[wildcardUseIndex] = true;
            wildcard = union;
            wildcardUseIndex = -1;
          }
        }
      }
    }
    if (!multipleWildcards) {
      if (children == a.getChildren())
        return a;
      return new AttributeGroup(a.getLocation(), a.getAnnotation(), children);
    }
    List<AttributeUse> newChildren = new Vector<AttributeUse>();
    for (int i = 0; i < removeWildcard.length; i++) {
      AttributeUse att = children.get(i);
      if (removeWildcard[i])
        att = att.accept(new AttributeTransformer(null, null, false));
      newChildren.add(att);
    }
    if (wildcardUseIndex == -1)
      newChildren.add(new WildcardAttribute(a.getLocation(), null, wildcard));
    return new AttributeGroup(a.getLocation(), a.getAnnotation(), newChildren);
  }

  public AttributeUse visitAttributeUseChoice(AttributeUseChoice a) {
    List<AttributeUse> children = transformAttributeUseList(a.getChildren());
    Map<Name, SingleAttributeUse>[] maps = (Map<Name, SingleAttributeUse>[])new Map[children.size()];
    int wildcardUseIndex = -1;
    Wildcard wildcard = null;
    for (int i = 0; i < maps.length; i++) {
      maps[i] = attributeMapper.getAttributeMap(children.get(i));
      Wildcard wc = attributeMapper.getAttributeWildcard(children.get(i));
      if (wc != null) {
        if (wildcard == null) {
          wildcard = wc;
          wildcardUseIndex = i;
        }
        else {
          Wildcard union = Wildcard.union(wildcard, wc);
          if (!union.equals(wildcard)) {
            if (union.equals(wc))
              wildcardUseIndex = i;
            else
              wildcardUseIndex = -1;
            wildcard = union;
          }
        }
      }
    }
    Set<Name> required = new HashSet<Name>();
    Set<Name> union = new HashSet<Name>(maps[0].keySet());
    for (int i = 1; i < maps.length; i++)
      union.addAll(maps[i].keySet());
    Set<Name>[] retainAttributeNames = (Set<Name>[])new Set[children.size()];
    for (int i = 0; i < retainAttributeNames.length; i++)
      retainAttributeNames[i] = new HashSet<Name>();
    List<AttributeUse> newChildren = new Vector<AttributeUse>();
    for (Name name : union) {
      if (wildcard == null || !wildcard.contains(name)) {
        SingleAttributeUse[] uses = new SingleAttributeUse[maps.length];
        int useIndex = -1;
        boolean isRequired = true;
        for (int i = 0; i < maps.length; i++) {
          uses[i] = maps[i].get(name);
          if (uses[i] != null) {
            if (useIndex >= 0)
              useIndex = -2;
            else if (useIndex == -1)
              useIndex = i;
            if (uses[i].isOptional())
              isRequired = false;
          }
          else
            isRequired = false;
        }
        if (isRequired)
          required.add(name);
        if (useIndex < 0)
          useIndex = chooseUseIndex(uses);
        if (useIndex >= 0)
          retainAttributeNames[useIndex].add(name);
        else {
          List<SimpleType> choices = new Vector<SimpleType>();
          for (int i = 0; i < uses.length; i++)
            if (uses[i] != null && uses[i].getType() != null)
              choices.add(uses[i].getType());
          Attribute tem = new Attribute(a.getLocation(),
                                        null,
                                        name,
                                        new SimpleTypeUnion(a.getLocation(), null, choices).accept(this));
          if (isRequired)
            newChildren.add(tem);
          else
            newChildren.add(new OptionalAttribute(a.getLocation(), null, tem, null));
        }
      }
    }
    for (int i = 0; i < retainAttributeNames.length; i++) {
      AttributeUse tem = children.get(i).accept(new AttributeTransformer(retainAttributeNames[i],
                                                                                       required,
                                                                                       i == wildcardUseIndex));
      if (!tem.equals(AttributeGroup.EMPTY))
        newChildren.add(tem);
    }
    if (wildcard != null && wildcardUseIndex == -1)
      newChildren.add(new WildcardAttribute(a.getLocation(), null, wildcard));
    return new AttributeGroup(a.getLocation(), a.getAnnotation(), newChildren);
  }

  private static int chooseUseIndex(SingleAttributeUse[] uses) {
    for (int i = 0; i < uses.length; i++)
      if (uses[i] != null && uses[i].getType() == null && uses[i].getDefaultValue() == null)
        return i;
    int firstIndex = -1;
    for (int i = 0; i < uses.length; i++) {
      if (uses[i] != null) {
        if (firstIndex == -1)
          firstIndex = i;
        else if (!Equal.equal(uses[i].getType(), uses[firstIndex].getType())
                 || !Equal.equal(uses[i].getDefaultValue(), uses[firstIndex].getDefaultValue()))
          return -1;
      }
    }
    return firstIndex;
  }

  static class AttributeInfo {
    final Map<Name, SingleAttributeUse> map;
    final Wildcard wildcard;
    final static Map<Name, SingleAttributeUse> EMPTY_MAP = Collections.emptyMap();

    AttributeInfo(Map<Name, SingleAttributeUse> map, Wildcard wildcard) {
      this.map = map;
      this.wildcard = wildcard;
    }
  }

  class AttributeMapper extends AbstractAttributeUseVisitor<AttributeInfo> {
    private final Map<AttributeUse, AttributeInfo> cache = new HashMap<AttributeUse, AttributeInfo>();

    Map<Name, SingleAttributeUse> getAttributeMap(AttributeUse a) {
      return getAttributeInfo(a).map;
    }

    Wildcard getAttributeWildcard(AttributeUse a) {
      return getAttributeInfo(a).wildcard;
    }

    private AttributeInfo getAttributeInfo(AttributeUse a) {
      AttributeInfo info = cache.get(a);
      if (info == null) {
        info = a.accept(this);
        cache.put(a, info);
      }
      return info;
    }

    public AttributeInfo visitAttribute(Attribute a) {
      Map<Name, SingleAttributeUse> map = new HashMap<Name, SingleAttributeUse>();
      map.put(a.getName(), a);
      return new AttributeInfo(map, null);
    }

    public AttributeInfo visitAttributeGroup(AttributeGroup a) {
      Map<Name, SingleAttributeUse> map = new HashMap<Name, SingleAttributeUse>();
      Wildcard wildcard = null;
      for (AttributeUse child : a.getChildren()) {
        AttributeInfo info = getAttributeInfo(child);
        if (info.wildcard != null)
          wildcard = info.wildcard;
        map.putAll(info.map);
      }
      return new AttributeInfo(map, wildcard);
    }

    public AttributeInfo visitOptionalAttribute(OptionalAttribute a) {
      Map<Name, SingleAttributeUse> map = new HashMap<Name, SingleAttributeUse>();
      map.put(a.getAttribute().getName(), a);
      return new AttributeInfo(map, null);
    }

    public AttributeInfo visitAttributeGroupRef(AttributeGroupRef a) {
      return getAttributeInfo(getTransformedAttributeGroup(a.getName()));
    }

    public AttributeInfo visitWildcardAttribute(WildcardAttribute a) {
      return new AttributeInfo(AttributeInfo.EMPTY_MAP, a.getWildcard());
    }
  }

  class AttributeTransformer extends AbstractAttributeUseVisitor<AttributeUse> {
    private final Set<Name> retainNames;
    private final Set<Name> requiredNames;
    private final boolean retainWildcard;

    public AttributeTransformer(Set<Name> retainNames, Set<Name> requiredNames, boolean retainWildcard) {
      this.retainNames = retainNames;
      this.requiredNames = requiredNames;
      this.retainWildcard = retainWildcard;
    }

    public AttributeUse visitAttribute(Attribute a) {
      if (retainNames != null && !retainNames.contains(a.getName()))
        return AttributeGroup.EMPTY;
      if (requiredNames != null && !requiredNames.contains(a.getName()))
        return new OptionalAttribute(a.getLocation(), null, a, null);
      return a;
    }

    public AttributeUse visitOptionalAttribute(OptionalAttribute a) {
      if (retainNames != null && !retainNames.contains(a.getName()))
        return AttributeGroup.EMPTY;
      return a;
    }

    public AttributeUse visitWildcardAttribute(WildcardAttribute a) {
      if (!retainWildcard)
        return AttributeGroup.EMPTY;
      return a;
    }

    public AttributeUse visitAttributeGroupRef(AttributeGroupRef a) {
      AttributeUse refed = getTransformedAttributeGroup(a.getName());
      if (isOk(attributeMapper.getAttributeMap(refed))
          && (retainWildcard || attributeMapper.getAttributeWildcard(refed) == null))
        return a;
      return refed.accept(this);
    }

    private boolean isOk(Map<Name, SingleAttributeUse> map) {
      for (Map.Entry<Name, SingleAttributeUse> entry : map.entrySet()) {
        Name name = entry.getKey();
        SingleAttributeUse use = entry.getValue();
        if (retainNames != null && !retainNames.contains(name))
          return false;
        if (requiredNames != null && !use.isOptional() && !requiredNames.contains(name))
          return false;
      }
      return true;
    }

    public AttributeUse visitAttributeGroup(AttributeGroup a) {
      List<AttributeUse> children = a.getChildren();
      List<AttributeUse> transformedChildren = null;
      for (int i = 0, len = children.size(); i < len; i++) {
        AttributeUse child = children.get(i).accept(this);
        if (transformedChildren != null) {
          if (!child.equals(AttributeGroup.EMPTY))
            transformedChildren.add(child);
        }
        else if (child != children.get(i)) {
          transformedChildren = new Vector<AttributeUse>();
          for (int j = 0; j < i; j++)
            transformedChildren.add(children.get(j));
          if (!child.equals(AttributeGroup.EMPTY))
            transformedChildren.add(child);
        }
      }
      if (transformedChildren == null)
        return a;
      return new AttributeGroup(a.getLocation(), a.getAnnotation(), transformedChildren);
    }
  }

  public void visitAttributeGroup(AttributeGroupDefinition def) {
    def.setAttributeUses(getTransformedAttributeGroup(def.getName()));
  }

  private AttributeUse getTransformedAttributeGroup(String name) {
    AttributeGroupDefinition def = getSchema().getAttributeGroup(name);
    if (!transformedAttributeGroups.contains(name)) {
      def.setAttributeUses(def.getAttributeUses().accept(this));
      transformedAttributeGroups.add(name);
    }
    return def.getAttributeUses();
  }

  public Particle visitElement(Element p) {
    if (containsLegalAllGroup(p))
      preserveAllGroup = true;
    return super.visitElement(p);
  }

  private static boolean containsLegalAllGroup(Element p) {
    ComplexType t = p.getComplexType();
    if (!(t instanceof ComplexTypeComplexContent))
      return false;
    Particle particle = ((ComplexTypeComplexContent)t).getParticle();
    if (!(particle instanceof ParticleAll))
      return false;
    String ns = p.getName().getNamespaceUri();
    for (Particle child : ((ParticleAll)particle).getChildren()) {
      if (child instanceof ParticleRepeat) {
        Occurs occur = ((ParticleRepeat)child).getOccurs();
        if (occur.getMin() > 1 || occur.getMax() > 1)
          return false;
        child = ((ParticleRepeat)child).getChild();
      }
      if (!(child instanceof Element))
        return false;
      if (!((Element)child).getName().getNamespaceUri().equals(ns))
        return false;
    }
    return true;
  }
}
