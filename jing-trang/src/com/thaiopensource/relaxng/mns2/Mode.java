package com.thaiopensource.relaxng.mns2;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import java.util.Hashtable;

class Mode {
  static final String ANY_NAMESPACE = "##any";
  static final int ATTRIBUTE_PROCESSING_NONE = 0;
  static final int ATTRIBUTE_PROCESSING_QUALIFIED = 1;
  static final int ATTRIBUTE_PROCESSING_FULL = 2;

  private final String name;
  private final boolean attributesSchema;
  private Mode baseMode;
  private boolean defined;
  private Locator whereDefined;
  private Locator whereUsed;
  private final Hashtable elementMap = new Hashtable();
  private final Hashtable attributeMap = new Hashtable();
  private int attributeProcessing;

  Mode(String name, boolean attributesSchema) {
    this.name = name;
    this.attributesSchema = attributesSchema;
    this.attributeProcessing = attributesSchema ? ATTRIBUTE_PROCESSING_FULL : ATTRIBUTE_PROCESSING_NONE;
  }

  String getName() {
    return name;
  }

  Mode getBaseMode() {
    return baseMode;
  }

  void setBaseMode(Mode baseMode) {
    this.baseMode = baseMode;
  }

  ActionSet getElementActions(String ns) {
    ActionSet actions = getElementActionsExplicit(ns);
    if (actions == null) {
      actions = getElementActionsExplicit(ANY_NAMESPACE);
      if (actions == null) {
        actions = new ActionSet();
        actions.addNoResultAction(new RejectAction(new ModeUsage(this)));
        elementMap.put(ANY_NAMESPACE, actions);
      }
      // this is not correct: it breaks a derived mode that use anyNamespace
      // elementMap.put(ns, actions);
    }
    return actions;
  }

  private ActionSet getElementActionsExplicit(String ns) {
    ActionSet actions = (ActionSet)elementMap.get(ns);
    if (actions == null && baseMode != null) {
      actions = baseMode.getElementActionsExplicit(ns);
      if (actions != null)
        elementMap.put(ns, actions);
    }
    return actions;
  }

  AttributeActionSet getAttributeActions(String ns) {
    AttributeActionSet actions = getAttributeActionsExplicit(ns);
    if (actions == null) {
      actions = getAttributeActionsExplicit(ANY_NAMESPACE);
      if (actions == null) {
        actions = new AttributeActionSet();
        if (attributesSchema)
          actions.setReject(true);
        else
          actions.setPass(true);
        attributeMap.put(ANY_NAMESPACE, actions);
      }
      // this is not correct: it breaks a derived mode that use anyNamespace
      // attributeMap.put(ns, actions);
    }
    return actions;
  }

  private AttributeActionSet getAttributeActionsExplicit(String ns) {
    AttributeActionSet actions = (AttributeActionSet)attributeMap.get(ns);
    if (actions == null && baseMode != null) {
      actions = baseMode.getAttributeActionsExplicit(ns);
      if (actions != null)
        attributeMap.put(ns, actions);
    }
    return actions;
  }

  int getAttributeProcessing() {
    return attributeProcessing;
  }

  Locator getWhereDefined() {
    return whereDefined;
  }

  boolean isDefined() {
    return defined;
  }

  Locator getWhereUsed() {
    return whereUsed;
  }

  void noteUsed(Locator locator) {
    if (whereUsed == null && locator != null)
      whereUsed = new LocatorImpl(locator);
  }

  void noteDefined(Locator locator) {
    defined = true;
    if (whereDefined == null && locator != null)
      whereDefined = new LocatorImpl(locator);
  }

  boolean bindElement(String ns, ActionSet actions) {
    if (elementMap.get(ns) != null)
      return false;
    elementMap.put(ns, actions);
    return true;
  }

  boolean bindAttribute(String ns, AttributeActionSet actions) {
    if (attributeMap.get(ns) != null)
      return false;
    if (attributeProcessing != ATTRIBUTE_PROCESSING_FULL
        && (!actions.getPass()
            || actions.getReject()
            || actions.getSchemas().length > 0))
      attributeProcessing = ((ns.equals("") || ns.equals(ANY_NAMESPACE))
                             ? ATTRIBUTE_PROCESSING_FULL
                             : ATTRIBUTE_PROCESSING_QUALIFIED);
    attributeMap.put(ns, actions);
    return true;
  }

}
