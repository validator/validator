package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.common.XmlWriter;
import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRestriction;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRef;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeUnion;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeList;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.Occurs;
import com.thaiopensource.relaxng.output.xsd.basic.Facet;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleRepeat;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleSequence;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleChoice;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleAll;
import com.thaiopensource.relaxng.output.xsd.basic.GroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUseVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeComplexContent;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeSimpleContent;
import com.thaiopensource.relaxng.output.xsd.basic.SchemaVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.AbstractSchemaVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.GroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.RootDeclaration;
import com.thaiopensource.relaxng.output.xsd.basic.StructureVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.Structure;
import com.thaiopensource.relaxng.output.xsd.basic.OptionalAttribute;
import com.thaiopensource.relaxng.output.xsd.basic.SchemaWalker;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroup;
import com.thaiopensource.relaxng.output.xsd.basic.AbstractAttributeUseVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.Wildcard;
import com.thaiopensource.relaxng.output.xsd.basic.WildcardAttribute;
import com.thaiopensource.relaxng.output.xsd.basic.WildcardElement;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeNotAllowedContent;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexType;
import com.thaiopensource.relaxng.output.xsd.basic.Annotated;
import com.thaiopensource.relaxng.output.xsd.basic.Annotation;
import com.thaiopensource.relaxng.output.xsd.basic.Comment;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Collections;
import java.io.IOException;

public class BasicOutput {
  static class Options {
    String anyProcessContents = "skip";
    String anyAttributeProcessContents = "skip";
  }
  private final XmlWriter xw;
  private final Schema schema;
  private final SimpleTypeOutput simpleTypeOutput = new SimpleTypeOutput();
  private final ComplexTypeOutput complexTypeOutput = new ComplexTypeOutput();
  private final AttributeUseOutput attributeUseOutput = new AttributeUseOutput();
  private final AttributeUseVisitor attributeWildcardOutput = new AttributeWildcardOutput();
  private final ParticleOutput particleOutput = new ParticleOutput();
  private final ParticleVisitor globalElementOutput = new GlobalElementOutput();
  private final GlobalAttributeOutput globalAttributeOutput = new GlobalAttributeOutput();
  private final SchemaVisitor schemaOutput = new SchemaOutput();
  private final StructureVisitor movedStructureOutput = new MovedStructureOutput();
  private final SimpleTypeVisitor simpleTypeNamer = new SimpleTypeNamer();
  private final NamespaceManager nsm;
  private final PrefixManager pm;
  private final String targetNamespace;
  private final OutputDirectory od;
  private final String sourceUri;
  private final ComplexTypeSelector complexTypeSelector;
  private final AbstractElementTypeSelector abstractElementTypeSelector;
  private final Set globalElementsDefined;
  private final Set globalAttributesDefined;
  private final String xsPrefix;
  private final Options options;

  class SimpleTypeOutput implements SimpleTypeVisitor {
    public Object visitRestriction(SimpleTypeRestriction t) {
      boolean hadPatternFacet = false;
      for (Iterator iter = t.getFacets().iterator(); iter.hasNext();) {
        if (((Facet)iter.next()).getName().equals("pattern")) {
          if (!hadPatternFacet)
            hadPatternFacet = true;
          else {
            xw.startElement(xs("restriction"));
            xw.startElement(xs("simpleType"));
          }
        }
      }
      xw.startElement(xs("restriction"));
      xw.attribute("base", xs(t.getName()));
      hadPatternFacet = false;
      for (Iterator iter = t.getFacets().iterator(); iter.hasNext();) {
        Facet facet = (Facet)iter.next();
        if (facet.getName().equals("pattern")) {
          if (!hadPatternFacet) {
            hadPatternFacet = true;
            outputFacet(facet);
          }
        }
        else
          outputFacet(facet);
      }
      xw.endElement();
      hadPatternFacet = false;
      for (Iterator iter = t.getFacets().iterator(); iter.hasNext();) {
        Facet facet = (Facet)iter.next();
        if (facet.getName().equals("pattern")) {
          if (!hadPatternFacet)
            hadPatternFacet = true;
          else {
            xw.endElement();
            outputFacet(facet);
            xw.endElement();
          }
        }
      }
      return null;
    }

    private void outputFacet(Facet facet) {
      xw.startElement(xs(facet.getName()));
      xw.attribute("value", facet.getValue());
      String prefix = facet.getPrefix();
      if (prefix != null && !prefix.equals(topLevelPrefix(facet.getNamespace())))
        xw.attribute(prefix.equals("") ? "xmlns" : "xmlns:" + prefix, facet.getNamespace());
      outputAnnotation(facet);
      xw.endElement();
    }


