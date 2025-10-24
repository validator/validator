package com.thaiopensource.validate.nvdl;

/**
 * Stores a set of element actions.
 * The actions are result actions and no result actions.
 * An action set contains only one result action and more no result actions.
 *
 */
class ActionSet {
  /**
   * The result action.
   */
  private ResultAction resultAction;
  /**
   * The no result actions.
   */
  private NoResultAction[] noResultActions = new NoResultAction[0];

  /**
   * Cancel nested actions flag.
   */
  private boolean cancelNestedActions;
  
  /**
   * Getter for the result action.
   * @return The result action.
   */
  ResultAction getResultAction() {
    return resultAction;
  }

  /**
   * Setter for the result action.
   * @param resultAction The result action.
   */
  void setResultAction(ResultAction resultAction) {
    this.resultAction = resultAction;
  }

  /**
   * Adds a no result action to the no result actions.
   * @param action The no result action.
   */
  void addNoResultAction(NoResultAction action) {
    NoResultAction[] actions = new NoResultAction[noResultActions.length + 1];
    System.arraycopy(noResultActions, 0, actions, 0, noResultActions.length);
    actions[noResultActions.length] = action;
    noResultActions = actions;
  }

  /**
   * Getter for the no result actions array.
   * @return The no result actions.
   */
  NoResultAction[] getNoResultActions() {
    return noResultActions;
  }

  /**
   * Getter for the cancel nested actions flag. 
   */
  boolean getCancelNestedActions() {
    return cancelNestedActions;
  }
  
  /**
   * Set the cancel nested actions flag.
   * @param cancelNestedActions The new value.
   */
  void setCancelNestedActions(boolean cancelNestedActions) {
    this.cancelNestedActions = cancelNestedActions;
  }
  
  
  /**
   * Gets a new ActionSet containing all the actions with the
   * current mode changed.
   * 
   * @param mode The new current mode.
   * @return A new ActionSet with actions with the current mode changed.
   */
  ActionSet changeCurrentMode(Mode mode) {
    ActionSet actions = new ActionSet();
    if (this.resultAction != null)
      actions.resultAction = this.resultAction.changeCurrentMode(mode);
    actions.noResultActions = new NoResultAction[this.noResultActions.length];
    for (int i = 0; i < actions.noResultActions.length; i++)
      actions.noResultActions[i] = this.noResultActions[i].changeCurrentMode(mode);
    return actions;
  }
}
