package com.thaiopensource.validate.nvdl;

import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.xml.util.Name;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.helpers.AttributesImpl;

/**
 * Implementation of a validator of XML documents against NVDL scripts.
 */
class ValidatorImpl extends DefaultHandler implements Validator {
  /**
   * The name for the virtual element that we use for attribute section validation.
   * It has http://purl.oclc.org/dsdl/nvdl/ns/instance/1.0 as namespace and
   * virtualElement as name.
   */
  static final Name OWNER_NAME = new Name("http://purl.oclc.org/dsdl/nvdl/ns/instance/1.0", "virtualElement");

  /**
   * A value for really no namespace, that is different than any other value 
   * for any possible namespace including no namespace which is an empty string. 
   */
  private static final String NO_NS = "\0";
  
  /**
   * The error handler.
   */
  private final ErrorHandler eh;
  
  /**
   * Properties.
   */
  private final PropertyMap properties;

  /**
   * Triggers.
   * Specifies elements that start a new section.
   */
  private final List triggers;
  
  /**
   * Source locator.
   */
  private Locator locator;
  
  /**
   * Points to the current section.
   */
  private Section currentSection;
  
  /**
   * The current namespace context, points to the last prefix mapping
   * the previous can be found on getParent and so on.
   */
  private PrefixMapping prefixMapping = null;
  
  /**
   * A hashtable that keeps a stack of validators for schemas. 
   */
  private final Hashtable validatorHandlerCache = new Hashtable();
  
  /**
   * Message localizer to report error messages from keys.
   */
  private final Localizer localizer = new Localizer(ValidatorImpl.class);
  
  /**
   * keeps the no result actions for a section to avoid duplicating them
   * as the same action can be specified by multiple programs in a section.
   */
  private final Hashset noResultActions = new Hashset();
  
  /**
   * Stores index sets for attributed for each namespace.
   */
  private final Hashtable attributeNamespaceIndexSets = new Hashtable();
  
  /**
   * Sores the index sets for attributes for each active handler.
   * The index set specifies what attributes should be given to what handlers.
   */
  private final Vector activeHandlersAttributeIndexSets = new Vector();
  
  /**
   * Attribute schemas for a namespace.
   * It is used to avoid validating twice the set of attributes 
   * from a namespace with the same schema.
   */
  private final Hashset attributeSchemas = new Hashset();
  
  /**
   * Flag indicating if we had a reject action on attributes from this namespace.
   * Useful to avoid reporting the same error multiple times.
   */
  private boolean attributeNamespaceRejected;
  
  /**
   * We use this to compute
   * only once the filtered attributes for a namespace, 
   * laysily when we will need them for the first time.
   */
  private Attributes filteredAttributes;
  
  /**
   * The start mode for this NVDL script.
   */
  private final Mode startMode;

  /**
   * Stores the element local names. Used for triggers.
   */
  private final Stack elementsLocalNameStack;
  
  /**
   * Namespace context. Alinked list of proxy namespace
   * mapping linking to parent.
   */
  static private class PrefixMapping {
    /**
     * Prefix.
     */
    final String prefix;
    /**
     * Namespace uri.
     */
    final String uri;
    /**
     * Link to parent mapping.
     */
    final PrefixMapping parent;

    /**
     * Constructor.
     * @param prefix The prefix.
     * @param uri The namespace.
     * @param parent Parent mapping.
     */
    PrefixMapping(String prefix, String uri, PrefixMapping parent) {
      this.prefix = prefix;
      this.uri = uri;
      this.parent = parent;
    }
  }

  /**
   * Store section information.
   */
  private class Section implements SectionState {
    /**
     * The parent section.
     */
    final Section parent;
    /**
     * Namespace of this section.  Empty string for absent.
     */
    final String ns;
    /**
     * Number of open elements in this section.
     */
    int depth = 0;
    /**
     * List of the Validators rooted in this section
     */
    final Vector validators = new Vector();
    final Vector schemas = new Vector();
    /**
     * List of the ContentHandlers that want to see the elements in this section
     */
    final Vector activeHandlers = new Vector();
    final Vector activeHandlersAttributeModeUsage = new Vector();
    final Vector attributeValidationModeUsages = new Vector();
    /**
     * List of Programs saying what to do with child sections
     */
    final Vector childPrograms = new Vector();
    