    public Object visitRef(SimpleTypeRef t) {
      xw.startElement(xs("restriction"));
      xw.attribute("base", qualifyRef(schema.getSimpleType(t.getName()).getParentSchema().getUri(),
                                      t.getName()));
      xw.endElement();
      return null;
    }

    public Object visitUnion(SimpleTypeUnion t) {
      xw.startElement(xs("union"));
      StringBuffer buf = new StringBuffer();
      for (Iterator iter = t.getChildren().iterator(); iter.hasNext();) {
        String typeName = (String)((SimpleType)iter.next()).accept(simpleTypeNamer);
        if (typeName != null) {
          if (buf.length() != 0)
            buf.append(' ');
          buf.append(typeName);
        }
      }
      if (buf.length() != 0)
        xw.attribute("memberTypes", buf.toString());
      outputAnnotation(t);
      for (Iterator iter = t.getChildren().iterator(); iter.hasNext();) {
        SimpleType simpleType = (SimpleType)iter.next();
        if (simpleType.accept(simpleTypeNamer) == null)
          outputWrap(simpleType, null);
      }
      xw.endElement();
      return null;
    }

    public Object visitList(SimpleTypeList t) {
      Occurs occ = t.getOccurs();
      if (!occ.equals(Occurs.ZERO_OR_MORE)) {
        xw.startElement(xs("restriction"));
        xw.startElement(xs("simpleType"));
      }
      xw.startElement(xs("list"));
      outputWrap(t.getItemType(), "itemType", t);
      xw.endElement();
      if (!occ.equals(Occurs.ZERO_OR_MORE)) {
        xw.endElement();
        if (occ.getMin() == occ.getMax()) {
          xw.startElement(xs("length"));
          xw.attribute("value", Integer.toString(occ.getMin()));
          xw.endElement();
        }
        else {
          if (occ.getMin() != 0) {
            xw.startElement(xs("minLength"));
            xw.attribute("value", Integer.toString(occ.getMin()));
            xw.endElement();
          }
          if (occ.getMax() != Occurs.UNBOUNDED) {
            xw.startElement(xs("maxLength"));
            xw.attribute("value", Integer.toString(occ.getMax()));
            xw.endElement();
          }
        }
        xw.endElement();
      }
      return null;
    }

    void outputWrap(SimpleType t, Annotated parent) {
      outputWrap(t, "type", parent);
    }

    void outputWrap(SimpleType t, String attributeName, Annotated parent) {
      String typeName = (String)t.accept(simpleTypeNamer);
      if (typeName != null) {
        xw.attribute(attributeName, typeName);
        if (parent != null)
          outputAnnotation(parent);
      }
      else {
        if (parent != null)
          outputAnnotation(parent);
        xw.startElement(xs("simpleType"));
        t.accept(this);
        xw.endElement();
      }
    }
  }

  class SimpleTypeNamer implements SimpleTypeVisitor {
    public Object visitRestriction(SimpleTypeRestriction t) {
      if (t.getFacets().size() > 0)
        return null;
      if (t.getAnnotation() != null)
        return null;
      return xs(t.getName());
    }

    public Object visitRef(SimpleTypeRef t) {
      if (t.getAnnotation() != null)
        return null;
      return qualifyRef(schema.getSimpleType(t.getName()).getParentSchema().getUri(),
                        t.getName());
    }

    public Object visitList(SimpleTypeList t) {
      return null;
    }

    public Object visitUnion(SimpleTypeUnion t) {
      return null;
    }
  }

  private static final int NORMAL_CONTEXT = 0;
  private static final int COMPLEX_TYPE_CONTEXT = 1;
  private static final int NAMED_GROUP_CONTEXT = 2;

  class ParticleOutput implements ParticleVisitor {
    private Occurs occ = Occurs.EXACTLY_ONE;
    private int context = NORMAL_CONTEXT;

    private boolean startWrapperForElement() {
      boolean needWrapper = context >= COMPLEX_TYPE_CONTEXT;
      context = NORMAL_CONTEXT;
      if (needWrapper)
        xw.startElement(xs("sequence"));
      xw.startElement(xs("element"));
      outputOccurAttributes();
      return needWrapper;
    }

    private boolean startWrapperForAny() {
      boolean needWrapper = context >= COMPLEX_TYPE_CONTEXT;
      context = NORMAL_CONTEXT;
      if (needWrapper)
        xw.startElement(xs("sequence"));
      xw.startElement(xs("any"));
      outputOccurAttributes();
      return needWrapper;
    }

