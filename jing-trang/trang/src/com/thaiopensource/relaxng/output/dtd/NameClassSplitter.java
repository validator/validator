package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.NameClass;

import java.util.List;
import java.util.Vector;

class NameClassSplitter extends AbstractVisitor {
  private List names = new Vector();

  static List split(NameClass nc) {
    NameClassSplitter splitter = new NameClassSplitter();
    nc.accept(splitter);
    return splitter.names;
  }

  private NameClassSplitter() {
  }

  public Object visitName(NameNameClass nc) {
    names.add(nc);
    return null;
  }

  public Object visitChoice(ChoiceNameClass nc) {
    List list = nc.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      ((NameClass)list.get(i)).accept(this);
    return null;
  }
}
