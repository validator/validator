package com.thaiopensource.validate.picl;

import org.xml.sax.Locator;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

class KeyConstraint implements Constraint {
  private final Pattern key;

  KeyConstraint(Pattern key) {
    this.key = key;
  }

  static class KeyIndex {
    private final Hashtable table;
    KeyIndex() {
      table = new Hashtable();
    }

    KeyInfo lookupCreate(Object key) {
      KeyInfo info = (KeyInfo)table.get(key);
      if (info == null) {
        info = new KeyInfo();
        table.put(key, info);
      }
      return info;
    }

    Enumeration keys() {
      return table.keys();
    }
  }

  static class KeyInfo {
    String representation;
    Locator firstKeyLocator;
    Vector pendingRefLocators;
  }

  static class KeySelectionHandler extends SelectedValueHandler {
    private final KeyIndex index;

    KeySelectionHandler(KeyIndex index) {
      this.index = index;
    }

    void select(ErrorContext ec, Locator locator, Object value, String representation) {
      KeyInfo info = index.lookupCreate(value);
      if (info.firstKeyLocator == null) {
        if (locator == null)
          locator = ec.saveLocator();
        info.firstKeyLocator = locator;
        info.pendingRefLocators = null;
        info.representation = representation;
      }
      else
        ec.error(locator, "duplicate_key", representation);
    }
  }

  public void activate(PatternManager pm) {
    activate(pm, new KeyIndex());
  }

  void activate(PatternManager pm, KeyIndex index) {
    pm.registerPattern(key, new ValueSelectionHandler(new KeySelectionHandler(index)));
  }
}
