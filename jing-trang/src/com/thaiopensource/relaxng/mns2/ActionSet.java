package com.thaiopensource.relaxng.mns2;

class ActionSet {
  private ResultAction resultAction;
  private NoResultAction[] noResultActions = new NoResultAction[0];

  ResultAction getResultAction() {
    return resultAction;
  }

  void setResultAction(ResultAction resultAction) {
    this.resultAction = resultAction;
  }

  void addNoResultAction(NoResultAction action) {
    NoResultAction[] actions = new NoResultAction[noResultActions.length + 1];
    System.arraycopy(noResultActions, 0, actions, 0, noResultActions.length);
    actions[noResultActions.length] = action;
    noResultActions = actions;
  }

  NoResultAction[] getNoResultActions() {
    return noResultActions;
  }
}