    /**
     * Keep the context stack if we have a context dependent section.
     */
    final Stack context = new Stack();
    /**
     * Flag indicating is this section depends on context or not.
     */
    boolean contextDependent = false;
    
    /**
     * Max attribute processing value from all modes
     * in this section.
     */
    int attributeProcessing = Mode.ATTRIBUTE_PROCESSING_NONE;

    /**
     * Stores the attach placeholder handlers.
     */
    final Vector placeholderHandlers = new Vector();
    /**
     * Stores the attach place holder mode usages.
     */
    final Vector placeholderModeUsages = new Vector();
        
    /**
     * Creates a section for a given namespace and links to to its parent section.
     * 
     * @param ns The section namespace.
     * @param parent The parent section.
     */
    Section(String ns, Section parent) {
      this.ns = ns;
      this.parent = parent;
    }

    /**
     * @param modeUsage The mode usage that determines the next mode.
     * @param handler The content handler that receives notifications.
     */
    public void addChildMode(ModeUsage modeUsage, ContentHandler handler) {
      childPrograms.addElement(new Program(modeUsage, handler));
      if (modeUsage.isContextDependent())
        contextDependent = true;
    }

    /**
     * Adds a validator.
     * @param schema The schema to validate against.
     * @param modeUsage The mode usage for this validate action.
     */
    public void addValidator(Schema schema, ModeUsage modeUsage) {
      // adds the schema to this section schemas
      schemas.addElement(schema);
      // creates the validator
      Validator validator = createValidator(schema);
      // adds the validator to this section validators
      validators.addElement(validator);
      // add the validator handler to the list of active handlers
      activeHandlers.addElement(validator.getContentHandler());
      // add the mode usage to the active handlers attribute mode usage list
      activeHandlersAttributeModeUsage.addElement(modeUsage);
      // compute the attribute processing
      attributeProcessing = Math.max(attributeProcessing,
                                     modeUsage.getAttributeProcessing());
      // add a child mode with this mode usage and the validator content handler
      childPrograms.addElement(new Program(modeUsage, validator.getContentHandler()));
      if (modeUsage.isContextDependent())
        contextDependent = true;
    }

    /**
     * Adds a handler for a mode usage.
     * @param handler The content handler to be added.
     * @param attributeModeUsage The mode usage.
     */
    public void addActiveHandler(ContentHandler handler, ModeUsage attributeModeUsage) {
      activeHandlers.addElement(handler);
      activeHandlersAttributeModeUsage.addElement(attributeModeUsage);
      attributeProcessing = Math.max(attributeProcessing,
                                     attributeModeUsage.getAttributeProcessing());
      if (attributeModeUsage.isContextDependent())
        contextDependent = true;
    }
    
    /**
     * Adds a mode usage to the attributeValidationModeUsages list
     * if we process attributes.
     */
    public void addAttributeValidationModeUsage(ModeUsage modeUsage) {
      int ap = modeUsage.getAttributeProcessing();
      if (ap != Mode.ATTRIBUTE_PROCESSING_NONE) {
        attributeValidationModeUsages.addElement(modeUsage);
        attributeProcessing = Math.max(ap, attributeProcessing);
        if (modeUsage.isContextDependent())
          contextDependent = true;
      }
    }
    
    /**
     * Reject content, report an error.
     */
    public void reject() throws SAXException {
      if (eh != null)
        eh.error(new SAXParseException(localizer.message("reject_element", ns),
                                       locator));
    }

    public void attachPlaceholder(ModeUsage modeUsage, ContentHandler handler) {
      placeholderHandlers.add(handler);
      placeholderModeUsages.add(modeUsage);
    }
    
  }

