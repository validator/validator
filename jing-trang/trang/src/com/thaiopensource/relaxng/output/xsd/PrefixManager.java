package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.parse.Context;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import com.thaiopensource.xml.util.Naming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;

public class PrefixManager implements SourceUriGenerator {

  private final Map prefixMap = new HashMap();
  private final Set usedPrefixes = new HashSet();
  /**
   * Set of prefixes that cannot be used for schema namespace.
   */
  private final Set reservedPrefixes = new HashSet();
  private int nextGenIndex = 1;
  static private final String[] xsdPrefixes  = { "xs", "xsd" };
  static private final int MAX_PREFIX_LENGTH = 10;

  static class PrefixUsage {
    int count;
  }

  class PrefixSelector extends AbstractVisitor {
    private final SchemaInfo si;
    private String inheritedNamespace;
    private final Map namespacePrefixUsageMap = new HashMap();

    PrefixSelector(SchemaInfo si) {
      this.si = si;
      this.inheritedNamespace = "";
      si.getGrammar().componentsAccept(this);
      Context context = si.getGrammar().getContext();
      if (context != null) {
        for (Enumeration enum = context.prefixes(); enum.hasMoreElements();) {
          String prefix = (String)enum.nextElement();
          if (!prefix.equals(""))
            notePrefix(prefix, resolveNamespace(context.resolveNamespacePrefix(prefix)));
        }
      }
    }

    public Object visitElement(ElementPattern p) {
      p.getNameClass().accept(this);
      p.getChild().accept(this);
      return null;
    }

    public Object visitAttribute(AttributePattern p) {
      return p.getNameClass().accept(this);
    }

    public Object visitChoice(ChoiceNameClass nc) {
      nc.childrenAccept(this);
      return null;
    }

    public Object visitName(NameNameClass nc) {
      notePrefix(nc.getPrefix(), resolveNamespace(nc.getNamespaceUri()));
      return null;
    }

    public Object visitValue(ValuePattern p) {
      for (Iterator iter = p.getPrefixMap().entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String prefix = (String)entry.getKey();
        if (prefix != null && !prefix.equals("")) {
          String ns = resolveNamespace((String)entry.getValue());
          notePrefix(prefix, ns);
          if (!ns.equals(WellKnownNamespaces.XML_SCHEMA))
            reservedPrefixes.add(prefix);
        }
      }
      return null;
    }

    private String resolveNamespace(String ns) {
      return ns == NameNameClass.INHERIT_NS ? inheritedNamespace : ns;
    }

    private void notePrefix(String prefix, String ns) {
      if (prefix == null || ns == null || ns.equals(""))
        return;
      Map prefixUsageMap = (Map)namespacePrefixUsageMap.get(ns);
      if (prefixUsageMap == null) {
        prefixUsageMap = new HashMap();
        namespacePrefixUsageMap.put(ns, prefixUsageMap);
      }
      PrefixUsage prefixUsage = (PrefixUsage)prefixUsageMap.get(prefix);
      if (prefixUsage == null) {
        prefixUsage = new PrefixUsage();
        prefixUsageMap.put(prefix, prefixUsage);
      }
      prefixUsage.count++;
    }

    public Object visitComposite(CompositePattern p) {
      p.childrenAccept(this);
      return null;
    }

    public Object visitUnary(UnaryPattern p) {
      return p.getChild().accept(this);
    }

    public Object visitDefine(DefineComponent c) {
      c.getBody().accept(this);
      return null;
    }

    public Object visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      String saveInheritedNamespace = inheritedNamespace;
      inheritedNamespace = c.getNs();
      si.getSchema(c.getHref()).componentsAccept(this);
      inheritedNamespace = saveInheritedNamespace;
      return null;
    }

    void assignPrefixes() {
      for (Iterator iter = namespacePrefixUsageMap.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String ns = (String)entry.getKey();
        if (!ns.equals("") && !ns.equals(WellKnownNamespaces.XML)) {
          Map prefixUsageMap = (Map)entry.getValue();
          if (prefixUsageMap != null) {
            Map.Entry best = null;
            for (Iterator entryIter = prefixUsageMap.entrySet().iterator(); entryIter.hasNext();) {
              Map.Entry tem = (Map.Entry)entryIter.next();
              if ((best == null
                   || ((PrefixUsage)tem.getValue()).count > ((PrefixUsage)best.getValue()).count)
                  && prefixOk((String)tem.getKey(), ns))
                best = tem;
            }
            if (best != null)
              usePrefix((String)best.getKey(), ns);
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
    String prefix = (String)prefixMap.get(namespace);
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
