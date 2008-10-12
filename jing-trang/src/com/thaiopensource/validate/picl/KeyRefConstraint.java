package com.thaiopensource.validate.picl;

import org.xml.sax.Locator;

import java.util.Vector;
import java.util.Enumeration;

class KeyRefConstraint extends KeyConstraint {
  private final Pattern ref;

  static class RefSelectionHandler extends SelectedValueHandler {
    private final KeyIndex index;

    RefSelectionHandler(KeyConstraint.KeyIndex index) {
      this.index = index;
    }

    void select(ErrorContext ec, Locator locator, Object value, String representation) {
      KeyInfo info = index.lookupCreate(value);
      if (info.firstKeyLocator == null) {
        if (info.pendingRefLocators == null)
          info.pendingRefLocators = new Vector();
        if (locator == null)
          locator = ec.saveLocator();
        info.pendingRefLocators.addElement(locator);
      }
      if (info.representation == null)
        info.representation = representation;
    }

    public void selectComplete(ErrorContext ec) {
      for (Enumeration e = index.keys(); e.hasMoreElements();) {
        Object key = e.nextElement();
        KeyInfo info = index.lookupCreate(key);
        if (info.pendingRefLocators == null)
          continue;
        for (int i = 0, len = info.pendingRefLocators.size(); i < len; i++) {
          Locator loc = (Locator)info.pendingRefLocators.elementAt(i);
          ec.error(loc, "undefined_key", info.representation);
        }
      }
    }
  }

  KeyRefConstraint(Pattern key, Pattern ref) {
    super(key);
    this.ref = ref;
  }

  void activate(PatternManager pm, KeyIndex index) {
    super.activate(pm, index);
    pm.registerPattern(ref, new ValueSelectionHandler(new RefSelectionHandler(index)));
  }
}
