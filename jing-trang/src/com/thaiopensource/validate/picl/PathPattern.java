package com.thaiopensource.validate.picl;

abstract class PathPattern extends Pattern {
  protected final String[] names;
  private final boolean[] descendantsOrSelf;
  static String ANY = "#any";

  PathPattern(String[] names, boolean[] descendantsOrSelf) {
    this.names = names;
    this.descendantsOrSelf = descendantsOrSelf;
  }

  boolean matchSegment(Path path, int pathStartIndex, int pathLength,
                       int patternStartIndex, int patternLength,
                       boolean ignoreRightmostDescendantsOrSelf) {
    if (patternLength > pathLength)
      return false;
    while (patternLength > 0
           && (ignoreRightmostDescendantsOrSelf
               || !descendantsOrSelf[patternStartIndex + patternLength])) {
      if (!matchStep(path, pathStartIndex + pathLength - 1, patternStartIndex + patternLength - 1))
        return false;
      pathLength--;
      patternLength--;
      ignoreRightmostDescendantsOrSelf = false;
    }
    while (patternLength > 0 && !descendantsOrSelf[patternStartIndex]) {
      if (!matchStep(path, pathStartIndex, patternStartIndex))
        return false;
      pathStartIndex++;
      patternStartIndex++;
      pathLength--;
      patternLength--;
    }
    if (patternLength == 0)
      return descendantsOrSelf[patternStartIndex] || pathLength == 0;
    for (pathLength--; pathLength >= patternLength; pathLength--)
      if (matchSegment(path, pathStartIndex, pathLength, patternStartIndex, patternLength, true))
        return true;
    return false;
  }

  private boolean matchStep(Path path, int pathIndex, int patternIndex) {
    patternIndex *= 2;
    return (ElementPathPattern.matchName(path.getNamespace(pathIndex), names[patternIndex])
            && ElementPathPattern.matchName(path.getLocalName(pathIndex), names[patternIndex + 1]));
  }

  static boolean matchName(String str, String pattern) {
    if (pattern == ElementPathPattern.ANY)
      return true;
    return str.equals(pattern);
  }

  String toString(boolean isAttribute) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0, j = 0; i < names.length; i += 2, j++) {
      if (j != 0)
        buf.append(descendantsOrSelf[j] ? "//" : "/");
      else if (descendantsOrSelf[0])
        buf.append(".//");
      if (isAttribute && i + 2 == names.length)
        buf.append('@');
      if (names[i] == ANY)
        buf.append('*');
      else {
        if (names[i].length() != 0) {
          buf.append('{');
          buf.append(names[i]);
          buf.append('}');
        }
        buf.append(names[i + 1] == ANY ? "*" : names[i + 1]);
      }
    }
    if (names.length == 0)
      buf.append(descendantsOrSelf[0] ? ".//." : ".");
    else if (descendantsOrSelf[descendantsOrSelf.length - 1])
      buf.append("//.");
    return buf.toString();
  }
}
