package com.thaiopensource.relaxng;

class ElementAlphabet extends Alphabet {
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
    addElement(((ElementAlphabet)a).nameClass);
  }

  void checkOverlap(Alphabet a) throws RestrictionViolationException {
    if (nameClass != null
	&& ((ElementAlphabet)a).nameClass != null
	&& OverlapDetector.overlap(nameClass, ((ElementAlphabet)a).nameClass))
      throw new RestrictionViolationException("interleave_element_overlap");
  }
}
