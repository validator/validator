package com.thaiopensource.validate.picl;

import org.xml.sax.Locator;

import java.util.Vector;
import java.util.Enumeration;

class KeyRefConstraint extends KeyConstraint {
  private final Pattern ref;

  static class RefSelectionHandler extends ValueSelectionHandler {
    private final KeyIndex index;

    RefSelectionHandler(KeyConstraint.KeyIndex index) {
      this.index = index;
    }

    void select(ErrorContext ec, String value) {
      KeyInfo info = index.lookupCreate(value);
      if (info.firstKeyLocator == null) {
        if (info.pendingRefLocators == null)
          info.pendingRefLocators = new Vector();
        info.pendingRefLocators.addElement(ec.saveLocator());
      }
    }

    public void selectComplete(ErrorContext ec) {
      for (Enumeration enum = index.keys(); enum.hasMoreElements();) {
        String key = (String)enum.nextElement();
        KeyInfo info = index.lookupCreate(key);
        if (info.pendingRefLocators == null)
          continue;
        for (int i = 0, len = info.pendingRefLocators.size(); i < len; i++) {
          Locator loc = (Locator)info.pendingRefLocators.elementAt(i);
          ec.error("undefined_key", key, loc);
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
    pm.registerPattern(ref, new RefSelectionHandler(index));
  }
}
