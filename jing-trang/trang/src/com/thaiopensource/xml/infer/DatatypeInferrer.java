package com.thaiopensource.xml.infer;

import com.thaiopensource.relaxng.output.common.Name;

class DatatypeInferrer {
  private final DatatypeRepertoire.Type[] possibleTypes;
  private int nTypes;
  private int typicalMask = 0;
  private final String uri;
  private boolean allWhiteSpace = true;

  DatatypeInferrer(DatatypeRepertoire datatypes, String value) {
    uri = datatypes.getUri();
    possibleTypes = new DatatypeRepertoire.Type[datatypes.size()];
    for (int i = 0; i < possibleTypes.length; i++)
      possibleTypes[i] = datatypes.get(i);
    nTypes = possibleTypes.length;
    addValue(value);
  }

  public void addValue(String value) {
    int nDeleted = 0;
    for (int i = 0; i < nTypes; i++) {
      if (!possibleTypes[i].matches(value))
        nDeleted++;
      else {
        if (possibleTypes[i].isTypical(value))
          typicalMask |= 1 << possibleTypes[i].getIndex();
        if (nDeleted > 0) {
          possibleTypes[i - nDeleted] = possibleTypes[i];
          possibleTypes[i] = null;
        }
      }
    }
    nTypes -= nDeleted;
    if (!isWhiteSpace(value))
      allWhiteSpace = false;
  }

  static boolean isWhiteSpace(String value) {
    for (int i = 0; i < value.length(); i++)
      switch (value.charAt(i)) {
        case ' ':
        case '\t':
        case '\n':
        case '\r':
          break;
        default:
          return false;
      }
    return true;
  }

  public Name getTypeName() {
    for (int i = 0; i < nTypes; i++)
      if (((1 << possibleTypes[i].getIndex()) & typicalMask) != 0)
        return new Name(uri, possibleTypes[i].getName());
    return null;
  }

  public boolean isAllWhiteSpace() {
    return allWhiteSpace;
  }
}
