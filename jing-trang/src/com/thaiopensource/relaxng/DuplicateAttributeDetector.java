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

  /*
  static class OverlapDetector implements NameClassVisitor {
    NameClass nc;
    boolean overlaps = false;

    OverlapDetector(NameClass nc) {
      this.nc = nc;
    }

    void visitChoice(NameClass nc1, NameClass nc2) {
      accept(nc1);
      accept(nc2);
    }

    void visitNsName(String ns);

    void visitNsNameExcept(String ns, NameClass ex);

    void visitAnyName() {
      overlaps = true;
    }

    void visitAnyNameExcept(NameClass ex) {
    }

    void visitName(String ns, String localName) {
      if (nc.contains(ns, localName))
	overlaps = true;
    }

    void visitNull() {
    }

    void visitError() {
    }
  }
  */
    
  static boolean overlap(NameClass nc1, NameClass nc2) {
    // new OverlapDetector(nc1).accept(nc2);
    return false;
  }
}

