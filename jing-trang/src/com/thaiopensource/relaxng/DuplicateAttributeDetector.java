package com.thaiopensource.relaxng;

import java.util.Vector;

class DuplicateAttributeDetector {
  private Vector nameClasses = new Vector();
  private Alternative alternatives = null;

  private static class Alternative {
    private int startIndex;
    private int endIndex;
    private Alternative parent;

    private Alternative(int startIndex, Alternative parent) {
      this.startIndex = startIndex;
      this.endIndex = startIndex;
      this.parent = parent;
    }
  }

  boolean addAttribute(NameClass nc) {
    int lim = nameClasses.size();
    for (Alternative a = alternatives; a != null; a = a.parent) {
      for (int i = a.endIndex; i < lim; i++)
	if (overlap(nc, (NameClass)nameClasses.elementAt(i)))
	  return false;
      lim = a.startIndex;
    }
    for (int i = 0; i < lim; i++)
      if (overlap(nc, (NameClass)nameClasses.elementAt(i)))
	return false;
    nameClasses.addElement(nc);
    return true;
  }

  void startChoice() {
    alternatives = new Alternative(nameClasses.size(), alternatives);
  }

  void alternative() {
    alternatives.endIndex = nameClasses.size();
  }

  void endChoice() {
    alternatives = alternatives.parent;
  }

  private static class OverlapDetector implements NameClassVisitor {
    private NameClass nc1;
    private NameClass nc2;
    boolean overlaps = false;

    static final String IMPOSSIBLE = "\u0000";

    OverlapDetector(NameClass nc1, NameClass nc2) {
      this.nc1 = nc1;
      this.nc2 = nc2;
      nc1.accept(this);
      nc2.accept(this);      
    }

    private void probe(String namespaceURI, String localName) {
      if (nc1.contains(namespaceURI, localName)
	  && nc2.contains(namespaceURI, localName))
	overlaps = true;
    }

    boolean getOverlaps() {
      return overlaps;
    }

    public void visitChoice(NameClass nc1, NameClass nc2) {
      nc1.accept(this);
      nc2.accept(this);
    }

    public void visitNsName(String ns) {
      probe(ns, IMPOSSIBLE);
    }

    public void visitNsNameExcept(String ns, NameClass ex) {
      probe(ns, IMPOSSIBLE);
      ex.accept(this);
    }

    public void visitAnyName() {
      probe(IMPOSSIBLE, IMPOSSIBLE);
    }

    public void visitAnyNameExcept(NameClass ex) {
      probe(IMPOSSIBLE, IMPOSSIBLE);
      ex.accept(this);
    }

    public void visitName(String ns, String localName) {
      probe(ns, localName);
    }

    public void visitNull() {
    }

    public void visitError() {
    }
  }

  private static boolean overlap(NameClass nc1, NameClass nc2) {
    if (nc2 instanceof SimpleNameClass) {
      SimpleNameClass snc = (SimpleNameClass)nc2;
      return nc1.contains(snc.getNamespaceURI(), snc.getLocalName());
    }
    if (nc1 instanceof SimpleNameClass) {
      SimpleNameClass snc = (SimpleNameClass)nc1;
      return nc2.contains(snc.getNamespaceURI(), snc.getLocalName());
    }
    return new OverlapDetector(nc1, nc2).getOverlaps();
  }
}
