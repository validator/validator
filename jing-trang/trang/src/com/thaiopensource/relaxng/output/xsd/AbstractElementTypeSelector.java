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

  ComplexType getAbstractElementType(Name name) {
    ComplexType ct = (ComplexType)abstractElementComplexTypeMap.get(name);
    if (ct == null) {
      ct = computeAbstractElementType(name);
      if (ct == null)
        ct = urType;
      abstractElementComplexTypeMap.put(name, ct);
    }
    if (ct == urType)
      return null;
    return ct;
  }

  private ComplexType computeAbstractElementType(Name name) {
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
    if (isValidlyDerived(ct1, ct2))
      return ct2;
    if (isValidlyDerived(ct2, ct1))
      return ct1;
    return null;
  }

  private boolean isValidlyDerived(ComplexType ct1, ComplexType ct2) {
    if (ct1 instanceof ComplexTypeComplexContent && ct2 instanceof ComplexTypeComplexContent)
      return isComplexContentValidlyDerived((ComplexTypeComplexContent)ct1, (ComplexTypeComplexContent)ct2);
    if (ct1 instanceof ComplexTypeSimpleContent && ct2 instanceof ComplexTypeSimpleContent)
      return isSimpleContentValidlyDerived((ComplexTypeSimpleContent)ct1, (ComplexTypeSimpleContent)ct2);
    return false;
  }

  private boolean isComplexContentValidlyDerived(ComplexTypeComplexContent ct1, ComplexTypeComplexContent ct2) {
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

  private boolean isSimpleContentValidlyDerived(ComplexTypeSimpleContent ct1, ComplexTypeSimpleContent ct2) {
    ComplexTypeSimpleContentExtension ex = complexTypeSelector.transformSimpleContent(ct2);
    if (!ex.getAttributeUses().equals(AttributeGroup.EMPTY))
      return false;
    String base = ex.getBase();
    if (base == null)
      return isSimpleTypeValidlyDerived(ct1.getSimpleType(), ex.getSimpleType());
    else
      return isSimpleTypeValidlyDerivedFromName(ct1.getSimpleType(), base);
  }

  private boolean isSimpleTypeValidlyDerived(SimpleType st1, SimpleType st2) {
    // XXX take advantage of cos-st-derived-ok 2.2.4 (SQC seems to have bugs here)
    if (st2.getAnnotation() != null)
      return false;
    if (st2 instanceof SimpleTypeRef)
      return isSimpleTypeValidlyDerivedFromName(st1, ((SimpleTypeRef)st2).getName());
    if (st2 instanceof SimpleTypeRestriction) {
      SimpleTypeRestriction restriction = (SimpleTypeRestriction)st2;
      if (restriction.getFacets().size() > 0)
        return false;
      return isSimpleTypeValidlyDerivedFromBuiltin(st1, restriction.getName());
    }
    return false;
  }

  private boolean isSimpleTypeValidlyDerivedFromName(SimpleType st, String typeName) {
    while (st instanceof SimpleTypeRef) {
      String tem = ((SimpleTypeRef)st).getName();
      if (tem.equals(typeName))
        return true;
      st = schema.getSimpleType(tem).getSimpleType();
    }
    return false;
  }

  private boolean isSimpleTypeValidlyDerivedFromBuiltin(SimpleType st, String builtinTypeName) {
    while (st instanceof SimpleTypeRef)
      st = schema.getSimpleType(((SimpleTypeRef)st).getName()).getSimpleType();
    if (!(st instanceof SimpleTypeRestriction))
      return false;
    String tem = ((SimpleTypeRestriction)st).getName();
    do {
      if (tem.equals(builtinTypeName))
        return true;
      tem = BuiltinSimpleTypeHierarchy.getParentType(tem);
    } while (tem != null);
    return false;
  }

  private ComplexType getElementType(Name name, NamespaceManager nsm) {
    Element element = nsm.getGlobalElement(name);
    if (element != null)
      return element.getComplexType();
    return getAbstractElementType(name);
  }
}

