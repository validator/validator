package com.thaiopensource.validate.nvdl;

import java.util.regex.PatternSyntaxException;

/**
 * Stores information about a namespace specification.
 * A namespace is specified with a namespace pattern and a wildcard.
 * The wildcard can be present in multiple places in the namespace 
 * specification and each occurence of the wildcard can be replaced with
 * an arbitrary sequence of characters.
 * 
 * @author george
 */
class NamespaceSpecification {
  /**
   * Default value for wildcard.
   */
  public static String DEFAULT_WILDCARD = "*";
  
  /**
   * Constant for any namespace.
   */
  static final String ANY_NAMESPACE = "##any";

  /**
   * The namespace pattern, may contain one or more occurances of the wildcard.
   */
  String ns="\0";
  
  /**
   * The wildcard character, by default it is *.
   */
  String wildcard = DEFAULT_WILDCARD;
    
  /**
   * Creates a namespace specification from a namespace pattern
   * using the default wildcard, that is *.
   * @param ns The namespace pattern
   */
  public NamespaceSpecification(String ns) {
    this(ns, DEFAULT_WILDCARD);
  }
  
  /**
   * Creates a namespace specification from a namespace pattern
   * and a given wildcard.
   * @param ns The namespace pattern
   * @param wildcard The given wildcard character.
   */
  public NamespaceSpecification(String ns, String wildcard) {
    this.ns = ns;
    this.wildcard = wildcard;
  }

  /**
   * Check if this namespace specification competes with 
   * another namespace specification.
   * @param other The namespace specification we need to check if
   * it competes with this namespace specification.
   * @return true if the namespace specifications compete.
   */
  public boolean compete(NamespaceSpecification other) {
    // if no wildcard for other then we check coverage
    if ("".equals(other.wildcard)) {
      return covers(other.ns);
    }
    // split the namespaces at wildcards     
    String[] otherParts = split(other.ns, other.wildcard);
    
    // if the given namepsace specification does not use its wildcard
    // then we just look if the current namespace specification covers it
    if (otherParts.length == 1) {
      return covers(other.ns);
    }
    // if no wildcard for the current namespace specification
    if ("".equals(wildcard)) {
      return other.covers(ns);
    }
    // also for the current namespace specification
    String[] parts = split(ns, wildcard); 
    // now check if the current namespace specification is just an URI
    if (parts.length == 1) {
      return other.covers(ns);
    }
    // now each namespace specification contains wildcards
    // suppose we have 
    // ns   = a1*a2*...*an 
    // and 
    // other.ns = b1*b2*...*bm
    // then we only need to check matchPrefix(a1, b1) and matchPrefix(an, bn) where
    // matchPrefix(a, b) means a starts with b or b starts with a.
    return matchPrefix(parts[0], otherParts[0])
           || matchPrefix(parts[parts.length - 1], otherParts[otherParts.length - 1]);
  }

  /**
   * Checks with either of the strings starts with the other.
   * @param s1 a String
   * @param s2 a String
   * @return true if s1 starts with s2 or s2 starts with s1, false otherwise
   */
  static private boolean matchPrefix(String s1, String s2) {
    return s1.startsWith(s2) || s2.startsWith(s1);
  }

  private String[] split(String value, String regexp) {
    String[] parts;
    try {
      parts = value.split("\\" + regexp, -1);
    }
    catch (PatternSyntaxException e) {
      try {
        parts = value.split(regexp, -1);
      }
      catch (PatternSyntaxException e2) {
        parts = new String[]{value};
      }
    }
    return parts;
  }

  /**
   * Checks if a namespace specification covers a specified URI.
   * any namespace pattern covers only the any namespace uri. 
   * @param uri The uri to be checked.
   * @return true if the namespace pattern covers the specified uri.
   */
  public boolean covers(String uri) {
    // any namspace covers only the any namespace uri
    // no wildcard ("") requires equality between namespaces.
    if (ANY_NAMESPACE.equals(ns) || "".equals(wildcard)) {
      return ns.equals(uri);
    }
    String[] parts = split(ns, wildcard);    
    // no wildcard
    if (parts.length == 1) {
      return ns.equals(uri);
    }
    // at least one wildcard, we need to check that the start and end are the same
    // then we get to match a string against a pattern like *p1*...*pn*
    if (!uri.startsWith(parts[0])) {
      return false;
    }
    if (!uri.endsWith(parts[parts.length - 1])) {
      return false;
    }
    // Check that all remaining parts match the remaining URI.
    int start = parts[0].length();
    int end = uri.length() - parts[parts.length - 1].length();
    for (int i = 1; i < parts.length - 1; i++) {
      if (start > end) {
        return false;
      }
      int match = uri.indexOf(parts[i], start);
      if (match == -1 || match + parts[i].length() > end) {
        return false;
      }
      start = match + parts[i].length();
    }    
    return true;
  }

  /**
   * Checks for equality with another Namespace specification.
   */
  public boolean equals(Object obj) {
    if (obj instanceof NamespaceSpecification) {
      NamespaceSpecification other = (NamespaceSpecification)obj;
      return ns.equals(other.ns) && wildcard.equals(other.wildcard);
    }    
    return false;
  }
  
  /**
   * Get a hashcode for this namespace specification.
   */
  public int hashCode() {
    return (wildcard + "|" + ns).hashCode();
  }
}
