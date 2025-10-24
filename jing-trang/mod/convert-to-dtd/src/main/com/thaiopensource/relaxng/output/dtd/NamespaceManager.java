package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class NamespaceManager {
  // map namespace URIs to non-empty prefix
  private final Map<String, String> namespaceUriMap = new HashMap<String, String>();
  private String defaultNamespaceUri = null;
  private final Set<String> usedPrefixes = new HashSet<String>();
  private final Set<String> unassignedNamespaceUris = new HashSet<String>();

  NamespaceManager() {
    usedPrefixes.add("xml");
    namespaceUriMap.put(WellKnownNamespaces.XML, "xml");
  }

  String getPrefixForNamespaceUri(String ns) {
    return namespaceUriMap.get(ns);
  }

  String getDefaultNamespaceUri() {
    return defaultNamespaceUri;
  }

  void assignPrefixes() {
    if (defaultNamespaceUri == null)
      defaultNamespaceUri = "";
    int n = 0;
    for (String ns : unassignedNamespaceUris) {
      for (; ;) {
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
    String assignedPrefix = namespaceUriMap.get(ns);
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