  /**
   * A program is a pair of mode usage and handler. 
   *
   */
  static private class Program {
    /**
     * The mode usage associated with the handler.
     */
    final ModeUsage modeUsage;
    
    /**
     * The handler associated with the mode usage.
     */
    final ContentHandler handler;

    /**
     * Creates an association between a mode usage and a handler.
     * @param modeUsage The mode usage.
     * @param handler The handler.
     */
    Program(ModeUsage modeUsage, ContentHandler handler) {
      this.modeUsage = modeUsage;
      this.handler = handler;
    }
  }

  /**
   * Creates a NVDL validator. The initial mode is specified by the mode parameter.
   * Initializes the current section.
   * @param mode The start mode.
   * param triggers The triggers specified by the NVDL script.
   * @param properties Validation properties.
   */
  ValidatorImpl(Mode mode, List triggers, PropertyMap properties) {
    this.properties = properties;
    this.triggers = triggers;
    this.eh = ValidateProperty.ERROR_HANDLER.get(properties);
    this.startMode = mode;
    this.elementsLocalNameStack = new Stack();
    initCurrentSection();
  }

  /**
   * Initializes the current session.
   * Creates a section for a dummy namespace (differnet of "", that is no namespace).
   * Adds as child mode usage for this a mode usage with start mode as current mode 
   * and that uses start mode. No content handler is set on addChildMode.
   *
   */
  private void initCurrentSection() {
    currentSection = new Section(NO_NS, null);
    currentSection.addChildMode(new ModeUsage(startMode, startMode), null);
  }
  
  /**
   * Set document locator callback.
   * @param locator The document locator.
   */
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  /**
   * characters callback.
   * Dispatch it to all active handlers from the current section.
   */
  public void characters(char ch[], int start, int length)
          throws SAXException {
    for (int i = 0, len = currentSection.activeHandlers.size(); i < len; i++)
      ((ContentHandler)(currentSection.activeHandlers.elementAt(i))).characters(ch, start, length);

  }

