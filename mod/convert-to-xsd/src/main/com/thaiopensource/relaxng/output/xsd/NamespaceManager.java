package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.Definition;
import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.xsd.basic.GroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.GroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.Include;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleChoice;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.xsd.basic.SchemaWalker;
import com.thaiopensource.relaxng.output.xsd.basic.Structure;
import com.thaiopensource.relaxng.output.xsd.basic.Wildcard;
import com.thaiopensource.relaxng.output.xsd.basic.WildcardAttribute;
import com.thaiopensource.relaxng.output.xsd.basic.WildcardElement;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class NamespaceManager {
  private final Schema schema;
  private final Map<Name, NameInfo> elementNameMap = new HashMap<Name, NameInfo>();
  private final Map<Name, NameInfo> attributeNameMap = new HashMap<Name, NameInfo>();
  private final Map<Object, Name> substitutionGroupMap = new HashMap<Object, Name>();
  private final Map<String, Name> groupDefinitionAbstractElementMap = new HashMap<String, Name>();
  private final Map<Name, List<Name>> abstractElementSubstitutionGroupMemberMap = new HashMap<Name, List<Name>>();

  static class SourceUri {
    String targetNamespace;
    // list of strings giving included URIs
    final List<String> includes = new Vector<String>();
  }

  static class TargetNamespace {
    String rootSchema;
    final List<Structure> movedStructures = new Vector<Structure>();
    final Set<Structure> movedStructureSet = new HashSet<Structure>();
    final Map<Structure, String> movedStructureNameMap = new HashMap<Structure, String>();
    final Set<String> movedElementNameSet = new HashSet<String>();
    final Set<String> movedAttributeNameSet = new HashSet<String>();
    boolean movedOtherElement = false;
    boolean movedOtherAttribute = false;
    String otherElementName;
    String otherAttributeName;
  }

  static class NameInfo {
    static final int OCCUR_NONE = 0;
    static final int OCCUR_NESTED = 1;
    static final int OCCUR_TOP = 2;
    static final int OCCUR_MOVE = 3;
    static final int OCCUR_ROOT = 4;
    int occur = OCCUR_NONE;
    Structure globalType = null;
  }

  // Maps sourceUri to SourceUri
  private final Map<String, SourceUri> sourceUriMap = new HashMap<String, SourceUri>();
  // Maps targetNamespace to TargetNamespace
  private final Map<String, TargetNamespace> targetNamespaceMap = new HashMap<String,TargetNamespace>();

  class IncludeFinder extends SchemaWalker {
    private final SourceUri source;
    IncludeFinder(Schema schema) {
      source = lookupSourceUri(schema.getUri());
      schema.accept(this);
    }

    public void visitInclude(Include include) {
      Schema included = include.getIncludedSchema();
      source.includes.add(included.getUri());
      new IncludeFinder(included);
    }
  }

  class RootMarker extends SchemaWalker {
    public void visitGroup(GroupDefinition def) {
    }

    public VoidValue visitElement(Element p) {
      NameInfo info = lookupElementName(p.getName());
      info.globalType = p;
      info.occur = NameInfo.OCCUR_ROOT;
      lookupTargetNamespace(p.getName().getNamespaceUri());
      return VoidValue.VOID;
    }
  }

  static class NamespaceUsage {
    int elementCount;
    int attributeCount;
    static boolean isBetter(NamespaceUsage n1, NamespaceUsage n2) {
      return (n1.elementCount > n2.elementCount
              || (n1.elementCount == n2.elementCount
                  && n1.attributeCount > n2.attributeCount));
    }
    public boolean equals(Object obj) {
      if (!(obj instanceof NamespaceUsage))
        return false;
      NamespaceUsage other = (NamespaceUsage)obj;
      return (elementCount == other.elementCount
              && attributeCount == other.attributeCount);
    }
  }

  class TargetNamespaceSelector extends SchemaWalker {
    private boolean nested;
    private final Map<String, NamespaceUsage> namespaceUsageMap = new HashMap<String, NamespaceUsage>();

    TargetNamespaceSelector(Schema schema) {
      schema.accept(this);
      lookupSourceUri(schema.getUri()).targetNamespace = selectTargetNamespace();
    }

    public VoidValue visitElement(Element element) {
      NamespaceUsage usage = getUsage(element.getName().getNamespaceUri());
      if (!nested)
        usage.elementCount++;
      boolean saveNested = nested;
      nested = true;
      element.getComplexType().accept(this);
      nested = saveNested;
      return VoidValue.VOID;
    }

    public VoidValue visitAttribute(Attribute a) {
      NamespaceUsage usage = getUsage(a.getName().getNamespaceUri());
      if (!nested)
        usage.attributeCount++;
      return VoidValue.VOID;
    }


    public VoidValue visitWildcardElement(WildcardElement p) {
      return visitWildcard(p.getWildcard());
    }

    public VoidValue visitWildcardAttribute(WildcardAttribute a) {
       return visitWildcard(a.getWildcard());
    }

    private VoidValue visitWildcard(Wildcard wc) {
      String ns = otherNamespace(wc);
      if (ns != null) {
        lookupTargetNamespace(ns);
        if (!nested)
          getUsage(ns).attributeCount++;
      }
      return VoidValue.VOID;
    }

    private NamespaceUsage getUsage(String ns) {
      NamespaceUsage usage = namespaceUsageMap.get(ns);
      if (usage == null) {
        usage = new NamespaceUsage();
        namespaceUsageMap.put(ns, usage);
        if (!ns.equals(""))
          lookupTargetNamespace(ns);
      }
      return usage;
    }

    public void visitInclude(Include include) {
      new TargetNamespaceSelector(include.getIncludedSchema());
    }

    String selectTargetNamespace() {
      Map.Entry<String,NamespaceUsage> best = null;
      for (Map.Entry<String, NamespaceUsage> tem : namespaceUsageMap.entrySet()) {
        if (best == null
            || NamespaceUsage.isBetter(tem.getValue(),
                                       best.getValue())
            // avoid output depending on order of hash table iteration
            || (tem.getValue().equals(best.getValue())
                && (tem.getKey()).compareTo(best.getKey()) < 0))
          best = tem;
      }
      namespaceUsageMap.clear();
      if (best == null)
        return null;
      String targetNamespace = best.getKey();
      // for "" case
      lookupTargetNamespace(targetNamespace);
      return targetNamespace;
    }
  }

  class GlobalElementSelector extends SchemaWalker {
    private final boolean absentTargetNamespace;
    private boolean nested = false;

    GlobalElementSelector(Schema schema) {
      absentTargetNamespace = getTargetNamespace(schema.getUri()).equals("");
      schema.accept(this);
    }

    public VoidValue visitElement(Element element) {
      Name name = element.getName();
      if (!name.getNamespaceUri().equals("") || absentTargetNamespace) {
        NameInfo info = lookupElementName(name);
        int occur = nested ? NameInfo.OCCUR_NESTED : NameInfo.OCCUR_TOP;
        if (occur > info.occur) {
          info.occur = occur;
          info.globalType = element;
        }
        else if (occur == info.occur && !element.equals(info.globalType))
          info.globalType = null;
      }
      boolean saveNested = nested;
      nested = true;
      element.getComplexType().accept(this);
      nested = saveNested;
      return VoidValue.VOID;
    }

    public void visitInclude(Include include) {
      new GlobalElementSelector(include.getIncludedSchema());
    }
  }

  class StructureMover extends SchemaWalker {
    private final String currentNamespace;

    StructureMover(String currentNamespace) {
      this.currentNamespace = currentNamespace;
    }

    public VoidValue visitElement(Element p) {
      NameInfo info = lookupElementName(p.getName());
      String ns = p.getName().getNamespaceUri();
      if (ns.equals(currentNamespace) || (ns.equals("") && !p.equals(info.globalType)))
        p.getComplexType().accept(this);
      else {
        noteMoved(info, p);
        moveStructure(p);
        p.getComplexType().accept(new StructureMover(ns));
      }
      return VoidValue.VOID;
    }

    public VoidValue visitAttribute(Attribute a) {
      String ns = a.getName().getNamespaceUri();
      if (!ns.equals("") && !ns.equals(currentNamespace)) {
        noteMoved(lookupAttributeName(a.getName()), a);
        moveStructure(a);
      }
      return VoidValue.VOID;
    }

    private void noteMoved(NameInfo info, Structure s) {
      if (info.occur < NameInfo.OCCUR_MOVE) {
        info.occur = NameInfo.OCCUR_MOVE;
        info.globalType = s;
      }
      else if (info.occur == NameInfo.OCCUR_MOVE && !s.equals(info.globalType))
        info.globalType = null;
    }

    private void moveStructure(Structure p) {
      TargetNamespace tn = lookupTargetNamespace(p.getName().getNamespaceUri());
      if (!tn.movedStructureSet.contains(p)) {
        tn.movedStructureSet.add(p);
        tn.movedStructures.add(p);
      }
    }

    public void visitInclude(Include include) {
      Schema included = include.getIncludedSchema();
      included.accept(new StructureMover(getTargetNamespace(included.getUri())));
    }

    public VoidValue visitWildcardElement(WildcardElement p) {
      return visitWildcard(p.getWildcard(), true);
    }

    public VoidValue visitWildcardAttribute(WildcardAttribute a) {
      return visitWildcard(a.getWildcard(), false);
    }

    private VoidValue visitWildcard(Wildcard wc, boolean isElement) {
      String ns = otherNamespace(wc);
      if (ns != null && !ns.equals(currentNamespace)) {
        TargetNamespace tn = lookupTargetNamespace(ns);
        if (isElement)
          tn.movedOtherElement = true;
        else
          tn.movedOtherAttribute = true;
      }
      return VoidValue.VOID;
    }
  }

  NamespaceManager(Schema schema, Guide guide, SourceUriGenerator sug) {
    this.schema = schema;
    new IncludeFinder(schema);
    schema.accept(new RootMarker());
    assignTargetNamespaces();
    new GlobalElementSelector(schema);
    findSubstitutionGroups(guide);
    chooseRootSchemas(sug);
    schema.accept(new StructureMover(getTargetNamespace(schema.getUri())));
  }

  private void assignTargetNamespaces() {
    new TargetNamespaceSelector(schema);
    // TODO maybe use info from <start> to select which targetNamespace of included schemas to use
    String ns = filterUpTargetNamespace(schema.getUri());
    if (ns == null) {
      lookupTargetNamespace("");
      lookupSourceUri(schema.getUri()).targetNamespace = "";
      ns = "";
    }
    inheritDownTargetNamespace(schema.getUri(), ns);
  }

  private String filterUpTargetNamespace(String sourceUri) {
    String ns = getTargetNamespace(sourceUri);
    if (ns != null)
      return ns;
    List<String> includes = lookupSourceUri(sourceUri).includes;
    if (includes.size() == 0)
      return null;
    Map<String, Integer> occurMap = new HashMap<String, Integer>();
    for (String include : includes) {
      String tem = filterUpTargetNamespace(include);
      if (tem != null) {
        Integer count = occurMap.get(tem);
        occurMap.put(tem, count == null ? 1 : count + 1);
      }
    }
    Map.Entry<String,Integer> best = null;
    boolean bestAmbig = false;
    for (Map.Entry<String, Integer> tem : occurMap.entrySet()) {
      if (best == null || tem.getValue() > best.getValue()) {
        best = tem;
        bestAmbig = false;
      }
      else if ((tem.getValue()).intValue() == (best.getValue()).intValue())
        bestAmbig = true;
    }
    if (best == null || bestAmbig)
      return null;
    ns = best.getKey();
    lookupSourceUri(sourceUri).targetNamespace = ns;
    return ns;
  }

  private void inheritDownTargetNamespace(String sourceUri, String targetNamespace) {
    for (String uri : lookupSourceUri(sourceUri).includes) {
      String ns = lookupSourceUri(uri).targetNamespace;
      if (ns == null) {
        ns = targetNamespace;
        lookupSourceUri(uri).targetNamespace = ns;
      }
      inheritDownTargetNamespace(uri, ns);
    }
  }

  private void chooseRootSchemas(SourceUriGenerator sug) {
    for (Map.Entry<String, TargetNamespace> entry : targetNamespaceMap.entrySet()) {
      String ns = entry.getKey();
      List<String> list = new Vector<String>();
      findRootSchemas(schema.getUri(), ns, list);
      if (list.size() == 1)
        (entry.getValue()).rootSchema = list.get(0);
      else {
        String sourceUri = sug.generateSourceUri(ns);
        lookupSourceUri(sourceUri).includes.addAll(list);
        lookupSourceUri(sourceUri).targetNamespace = ns;
        (entry.getValue()).rootSchema = sourceUri;
        schema.addInclude(sourceUri, schema.getEncoding(), null, null);
      }
    }
  }

  boolean isGlobal(Element element) {
    return element.equals(lookupElementName(element.getName()).globalType);
  }

  Element getGlobalElement(Name name) {
    NameInfo info = elementNameMap.get(name);
    if (info == null)
      return null;
    return (Element)info.globalType;
  }

  boolean isGlobal(Attribute attribute) {
    return attribute.equals(lookupAttributeName(attribute.getName()).globalType);
  }

  String getProxyName(Structure struct) {
    String ns = struct.getName().getNamespaceUri();
    TargetNamespace tn = lookupTargetNamespace(ns);
    String name = tn.movedStructureNameMap.get(struct);
    if (name == null) {
      name = generateName(ns, tn, struct.getName().getLocalName(), struct instanceof Element);
      tn.movedStructureNameMap.put(struct, name);
    }
    return name;
  }

  String getOtherElementName(String ns) {
    TargetNamespace tn = lookupTargetNamespace(ns);
    if (!tn.movedOtherElement)
      return null;
    if (tn.otherElementName == null)
      tn.otherElementName = generateName(ns, tn, "local", true);
    return tn.otherElementName;
  }

  String getOtherAttributeName(String ns) {
    TargetNamespace tn = lookupTargetNamespace(ns);
    if (!tn.movedOtherAttribute)
      return null;
    if (tn.otherAttributeName == null)
      tn.otherAttributeName = generateName(ns, tn, "local", false);
    return tn.otherAttributeName;
  }

  private String generateName(String ns, TargetNamespace tn, String base, boolean isElement) {
    Set<String> movedStructureNameSet = isElement ? tn.movedElementNameSet : tn.movedAttributeNameSet;
    String name = base;
    for (int n = 1;; n++) {
      if (!movedStructureNameSet.contains(name)) {
        Definition def;
        if (isElement)
          def = schema.getGroup(name);
        else
          def = schema.getAttributeGroup(name);
        if (def == null
            || !getTargetNamespace(def.getParentSchema().getUri()).equals(ns)
            || (def instanceof GroupDefinition
                && getElementNameForGroupRef((GroupDefinition)def) != null))
          break;
      }
      name = base + Integer.toString(n);
    }
    movedStructureNameSet.add(name);
    return name;
  }

  static class GroupDefinitionFinder extends SchemaWalker {
    final List<GroupDefinition> list = new Vector<GroupDefinition>();

    public void visitGroup(GroupDefinition def) {
      list.add(def);
    }

    static List<GroupDefinition> findGroupDefinitions(Schema schema) {
      GroupDefinitionFinder gdf = new GroupDefinitionFinder();
      schema.accept(gdf);
      return gdf.list;
    }
  }

  private void findSubstitutionGroups(Guide guide) {
    List<GroupDefinition> groups = GroupDefinitionFinder.findGroupDefinitions(schema);
    Map<Name, String> elementNameToGroupName = new HashMap<Name, String>();
    while (addAbstractElements(guide, groups, elementNameToGroupName))
      ;
    cleanSubstitutionGroupMap(elementNameToGroupName);
    cleanAbstractElementSubstitutionGroupMemberMap(elementNameToGroupName);
  }

  private boolean addAbstractElements(Guide guide, List<GroupDefinition> groups, Map<Name, String> elementNameToGroupName) {
    Set<Name> newAbstractElements = new HashSet<Name>();
    for (GroupDefinition def : groups) {
      if (guide.getGroupEnableAbstractElement(def.getName())
          && getGroupDefinitionAbstractElementName(def) == null) {
        Name elementName = abstractElementName(def);
        if (elementName != null) {
          List<Name> members = substitutionGroupMembers(def);
          if (members != null) {
            elementNameToGroupName.put(elementName, def.getName());
            addSubstitutionGroup(elementName, members, newAbstractElements);
          }
        }
      }
    }
    if (newAbstractElements.size() == 0)
      return false;
    for (Name name : newAbstractElements) {
      groupDefinitionAbstractElementMap.put(elementNameToGroupName.get(name), name);
    }
    return true;
  }

  private void addSubstitutionGroup(Name elementName, List<Name> members, Set<Name> newAbstractElements) {
    for (Name member : members) {
      Name old = getSubstitutionGroup(member);
      if (old != null && !old.equals(elementName)) {
        newAbstractElements.remove(old);
        return;
      }
      substitutionGroupMap.put(member, elementName);
    }
    newAbstractElements.add(elementName);
    abstractElementSubstitutionGroupMemberMap.put(elementName, members);
  }

  private void cleanSubstitutionGroupMap(Map<Name, String> elementNameToGroupName) {
    for (Iterator<Map.Entry<Object,Name>> iter = substitutionGroupMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry<Object,Name> entry = iter.next();
      Name head = entry.getValue();
      if (groupDefinitionAbstractElementMap.get(elementNameToGroupName.get(head)) == null)
        iter.remove();
    }
  }

  private void cleanAbstractElementSubstitutionGroupMemberMap(Map<Name, String> elementNameToGroupName) {
    for (Iterator<Name> iter = abstractElementSubstitutionGroupMemberMap.keySet().iterator(); iter.hasNext();) {
      if (groupDefinitionAbstractElementMap.get(elementNameToGroupName.get(iter.next())) == null)
        iter.remove();
    }
  }

  private Name abstractElementName(GroupDefinition def) {
    Name name = new Name(getTargetNamespace(def.getParentSchema().getUri()), def.getName());
    if (lookupElementName(name).globalType != null)
      return null;
    return name;
  }

  private List<Name> substitutionGroupMembers(GroupDefinition def) {
    if (def.getParticle() instanceof Element)
      return null;
    List<Name> members = new Vector<Name>();
    if (!particleMembers(def.getParticle(), members))
      return null;
    return members;
  }

  private boolean particleMembers(Particle child, List<Name> members) {
    if (child instanceof Element) {
      Element e = (Element)child;
      if (!isGlobal(e))
        return false;
      members.add(e.getName());
    }
    else if (child instanceof GroupRef) {
      Name name = getElementNameForGroupRef(schema.getGroup(((GroupRef)child).getName()));
      if (name == null)
        return false;
      members.add(name);
    }
    else if (child instanceof ParticleChoice) {
      for (Particle particle : ((ParticleChoice)child).getChildren()) {
        if (!particleMembers(particle, members))
          return false;
      }
    }
    else
      return false;
    return true;
  }

  Name getElementNameForGroupRef(GroupDefinition def) {
    Name abstractElementName = getGroupDefinitionAbstractElementName(def);
    if (abstractElementName != null)
      return abstractElementName;
    return getGroupDefinitionSingleElementName(def);
  }

  boolean isGroupDefinitionOmitted(GroupDefinition def) {
    return getGroupDefinitionSingleElementName(def) != null;
  }

  Name getGroupDefinitionAbstractElementName(GroupDefinition def) {
    return groupDefinitionAbstractElementMap.get(def.getName());
  }

  List<Name> getAbstractElementSubstitutionGroupMembers(Name name) {
    return abstractElementSubstitutionGroupMemberMap.get(name);
  }

  private Name getGroupDefinitionSingleElementName(GroupDefinition def) {
    Particle particle = def.getParticle();
    if (!(particle instanceof Element) || !isGlobal((Element)particle))
      return null;
    return ((Element)particle).getName();
  }

  Name getSubstitutionGroup(Name name) {
    return substitutionGroupMap.get(name);
  }

  String getTargetNamespace(String schemaUri) {
    return lookupSourceUri(schemaUri).targetNamespace;
  }

  boolean isTargetNamespace(String ns) {
    return targetNamespaceMap.get(ns) != null;
  }

  Set<String> getTargetNamespaces() {
    return targetNamespaceMap.keySet();
  }

  String getRootSchema(String targetNamespace) {
    return lookupTargetNamespace(targetNamespace).rootSchema;
  }

  List<Structure> getMovedStructures(String namespace) {
    return lookupTargetNamespace(namespace).movedStructures;
  }

  List<String> effectiveIncludes(String sourceUri) {
    String ns = getTargetNamespace(sourceUri);
    List<String> list = new Vector<String>();
    for (String uri : lookupSourceUri(sourceUri).includes)
      findRootSchemas(uri, ns, list);
    return list;
  }

  private void findRootSchemas(String sourceUri, String ns, List<String> list) {
    if (getTargetNamespace(sourceUri).equals(ns))
      list.add(sourceUri);
    else {
      for (String uri : lookupSourceUri(sourceUri).includes)
        findRootSchemas(uri, ns, list);
    }
  }

  private SourceUri lookupSourceUri(String uri) {
    SourceUri s = sourceUriMap.get(uri);
    if (s == null) {
      s = new SourceUri();
      sourceUriMap.put(uri, s);
    }
    return s;
  }

  private TargetNamespace lookupTargetNamespace(String ns) {
    TargetNamespace t = targetNamespaceMap.get(ns);
    if (t == null) {
      t = new TargetNamespace();
      targetNamespaceMap.put(ns, t);
    }
    return t;
  }

  private NameInfo lookupElementName(Name name) {
    NameInfo info = elementNameMap.get(name);
    if (info == null) {
      info = new NameInfo();
      elementNameMap.put(name, info);
    }
    return info;
  }

  private NameInfo lookupAttributeName(Name name) {
    NameInfo info = attributeNameMap.get(name);
    if (info == null) {
      info = new NameInfo();
      attributeNameMap.put(name, info);
    }
    return info;
  }

  static String otherNamespace(Wildcard wc) {
    if (wc.isPositive())
      return null;
    Set<String> namespaces = wc.getNamespaces();
    switch (namespaces.size()) {
    case 2:
      if (!namespaces.contains(""))
        return null;
      Iterator<String> iter = namespaces.iterator();
      String ns = iter.next();
      if (!ns.equals(""))
        return ns;
      return iter.next();
    case 1:
      if (namespaces.contains(""))
        return "";
    }
    return null;
  }

}
