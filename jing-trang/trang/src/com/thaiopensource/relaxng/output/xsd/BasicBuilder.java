package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractPatternVisitor;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.AnnotationChild;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Comment;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementAnnotation;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Param;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.edit.TextAnnotation;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.output.common.NameClassSplitter;
import com.thaiopensource.relaxng.output.xsd.basic.Annotation;
import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroup;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUseChoice;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexType;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeComplexContent;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeNotAllowedContent;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeSimpleContent;
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
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeList;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRef;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRestriction;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeUnion;
import com.thaiopensource.relaxng.output.xsd.basic.Wildcard;
import com.thaiopensource.relaxng.output.xsd.basic.WildcardAttribute;
import com.thaiopensource.relaxng.output.xsd.basic.WildcardElement;
import com.thaiopensource.xml.util.Name;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class BasicBuilder {
  private final PatternVisitor<SimpleType> simpleTypeBuilder = new SimpleTypeBuilder();
  private final PatternVisitor<AttributeUse> attributeUseBuilder = new AttributeUseBuilder();
  private final PatternVisitor<AttributeUse> optionalAttributeUseBuilder = new OptionalAttributeUseBuilder();
  private final PatternVisitor<Particle> particleBuilder = new ParticleBuilder();
  private final PatternVisitor<Occurs> occursCalculator = new OccursCalculator();
  private final ComponentVisitor<VoidValue> schemaBuilder;
  private final ErrorReporter er;
  private final String inheritedNamespace;
  private final Schema schema;
  private final SchemaInfo si;
  private final Guide guide;

  /**
   * Preconditions for calling visit methods in this class are that the child type
   * - contains DATA
   * - does not contains ELEMENT
   * - does not contain TEXT
   */
  private class SimpleTypeBuilder extends AbstractPatternVisitor<SimpleType> {
    public SimpleType visitData(DataPattern p) {
      String library = p.getDatatypeLibrary();
      String type = p.getType();
      List<Facet> facets = new Vector<Facet>();
      SourceLocation location = p.getSourceLocation();
      if (!library.equals("") && !library.equals(WellKnownNamespaces.XML_SCHEMA_DATATYPES)) {
        type = "string";
        er.warning("unsupported_datatype_library", library, location);
      }
      else {
        if (type.equals("NOTATION"))
          type = "QName";
        for (Param param : p.getParams())
          facets.add(new Facet(param.getSourceLocation(),
                               makeAnnotation(param),
                               param.getName(),
                               param.getValue()));
      }
      return new SimpleTypeRestriction(location, makeAnnotation(p), type, facets);
    }

    public SimpleType visitValue(ValuePattern p) {
      String library = p.getDatatypeLibrary();
      String type = p.getType();
      List<Facet> facets = new Vector<Facet>();
      SourceLocation location = p.getSourceLocation();
      if (!library.equals("") && !library.equals(WellKnownNamespaces.XML_SCHEMA_DATATYPES)) {
        type = "string";
        er.warning("unsupported_datatype_library", library, location);
      }
      else {
        if (type.equals("NOTATION"))
          type = "QName";
        String prefix = null;
        String namespace = null;
        Iterator<Map.Entry<String,String>> bindings = p.getPrefixMap().entrySet().iterator();
        if (bindings.hasNext()) {
          Map.Entry<String,String> binding = bindings.next();
          prefix = binding.getKey();
          namespace = resolveNamespace(binding.getValue());
        }
        facets.add(new Facet(location, makeAnnotation(p), "enumeration", p.getValue(), prefix, namespace));
      }
      return new SimpleTypeRestriction(location, null, type, facets);
    }

    public SimpleType visitComposite(CompositePattern p) {
      List<SimpleType> result = new Vector<SimpleType>();
      for (Pattern child : p.getChildren()) {
        if (si.getChildType(child).contains(ChildType.DATA))
          result.add(child.accept(this));
      }
      if (result.size() == 1)
        return result.get(0);
      else
        return new SimpleTypeUnion(p.getSourceLocation(), makeAnnotation(p), result);
    }

    public SimpleType visitUnary(UnaryPattern p) {
      return p.getChild().accept(this);
    }

    public SimpleType visitList(ListPattern p) {
      SourceLocation location = p.getSourceLocation();
      Pattern child = p.getChild();
      ChildType childType = si.getChildType(child);
      if (childType.equals(ChildType.EMPTY))
        return makeEmptySimpleType(location);
      boolean bad = false;
      if (childType.contains(ChildType.ELEMENT)) {
        er.warning("list_contains_element", location);
        bad = true;
      }
      if (childType.contains(ChildType.ATTRIBUTE)) {
        er.warning("list_contains_attribute", location);
        bad = true;
      }
      if (childType.contains(ChildType.TEXT)) {
        er.warning("list_contains_text", location);
        bad = true;
      }
      if (bad)
        return makeStringType(location);
      // the type isn't NOT_ALLOWED, because the list would have type NOT_ALLOWED if it was
      // the type isn't EMPTY (checked above)
      // the type does not contain TEXT, ELEMENT or ATTRIBUTE (checked above)
      // therefore the type must contain DATA
      // so the preconditions for calling accept(this) are met
      return new SimpleTypeList(location,
                                makeAnnotation(p),
                                child.accept(this),
                                child.accept(occursCalculator));
    }

    public SimpleType visitRef(RefPattern p) {
      return new SimpleTypeRef(p.getSourceLocation(), makeAnnotation(p), p.getName());
    }

    public SimpleType visitPattern(Pattern p) {
      // TODO throw an error
      return null;
    }
  }

  class OccursCalculator extends AbstractPatternVisitor<Occurs> {
    public Occurs visitOptional(OptionalPattern p) {
      return new Occurs(0, p.getChild().accept(this).getMax());
    }

    public Occurs visitZeroOrMore(ZeroOrMorePattern p) {
      return new Occurs(0, Occurs.UNBOUNDED);
    }

    public Occurs visitOneOrMore(OneOrMorePattern p) {
      return new Occurs(p.getChild().accept(this).getMin(), Occurs.UNBOUNDED);
    }

    public Occurs visitData(DataPattern p) {
      return Occurs.EXACTLY_ONE;
    }

    public Occurs visitValue(ValuePattern p) {
      return Occurs.EXACTLY_ONE;
    }

    public Occurs visitEmpty(EmptyPattern p) {
      return new Occurs(0, 0);
    }

    private Occurs sum(CompositePattern p) {
      Occurs occ = new Occurs(0, 0);
      List<Pattern> children = p.getChildren();
      for (int i = 0, len = children.size(); i < len; i++)
        occ = Occurs.add(occ, children.get(i).accept(this));
      return occ;
    }

    public Occurs visitInterleave(InterleavePattern p) {
      return sum(p);
    }

    public Occurs visitGroup(GroupPattern p) {
      return sum(p);
    }

    public Occurs visitChoice(ChoicePattern p) {
      List<Pattern> children = p.getChildren();
      Occurs occ = children.get(0).accept(this);
      for (int i = 1, len = children.size(); i < len; i++) {
        Occurs tem = children.get(i).accept(this);
        occ = new Occurs(Math.min(occ.getMin(), tem.getMin()),
                         Math.max(occ.getMax(), tem.getMax()));
      }
      return occ;
    }

    public Occurs visitRef(RefPattern p) {
      return si.getBody(p).accept(this);
    }

    public Occurs visitPattern(Pattern p) {
      return null;
    }
  }

  /**
   * Precondition for calling visit methods in this class is that the child type
   * contains ELEMENT.
   */
  class ParticleBuilder extends AbstractPatternVisitor<Particle> {
    public Particle visitElement(ElementPattern p) {
      ComplexType type;
      Pattern child = p.getChild();
      ChildType ct = si.getChildType(child);
      AttributeUse attributeUses;
      if (ct.contains(ChildType.ATTRIBUTE))
        attributeUses = child.accept(attributeUseBuilder);
      else
        attributeUses = AttributeGroup.EMPTY;
      Particle particle = null;
      boolean mixed = false;
      if (ct.contains(ChildType.ELEMENT)) {
        if (ct.contains(ChildType.DATA))
          mixed = true;  // TODO give an error
        particle = child.accept(particleBuilder);
      }
      if (ct.contains(ChildType.TEXT))
        mixed = true;
      if (particle == null && mixed && attributeUses.equals(AttributeGroup.EMPTY))
        type = new ComplexTypeSimpleContent(attributeUses,
                                            makeStringType(p.getSourceLocation()));
      else if (ct.contains(ChildType.DATA) && !mixed && particle == null) {
        SimpleType simpleType = child.accept(simpleTypeBuilder);
        if (ct.contains(ChildType.EMPTY))
          simpleType = makeUnionWithEmptySimpleType(simpleType, p.getSourceLocation());
        type = new ComplexTypeSimpleContent(attributeUses, simpleType);
      }
      else if (ct.equals(ChildType.NOT_ALLOWED))
        type = new ComplexTypeNotAllowedContent();
      else
        type = new ComplexTypeComplexContent(attributeUses, particle, mixed);
      List<NameNameClass> names = NameClassSplitter.split(p.getNameClass());
      Wildcard[] wc = splitElementWildcard(WildcardBuilder.createWildcard(p.getNameClass(), inheritedNamespace));
      Annotation annotation = makeAnnotation(p);
      Annotation elementAnnotation = names.size() + wc.length == 1 ? annotation : null;
      List<Particle> result = new Vector<Particle>();
      for (NameNameClass name : names)
        result.add(new Element(p.getSourceLocation(), elementAnnotation, makeName(name), type));
      for (int i = 0; i < wc.length; i++)
        result.add(new WildcardElement(p.getSourceLocation(), elementAnnotation, wc[i]));
      if (result.size() == 1)
        return result.get(0);
      return new ParticleChoice(p.getSourceLocation(), annotation, result);
    }

    public Particle visitOneOrMore(OneOrMorePattern p) {
      return new ParticleRepeat(p.getSourceLocation(),
                                makeAnnotation(p),
                                p.getChild().accept(this),
                                Occurs.ONE_OR_MORE);
    }

    public Particle visitZeroOrMore(ZeroOrMorePattern p) {
      return new ParticleRepeat(p.getSourceLocation(),
                                makeAnnotation(p),
                                p.getChild().accept(this),
                                Occurs.ZERO_OR_MORE);

    }

    public Particle visitOptional(OptionalPattern p) {
      return new ParticleRepeat(p.getSourceLocation(),
                                makeAnnotation(p),
                                p.getChild().accept(this),
                                Occurs.OPTIONAL);
    }

    public Particle visitChoice(ChoicePattern p) {
      List<Particle> children = new Vector<Particle>();
      boolean optional = false;
      for (Pattern pattern : p.getChildren()) {
        ChildType ct = si.getChildType(pattern);
        if (ct.contains(ChildType.ELEMENT))
          children.add(pattern.accept(this));
        else if (!ct.equals(ChildType.NOT_ALLOWED))
          optional = true;
      }
      Annotation annotation = makeAnnotation(p);
      Particle result;
      if (children.size() == 1 && annotation == null)
        result = children.get(0);
      else
        result = new ParticleChoice(p.getSourceLocation(), annotation, children);
      if (optional)
        return new ParticleRepeat(p.getSourceLocation(), null, result, Occurs.OPTIONAL);
      return result;
    }

    public Particle visitGroup(GroupPattern p) {
      Annotation annotation = makeAnnotation(p);
      List<Particle> children = buildChildren(p);
      if (children.size() == 1 && annotation == null)
        return children.get(0);
      else
        return new ParticleSequence(p.getSourceLocation(), annotation, children);
    }

    public Particle visitInterleave(InterleavePattern p) {
      Annotation annotation = makeAnnotation(p);
      List<Particle> children = buildChildren(p);
      if (children.size() == 1 && annotation == null)
        return children.get(0);
      else
        return new ParticleAll(p.getSourceLocation(), annotation, children);
    }

    private List<Particle> buildChildren(CompositePattern p) {
      List<Particle> result = new Vector<Particle>();
      for (Pattern pattern : p.getChildren()) {
        if (si.getChildType(pattern).contains(ChildType.ELEMENT))
          result.add(pattern.accept(this));
      }
      return result;
    }

    public Particle visitMixed(MixedPattern p) {
      return p.getChild().accept(this);
    }

    public Particle visitRef(RefPattern p) {
      return new GroupRef(p.getSourceLocation(), makeAnnotation(p), p.getName());
    }

    public Particle visitPattern(Pattern p) {
      return null;
    }
  }


  /**
   * Precondition for visitMethods is that the childType contains ATTRIBUTE
   */
  class OptionalAttributeUseBuilder extends AbstractPatternVisitor<AttributeUse> {
    public AttributeUse visitAttribute(AttributePattern p) {
      SourceLocation location = p.getSourceLocation();
      Pattern child = p.getChild();
      ChildType ct = si.getChildType(child);
      SimpleType value;
      if (ct.contains(ChildType.DATA) && !ct.contains(ChildType.TEXT)) {
        value = child.accept(simpleTypeBuilder);
        if (ct.contains(ChildType.EMPTY))
          value = makeUnionWithEmptySimpleType(value, location);
      }
      else if (ct.contains(ChildType.EMPTY) && !ct.contains(ChildType.TEXT))
        value = makeEmptySimpleType(location);
      else
        value = null;
      List<NameNameClass> names = NameClassSplitter.split(p.getNameClass());
      Wildcard wc = WildcardBuilder.createWildcard(p.getNameClass(), inheritedNamespace);
      List<AttributeUse> choices = new Vector<AttributeUse>();
      Annotation annotation = makeAnnotation(p);
      boolean singleChoice = names.size() + (wc != null ? 1 : 0) == 1;
      Annotation attributeAnnotation = singleChoice ? annotation : null;
      for (NameNameClass name : names) {
        Attribute att = new Attribute(location,
                                      attributeAnnotation,
                                      makeName((name)),
                                      value);
        if (!singleChoice || isOptional())
          choices.add(new OptionalAttribute(att.getLocation(), null, att,
                                            p.getAttributeAnnotation(WellKnownNamespaces.RELAX_NG_COMPATIBILITY_ANNOTATIONS,
                                                                     "defaultValue")));
        else
          choices.add(att);
      }

      if (wc != null) {
        if (!allowsAnyString(child))
          er.warning("wildcard_attribute_value", p.getSourceLocation());
        if (!isOptional())
          er.warning("wildcard_attribute_optional", p.getSourceLocation());
        choices.add(new WildcardAttribute(p.getSourceLocation(), attributeAnnotation, wc));
      }
      if (choices.size() == 1)
        return choices.get(0);
      return new AttributeGroup(p.getSourceLocation(), annotation, choices);
    }

    boolean isOptional() {
      return true;
    }

    public AttributeUse visitOneOrMore(OneOrMorePattern p) {
      return p.getChild().accept(this);
    }

    public AttributeUse visitZeroOrMore(ZeroOrMorePattern p) {
      return p.getChild().accept(optionalAttributeUseBuilder);
    }

    public AttributeUse visitOptional(OptionalPattern p) {
      return p.getChild().accept(optionalAttributeUseBuilder);
    }

    public AttributeUse visitRef(RefPattern p) {
      AttributeUse ref = new AttributeGroupRef(p.getSourceLocation(), makeAnnotation(p), p.getName());
      if (!isOptional())
        return ref;
      List<AttributeUse> choices = new Vector<AttributeUse>();
      choices.add(ref);
      choices.add(AttributeGroup.EMPTY);
      return new AttributeUseChoice(p.getSourceLocation(), null, choices);
    }

    public AttributeUse visitComposite(CompositePattern p) {
      List<AttributeUse> uses = new Vector<AttributeUse>();
      for (Pattern child : p.getChildren()) {
        if (si.getChildType(child).contains(ChildType.ATTRIBUTE))
          uses.add(child.accept(this));
      }
      if (uses.size() == 0)
        return AttributeGroup.EMPTY;
      if (uses.size() == 1)
        return uses.get(0);
      if (isOptional())
        er.warning("optional_attribute_group", p.getSourceLocation());
      return new AttributeGroup(p.getSourceLocation(), null, uses);
    }

    public AttributeUse visitChoice(ChoicePattern p) {
      PatternVisitor<AttributeUse> childVisitor = this;
      for (Pattern child : p.getChildren()) {
        if (!si.getChildType(child).contains(ChildType.ATTRIBUTE)) {
          childVisitor = optionalAttributeUseBuilder;
          break;
        }
      }
      boolean hasChildren = false;
      List<AttributeUse> uses = new Vector<AttributeUse>();
      for (Pattern child : p.getChildren()) {
        ChildType ct = si.getChildType(child);
        if (ct.contains(ChildType.ATTRIBUTE)) {
          AttributeUse use = child.accept(childVisitor);
          if (uses.size() != 1 || !use.equals(uses.get(0)))
            uses.add(use);
        }
        if (ct.contains(ChildType.ELEMENT)
            || ct.contains(ChildType.DATA)
            || ct.contains(ChildType.TEXT))
          hasChildren = true;
      }
      if (hasChildren)
        er.warning("attribute_child_choice", p.getSourceLocation());
      if (uses.size() == 1)
        return uses.get(0);
      return new AttributeUseChoice(p.getSourceLocation(), null, uses);
    }

    public AttributeUse visitPattern(Pattern p) {
      return null;
    }
  }

  class AttributeUseBuilder extends OptionalAttributeUseBuilder {
    boolean isOptional() {
      return false;
    }
  }

  class SchemaBuilder implements ComponentVisitor<VoidValue> {
    boolean groupEnableAbstractElements;

    SchemaBuilder(boolean groupEnableAbstractElements) {
      this.groupEnableAbstractElements = groupEnableAbstractElements;
    }

    public VoidValue visitDefine(DefineComponent c) {
      addLeadingComments(c);
      String name = c.getName();
      SourceLocation location = c.getSourceLocation();
      Annotation annotation = makeAnnotation(c);
      if (name == DefineComponent.START) {
        if (!si.isIgnored(c)) {
          Pattern body = c.getBody();
          ChildType ct = si.getChildType(body);
          if (ct.contains(ChildType.ELEMENT))
            schema.addRoot(body.accept(particleBuilder),
                           location,
                           annotation);
        }
      }
      else {
        Pattern body = si.getBody(c);
        if (body != null) {
          ChildType ct = si.getChildType(body);
          if (ct.contains(ChildType.ELEMENT)) {
            guide.setGroupEnableAbstractElement(name,
                                                getGroupEnableAbstractElements(c, groupEnableAbstractElements));
            schema.defineGroup(name,
                               body.accept(particleBuilder),
                               location,
                               annotation);
          }
          else if (ct.contains(ChildType.DATA) && !ct.contains(ChildType.TEXT))
            schema.defineSimpleType(name,
                                    body.accept(simpleTypeBuilder),
                                    location,
                                    annotation);
          if (ct.contains(ChildType.ATTRIBUTE))
            schema.defineAttributeGroup(name,
                                        body.accept(attributeUseBuilder),
                                        location,
                                        annotation);
        }
      }
      addTrailingComments(c);
      return VoidValue.VOID;
    }

    public VoidValue visitDiv(DivComponent c) {
      addLeadingComments(c);
      addInitialChildComments(c);
      boolean saveGroupEnableAbstractElements = groupEnableAbstractElements;
      groupEnableAbstractElements = getGroupEnableAbstractElements(c, groupEnableAbstractElements);
      c.componentsAccept(this);
      groupEnableAbstractElements = saveGroupEnableAbstractElements;
      addTrailingComments(c);
      return VoidValue.VOID;
    }

    public VoidValue visitInclude(IncludeComponent c) {
      addLeadingComments(c);
      addInitialChildComments(c);
      boolean saveGroupEnableAbstractElements = groupEnableAbstractElements;
      groupEnableAbstractElements = getGroupEnableAbstractElements(c, groupEnableAbstractElements);
      c.componentsAccept(this);
      String uri = c.getHref();
      Schema sub = schema.addInclude(uri, si.getEncoding(uri), c.getSourceLocation(), makeAnnotation(c));
      GrammarPattern includedGrammar = si.getSchema(uri);
      new BasicBuilder(er,
                       si,
                       guide,
                       sub,
                       resolveNamespace(c.getNs()),
                       includedGrammar,
                       groupEnableAbstractElements).processGrammar(includedGrammar);
      groupEnableAbstractElements = saveGroupEnableAbstractElements;
      addTrailingComments(c);
      return VoidValue.VOID;
    }
  }

  private BasicBuilder(ErrorReporter er, SchemaInfo si, Guide guide, Schema schema, String inheritedNamespace, Annotated annotated, boolean groupEnableAbstractElements) {
    this.er = er;
    this.si = si;
    this.guide = guide;
    this.schema = schema;
    this.inheritedNamespace = inheritedNamespace;
    this.schemaBuilder = new SchemaBuilder(getGroupEnableAbstractElements(annotated, groupEnableAbstractElements));
  }

  static Schema buildBasicSchema(SchemaInfo si, Guide guide, ErrorReporter er) {
    GrammarPattern grammar = si.getGrammar();
    Schema schema = new Schema(grammar.getSourceLocation(), makeAnnotation(grammar), si.getMainUri(), si.getEncoding(si.getMainUri()));
    new BasicBuilder(er, si, guide, schema, "", grammar,
                     guide.getDefaultGroupEnableAbstractElements()).processGrammar(grammar);
    return schema;
  }

  private void processGrammar(GrammarPattern grammar) {
    copyComments(grammar.getLeadingComments(), schema.getLeadingComments());
    addInitialChildComments(grammar);
    grammar.componentsAccept(schemaBuilder);
    copyComments(grammar.getFollowingElementAnnotations(), schema.getTrailingComments());
  }

  private static SimpleType makeUnionWithEmptySimpleType(SimpleType type, SourceLocation location) {
    List<SimpleType> list = new Vector<SimpleType>();
    list.add(type);
    list.add(makeEmptySimpleType(location));
    return new SimpleTypeUnion(location, null, list);
  }

  private static SimpleType makeEmptySimpleType(SourceLocation location) {
    List<Facet> facets = new Vector<Facet>();
    facets.add(new Facet(location, null, "length", "0"));
    return new SimpleTypeRestriction(location, null, "token", facets);
  }

  private static SimpleType makeStringType(SourceLocation sourceLocation) {
    List<Facet> facets = Collections.emptyList();
    return new SimpleTypeRestriction(sourceLocation,
                                     null,
                                     "string",
                                     facets);
  }

  private Name makeName(NameNameClass nc) {
    return new Name(resolveNamespace(nc.getNamespaceUri()), nc.getLocalName());
  }

  private String resolveNamespace(String ns) {
    return resolveNamespace(ns, inheritedNamespace);
  }

  private static String resolveNamespace(String ns, String inheritedNamespace) {
    if (ns == NameNameClass.INHERIT_NS)
      return inheritedNamespace;
    return ns;
  }

  private static Wildcard[] splitElementWildcard(Wildcard wc) {
    if (wc == null)
      return new Wildcard[0];
    if (wc.isPositive() || wc.getNamespaces().contains("") || wc.getNamespaces().size() != 1)
      return new Wildcard[] { wc };
    Set<String> positiveNamespaces = new HashSet<String>();
    positiveNamespaces.add("");
    Set<String> negativeNamespaces = new HashSet<String>();
    negativeNamespaces.add(wc.getNamespaces().iterator().next());
    negativeNamespaces.add("");
    Set<Name> positiveExcludeNames = new HashSet<Name>();
    Set<Name> negativeExcludeNames = new HashSet<Name>();
    for (Name name : wc.getExcludedNames())
      (name.getNamespaceUri().equals("") ? positiveExcludeNames : negativeExcludeNames).add(name);
    return new Wildcard[] {
      new Wildcard(false, negativeNamespaces, negativeExcludeNames),
      new Wildcard(true, positiveNamespaces, positiveExcludeNames)
    };
  }

  private boolean allowsAnyString(Pattern p) {
    while (p instanceof RefPattern)
       p = si.getBody((RefPattern)p);
    if (p instanceof TextPattern)
      return true;
    if (!(p instanceof DataPattern))
      return false;
    DataPattern dp = (DataPattern)p;
    if (dp.getParams().size() != 0)
      return false;
    String lib = dp.getDatatypeLibrary();
    if (lib.equals(""))
      return true;
    if (!lib.equals(WellKnownNamespaces.XML_SCHEMA_DATATYPES))
      return false;
    String type = dp.getType();
    return type.equals("string") || type.equals("token") || type.equals("normalizedString");
  }

  private static Annotation makeAnnotation(Annotated annotated) {
    List<AnnotationChild> elements = (annotated.mayContainText()
                                      ? annotated.getFollowingElementAnnotations()
                                      : annotated.getChildElementAnnotations());
    for (AnnotationChild child : elements) {
      // child might be a Comment
      if (child instanceof ElementAnnotation) {
        ElementAnnotation element = (ElementAnnotation)child;
        if (element.getNamespaceUri().equals(WellKnownNamespaces.RELAX_NG_COMPATIBILITY_ANNOTATIONS)
            && element.getLocalName().equals("documentation")) {
          String value = getAtomicValue(element);
          if (value == null)
            break;
          return new Annotation(value);
        }
      }
    }
    return null;
  }

  private static String getAtomicValue(ElementAnnotation elem) {
    String value = null;
    StringBuffer buf = null;
    List<AnnotationChild> children = elem.getChildren();
    for (int i = 0, len = children.size(); i < len; i++) {
      Object obj = children.get(i);
      if (obj instanceof TextAnnotation) {
        String tem = ((TextAnnotation)obj).getValue();
        if (buf != null)
          buf.append(tem);
        else if (value == null)
          value = tem;
        else {
          buf = new StringBuffer(value);
          buf.append(tem);
          value = null;
        }
      }
      else if (obj instanceof ElementAnnotation)
        return null;
    }
    if (buf != null)
      return buf.toString();
    if (value != null)
      return value;
    return "";
  }

  static private final String GUIDE_NAMESPACE = "http://www.thaiopensource.com/ns/relaxng/xsd";

  private static boolean getGroupEnableAbstractElements(Annotated annotated, boolean current) {
    String value = annotated.getAttributeAnnotation(GUIDE_NAMESPACE, "enableAbstractElements");
    if (value != null) {
      value = value.trim();
      if (value.equals("true"))
        current = true;
      else if (value.equals("false"))
        current = false;
    }
    return current;
  }

  private void addLeadingComments(Annotated annotated) {
    addComments(annotated.getLeadingComments());
  }

  private void addInitialChildComments(Annotated annotated) {
    addComments(annotated.getChildElementAnnotations());
  }

  private void addTrailingComments(Annotated annotated) {
    addComments(annotated.getFollowingElementAnnotations());
  }

  private void addComments(List<? extends AnnotationChild> list) {
    for (AnnotationChild child : list) {
      if (child instanceof Comment) {
        Comment comment = (Comment)child;
        schema.addComment(comment.getValue(), comment.getSourceLocation());
      }
    }
  }

  private static void copyComments(List<? extends AnnotationChild> fromList,
                                   List<com.thaiopensource.relaxng.output.xsd.basic.Comment> toList) {
    for (AnnotationChild child : fromList) {
      if (child instanceof Comment) {
        Comment comment = (Comment)child;
        toList.add(new com.thaiopensource.relaxng.output.xsd.basic.Comment(comment.getSourceLocation(),
                                                                           comment.getValue()));
      }
    }
  }
}