    private boolean startWrapperForGroupRef() {
      boolean needWrapper = context == NAMED_GROUP_CONTEXT;
      context = NORMAL_CONTEXT;
      if (needWrapper)
        xw.startElement(xs("sequence"));
      xw.startElement(xs("group"));
      outputOccurAttributes();
      return needWrapper;
    }

    private boolean startWrapperForGroup(String groupType) {
      boolean needWrapper = context == NAMED_GROUP_CONTEXT && !occ.equals(Occurs.EXACTLY_ONE);
      context = NORMAL_CONTEXT;
      if (needWrapper)
        xw.startElement(xs("sequence"));
      xw.startElement(xs(groupType));
      outputOccurAttributes();
      return needWrapper;
    }

    private void endWrapper(boolean extra) {
      xw.endElement();
      if (extra)
        xw.endElement();
    }

    public Object visitElement(Element p) {
      boolean usedWrapper;
      if (nsm.isGlobal(p)) {
        usedWrapper = startWrapperForElement();
        xw.attribute("ref", qualifyName(p.getName()));
      }
      else if (!namespaceIsLocal(p.getName().getNamespaceUri())) {
        usedWrapper = startWrapperForGroupRef();
        xw.attribute("ref", qualifyName(p.getName().getNamespaceUri(),
                                        nsm.getProxyName(p)));
      }
      else {
        usedWrapper = startWrapperForElement();
        xw.attribute("name", p.getName().getLocalName());
        if (!p.getName().getNamespaceUri().equals(targetNamespace))
          xw.attribute("form", "unqualified");
        complexTypeOutput.parent = p;
        p.getComplexType().accept(complexTypeOutput);
      }
      endWrapper(usedWrapper);
      return null;
    }

    public Object visitWildcardElement(WildcardElement p) {
      String ns = NamespaceManager.otherNamespace(p.getWildcard());
      boolean usedWrapper;
      if (ns != null && !ns.equals(targetNamespace)) {
        usedWrapper = startWrapperForGroupRef();
        xw.attribute("ref", qualifyName(ns, nsm.getOtherElementName(ns)));
      }
      else {
        usedWrapper = startWrapperForAny();
        namespaceAttribute(p.getWildcard());
        xw.attribute("processContents", options.anyProcessContents);
        outputAnnotation(p);
      }
      endWrapper(usedWrapper);
      return null;
    }

    public Object visitRepeat(ParticleRepeat p) {
      occ = Occurs.multiply(occ, p.getOccurs());
      p.getChild().accept(this);
      return null;
    }

    public Object visitSequence(ParticleSequence p) {
      boolean usedWrapper = startWrapperForGroup("sequence");
      outputAnnotation(p);
      outputParticles(p.getChildren());
      endWrapper(usedWrapper);
      return null;
    }

    public Object visitChoice(ParticleChoice p) {
      boolean usedWrapper = startWrapperForGroup("choice");
      outputAnnotation(p);
      outputParticles(p.getChildren());
      endWrapper(usedWrapper);
      return null;
    }

    public Object visitAll(ParticleAll p) {
      boolean usedWrapper = startWrapperForGroup("all");
      outputAnnotation(p);
      outputParticles(p.getChildren());
      endWrapper(usedWrapper);
      return null;
    }

    private void outputParticles(List particles) {
      for (Iterator iter = particles.iterator(); iter.hasNext();)
        ((Particle)iter.next()).accept(this);
    }

    public Object visitGroupRef(GroupRef p) {
      String groupName = p.getName();
      GroupDefinition def = schema.getGroup(groupName);
      Name elementName = nsm.getElementNameForGroupRef(def);
      boolean usedWrapper;
      if (elementName != null) {
        usedWrapper = startWrapperForElement();
        xw.attribute("ref", qualifyName(elementName));
      }
      else {
        usedWrapper = startWrapperForGroupRef();
        xw.attribute("ref", qualifyRef(def.getParentSchema().getUri(), groupName));
      }
      outputAnnotation(p);
      endWrapper(usedWrapper);
      return null;
    }

    void outputOccurAttributes() {
      if (occ.getMin() != 1)
        xw.attribute("minOccurs", Integer.toString(occ.getMin()));
      if (occ.getMax() != 1)
        xw.attribute("maxOccurs",
                     occ.getMax() == Occurs.UNBOUNDED ? "unbounded" : Integer.toString(occ.getMax()));
      occ = Occurs.EXACTLY_ONE;
    }
  }

  class ComplexTypeOutput implements ComplexTypeVisitor {
    Annotated parent;

