package com.thaiopensource.relaxng.output.xsd;

import java.util.Set;
import java.util.HashSet;

class Guide {
  private boolean defaultGroupEnableAbstractElements;
  private final Set nonDefaultGroupSet = new HashSet();

  Guide(boolean defaultGroupEnableAbstractElements) {
    this.defaultGroupEnableAbstractElements = defaultGroupEnableAbstractElements;
  }

  void setDefaultGroupEnableAbstractElements(boolean defaultGroupEnableAbstractElements) {
    this.defaultGroupEnableAbstractElements = defaultGroupEnableAbstractElements;
  }

  void setGroupEnableAbstractElement(String name, boolean enable) {
    if (enable != defaultGroupEnableAbstractElements)
      nonDefaultGroupSet.add(name);
  }

  boolean getGroupEnableAbstractElement(String name) {
    return nonDefaultGroupSet.contains(name)
            ? !defaultGroupEnableAbstractElements
            : defaultGroupEnableAbstractElements;
  }

  boolean getDefaultGroupEnableAbstractElements() {
    return defaultGroupEnableAbstractElements;
  }
}
