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
  private Map attributeTypeMap = new HashMap();
  private DatatypeInferrer valueInferrer;
  private Set requiredAttributeNames = new HashSet();
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
    for (Iterator iter = mixedContentNames.iterator(); iter.hasNext();) {
      Name name = (Name)iter.next();
      if (name != ContentModelInferrer.START && name != ContentModelInferrer.END)
        p = new ChoiceParticle(p, new ElementParticle(name));
    }
    return new OneOrMoreParticle(p);
  }

  boolean wantValue() {
    return contentModelInferrer == null && mixedContentNames == null;
  }

  void addSequence(Name e1, Name e2) {
    if (valueInferrer != null) {
      if (valueInferrer.isAllWhiteSpace()) {
        if (contentModelInferrer == null)
          contentModelInferrer = new ContentModelInferrer();
        contentModelInferrer.addSequence(ContentModelInferrer.START, ContentModelInferrer.END);
      }
      else
        useMixedContent();
    }
    if (mixedContentNames != null)
      mixedContentNames.add(e2);
    else {
      if (contentModelInferrer == null)
        contentModelInferrer = new ContentModelInferrer();
      contentModelInferrer.addSequence(e1, e2);
    }
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

  void setMulti(Name name) {
    if (contentModelInferrer != null)
      contentModelInferrer.setMulti(name);
  }
}
