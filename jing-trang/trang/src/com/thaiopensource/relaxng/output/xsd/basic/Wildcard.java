package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.output.common.Name;

import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class Wildcard {
  private final boolean positive;
  private final Set namespaces;
  private final Set excludedNames;

  public Wildcard(boolean positive, Set namespaces, Set excludedNames) {
    this.positive = positive;
    this.namespaces = Collections.unmodifiableSet(namespaces);
    this.excludedNames = Collections.unmodifiableSet(excludedNames);
  }

  public boolean isPositive() {
    return positive;
  }

  public Set getNamespaces() {
    return namespaces;
  }

  public Set getExcludedNames() {
    return excludedNames;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Wildcard))
      return false;
    Wildcard other = (Wildcard)obj;
    return (this.positive == other.positive
            && this.namespaces.equals(other.namespaces)
            && this.excludedNames.equals(other.excludedNames));
  }

  public int hashCode() {
    return namespaces.hashCode() ^ excludedNames.hashCode();
  }

  public boolean contains(Name name) {
    return namespaces.contains(name.getNamespaceUri()) == positive && !excludedNames.contains(name);
  }

  public static Wildcard union(Wildcard wc1, Wildcard wc2) {
    boolean positive;
    Set namespaces = new HashSet();
    if (wc1.isPositive() && wc2.isPositive()) {
      positive = true;
      namespaces.addAll(wc1.getNamespaces());
      namespaces.addAll(wc2.getNamespaces());
    }
    else {
      positive = false;
      if (!wc1.isPositive() && !wc2.isPositive()) {
        namespaces.addAll(wc1.getNamespaces());
        namespaces.retainAll(wc2.getNamespaces());
      }
      else if (!wc1.isPositive()) {
        namespaces.addAll(wc1.getNamespaces());
        namespaces.removeAll(wc2.getNamespaces());
      }
      else {
        namespaces.addAll(wc2.getNamespaces());
        namespaces.removeAll(wc1.getNamespaces());
      }
    }
    Set excludedNames = new HashSet();
    addExcludedNames(excludedNames, wc1, wc2);
    addExcludedNames(excludedNames, wc2, wc1);
    return new Wildcard(positive, namespaces, excludedNames);
  }

  /**
   * Add to result all members of the excludedNames of wc1 that are not contained in wc2.
   */
  private static void addExcludedNames(Set result, Wildcard wc1, Wildcard wc2) {
    for (Iterator iter = wc1.getExcludedNames().iterator(); iter.hasNext();) {
      Name name = (Name)iter.next();
      if (!wc2.contains(name))
        result.add(name);
    }
  }
}