    public Object visitComplexContent(ComplexTypeComplexContent t) {
      outputComplexTypeComplexContent(complexTypeSelector.transformComplexContent(t), null, parent);
      return null;
    }

    public Object visitSimpleContent(ComplexTypeSimpleContent t) {
      outputComplexTypeSimpleContent(complexTypeSelector.transformSimpleContent(t), null, parent);
      return null;
    }

    public Object visitNotAllowedContent(ComplexTypeNotAllowedContent t) {
      xw.startElement(xs("complexType"));
      xw.startElement(xs("choice"));
      xw.endElement();
      xw.endElement();
      return null;
    }
  }

  class AttributeUseOutput extends SchemaWalker {
    boolean isOptional = false;
    String defaultValue = null;

    public Object visitOptionalAttribute(OptionalAttribute a) {
      isOptional = true;
      defaultValue = a.getDefaultValue();
      a.getAttribute().accept(this);
      isOptional = false;
      defaultValue = null;
      return null;
    }

    public Object visitAttribute(Attribute a) {
      if (nsm.isGlobal(a)) {
        xw.startElement(xs("attribute"));
        xw.attribute("ref", qualifyName(a.getName()));
        if (!isOptional)
          xw.attribute("use", "required");
        else if (defaultValue != null)
          xw.attribute("default", defaultValue);
        xw.endElement();
      }
      else if (namespaceIsLocal(a.getName().getNamespaceUri())) {
        xw.startElement(xs("attribute"));
        xw.attribute("name", a.getName().getLocalName());
        if (!isOptional)
          xw.attribute("use", "required");
        else if (defaultValue != null)
          xw.attribute("default", defaultValue);
        if (!a.getName().getNamespaceUri().equals(""))
          xw.attribute("form", "qualified");
        if (a.getType() != null)
          simpleTypeOutput.outputWrap(a.getType(), a);
        else
          outputAnnotation(a);
        xw.endElement();
      }
      else {
        xw.startElement(xs("attributeGroup"));
        xw.attribute("ref",
                     qualifyName(a.getName().getNamespaceUri(),
                                 nsm.getProxyName(a)));
        xw.endElement();
      }
      return null;
    }

    public Object visitAttributeGroupRef(AttributeGroupRef a) {
      xw.startElement(xs("attributeGroup"));
      String name = a.getName();
      xw.attribute("ref",
                   qualifyRef(schema.getAttributeGroup(name).getParentSchema().getUri(), name));
      xw.endElement();
      return null;
    }
  }

  class AttributeWildcardOutput extends SchemaWalker {
    public Object visitWildcardAttribute(WildcardAttribute a) {
      String ns = NamespaceManager.otherNamespace(a.getWildcard());
      if (ns != null && !ns.equals(targetNamespace)) {
        xw.startElement(xs("attributeGroup"));
        xw.attribute("ref", qualifyName(ns, nsm.getOtherAttributeName(ns)));
        xw.endElement();
      }
      else {
        xw.startElement(xs("anyAttribute"));
        namespaceAttribute(a.getWildcard());
        xw.attribute("processContents", options.anyAttributeProcessContents);
        xw.endElement();
      }
      return null;
    }
  }

  class GlobalElementOutput implements ParticleVisitor, ComplexTypeVisitor {
    public Object visitElement(Element p) {
      Name name = p.getName();
      if (nsm.isGlobal(p)
          && name.getNamespaceUri().equals(targetNamespace)
          && !globalElementsDefined.contains(name)) {
        globalElementsDefined.add(name);
        xw.startElement(xs("element"));
        xw.attribute("name", name.getLocalName());
        outputComplexType(name, p.getComplexType(), p);
        xw.endElement();
      }
      return p.getComplexType().accept(this);
    }

    public Object visitRepeat(ParticleRepeat p) {
      return p.getChild().accept(this);
    }

    void visitList(List list) {
      for (Iterator iter = list.iterator(); iter.hasNext();)
        ((Particle)iter.next()).accept(this);
    }

    public Object visitSequence(ParticleSequence p) {
      visitList(p.getChildren());
      return null;
    }

    public Object visitChoice(ParticleChoice p) {
      visitList(p.getChildren());
      return null;
    }

    public Object visitAll(ParticleAll p) {
      visitList(p.getChildren());
      return null;
    }

    public Object visitGroupRef(GroupRef p) {
      return null;
    }

    public Object visitWildcardElement(WildcardElement p) {
      return null;
    }

    public Object visitComplexContent(ComplexTypeComplexContent t) {
      if (t.getParticle() == null)
        return null;
      return t.getParticle().accept(this);
    }

