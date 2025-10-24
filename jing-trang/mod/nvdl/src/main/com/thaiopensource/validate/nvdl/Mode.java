package com.thaiopensource.validate.nvdl;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


class Mode {
  static final int ATTRIBUTE_PROCESSING_NONE = 0;
  static final int ATTRIBUTE_PROCESSING_QUALIFIED = 1;
  static final int ATTRIBUTE_PROCESSING_FULL = 2;
  
  /**
   * A special mode. In a mode usage this will be 
   * resolved by the mode usage to the actual current mode
   * from that mode usage.
   */
  static final Mode CURRENT = new Mode("#current", null);

  /**
   * Mode name prefix used for inline anonymous modes.
   */
  private static final String ANONYMOUS_MODE_NAME_PREFIX = "#anonymous#";
  
  /**
   * Inline anonymous modes counter.
   */
  private static int anonymousModeCounter = 0;
  
  /**
   * Flag for anonymous modes.
   */
  private boolean anonymous;
  
  /**
   * The mode name.
   */
  private final String name;
  
  /**
   * The base mode.
   */
  private Mode baseMode;
  
  /**
   * Flag indicating if this mode is defined by the user
   * or is an automatically generated mode.
   */
  private boolean defined;
  /**
   * Locate the place where this mode is defined.
   */
  private Locator whereDefined;
  
  /**
   * Locate the place this mode is first used.
   * Useful to report with location errors like 
   * 'Mode "xxx" not defined'.
   */
  private Locator whereUsed;
  private final Hashtable elementMap = new Hashtable();
  private final Hashtable attributeMap = new Hashtable();
  private int attributeProcessing = -1;

  /**
   * Namespace specification elements map.
   */
  private final Hashtable nssElementMap = new Hashtable();

  /**
   * Namespace specification attributes map.
   */
  private final Hashtable nssAttributeMap = new Hashtable();
  
  /**
   * List with included modes.
   */
  private List includedModes = new ArrayList();
  
  void addIncludedMode(Mode mode) {
    includedModes.add(mode);
  }
  
  /**
   * Creates a mode extending a base mode.
   * @param name The new mode name.
   * @param baseMode The base mode.
   */
  Mode(String name, Mode baseMode) {
    this.name = name;
    this.baseMode = baseMode;
  }

  /**
   * Creates an anonymous mode.
   * @param baseMode
   */
  public Mode(Mode baseMode) {
    this(ANONYMOUS_MODE_NAME_PREFIX+anonymousModeCounter++, baseMode);
    anonymous = true;
  }

  /**
   * Get this mode name.
   * @return The name.
   */
  String getName() {
    return name;
  }

  /**
   * Get the base mode.
   * @return The base mode.
   */
  Mode getBaseMode() {
    return baseMode;
  }

  /**
   * Set a base mode.
   * @param baseMode The new base mode.
   */
  void setBaseMode(Mode baseMode) {
    this.baseMode = baseMode;
  }

  /**
   * Get the set of element actions for a given namespace.
   * If this mode has an explicit handling of that namespace then we get those
   * actions, otherwise we get the actions for any namespace.
   * @param ns The namespace we look for element actions for.
   * @return A set of element actions.
   */
  ActionSet getElementActions(String ns) {
    ActionSet actions = getElementActionsExplicit(ns);
    if (actions == null) {
      actions = getElementActionsExplicit(NamespaceSpecification.ANY_NAMESPACE);
      // this is not correct: it breaks a derived mode that use anyNamespace
      // elementMap.put(ns, actions);
    }
    return actions;
  }