  /**
   * ignorable whitespace callback.
   * Dispatch it to all active handlers from the current section.
   */
  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    for (int i = 0, len = currentSection.activeHandlers.size(); i < len; i++)
      ((ContentHandler)(currentSection.activeHandlers.elementAt(i))).ignorableWhitespace(ch, start, length);
  }

  /**
   * startElement callback.
   * 
   * @param uri The element namespace.
   * @param localName The element local name.
   * @param qName The element qualified name.
   * @param attributes The attributes for this element.
   */
  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    
    // if we have a different namespace than the current section namespace
    // then we start a new section on the new namespace.
    if (!uri.equals(currentSection.ns))
      startSection(uri);
    else
    if (trigger(uri, localName, String.valueOf(elementsLocalNameStack.peek())))
      startSection(uri);
    
    elementsLocalNameStack.push(localName);
    // increase the depth in the current section as we have a new element
    currentSection.depth++;
    // if the current section contains context dependent mode usages then
    // we record the local elements in a stack as they form the current path
    // that determines the context
    if (currentSection.contextDependent)
      currentSection.context.push(localName);
    // check if we need to filter attributes or not
    // and process attributes, eventually validating attribute sections
    boolean transformAttributes = processAttributes(attributes);
    // iterate the active session handlers and call start element on them
    for (int i = 0, len = currentSection.activeHandlers.size(); i < len; i++) {
      ContentHandler handler = (ContentHandler)(currentSection.activeHandlers.elementAt(i));
      handler.startElement(uri, localName, qName,
                           transformAttributes
                           // if we need to filter attributes keep the ones the handler is interested in.
                           ? filterAttributes((IntSet)activeHandlersAttributeIndexSets.elementAt(i),
                                              attributes)
                           // otherwise just pass all the attributes
                           : attributes);
    }
    if (currentSection.depth==1 && currentSection.placeholderHandlers.size()>0) {
      AttributesImpl atts = new AttributesImpl();
      atts.addAttribute("", "ns", "ns", "", uri);
      atts.addAttribute("", "localName", "localName", "", localName);
      for (int i = 0, len = currentSection.placeholderHandlers.size(); i < len; i++) {
        ContentHandler handler = (ContentHandler)(currentSection.placeholderHandlers.elementAt(i));
        handler.startPrefixMapping("", "http://purl.oclc.org/dsdl/nvdl/ns/instance/1.0");
        handler.startElement("http://purl.oclc.org/dsdl/nvdl/ns/instance/1.0", "placeholder", "placeholder", atts);
      }
    }    
  }

  /**
   * Checks if a trigger matches.
   * @param ns The namespace.
   * @param name The local name.
   * @param parent The local name of the parent.
   * @return true if we have a trigger set, otherwise false.
   */
  private boolean trigger(String namespace, String name, String parent) {
    // iterate triggers
    Iterator i = triggers.iterator();
    while (i.hasNext()) {
      Trigger t = (Trigger)i.next();
      if ((t.namespace.equals(namespace) && t.elementNames.contains(name) && !t.elementNames.contains(parent))) {
        return true;
      }
    }
    return false;
  }  
  
  /**
   * Get the filtered attributes.
   * It checks if we want all the attributes and in that case returns the initial attributes,
   * otherwise creates a FilteredAttributes instance based on the index set and on the attributes.
   * @param indexSet The set with the indexes of the attributes we want to keep.
   * @param attributes The list of attributes
   * @return the attributes containing only those whose indexes are in the indexSet.
   */
  private static Attributes filterAttributes(IntSet indexSet, Attributes attributes) {
    if (indexSet.size() == attributes.getLength())
      return attributes;
    return new FilteredAttributes(indexSet, attributes);
  }

  /**
   * Processes the element attributes.
   * 
   * @param attributes The element attributes
   * @return true if we need to filter attributes when we pass them to the 
   * active handlers, false if we can just pass the initial attributes
   * to all the active content handlers
   * @throws SAXException
   */
  private boolean processAttributes(Attributes attributes) throws SAXException {
    // if no match on attributes or no attributes -> no need to filter them.
    if (currentSection.attributeProcessing == Mode.ATTRIBUTE_PROCESSING_NONE
        || attributes.getLength() == 0)
      return false;
    
    // clear the attributeNamespaceIndexSets hashtable.
    attributeNamespaceIndexSets.clear();
    // creates index sets based on namespace for the attributes
    // and places them in the attributeNamespaceIndexSets hashtable 
    for (int i = 0, len = attributes.getLength(); i < len; i++) {
      String ns = attributes.getURI(i);
      IntSet indexSet = (IntSet)attributeNamespaceIndexSets.get(ns);
      if (indexSet == null) {
        indexSet = new IntSet();
        attributeNamespaceIndexSets.put(ns, indexSet);
      }
      indexSet.add(i);
    }
    // if we need to process only qualified attributes and we have attributes 
    // only in no namespace then return false, no need to filter the attributes
    if (currentSection.attributeProcessing == Mode.ATTRIBUTE_PROCESSING_QUALIFIED
        && attributeNamespaceIndexSets.size() == 1
        && attributeNamespaceIndexSets.get("") != null)
      return false;
    // Computes the index sets for each handler
    // get the attribute modes for handlers
    Vector handlerModes = currentSection.activeHandlersAttributeModeUsage;
    // resize the index set list to the number of handlers
    activeHandlersAttributeIndexSets.setSize(handlerModes.size());
    // creates empty index sets for all handlers - initialization
    for (int i = 0, len = handlerModes.size(); i < len; i++)
      activeHandlersAttributeIndexSets.setElementAt(new IntSet(), i);
    // we hope we will not need attribute filtering, so we start with transform false.
    boolean transform = false;
    // get the list of attribute validation mode usages
    Vector validationModes = currentSection.attributeValidationModeUsages;
    // iterate on all attribute namespaces
    for (Enumeration e = attributeNamespaceIndexSets.keys(); e.hasMoreElements();) {
      String ns = (String)e.nextElement();
      // get the index set that represent the attributes in the ns namespace
      IntSet indexSet = (IntSet)attributeNamespaceIndexSets.get(ns);
      // clear attribute schemas for this namespace
      // it is used to avoid validating twice the set of attributes 
      // from this namespace with the same schema.
      attributeSchemas.clear();
      // set the filetered attributes to null - we use this to compute
      // only one the filtered attributes for this namespace, laysily when we 
      // will need them for the first time.
      filteredAttributes = null;
      // flag indicating if we had a reject action on attributes from this namespace
      // we initialize it here in the iteration on attribute namespaces
      attributeNamespaceRejected = false;
      // iterates all the handler modes and compute the index sets for all handlers
      for (int i = 0, len = handlerModes.size(); i < len; i++) {
        ModeUsage modeUsage = (ModeUsage)handlerModes.elementAt(i);
        // get the attribute actions for this mode usage, ns namespace 
        // and for the attributes in this namespace
        AttributeActionSet actions = processAttributeSection(modeUsage, ns, indexSet, attributes);
        // if we need to attach the attributes we mark that they should be passed
        // to the handler by adding them to the index set for the handler
        if (actions.getAttach())
          ((IntSet)activeHandlersAttributeIndexSets.get(i)).addAll(indexSet);
        else
        // if that attributes are not attached then we set the transform flag to 
        // true as that means we need to filter out these attributes for the current handler
          transform = true;
      }
      // iterate the attribute validation mode usages
      // and process the attribute section with the attributes
      // from the current namespace
      for (int i = 0, len = validationModes.size(); i < len; i++) {
        ModeUsage modeUsage = (ModeUsage)validationModes.elementAt(i);
        // validation means no result actions, so we are not 
        // interested in the attribute action set returned by
        // the processAttributeSection method
        processAttributeSection(modeUsage, ns, indexSet, attributes);
      }
    }
    return transform;
  }
  
  /**
   * Process an attributes section in a specific mode usage.
   * @param modeUsage The mode usage
   * @param ns The attribute section namespace
   * @param indexSet The indexes of the attributes in the given namespace
   * @param attributes All the attributes
   * @return The set of attribute actions
   * @throws SAXException
   */
  private AttributeActionSet processAttributeSection(ModeUsage modeUsage,
                                                     String ns,
                                                     IntSet indexSet,
                                                     Attributes attributes)
          throws SAXException {
    // get the next mode from the mode usage depending on context
    Mode mode = modeUsage.getMode(currentSection.context);
    // get the attribute action set
    AttributeActionSet actions = mode.getAttributeActions(ns);
    // Check if we have a reject action and if we did not reported already 
    // the reject attribute error for this namespace
    if (actions.getReject() && !attributeNamespaceRejected) {
      // set the flag to avoid reporting this error again for the same namespace
      attributeNamespaceRejected = true;
      if (eh != null)
        eh.error(new SAXParseException(localizer.message("reject_attribute", ns),
                                       locator));
    }
    // get the eventual schemas and validate the attributes against them
    Schema[] schemas = actions.getSchemas();
    for (int j = 0; j < schemas.length; j++) {
      // if we already validated against this schema, skip it
      if (attributeSchemas.contains(schemas[j]))
        continue;
      // add the schema so that we will not validate again the same attributes against it
      attributeSchemas.add(schemas[j]);
      // if we do not computed the filtered attributes for this namespace, compute them
      if (filteredAttributes == null)
        filteredAttributes = filterAttributes(indexSet, attributes);
      // validate the filtered attributes with the schema
      validateAttributes(schemas[j], filteredAttributes);
    }
    // return the actions in case they are needed further.
    return actions;
  }

  /**
   * Validates a set of attributes with an attribute schema.
   * @param schema The attributes schema.
   * @param attributes The attributes to be validated
   * @throws SAXException
   */
  private void validateAttributes(Schema schema, Attributes attributes) throws SAXException {
	// creates a validator for this attributes schema.
    Validator validator = createValidator(schema);
    // get its content handler
    ContentHandler ch = validator.getContentHandler();
    // initializes the handler with locator and proxy namespace mapping.
    initHandler(ch);
    // notifies a the wrapper element with the attributes
    ch.startElement(OWNER_NAME.getNamespaceUri(), OWNER_NAME.getLocalName(), OWNER_NAME.getLocalName(), attributes);
    ch.endElement(OWNER_NAME.getNamespaceUri(), OWNER_NAME.getLocalName(), OWNER_NAME.getLocalName());
    // removes namespaces and signals end document to the handler
    cleanupHandler(ch);
    // release the validator so further validate actions with this schema can reuse it
    releaseValidator(schema, validator);
  }

  /**
   * Start a new section on a given namespace.
   * Called from startElement when we encounter an element
   * whose namepsace does not match the current section namespace
   * or if we get an element declared as a new section trigger in the
   * NVDL script.
   * @param uri The new namespace.
   * @throws SAXException
   */
  private void startSection(String uri) throws SAXException {
    // creates a new section having the current section as parent section
    Section section = new Section(uri, currentSection);
    // get the programs of the current section
    Vector childPrograms = currentSection.childPrograms;
    // clear the current no result (validation) actions
    noResultActions.clear();
    // iterates current section programs
    for (int i = 0, len = childPrograms.size(); i < len; i++) {
      Program program = (Program)childPrograms.elementAt(i);
      // get the mode usage for the program
      // and determine the use mode from the mode usage based on the current section context
      // and then get the element actions from that determined mode
      // that apply to the new namespace
      ActionSet actions = program.modeUsage.getMode(currentSection.context).getElementActions(uri);
      // check if we have a result action attach/unwrap
      // and perform it on the program handler and the new section
      ResultAction resultAction = actions.getResultAction();
      if (resultAction != null)
        resultAction.perform(program.handler, section);
      // get the no result (validate, allow, reject) actions
      NoResultAction[] nra = actions.getNoResultActions();
      for (int j = 0; j < nra.length; j++) {
        NoResultAction tem = nra[j];
        // if we did not encountered this action already then perform it on the
        // section and add it to the noResultActions list
        if (!noResultActions.contains(tem)) {
          nra[j].perform(section);
          noResultActions.add(tem);
        }
      }
    }
    // iterate the validators on the new section and set their content
    // handler to receive notifications and set the locator,
    // call start document, and bind the current namespace context. 
    for (int i = 0, len = section.validators.size(); i < len; i++)
      initHandler(((Validator)section.validators.elementAt(i)).getContentHandler());
    // store the new section as the current section
    currentSection = section;
  }

  /**
   * Initialize a content handler. This content handler will receive the
   * document fragment starting at the current element. Therefore we need
   * to set a locator, call startDocument and give the current namespace 
   * content to that content handler.
   * @param ch The content handler.
   * @throws SAXException
   */
  private void initHandler(ContentHandler ch) throws SAXException {
    // set the locator
    if (locator != null)
      ch.setDocumentLocator(locator);
    // start the document
    ch.startDocument();
    // set the namespace context
    for (PrefixMapping pm = prefixMapping; pm != null; pm = pm.parent)
      ch.startPrefixMapping(pm.prefix, pm.uri);
  }

  /**
   * endElement callback
   * @param uri The namespace uri
   * @param localName The element local name
   * @param qName The element qualified name
   */
  public void endElement(String uri, String localName, String qName)
          throws SAXException {
	  
	  elementsLocalNameStack.pop();
    // iterate the active handlers from the current section and call
    // endElement on them
    for (int i = 0, len = currentSection.activeHandlers.size(); i < len; i++)
      ((ContentHandler)(currentSection.activeHandlers.elementAt(i))).endElement(uri, localName, qName);
    // decrease the current section depth
    currentSection.depth--;
    // if we keep context information (if the section is context dependent)
    // then remove that information
    if (currentSection.contextDependent)
      currentSection.context.pop();
    // if we have zero depth then the current section was ended, so we call endSection
    if (currentSection.depth == 0) {
      for (int i = 0, len = currentSection.placeholderHandlers.size(); i < len; i++) {
        ContentHandler handler = (ContentHandler)(currentSection.placeholderHandlers.elementAt(i));
        handler.endPrefixMapping("");
        handler.endElement("http://purl.oclc.org/dsdl/nvdl/ns/instance/1.0", "placeholder", "placeholder");
      }    
      endSection();
    }
  }

  /**
   * End a section, its depth reached zero.
   * @throws SAXException
   */
  private void endSection() throws SAXException {
    // iterate validators
    for (int i = 0, len = currentSection.validators.size(); i < len; i++) {
      Validator validator = (Validator)currentSection.validators.elementAt(i);
      // remove namespaces and call end document on each handler
      cleanupHandler(validator.getContentHandler());
      // release the validators to the cache be reused further on other sections
      releaseValidator((Schema)currentSection.schemas.elementAt(i), validator);
      // endDocument() on one of the validators may throw an exception
      // in this case we don't want to release the validator twice
      currentSection.validators.setElementAt(null, i);
    }
    // set the parent section as the current section
    currentSection = currentSection.parent;
  }

  /**
   * Cleanup a handler.
   * Remove proxy namespace mappings calling endPrefixMapping and calls also endDocument
   * to signal that the source was ended.
   * @param vh The validator content handler to clean up.
   * @throws SAXException
   */
  private void cleanupHandler(ContentHandler vh) throws SAXException {
    for (PrefixMapping pm = prefixMapping; pm != null; pm = pm.parent)
      vh.endPrefixMapping(pm.prefix);
    vh.endDocument();
  }

  /**
   * endDocument callback
   * We should be in the initial section now so no op is required.
   */
  public void endDocument()
          throws SAXException {
  }

  /**
   * start prefix mapping callback
   */
  public void startPrefixMapping(String prefix, String uri)
          throws SAXException {
    super.startPrefixMapping(prefix, uri);
    prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
  }

  /**
   * end prefix mapping callback
   */
  public void endPrefixMapping(String prefix)
          throws SAXException {
    super.endPrefixMapping(prefix);
    prefixMapping = prefixMapping.parent;
  }

  /**
   * Get a validator for a schema.
   * If we already have a validator for this schema available in cache 
   * then we will use it and remove it from cache. At the end it will be
   * added back to the cache through releaseValidator.
   * @param schema The schema we need a validaor for.
   * @return A Validator for the given schema.
   */
  private Validator createValidator(Schema schema) {
    Stack stack = (Stack)validatorHandlerCache.get(schema);
    if (stack == null) {
      stack = new Stack();
      validatorHandlerCache.put(schema, stack);
    }
    if (stack.empty())
      return schema.createValidator(properties);
    return (Validator)stack.pop();
  }

  /**
   * Releases a validator for a given schema. Put that validator in the 
   * cache so that further actions to validate against this schema will 
   * be able to use this validator instead of creating a new one.
   * @param schema The schema the validator validates against
   * @param vh The validator.
   */
  private void releaseValidator(Schema schema, Validator vh) {
    if (vh == null)
      return;
    vh.reset();
    ((Stack)validatorHandlerCache.get(schema)).push(vh);
  }

  /**
   * Reset the NVDL validator so it can be used further on
   * other sources.
   */
  public void reset() {
    // iterrate all sections from the current section up to the root.
    for (; currentSection != null; currentSection = currentSection.parent) {
      // if we have validators in this section iterate them
      for (int i = 0, len = currentSection.validators.size(); i < len; i++)
        // release the validator
        releaseValidator((Schema)currentSection.schemas.elementAt(i),
                         (Validator)currentSection.validators.elementAt(i));
    }
    // create the initial section in the start mode.
    initCurrentSection();
  }

  /**
   * Get the content handler for this NVDL validator.
   */
  public ContentHandler getContentHandler() {
    return this;
  }

  /**
   * Get the DTD handler for this NVDL validator.
   */
  public DTDHandler getDTDHandler() {
    return this;
  }
}