    public Object visitSimpleContent(ComplexTypeSimpleContent t) {
      return null;
    }

    public Object visitNotAllowedContent(ComplexTypeNotAllowedContent t) {
      return null;
    }
  }

  class GlobalAttributeOutput extends AbstractAttributeUseVisitor {
    public Object visitAttributeGroup(AttributeGroup a) {
      for (Iterator iter = a.getChildren().iterator(); iter.hasNext();)
        ((AttributeUse)iter.next()).accept(this);
      return null;
    }

    public Object visitAttribute(Attribute a) {
      Name name = a.getName();
      if (nsm.isGlobal(a)
          && name.getNamespaceUri().equals(targetNamespace)
          && !globalAttributesDefined.contains(name)) {
        globalAttributesDefined.add(name);
        xw.startElement(xs("attribute"));
        xw.attribute("name", name.getLocalName());
        if (a.getType() != null)
          simpleTypeOutput.outputWrap(a.getType(), a);
        xw.endElement();
      }
      return null;
    }

    public Object visitOptionalAttribute(OptionalAttribute a) {
      return a.getAttribute().accept(this);
    }

    public Object visitAttributeGroupRef(AttributeGroupRef a) {
      return null;
    }

    public Object visitWildcardAttribute(WildcardAttribute a) {
      return null;
    }
  }

  class SchemaOutput extends AbstractSchemaVisitor {
    public void visitGroup(GroupDefinition def) {
      Particle particle = def.getParticle();
      ComplexTypeComplexContentExtension ct = complexTypeSelector.createComplexTypeForGroup(def.getName(), nsm);
      if (ct != null) {
        Annotated anno;
        if (tryAbstractElement(def))
          anno = null;
        else
          anno = def;
        outputComplexTypeComplexContent(ct, def.getName(), anno);
      }
      else if (!nsm.isGroupDefinitionOmitted(def)
               && !tryAbstractElement(def)
               && !tryElementChoiceSameType(def)) {
        xw.startElement(xs("group"));
        xw.attribute("name", def.getName());
        outputAnnotation(def);
        particleOutput.context = NAMED_GROUP_CONTEXT;
        particle.accept(particleOutput);
        xw.endElement();
      }
      particle.accept(globalElementOutput);
    }

    private boolean tryAbstractElement(GroupDefinition def) {
      Name name = nsm.getGroupDefinitionAbstractElementName(def);
      if (name == null)
        return false;
      xw.startElement(xs("element"));
      xw.attribute("name", name.getLocalName());
      xw.attribute("abstract", "true");
      outputComplexType(name, abstractElementTypeSelector.getAbstractElementType(name), def);
      xw.endElement();
      return true;
    }

    private boolean tryElementChoiceSameType(GroupDefinition def) {
      Particle particle = def.getParticle();
      if (!(particle instanceof ParticleChoice))
        return false;
      List children = ((ParticleChoice)particle).getChildren();
      if (children.size() <= 1)
        return false;
      Iterator iter = children.iterator();
      Particle first = (Particle)iter.next();
      if (!(first instanceof Element))
        return false;
      if (!((Element)first).getName().getNamespaceUri().equals(targetNamespace))
        return false;
      ComplexType type = ((Element)first).getComplexType();
      do {
        Particle tem = (Particle)iter.next();
        if (!(tem instanceof Element))
          return false;
        if (!((Element)tem).getComplexType().equals(type))
          return false;
        if (!((Element)tem).getName().getNamespaceUri().equals(targetNamespace))
          return false;
      } while (iter.hasNext());
      if (type instanceof ComplexTypeComplexContent) {
        ComplexTypeComplexContentExtension t = complexTypeSelector.transformComplexContent((ComplexTypeComplexContent)type);
        if (t.getBase() != null && t.getParticle() == null && !t.isMixed() && t.getAttributeUses().equals(AttributeGroup.EMPTY))
          return false;
        outputComplexTypeComplexContent(t, def.getName(), null);
      }
      else {
        ComplexTypeSimpleContentExtension t = complexTypeSelector.transformSimpleContent((ComplexTypeSimpleContent)type);
        if (t.getAttributeUses().equals(AttributeGroup.EMPTY)
                && (t.getBase() != null || t.getSimpleType().accept(simpleTypeNamer) != null))
          return false;
        outputComplexTypeSimpleContent(t, def.getName(), null);
      }
      xw.startElement(xs("group"));
      xw.attribute("name", def.getName());
      outputAnnotation(def);
      xw.startElement(xs("choice"));
      for (iter = children.iterator(); iter.hasNext();) {
        Element element = (Element)iter.next();
        xw.startElement(xs("element"));
        if (nsm.isGlobal(element))
          xw.attribute("ref", qualifyName(element.getName()));
        else {
          xw.attribute("name", element.getName().getLocalName());
          xw.attribute("type", def.getName());
          outputAnnotation(element);
        }
        xw.endElement();
      }
      xw.endElement();
      xw.endElement();
      for (iter = children.iterator(); iter.hasNext();) {
        Element element = (Element)iter.next();
        if (nsm.isGlobal(element) && !globalElementsDefined.contains(element.getName())) {
          globalElementsDefined.add(element.getName());
          xw.startElement(xs("element"));
          xw.attribute("name", element.getName().getLocalName());
          xw.attribute("type", def.getName());
          outputAnnotation(element);
          xw.endElement();
        }
      }
      return true;
    }