  /**
   * Look for element actions specifically specified
   * for this namespace. If the current mode does not have
   * actions for that namespace look at base modes. If the actions 
   * are defined in a base mode we need to get a copy of those actions
   * associated with this mode, so we call changeCurrentMode on them.
   * 
   * @param ns The namespace
   * @return A set of element actions.
   */
  private ActionSet getElementActionsExplicit(String ns) {
    ActionSet actions = (ActionSet)elementMap.get(ns);
    if (actions==null) {
      // iterate namespace specifications.
      for (Enumeration e = nssElementMap.keys(); e.hasMoreElements() && actions==null;) {
        NamespaceSpecification nssI = (NamespaceSpecification)e.nextElement();
        // If a namespace specification convers the current namespace URI then we get those actions.
        if (nssI.covers(ns)) {
          actions = (ActionSet)nssElementMap.get(nssI);
        }
      }
      // Store them in the element Map for faster access next time.
      if (actions!=null) {
        elementMap.put(ns, actions);
      }
    }
    // Look into the included modes
    if (actions == null && includedModes != null) {
      Iterator i = includedModes.iterator();
      while (actions == null && i.hasNext()) {
        Mode includedMode = (Mode)i.next();
        actions = includedMode.getElementActionsExplicit(ns);
      }
      if (actions != null) {
        actions = actions.changeCurrentMode(this);                    
        elementMap.put(ns, actions);
      }
    }
        
    // No actions specified, look into the base mode.
    if (actions == null && baseMode != null) {
      actions = baseMode.getElementActionsExplicit(ns);
      if (actions != null) {
        actions = actions.changeCurrentMode(this);
        elementMap.put(ns, actions);
      }
    }

    if (actions!=null && actions.getCancelNestedActions()) {
      actions = null;
    }
    
    return actions;
  }

  /**
   * Get the set of attribute actions for a given namespace.
   * If this mode has an explicit handling of that namespace then we get those
   * actions, otherwise we get the actions for any namespace.
   * @param ns The namespace we look for attribute actions for.
   * @return A set of attribute actions.
   */
  AttributeActionSet getAttributeActions(String ns) {
    AttributeActionSet actions = getAttributeActionsExplicit(ns);
    if (actions == null) {
      actions = getAttributeActionsExplicit(NamespaceSpecification.ANY_NAMESPACE);
      // this is not correct: it breaks a derived mode that use anyNamespace
      // attributeMap.put(ns, actions);
    }
    return actions;
  }

  /**
   * Look for attribute actions specifically specified
   * for this namespace. If the current mode does not have
   * actions for that namespace look at base modes. If the actions 
   * are defined in a base mode we need to get a copy of those actions
   * associated with this mode, so we call changeCurrentMode on them.
   * 
   * @param ns The namespace
   * @return A set of attribute actions.
   */
   private AttributeActionSet getAttributeActionsExplicit(String ns) {
    AttributeActionSet actions = (AttributeActionSet)attributeMap.get(ns);
    if (actions==null) {
      // iterate namespace specifications.
      for (Enumeration e = nssAttributeMap.keys(); e.hasMoreElements() && actions==null;) {
        NamespaceSpecification nssI = (NamespaceSpecification)e.nextElement();
        // If a namespace specification convers the current namespace URI then we get those actions.
        if (nssI.covers(ns)) {
          actions = (AttributeActionSet)nssAttributeMap.get(nssI);
        }
      }
      // Store them in the element Map for faster access next time.
      if (actions!=null) {
        attributeMap.put(ns, actions);
      }
    }
    // Look into the included modes
    if (actions == null && includedModes != null) {
      Iterator i = includedModes.iterator();
      while (actions == null && i.hasNext()) {
        Mode includedMode = (Mode)i.next();
        actions = includedMode.getAttributeActionsExplicit(ns);
      }
      if (actions != null) {
        attributeMap.put(ns, actions);
      }
    }
    
    if (actions == null && baseMode != null) {
      actions = baseMode.getAttributeActionsExplicit(ns);
      if (actions != null)
        attributeMap.put(ns, actions);
    }
    
    if (actions!=null && actions.getCancelNestedActions()) {
      actions = null;
    }
    return actions;
  }

