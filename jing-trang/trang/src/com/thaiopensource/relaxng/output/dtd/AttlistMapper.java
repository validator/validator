package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NameNameClass;

import java.util.Map;
import java.util.HashMap;

class AttlistMapper {
  private final Map elementToAttlistMap = new HashMap();
  private final Map paramEntityToElementMap = new HashMap();

  void noteAttribute(ElementPattern e) {
    elementToAttlistMap.put(e, Boolean.FALSE);
  }

  void noteAttributeGroupRef(ElementPattern e, String paramEntityName) {
    if (e != null) {
      if (elementToAttlistMap.get(e) != null)
        elementToAttlistMap.put(e, Boolean.FALSE);
      else
        elementToAttlistMap.put(e, paramEntityName);
    }
    if (e == null || paramEntityToElementMap.get(paramEntityName) != null)
      paramEntityToElementMap.put(paramEntityName, Boolean.FALSE);
    else
      paramEntityToElementMap.put(paramEntityName, e);
  }

  String getParamEntityElementName(String name) {
    Object elem = paramEntityToElementMap.get(name);
    if (elem == null || elem == Boolean.FALSE)
      return null;
    Object tem = elementToAttlistMap.get(elem);
    if (!name.equals(tem))
      return null;
    NameClass nc = ((ElementPattern)elem).getNameClass();
    if (!(nc instanceof NameNameClass))
      return null;
    return ((NameNameClass)nc).getLocalName();
  }
}
