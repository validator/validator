package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

class NamespaceManager {
  // map namespace URIs to non-empty prefix
  private final Map namespaceUriMap = new HashMap();
  private String defaultNamespaceUri = null;
  private final Set usedPrefixes = new HashSet();
  private final Set unassignedNamespaceUris = new HashSet();

  NamespaceManager() {
    usedPrefixes.add("xml");
    namespaceUriMap.put(WellKnownNamespaces.XML, "xml");
  }

  String getPrefixForNamespaceUri(String ns) {
    return (String)namespaceUriMap.get(ns);
  }

  String getDefaultNamespaceUri() {
    return defaultNamespaceUri;
  }

  void assignPrefixes() {
    if (defaultNamespaceUri == null)
      defaultNamespaceUri = "";
    int n = 0;
    for (Iterator iter = unassignedNamespaceUris.iterator(); iter.hasNext();) {
      String ns = (String)iter.next();
      for (;;) {
        ++n;
        String prefix = "ns" + Integer.toString(n);
        if (!usedPrefixes.contains(prefix)) {
          namespaceUriMap.put(ns, prefix);
          break;
        }
      }
    }
  }

  void noteName(NameNameClass nc, boolean defaultable) {
    String ns = nc.getNamespaceUri();
    if (ns.equals("") || ns == NameClass.INHERIT_NS) {
      if (defaultable)
        defaultNamespaceUri = "";
      return;
    }
    String assignedPrefix = (String)namespaceUriMap.get(ns);
    if (assignedPrefix != null)
      return;
    String prefix = nc.getPrefix();
    if (prefix == null) {
      if (defaultNamespaceUri == null && defaultable)
        defaultNamespaceUri = ns;
      unassignedNamespaceUris.add(ns);
    }
    else {
      if (usedPrefixes.contains(prefix))
        unassignedNamespaceUris.add(ns);
      else {
        usedPrefixes.add(prefix);
        namespaceUriMap.put(ns, prefix);
        unassignedNamespaceUris.remove(ns);
      }
    }
  }
}
