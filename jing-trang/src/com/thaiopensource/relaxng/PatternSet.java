package com.thaiopensource.relaxng;

import java.util.Vector;

class PatternSet {
  Vector v = new Vector();

  void add(Pattern p) {
    for (int i = 0; i < v.size(); i++)
      if (v.elementAt(i) == p)
	return;
    v.addElement(p);
  }

  Pattern[] toArray() {
    int n = v.size();
    Pattern[] result = new Pattern[n];
    for (int i = 0; i < n; i++)
      result[i] = (Pattern)v.elementAt(i);
    return result;
  }
}