    public void visitSimpleType(SimpleTypeDefinition def) {
      ComplexTypeSimpleContentExtension ct = complexTypeSelector.createComplexTypeForSimpleType(def.getName());
      if (ct != null)
        outputComplexTypeSimpleContent(ct, def.getName(), def);
      else {
        xw.startElement(xs("simpleType"));
        xw.attribute("name", def.getName());
        outputAnnotation(def);
        def.getSimpleType().accept(simpleTypeOutput);
        xw.endElement();
      }
    }

    public void visitAttributeGroup(AttributeGroupDefinition def) {
      if (complexTypeSelector.isComplexType(def.getName()))
        return;
      xw.startElement(xs("attributeGroup"));
      xw.attribute("name", def.getName());
      outputAnnotation(def);
      outputAttributeUse(def.getAttributeUses());
      xw.endElement();
      def.getAttributeUses().accept(globalAttributeOutput);
    }

    public void visitRoot(RootDeclaration decl) {
      decl.getParticle().accept(globalElementOutput);
    }

    public void visitComment(Comment comment) {
      xw.comment(comment.getContent());
    }
  }

  class MovedStructureOutput implements StructureVisitor {
    public Object visitElement(Element element) {
      if (!nsm.isGlobal(element)) {
        xw.startElement(xs("group"));
        xw.attribute("name", nsm.getProxyName(element));
        particleOutput.context = NAMED_GROUP_CONTEXT;
        particleOutput.visitElement(element);
        xw.endElement();
      }
      globalElementOutput.visitElement(element);
      return null;
    }

    public Object visitAttribute(Attribute attribute) {
      if (!nsm.isGlobal(attribute)) {
        xw.startElement(xs("attributeGroup"));
        xw.attribute("name", nsm.getProxyName(attribute));
        attributeUseOutput.visitAttribute(attribute);
        xw.endElement();
      }
      globalAttributeOutput.visitAttribute(attribute);
      return null;
    }
  }

  static void output(Schema schema, Guide guide, PrefixManager pm, OutputDirectory od,
                     Options options, ErrorReporter er) throws IOException {
    NamespaceManager nsm = new NamespaceManager(schema, guide, pm);
    ComplexTypeSelector cts = new ComplexTypeSelector(schema);
    AbstractElementTypeSelector aets = new AbstractElementTypeSelector(schema, nsm, cts);
    Set globalElementsDefined = new HashSet();
    Set globalAttributesDefined = new HashSet();
    try {
      for (Iterator iter = schema.getSubSchemas().iterator(); iter.hasNext();)
        new BasicOutput((Schema)iter.next(), er, od, options, nsm, pm, cts, aets,
                        globalElementsDefined, globalAttributesDefined).output();
    }
    catch (XmlWriter.WrappedException e) {
      throw e.getIOException();
    }
  }

  private BasicOutput(Schema schema, ErrorReporter er, OutputDirectory od, Options options,
                     NamespaceManager nsm, PrefixManager pm, ComplexTypeSelector complexTypeSelector,
                     AbstractElementTypeSelector abstractElementTypeSelector,
                     Set globalElementsDefined, Set globalAttributesDefined) throws IOException {
    this.schema = schema;
    this.nsm = nsm;
    this.pm = pm;
    this.complexTypeSelector = complexTypeSelector;
    this.abstractElementTypeSelector = abstractElementTypeSelector;
    this.globalElementsDefined = globalElementsDefined;
    this.globalAttributesDefined = globalAttributesDefined;
    this.sourceUri = schema.getUri();
    this.od = od;
    this.targetNamespace = nsm.getTargetNamespace(schema.getUri());
    this.xsPrefix = pm.getPrefix(WellKnownNamespaces.XML_SCHEMA);
    this.options = options;
    OutputDirectory.Stream stream = od.open(schema.getUri(), schema.getEncoding());
    xw = new XmlWriter(stream.getWriter(),
                       stream.getEncoding(),
                       stream.getCharRepertoire(),
                       od.getLineSeparator(),
                       od.getIndent(),
                       new String[0]);
  }

