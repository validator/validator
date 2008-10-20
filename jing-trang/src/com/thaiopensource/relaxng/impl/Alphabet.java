package com.thaiopensource.relaxng.impl;

class Alphabet {
  private NameClass nameClass;

  boolean isEmpty() {
    return nameClass == null;
  }

  void addElement(NameClass nc) {
    if (nameClass == null)
      nameClass = nc;
    else if (nc != null)
      nameClass = new ChoiceNameClass(nameClass, nc);
  }

  void addAlphabet(Alphabet a) {
    addElement(a.nameClass);
  }

  void checkOverlap(Alphabet a) throws RestrictionViolationException {
    if (nameClass != null && a.nameClass != null) {
      NameClass overlapExample = OverlapDetector.getOverlapExample(nameClass, a.nameClass);
      if (overlapExample != null) {
        if (overlapExample instanceof SimpleNameClass)
          throw new RestrictionViolationException("interleave_element_overlap_name",
                                                  ((SimpleNameClass)overlapExample).getName());
        if (overlapExample instanceof NsNameClass)
          throw new RestrictionViolationException("interleave_element_overlap_ns",
                                                  ((NsNameClass)overlapExample).getNamespaceUri());
        throw new RestrictionViolationException("interleave_element_overlap");
      }
    }

  }
}
