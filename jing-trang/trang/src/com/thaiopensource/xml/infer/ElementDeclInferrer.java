package com.thaiopensource.xml.infer;

import com.thaiopensource.relaxng.output.common.Name;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;

class ElementDeclInferrer {
  private final DatatypeRepertoire datatypes;
  private ContentModelInferrer contentModelInferrer;
  private final Map attributeTypeMap = new HashMap();
  private DatatypeInferrer valueInferrer;
  private final Set requiredAttributeNames = new HashSet();
  private Set mixedContentNames = null;

  ElementDeclInferrer(DatatypeRepertoire datatypes, List attributeNames) {
    this.datatypes = datatypes;
    requiredAttributeNames.addAll(attributeNames);
  }

  ElementDecl infer() {
    ElementDecl decl = new ElementDecl();
    for (Iterator iter = attributeTypeMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      decl.getAttributeDecls().put(entry.getKey(),
                                   new AttributeDecl(((DatatypeInferrer)entry.getValue()).getTypeName(),
                                                     !requiredAttributeNames.contains(entry.getKey())));
    }
    if (contentModelInferrer != null)
      decl.setContentModel(contentModelInferrer.inferContentModel());
    else if (mixedContentNames != null)
      decl.setContentModel(makeMixedContentModel());
    else {
      if (valueInferrer.isAllWhiteSpace())
        decl.setContentModel(new EmptyParticle());
      else {
        Name typeName = valueInferrer.getTypeName();
        if (typeName == null)
          decl.setContentModel(new TextParticle());
        else
          decl.setDatatype(typeName);
      }
    }
    return decl;
  }

  private Particle makeMixedContentModel() {
    Particle p = new TextParticle();
    for (Iterator iter = mixedContentNames.iterator(); iter.hasNext();)
      p = new ChoiceParticle(p, new ElementParticle((Name)iter.next()));
    return new OneOrMoreParticle(p);
  }

  boolean wantValue() {
    return contentModelInferrer == null && mixedContentNames == null;
  }

  void addElement(Name elementName) {
    if (valueInferrer != null) {
      if (valueInferrer.isAllWhiteSpace()) {
        if (contentModelInferrer == null)
          contentModelInferrer = ContentModelInferrer.createContentModelInferrer();
        // Previously had all elements contained only white space.
        // Equivalent to an empty content model.
        contentModelInferrer.endSequence();
        valueInferrer = null;
      }
      else
        useMixedContent();
    }
    if (mixedContentNames != null)
      mixedContentNames.add(elementName);
    else {
      if (contentModelInferrer == null)
        contentModelInferrer = ContentModelInferrer.createContentModelInferrer();
      contentModelInferrer.addElement(elementName);
    }
  }

  void endSequence() {
    if (contentModelInferrer != null)
      contentModelInferrer.endSequence();
  }

  void addValue(String value) {
    if (valueInferrer == null)
      valueInferrer = new DatatypeInferrer(datatypes, value);
    else
      valueInferrer.addValue(value);
  }

  void addText() {
    useMixedContent();
  }

  private void useMixedContent() {
    if (mixedContentNames == null) {
      mixedContentNames = new HashSet();
      if (contentModelInferrer != null) {
        mixedContentNames.addAll(contentModelInferrer.getElementNames());
        contentModelInferrer = null;
      }
      valueInferrer = null;
    }
  }

  void addAttributeNames(List attributeNames) {
    requiredAttributeNames.retainAll(attributeNames);
  }

  void addAttributeValue(Name name, String value) {
    DatatypeInferrer dt = (DatatypeInferrer)attributeTypeMap.get(name);
    if (dt == null) {
      dt = new DatatypeInferrer(datatypes, value);
      attributeTypeMap.put(name, dt);
    }
    else
      dt.addValue(value);
  }

}