  private String topLevelPrefix(String ns) {
    if (!nsm.isTargetNamespace(ns))
      return null;
    if (ns.equals(""))
      return "";
    return pm.getPrefix(ns);
  }

  private void output() {
    outputCommentList(schema.getLeadingComments());
    xw.startElement(xs("schema"));
    xw.attribute("xmlns:" + xsPrefix, WellKnownNamespaces.XML_SCHEMA);
    xw.attribute("elementFormDefault", "qualified");
    if (!targetNamespace.equals(""))
      xw.attribute("targetNamespace", targetNamespace);
    for (Iterator iter = nsm.getTargetNamespaces().iterator(); iter.hasNext();) {
      String ns = (String)iter.next();
      if (!ns.equals("")) {
        String prefix = pm.getPrefix(ns);
        if (!prefix.equals("xml"))
          xw.attribute("xmlns:" + pm.getPrefix(ns), ns);
      }
    }
    for (Iterator iter = nsm.effectiveIncludes(schema.getUri()).iterator(); iter.hasNext();)
      outputInclude((String)iter.next());
    List targetNamespaces = new Vector();
    targetNamespaces.addAll(nsm.getTargetNamespaces());
    Collections.sort(targetNamespaces);
    for (Iterator iter = targetNamespaces.iterator(); iter.hasNext();) {
      String ns = (String)iter.next();
      if (!ns.equals(targetNamespace))
        outputImport(ns, nsm.getRootSchema(ns));
    }
    schema.accept(schemaOutput);
    if (nsm.getRootSchema(targetNamespace).equals(sourceUri)) {
      for (Iterator iter = nsm.getMovedStructures(targetNamespace).iterator(); iter.hasNext();)
        ((Structure)iter.next()).accept(movedStructureOutput);
      outputOther();
    }
    xw.endElement();
    outputCommentList(schema.getTrailingComments());
    xw.close();
  }

  private String xs(String name) {
    return xsPrefix + ":" + name;
  }

  private boolean namespaceIsLocal(String ns) {
    return ns.equals(targetNamespace) || ns.equals("");
  }

  private void outputAttributeUse(AttributeUse use) {
    use.accept(attributeUseOutput);
    use.accept(attributeWildcardOutput);
  }

  private void namespaceAttribute(Wildcard wc) {
    if (wc.isPositive()) {
      StringBuffer buf = new StringBuffer();
      List namespaces = new Vector(wc.getNamespaces());
      Collections.sort(namespaces);
      for (Iterator iter = namespaces.iterator(); iter.hasNext();) {
        if (buf.length() > 0)
          buf.append(' ');
        String ns = (String)iter.next();
        if (ns.equals(""))
          buf.append("##local");
        else if (ns.equals(targetNamespace))
          buf.append("##targetNamespace");
        else
          buf.append(ns);
      }
      xw.attribute("namespace", buf.toString());
    }
    else {
      if (targetNamespace.equals(NamespaceManager.otherNamespace(wc)))
        xw.attribute("namespace", "##other");
    }
  }

  private String qualifyRef(String schemaUri, String localName) {
    return qualifyName(nsm.getTargetNamespace(schemaUri), localName);
  }

  private String qualifyName(Name name) {
    return qualifyName(name.getNamespaceUri(), name.getLocalName());
  }

  private String qualifyName(String ns, String localName) {
    if (ns.equals(""))
      return localName;
    return pm.getPrefix(ns) + ":" + localName;
  }

  private void outputOther() {
    String name = nsm.getOtherElementName(targetNamespace);
    if (name != null) {
      xw.startElement(xs("group"));
      xw.attribute("name", name);
      xw.startElement(xs("sequence"));
      xw.startElement(xs("any"));
      xw.attribute("namespace", "##other");
      xw.attribute("processContents", options.anyProcessContents);
      xw.endElement();
      xw.endElement();
      xw.endElement();
    }
    name = nsm.getOtherAttributeName(targetNamespace);
    if (name != null) {
      xw.startElement(xs("attributeGroup"));
      xw.attribute("name", name);
      xw.startElement(xs("anyAttribute"));
      xw.attribute("namespace", "##other");
      xw.attribute("processContents", options.anyAttributeProcessContents);
      xw.endElement();
      xw.endElement();
    }
  }

