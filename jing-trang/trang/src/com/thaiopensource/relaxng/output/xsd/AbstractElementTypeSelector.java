package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexType;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeNotAllowedContent;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeComplexContent;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeSimpleContent;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroup;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRestriction;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRef;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.common.Name;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

class AbstractElementTypeSelector {
  private final Schema schema;
  private final NamespaceManager nsm;
  private final ComplexTypeSelector complexTypeSelector;
  private final Map abstractElementComplexTypeMap = new HashMap();
  private final ComplexType urType = new ComplexTypeNotAllowedContent();

  AbstractElementTypeSelector(Schema schema, NamespaceManager nsm, ComplexTypeSelector complexTypeSelector) {
    this.schema = schema;
    this.nsm = nsm;
    this.complexTypeSelector = complexTypeSelector;
  }

  ComplexType getAbstractElementType(Name name, NamespaceManager nsm) {
    ComplexType ct = (ComplexType)abstractElementComplexTypeMap.get(name);
    if (ct == null) {
      ct = computeAbstractElementType(name, nsm);
      if (ct == null)
        ct = urType;
      abstractElementComplexTypeMap.put(name, ct);
    }
    if (ct == urType)
      return null;
    return ct;
  }

  private ComplexType computeAbstractElementType(Name name, NamespaceManager nsm) {
    List members = nsm.getAbstractElementSubstitutionGroupMembers(name);
    if (members == null)
      return null;
    ComplexType commonType = null;
    for (Iterator iter = members.iterator(); iter.hasNext();) {
      ComplexType ct = getElementType((Name)iter.next(), nsm);
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
    ComplexTypeComplexContentExtension ex = complexTypeSelector.transformComplexContent(ct2);
    String base = ex.getBase();
    if (base == null || ex.getParticle() != null || !ex.getAttributeUses().equals(AttributeGroup.EMPTY) || ex.isMixed())
      return false;
    Particle particle = ct1.getParticle();
    for (;;) {
      String tem = complexTypeSelector.particleBase(particle);
      if (base.equals(tem))
        return true;
      if (tem == null)
        break;
      if (!complexTypeSelector.isComplexType(tem))
        break;
      particle = schema.getGroup(tem).getParticle();
    }
    return false;
  }

  private boolean hasBaseTypeSimpleContent(ComplexTypeSimpleContent ct1, ComplexTypeSimpleContent ct2) {
    ComplexTypeSimpleContentExtension ex = complexTypeSelector.transformSimpleContent(ct2);
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

  private ComplexType getElementType(Name name, NamespaceManager nsm) {
    Element element = nsm.getGlobalElement(name);
    if (element != null)
      return element.getComplexType();
    return getAbstractElementType(name, nsm);
  }
}

