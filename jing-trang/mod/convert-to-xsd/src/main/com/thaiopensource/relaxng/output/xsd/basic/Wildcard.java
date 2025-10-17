package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.xml.util.Name;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Wildcard {
  private final boolean positive;
  private final Set<String> namespaces;
  private final Set<Name> excludedNames;

  public Wildcard(boolean positive, Set<String> namespaces, Set<Name> excludedNames) {
    this.positive = positive;
    this.namespaces = Collections.unmodifiableSet(namespaces);
    this.excludedNames = Collections.unmodifiableSet(excludedNames);
  }

  public boolean isPositive() {
    return positive;
  }

  public Set<String> getNamespaces() {
    return namespaces;
  }

  public Set<Name> getExcludedNames() {
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
    Set<String> namespaces = new HashSet<String>();
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
    Set<Name> excludedNames = new HashSet<Name>();
    addExcludedNames(excludedNames, wc1, wc2);
    addExcludedNames(excludedNames, wc2, wc1);
    return new Wildcard(positive, namespaces, excludedNames);
  }

  /**
   * Add to result all members of the excludedNames of wc1 that are not contained in wc2.
   */
  private static void addExcludedNames(Set<Name> result, Wildcard wc1, Wildcard wc2) {
    for (Name name : wc1.getExcludedNames()) {
      if (!wc2.contains(name))
        result.add(name);
    }
  }
}