  private void outputInclude(String href) {
    xw.startElement(xs("include"));
    xw.attribute("schemaLocation", od.reference(sourceUri, href));
    xw.endElement();
  }

  private void outputImport(String ns, String href) {
    xw.startElement(xs("import"));
    if (!ns.equals(""))
      xw.attribute("namespace", ns);
    xw.attribute("schemaLocation", od.reference(sourceUri, href));
    xw.endElement();
  }

  private void outputComplexTypeComplexContent(ComplexTypeComplexContentExtension t, String name, Annotated parent) {
    String base = t.getBase();
    if (base != null) {
      base = qualifyRef(schema.getGroup(base).getParentSchema().getUri(), base);
      if (name == null
          && t.getParticle() == null
          && !t.isMixed()
          && t.getAttributeUses().equals(AttributeGroup.EMPTY)) {
        xw.attribute("type", base);
        if (parent != null)
          outputAnnotation(parent);
        return;
      }
    }
    if (name == null && parent != null)
      outputAnnotation(parent);
    xw.startElement(xs("complexType"));
    if (name != null)
      xw.attribute("name", name);
    if (t.isMixed())
      xw.attribute("mixed", "true");
    if (name != null && parent != null)
      outputAnnotation(parent);
    if (base != null) {
      xw.startElement(xs("complexContent"));
      xw.startElement(xs("extension"));
      xw.attribute("base", base);
    }
    if (t.getParticle() != null) {
      particleOutput.context = COMPLEX_TYPE_CONTEXT;
      t.getParticle().accept(particleOutput);
    }
    outputAttributeUse(t.getAttributeUses());
    if (base != null) {
      xw.endElement();
      xw.endElement();
    }
    xw.endElement();
  }

  private void outputComplexTypeSimpleContent(ComplexTypeSimpleContentExtension t, String name, Annotated parent) {
    String base = t.getBase();
    AttributeUse attributeUses = t.getAttributeUses();
    if (base != null) {
      base = qualifyRef(schema.getSimpleType(base).getParentSchema().getUri(), base);
      if (name == null && attributeUses.equals(AttributeGroup.EMPTY)) {
        xw.attribute("type", base);
        if (parent != null)
          outputAnnotation(parent);
        return;
      }
    }
    else if (attributeUses.equals(AttributeGroup.EMPTY)) {
      simpleTypeOutput.outputWrap(t.getSimpleType(), parent);
      return;
    }
    if (name == null && parent != null)
      outputAnnotation(parent);
    xw.startElement(xs("complexType"));
    if (name != null)
      xw.attribute("name", name);
    if (name != null && parent != null)
      outputAnnotation(parent);
    xw.startElement(xs("simpleContent"));
    if (base == null)
      base = (String)t.getSimpleType().accept(simpleTypeNamer);
    if (base != null) {
      xw.startElement(xs("extension"));
      xw.attribute("base", base);
    }
    else {
      xw.startElement(xs("restriction"));
      xw.attribute("base", xs("anyType"));
      simpleTypeOutput.outputWrap(t.getSimpleType(), null);
    }
    outputAttributeUse(attributeUses);
    xw.endElement();
    xw.endElement();
    xw.endElement();
  }

  private void outputComplexType(Name elementName, ComplexType ct, Annotated parent) {
    Name substitutionGroup = nsm.getSubstitutionGroup(elementName);
    if (substitutionGroup != null) {
      xw.attribute("substitutionGroup", qualifyName(substitutionGroup));
      if (ct != null && ct.equals(abstractElementTypeSelector.getAbstractElementType(substitutionGroup)))
        ct = null;
    }
    if (ct != null) {
      if (ct instanceof ComplexTypeNotAllowedContent) {
        xw.attribute("abstract", "true");
        outputAnnotation(parent);
      }
      else {
        complexTypeOutput.parent = parent;
        ct.accept(complexTypeOutput);
      }
    }
    else
      outputAnnotation(parent);
  }

  private void outputAnnotation(Annotated annotated) {
    Annotation annotation = annotated.getAnnotation();
    if (annotation == null)
      return;
    xw.startElement(xs("annotation"));
    String documentation = annotation.getDocumentation();
    if (documentation != null) {
      xw.startElement(xs("documentation"));
      xw.text(documentation);
      xw.endElement();
    }
    xw.endElement();
  }

  private void outputCommentList(List list) {
    for (Iterator iter = list.iterator(); iter.hasNext();)
      xw.comment(((Comment)iter.next()).getContent());
  }
}
