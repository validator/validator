package com.thaiopensource.validate.nvdl;

/**
 * Base action class. 
 */
abstract class Action {
  /**
   * Use mode when performing this action.
   */
  private final ModeUsage modeUsage;

  /**
   * Creates an action with a given mode usage.
   * @param modeUsage The mode usage.
   */
   Action(ModeUsage modeUsage) {
     this.modeUsage = modeUsage;
   }

   /**
    * Getter for the mode usage.
    * @return The mode usage for this action.
    */
   ModeUsage getModeUsage() {
     return modeUsage;
   }

   /**
    * Checks for equality, we need to have the same action class with the same modeUsage.
    */
   public boolean equals(Object obj) {
     return obj != null && obj.getClass() == getClass() && ((Action)obj).modeUsage.equals(modeUsage);
   }

   /**
    * Computes a hashCode for this action.
    */
  public int hashCode() {
    return getClass().hashCode() ^ modeUsage.hashCode();
  }
}
