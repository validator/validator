package com.thaiopensource.validate.picl;

import java.util.Vector;

class PatternBuilder {
  static final byte CHILD = 0;
  static final byte ATTRIBUTE = 1;

  private boolean hadDescendantOrSelf = false;
  private final Vector choices = new Vector();
  private final Vector names = new Vector();
  private final Vector descendantsOrSelf = new Vector();
  private static final int NO_ATTRIBUTE = 0;
  private static final int LAST_WAS_ATTRIBUTE = 1;
  private static final int NON_LEAF_ATTRIBUTE = 2;
  private int attributeType = NO_ATTRIBUTE;

  void addName(byte type, String namespaceUri, String localName) {
    descendantsOrSelf.addElement(Boolean.valueOf(hadDescendantOrSelf));
    hadDescendantOrSelf = false;
    names.addElement(namespaceUri);
    names.addElement(localName);
    switch (attributeType) {
    case LAST_WAS_ATTRIBUTE:
      attributeType = NON_LEAF_ATTRIBUTE;
      break;
    case NO_ATTRIBUTE:
      if (type == ATTRIBUTE)
        attributeType = LAST_WAS_ATTRIBUTE;
      break;
    }
  }

  void addAnyName(byte type) {
    addName(type, PathPattern.ANY, PathPattern.ANY);
  }

  void addNsName(byte type, String namespaceUri) {
    addName(type, namespaceUri, PathPattern.ANY);
  }

  void addDescendantsOrSelf() {
    if (attributeType == NO_ATTRIBUTE)
      hadDescendantOrSelf = true;
  }

  private PathPattern wrapUpAlternative() {
    PathPattern result;
    if (attributeType == NON_LEAF_ATTRIBUTE)
      result = null;
    else {
      String[] namesArray = new String[names.size()];
      for (int i = 0; i < namesArray.length; i++)
        namesArray[i] = (String)names.elementAt(i);
      boolean[] descendantsOrSelfArray = new boolean[descendantsOrSelf.size() + 1];
      for (int i = 0; i < descendantsOrSelfArray.length - 1; i++)
        descendantsOrSelfArray[i] = ((Boolean)descendantsOrSelf.elementAt(i)).booleanValue();
      descendantsOrSelfArray[descendantsOrSelfArray.length - 1] = hadDescendantOrSelf;
      if (attributeType == NO_ATTRIBUTE)
        result = new ElementPathPattern(namesArray, descendantsOrSelfArray);
      else
        result = new AttributePathPattern(namesArray, descendantsOrSelfArray);
    }
    cleanupAlternative();
    return result;
  }

  private void cleanupAlternative() {
    attributeType = NO_ATTRIBUTE;
    hadDescendantOrSelf = false;
    names.setSize(0);
    descendantsOrSelf.setSize(0);
  }

  void cleanup() {
    cleanupAlternative();
    choices.setSize(0);
  }

  void alternative() {
    Pattern pattern = wrapUpAlternative();
    if (pattern != null)
      choices.addElement(pattern);
  }

  Pattern createPattern() {
    Pattern pattern = wrapUpAlternative();
    if (choices.size() == 0) {
      if (pattern == null)
        return new NotAllowedPattern();
      return pattern;
    }
    else {
      if (pattern != null)
        choices.addElement(pattern);
      Pattern[] patterns = new Pattern[choices.size()];
      for (int i = 0; i < patterns.length; i++)
        patterns[i] = (Pattern)choices.elementAt(i);
      return new ChoicePattern(patterns);
    }
  }
}
