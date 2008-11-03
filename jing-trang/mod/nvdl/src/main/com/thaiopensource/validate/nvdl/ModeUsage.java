package com.thaiopensource.validate.nvdl;

import com.thaiopensource.util.Equal;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Stores mode usage information.
 */
class ModeUsage {
  /**
   * The use mode.
   */
  private final Mode mode;
  
  /**
   * The current mode used until now.
   */
  private final Mode currentMode;
  
  /**
   * Modes depending on context.
   */
  private ContextMap modeMap;
  private int attributeProcessing = -1;

  /**
   * Creates a use mode.
   * @param mode The mode to be used.
   * @param currentMode The mode used until the new mode.
   */
  ModeUsage(Mode mode, Mode currentMode) {
    this(mode, currentMode, null);
  }

  /**
   * Creates a use mode.
   * @param mode The mode to be used.
   * @param currentMode The mode used until now.
   * @param modeMap Modes to be used depending on context.
   */
  private ModeUsage(Mode mode, Mode currentMode, ContextMap modeMap) {
    this.mode = mode;
    this.currentMode = currentMode;
    this.modeMap = modeMap;
  }

  /**
   * Gets a new mode usage with a different current mode
   * but with the same mode and modeMap as this one.
   * @param currentMode The new current mode.
   * @return A new mode usage with the changed current mode.
   */
  ModeUsage changeCurrentMode(Mode currentMode) {
    return new ModeUsage(mode, currentMode, modeMap);
  }

  /**
   * Check to see if this mode usage is equals with another mode usage.
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof ModeUsage))
      return false;
    ModeUsage other = (ModeUsage)obj;
    return this.mode == other.mode && this.currentMode == other.currentMode && Equal.equal(this.modeMap, other.modeMap);
  }

  /**
   * Gets a hash code for this mode usage.
   */
  public int hashCode() {
    int hc = mode.hashCode() ^ currentMode.hashCode();
    if (modeMap != null)
      hc ^= modeMap.hashCode();
    return hc;
  }

  /**
   * Resolves the Mode.CURRENT to the currentMode for this mode usage.
   * If Mode.CURRENT is not passed as argument then the same mode is returned
   * with the exception of an anonymous mode that is not defined, when we
   * get also the current mode.
   * @param mode The mode to be resolved.
   * @return Either the current mode mode usage or the same mode passed as argument.
   */
  private Mode resolve(Mode mode) {
    if (mode == Mode.CURRENT) { 
      return currentMode;
    }
    // For an action that does not specify the useMode attribute
    // we create an anonymous next mode that becomes defined if we
    // have a nested mode element inside the action.
    // If we do not have a nested mode then the anonymous mode
    // is not defined and basically that means we should use the
    // current mode to perform that action.
    if (mode.isAnonymous() && !mode.isDefined()) {
      return currentMode;
    }
    return mode;
  }

  /**
   * Get the maximum attribute processing value from the default mode and
   * from all the modes specified in the contexts.
   * @return The attribute processing value.
   */
  int getAttributeProcessing() {
    if (attributeProcessing == -1) {
      attributeProcessing = resolve(mode).getAttributeProcessing();
      if (modeMap != null) {
        for (Enumeration e = modeMap.values();
             e.hasMoreElements()
             && attributeProcessing != Mode.ATTRIBUTE_PROCESSING_FULL;)
          attributeProcessing = Math.max(resolve((Mode)e.nextElement()).getAttributeProcessing(),
                                         attributeProcessing);
      }
    }
    return attributeProcessing;
  }

  /**
   * Check if we have context dependent modes.
   * @return true if the modeMap exists.
   */
  boolean isContextDependent() {
    return modeMap != null;
  }

  /**
   * Get the mode to be used for a specific context.
   * @param context The current context.
   * @return A mode.
   */
  Mode getMode(Vector context) {
    // first look in the modeMap if exists.
    if (modeMap != null) {
      Mode m = (Mode)modeMap.get(context);
      if (m != null)
        return resolve(m);
    }
    // if no modeMap or no context specific mode found then
    // return the default mode for this mode usage.
    return resolve(mode);
  }

  /**
   * Adds a new context (isRoot, path --> mode).
   * @param isRoot Flag indicating that the path starts or not with /
   * @param names The local names that form the path.
   * @param mode The mode for this path.
   * @return true if we do not have a duplicate path.
   */
  boolean addContext(boolean isRoot, Vector names, Mode mode) {
    if (modeMap == null)
      modeMap = new ContextMap();
    return modeMap.put(isRoot, names, mode);
  }
}
