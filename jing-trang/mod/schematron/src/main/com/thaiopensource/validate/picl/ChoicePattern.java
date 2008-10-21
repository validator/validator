package com.thaiopensource.validate.picl;

class ChoicePattern extends Pattern {
  private final Pattern[] choices;

  ChoicePattern(Pattern[] choices) {
    this.choices = choices;
  }

  boolean matches(Path path, int rootDepth) {
    for (int i = 0; i < choices.length; i++)
      if (choices[i].matches(path, rootDepth))
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