  /**
   * Computes (if not already computed) the attributeProcessing
   * for this mode and returns it.
   * If it find anything different than attach then we need to perform 
   * attribute processing.
   * If only attributes for a specific namespace have actions then we only need to
   * process qualified attributes, otherwise we need to process all attributes.
   * 
   * @return The attribute processing for this mode.
   */
  int getAttributeProcessing() {
    if (attributeProcessing == -1) {
      if (baseMode != null)
        attributeProcessing = baseMode.getAttributeProcessing();
      else
        attributeProcessing = ATTRIBUTE_PROCESSING_NONE;
      for (Enumeration e = nssAttributeMap.keys(); e.hasMoreElements() && attributeProcessing != ATTRIBUTE_PROCESSING_FULL;) {
        NamespaceSpecification nss = (NamespaceSpecification)e.nextElement();
        AttributeActionSet actions = (AttributeActionSet)nssAttributeMap.get(nss);
        if (!actions.getAttach()
            || actions.getReject()
            || actions.getSchemas().length > 0)
          attributeProcessing = ((nss.ns.equals("") || nss.ns.equals(NamespaceSpecification.ANY_NAMESPACE))
                                ? ATTRIBUTE_PROCESSING_FULL
                                : ATTRIBUTE_PROCESSING_QUALIFIED);
      }
    }
    return attributeProcessing;
  }

  /**
   * Get the locator that points to the place the 
   * mode is defined.
   * @return a locator.
   */
  Locator getWhereDefined() {
    return whereDefined;
  }

  /**
   * Getter for the defined flag.
   * @return defined.
   */
  boolean isDefined() {
    return defined;
  }
  
  /**
   * Checks if a mode is anonymous.
   * @return true if anonymous.
   */
  boolean isAnonymous() {
    return anonymous;
  }

  /**
   * Get a locator pointing to the first place this mode is used.
   * @return a locator.
   */
  Locator getWhereUsed() {
    return whereUsed;
  }

  /**
   * Record the locator if this is the first location this mode is used.
   * @param locator Points to the location this mode is used from.
   */
  void noteUsed(Locator locator) {
    if (whereUsed == null && locator != null)
      whereUsed = new LocatorImpl(locator);
  }

  /**
   * Record the locator this mode is defined at.
   * @param locator Points to the mode definition.
   */
  void noteDefined(Locator locator) {
    defined = true;
    if (whereDefined == null && locator != null)
      whereDefined = new LocatorImpl(locator);
  }

  /**
   * Adds a set of element actions to be performed in this mode
   * for elements in a specified namespace.
   *  
   * @param ns The namespace pattern.
   * @param wildcard The wildcard character.
   * @param actions The set of element actions.
   * @return true if successfully added, that is the namespace was
   * not already present in the elementMap, otherwise false, the 
   * caller should signal a script error in this case.
   */
  boolean bindElement(String ns, String wildcard, ActionSet actions) {
    NamespaceSpecification nss = new NamespaceSpecification(ns, wildcard);
    if (nssElementMap.get(nss) != null)
      return false;
    for (Enumeration e = nssElementMap.keys(); e.hasMoreElements();) {
      NamespaceSpecification nssI = (NamespaceSpecification)e.nextElement();
      if (nss.compete(nssI)) {
        return false;
      }
    }
    nssElementMap.put(nss, actions);
    return true;
  }

  /**
   * Adds a set of attribute actions to be performed in this mode
   * for attributes in a specified namespace.
   *  
   * @param ns The namespace pattern.
   * @param wildcard The wildcard character.
   * @param actions The set of attribute actions.
   * @return true if successfully added, that is the namespace was
   * not already present in the attributeMap, otherwise false, the 
   * caller should signal a script error in this case.
   */
  boolean bindAttribute(String ns, String wildcard, AttributeActionSet actions) {
    NamespaceSpecification nss = new NamespaceSpecification(ns, wildcard);
    if (nssAttributeMap.get(nss) != null)
      return false;
    for (Enumeration e = nssAttributeMap.keys(); e.hasMoreElements();) {
      NamespaceSpecification nssI = (NamespaceSpecification)e.nextElement();
      if (nss.compete(nssI)) {
        return false;
      }
    }
    nssAttributeMap.put(nss, actions);
    return true;    
  }
}
