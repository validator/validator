package com.thaiopensource.relaxng.mns2;

import com.thaiopensource.util.Equal;

import java.util.Vector;

class ModeUsage {
  private final Mode mode;
  private ContextMap modeMap;

  ModeUsage(Mode mode) {
    this.mode = mode;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof ModeUsage))
      return false;
    ModeUsage other = (ModeUsage)obj;
    return this.mode != other.mode && Equal.equal(this.modeMap, other.modeMap);
  }

  int getAttributeProcessing() {
    return mode.getAttributeProcessing();
  }

  boolean isContextDependent() {
    return modeMap != null;
  }

  Mode getMode(Vector context) {
    if (modeMap != null) {
      Mode m = (Mode)modeMap.get(context);
      if (m != null)
        return m;
    }
    return mode;
  }

  boolean addContext(boolean isRoot, Vector names, Mode mode) {
    if (modeMap == null)
      modeMap = new ContextMap();
    return modeMap.put(isRoot, names, mode);
  }
}
