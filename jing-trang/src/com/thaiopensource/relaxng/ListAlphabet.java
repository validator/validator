package com.thaiopensource.relaxng;

import java.util.Vector;
import org.relaxng.datatype.Datatype;

class ListAlphabet extends Alphabet {
  private Vector values = new Vector();
  private Datatype datatype;

  boolean isEmpty() {
    return datatype == null;
  }

  void addValue(Datatype dt, Object obj) throws RestrictionViolationException {
    if (datatype == null)
      datatype = dt;
    else if (datatype != dt)
      throw new RestrictionViolationException("interleave_inconsistent_datatype");
    values.addElement(obj);
  }

  void addData() throws RestrictionViolationException {
    throw new RestrictionViolationException("list_contains_interleave_contains_data");
  }

  void addAlphabet(Alphabet a) throws RestrictionViolationException {
    ListAlphabet la = (ListAlphabet)a;
    if (la.datatype == null)
      return;
    if (datatype == null) {
      datatype = la.datatype;
      values = la.values;
      return;
    }
    if (datatype != la.datatype)
      throw new RestrictionViolationException("interleave_inconsistent_datatype");
    for (int i = 0, n = la.values.size(); i < n; i++)
      values.addElement(la.values.elementAt(i));
  }

  void checkOverlap(Alphabet a) throws RestrictionViolationException {
    if (datatype == null)
      return;
    ListAlphabet la = (ListAlphabet)a;
    if (la.datatype == null)
      return;
    if (datatype != la.datatype)
      throw new RestrictionViolationException("interleave_inconsistent_datatype");
    for (int i = 0, m = values.size(); i < m; i++) {
      Object obj = values.elementAt(i);
      for (int j = 0, n = la.values.size(); j < n; j++)
	if (datatype.sameValue(obj, la.values.elementAt(j)))
	  throw new RestrictionViolationException("interleave_value_overlap");
    }
  }
}
