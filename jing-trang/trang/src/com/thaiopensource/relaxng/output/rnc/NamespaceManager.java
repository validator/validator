package com.thaiopensource.relaxng.output.rnc;

import com.thaiopensource.xml.util.WellKnownNamespaces;
import com.thaiopensource.relaxng.parse.SchemaBuilder;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.Vector;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

class NamespaceManager {

  static class NamespaceBindings {
    /**
     * maps prefixes to namespaces
     */
    private final Map prefixMap;
    /**
     * maps namespaces to preferred non-empty prefix
     */
    private final Map nsMap;

    private NamespaceBindings(Map prefixMap, Map nsMap) {
      this.prefixMap = prefixMap;
      this.nsMap = nsMap;
    }

    /**
     * Must return non-empty prefix.
     * ns may be empty string
     */
    String getNonEmptyPrefix(String ns) {
      return (String)nsMap.get(ns);
    }

    /**
     * prefix of empty string refers to default namespace
     * if no binding, return null
     */
    String getNamespaceUri(String prefix) {
      return (String)prefixMap.get(prefix);
    }

    Set getPrefixes() {
      return prefixMap.keySet();
    }

  }

  /**
   * maps a namespaceUri to Boolean; true means empty prefix is OK
   */
  private final Map requiredNamespaces = new HashMap();

  static private final String[] conventionalBindings = {
    // prefix, namespaceUri
    "", SchemaBuilder.INHERIT_NS,
    "inherit", SchemaBuilder.INHERIT_NS,
    "", "",
    "local", "",
    "rng", WellKnownNamespaces.RELAX_NG,
    "a", WellKnownNamespaces.RELAX_NG_COMPATIBILITY_ANNOTATIONS
  };


  static class Binding {
    private final String prefix;
    private final String namespaceUri;
    Binding(String prefix, String namespaceUri) {
      this.prefix = prefix;
      this.namespaceUri = namespaceUri;
    }

    String getPrefix() {
      return prefix;
    }

    String getNamespaceUri() {
      return namespaceUri;
    }

    public int hashCode() {
      return prefix.hashCode() ^ namespaceUri.hashCode();
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof Binding))
        return false;
      Binding other = (Binding)obj;
      return (this.prefix.equals(other.prefix)
              && this.namespaceUri.equals(other.namespaceUri));
    }
  }


  static class BindingUsage {
    boolean required;
    int usageCount;
  }

  /**
   * map Binding to BindingUsage
   */
  private final Map bindingUsageMap = new HashMap();

  void requireNamespace(String ns, boolean prefixMaybeEmpty) {
    Boolean b = (Boolean)requiredNamespaces.get(ns);
    if (b == null || (b.booleanValue() && !prefixMaybeEmpty)) {
      b = prefixMaybeEmpty ? Boolean.TRUE : Boolean.FALSE;
      requiredNamespaces.put(ns, b);
    }
  }

  /**
   * prefix may be empty string
   */
  void requireBinding(String prefix, String ns) {
    noteBinding(prefix, ns, true);
  }

  /**
   * prefix may be empty string
   */
  void preferBinding(String prefix, String ns) {
    noteBinding(prefix, ns, false);
  }

  private void noteBinding(String prefix, String ns, boolean required) {
    if (ns.equals(WellKnownNamespaces.XML))
      return;
    Binding b = new Binding(prefix, ns);
    BindingUsage bu = (BindingUsage)bindingUsageMap.get(b);
    if (bu == null) {
      bu = new BindingUsage();
      bindingUsageMap.put(b, bu);
    }
    if (required)
      bu.required = true;
    bu.usageCount++;
  }

  class BindingComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      BindingUsage bu1 = (BindingUsage)bindingUsageMap.get(o1);
      BindingUsage bu2 = (BindingUsage)bindingUsageMap.get(o2);
      // required precedes not required
      if (bu1.required != bu2.required)
        return bu1.required ? -1 : 1;
      // more usage precedes less usage
      if (bu1.usageCount != bu2.usageCount)
        return bu2.usageCount - bu1.usageCount;
      Binding b1 = (Binding)o1;
      Binding b2 = (Binding)o2;
      // Make it a total order to avoid depending on order of
      // iteration over HashSet.
      // prefer shorter prefix
      if (b1.prefix.length() != b2.prefix.length())
        return b1.prefix.length() - b2.prefix.length();
      int ret = b1.prefix.compareTo(b2.prefix);
      if (ret != 0)
        return ret;
      return b1.namespaceUri.compareTo(b2.namespaceUri);
    }
  }

  NamespaceBindings createBindings() {
    // maps prefix representing a string to a namespaceUri
    Map prefixMap = new HashMap();
    // maps namespace to preferred non-empty prefix
    Map nsMap = new HashMap();
    prefixMap.put("xml", WellKnownNamespaces.XML);
    requiredNamespaces.remove(WellKnownNamespaces.XML);
    List bindingList = new Vector();
    bindingList.addAll(bindingUsageMap.keySet());
    Collections.sort(bindingList, new BindingComparator());
    for (Iterator iter = bindingList.iterator(); iter.hasNext();) {
      Binding binding = (Binding)iter.next();
      if (prefixMap.get(binding.prefix) == null) {
        Boolean defaultOK = (Boolean)requiredNamespaces.get(binding.namespaceUri);
        boolean satisfiesRequirement = defaultOK != null && (binding.prefix.length() > 0 || defaultOK.booleanValue());
        if (((BindingUsage)bindingUsageMap.get(binding)).required || satisfiesRequirement) {
          prefixMap.put(binding.prefix, binding.namespaceUri);
          iter.remove();
          if (satisfiesRequirement)
            requiredNamespaces.remove(binding.namespaceUri);
          if (binding.prefix.length() > 0)
            nsMap.put(binding.namespaceUri, binding.prefix);
        }
      }
    }
    // use any of the bindings that we haven't yet used that don't conflict
    for (Iterator iter = bindingList.iterator(); iter.hasNext();) {
      Binding binding = (Binding)iter.next();
      if (prefixMap.get(binding.prefix) == null) {
        prefixMap.put(binding.prefix, binding.namespaceUri);
      }
    }
    for (int i = 0; i < conventionalBindings.length; i += 2) {
      String prefix = conventionalBindings[i];
      if (prefixMap.get(prefix) == null) {
        String ns = conventionalBindings[i + 1];
        Boolean defaultOK = (Boolean)requiredNamespaces.get(ns);
        if (defaultOK != null && (prefix.length() > 0 || defaultOK.booleanValue())) {
          prefixMap.put(prefix, ns);
          requiredNamespaces.remove(ns);
          if (prefix.length() > 0)
            nsMap.put(ns, prefix);
        }
      }
    }
    Iterator iter = requiredNamespaces.keySet().iterator();
    for (int i = 1; iter.hasNext(); i++) {
      String prefix = "ns" + Integer.toString(i);
      if (prefixMap.get(prefix) == null) {
        Object ns = iter.next();
        prefixMap.put(prefix, ns);
        nsMap.put(ns, prefix);
      }
    }
    return new NamespaceBindings(prefixMap, nsMap);
  }

}
