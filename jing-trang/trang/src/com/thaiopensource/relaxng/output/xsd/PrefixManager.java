package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.relaxng.parse.Context;
import com.thaiopensource.xml.util.Naming;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrefixManager implements SourceUriGenerator {

  private final Map<String, String> prefixMap = new HashMap<String, String>();
  private final Set<String> usedPrefixes = new HashSet<String>();
  /**
   * Set of prefixes that cannot be used for schema namespace.
   */
  private final Set<String> reservedPrefixes = new HashSet<String>();
  private int nextGenIndex = 1;
  static private final String[] xsdPrefixes  = { "xs", "xsd" };
  static private final int MAX_PREFIX_LENGTH = 10;

  static class PrefixUsage {
    int count;
  }

  class PrefixSelector extends AbstractVisitor {
    private final SchemaInfo si;
    private String inheritedNamespace;
    private final Map<String, Map<String, PrefixUsage>> namespacePrefixUsageMap = new HashMap<String, Map<String, PrefixUsage>>();

    PrefixSelector(SchemaInfo si) {
      this.si = si;
      this.inheritedNamespace = "";
      si.getGrammar().componentsAccept(this);
      Context context = si.getGrammar().getContext();
      if (context != null) {
        for (Enumeration e = context.prefixes(); e.hasMoreElements();) {
          String prefix = (String)e.nextElement();
          if (!prefix.equals(""))
            notePrefix(prefix, resolveNamespace(context.resolveNamespacePrefix(prefix)));
        }
      }
    }

    public VoidValue visitElement(ElementPattern p) {
      p.getNameClass().accept(this);
      p.getChild().accept(this);
      return VoidValue.VOID;
    }

    public VoidValue visitAttribute(AttributePattern p) {
      return p.getNameClass().accept(this);
    }

    public VoidValue visitChoice(ChoiceNameClass nc) {
      nc.childrenAccept(this);
      return VoidValue.VOID;
    }

    public VoidValue visitName(NameNameClass nc) {
      notePrefix(nc.getPrefix(), resolveNamespace(nc.getNamespaceUri()));
      return VoidValue.VOID;
    }

    public VoidValue visitValue(ValuePattern p) {
      for (Map.Entry<String, String> entry : p.getPrefixMap().entrySet()) {
        String prefix = entry.getKey();
        if (prefix != null && !prefix.equals("")) {
          String ns = resolveNamespace(entry.getValue());
          notePrefix(prefix, ns);
          if (!ns.equals(WellKnownNamespaces.XML_SCHEMA))
            reservedPrefixes.add(prefix);
        }
      }
      return VoidValue.VOID;
    }

    private String resolveNamespace(String ns) {
      return ns == NameNameClass.INHERIT_NS ? inheritedNamespace : ns;
    }

    private void notePrefix(String prefix, String ns) {
      if (prefix == null || ns == null || ns.equals(""))
        return;
      Map<String, PrefixUsage> prefixUsageMap = namespacePrefixUsageMap.get(ns);
      if (prefixUsageMap == null) {
        prefixUsageMap = new HashMap<String, PrefixUsage>();
        namespacePrefixUsageMap.put(ns, prefixUsageMap);
      }
      PrefixUsage prefixUsage = prefixUsageMap.get(prefix);
      if (prefixUsage == null) {
        prefixUsage = new PrefixUsage();
        prefixUsageMap.put(prefix, prefixUsage);
      }
      prefixUsage.count++;
    }

    public VoidValue visitComposite(CompositePattern p) {
      p.childrenAccept(this);
      return VoidValue.VOID;
    }

    public VoidValue visitUnary(UnaryPattern p) {
      return p.getChild().accept(this);
    }

    public VoidValue visitDefine(DefineComponent c) {
      c.getBody().accept(this);
      return VoidValue.VOID;
    }

    public VoidValue visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return VoidValue.VOID;
    }

    public VoidValue visitInclude(IncludeComponent c) {
      String saveInheritedNamespace = inheritedNamespace;
      inheritedNamespace = c.getNs();
      si.getSchema(c.getHref()).componentsAccept(this);
      inheritedNamespace = saveInheritedNamespace;
      return VoidValue.VOID;
    }

    void assignPrefixes() {
      for (Map.Entry<String, Map<String, PrefixUsage>> entry : namespacePrefixUsageMap.entrySet()) {
        String ns = entry.getKey();
        if (!ns.equals("") && !ns.equals(WellKnownNamespaces.XML)) {
          Map<String, PrefixUsage> prefixUsageMap = entry.getValue();
          if (prefixUsageMap != null) {
            Map.Entry<String, PrefixUsage> best = null;
            for (Map.Entry<String, PrefixUsage> tem : prefixUsageMap.entrySet()) {
              if ((best == null
                   || (tem.getValue()).count > (best.getValue()).count)
                  && prefixOk(tem.getKey(), ns))
                best = tem;
            }
            if (best != null)
              usePrefix(best.getKey(), ns);
          }
        }
      }
    }
  }

  PrefixManager(SchemaInfo si) {
    usePrefix("xml", WellKnownNamespaces.XML);
    new PrefixSelector(si).assignPrefixes();
  }

  String getPrefix(String namespace) {
    String prefix = prefixMap.get(namespace);
    if (prefix == null && namespace.equals(WellKnownNamespaces.XML_SCHEMA)) {
      for (int i = 0; i < xsdPrefixes.length; i++)
        if (tryUsePrefix(xsdPrefixes[i], namespace))
          return xsdPrefixes[i];
    }
    if (prefix == null)
      prefix = tryUseUri(namespace);
    if (prefix == null) {
      do {
        prefix = "ns" + Integer.toString(nextGenIndex++);
      } while (!tryUsePrefix(prefix, namespace));
    }
    return prefix;
  }

  private String tryUseUri(String namespace) {
    String segment = chooseSegment(namespace);
    if (segment == null)
      return null;
    if (segment.length() <= MAX_PREFIX_LENGTH && tryUsePrefix(segment, namespace))
      return segment;
    for (int i = 1; i <= segment.length(); i++) {
      String prefix = segment.substring(0, i);
      if (tryUsePrefix(prefix, namespace))
        return prefix;
    }
    return null;
  }

  private boolean tryUsePrefix(String prefix, String namespace) {
    if (!prefixOk(prefix, namespace))
      return false;
    usePrefix(prefix, namespace);
    return true;
  }

  private boolean prefixOk(String prefix, String namespace) {
    return (!usedPrefixes.contains(prefix)
            && !(reservedPrefixes.contains(prefix) && namespace.equals(WellKnownNamespaces.XML_SCHEMA)));
  }

  private void usePrefix(String prefix, String namespace) {
    usedPrefixes.add(prefix);
    prefixMap.put(namespace, prefix);
  }

  static private String chooseSegment(String ns) {
    int off = ns.indexOf('#');
    if (off >= 0) {
      String segment = ns.substring(off + 1).toLowerCase();
      if (Naming.isNcname(segment))
        return segment;
    }
    else
      off = ns.length();
    for (;;) {
      int i = ns.lastIndexOf('/', off - 1);
      if (i < 0 || (i > 0 && ns.charAt(i - 1) == '/'))
        break;
      String segment = ns.substring(i + 1, off).toLowerCase();
      if (segmentOk(segment))
        return segment;
      off = i;
    }
    off = ns.indexOf(':');
    if (off >= 0) {
      String segment = ns.substring(off + 1).toLowerCase();
      if (segmentOk(segment))
        return segment;
    }
    return null;
  }

  private static boolean segmentOk(String segment) {
    return Naming.isNcname(segment) && !segment.equals("ns") && !segment.equals("namespace");
  }

  public String generateSourceUri(String ns) {
    // TODO add method to OutputDirectory to do this properly
    if (ns.equals(""))
      return "local";
    else
      return "/" + getPrefix(ns);
  }
}
