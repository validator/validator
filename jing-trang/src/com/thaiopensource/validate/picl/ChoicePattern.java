package com.thaiopensource.validate.picl;

class ChoicePattern extends Pattern {
  private final Pattern[] choices;

  ChoicePattern(Pattern[] choices) {
    this.choices = choices;
  }

  boolean matchesAttribute(Path path, String namespaceUri, String localName, int rootDepth) {
    for (int i = 0; i < choices.length; i++)
      if (choices[i].matchesAttribute(path, namespaceUri, localName, rootDepth))
        return true;
    return false;
  }

  boolean matchesElement(Path path, int rootDepth) {
    for (int i = 0; i < choices.length; i++)
      if (choices[i].matchesElement(path, rootDepth))
        return true;
    return false;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < choices.length; i++) {
      if (i != 0)
        buf.append('|');
      buf.append(choices[i].toString());
    }
    return buf.toString();
  }
}
