package com.thaiopensource.validate.schematron;

import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.AbstractSchemaReader;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.prop.schematron.SchematronProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import com.thaiopensource.xml.sax.DelegatingContentHandler;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import com.thaiopensource.xml.sax.ForkContentHandler;
import com.thaiopensource.xml.sax.Resolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

class SchemaReaderImpl extends AbstractSchemaReader {
  static final String SCHEMATRON_URI = "http://www.ascc.net/xml/schematron";
  private static final String LOCATION_URI = "http://www.thaiopensource.com/ns/location";
  private static final String ERROR_URI = "http://www.thaiopensource.com/ns/error";
  private final Localizer localizer = new Localizer(SchemaReaderImpl.class);

  private final Class transformerFactoryClass;
  private final Templates schematron;
  private final Schema schematronSchema;
  private static final String SCHEMATRON_SCHEMA = "schematron.rnc";
  private static final String SCHEMATRON_STYLESHEET = "schematron.xsl";
  private static final PropertyId[] supportedPropertyIds = {
    ValidateProperty.ERROR_HANDLER,
    ValidateProperty.XML_READER_CREATOR,
    ValidateProperty.ENTITY_RESOLVER,
    ValidateProperty.URI_RESOLVER,
    ValidateProperty.RESOLVER,
    SchematronProperty.DIAGNOSE,
    SchematronProperty.PHASE,
  };

  SchemaReaderImpl(SAXTransformerFactory transformerFactory) throws TransformerConfigurationException, IncorrectSchemaException {
    this.transformerFactoryClass = transformerFactory.getClass();
    String resourceName = fullResourceName(SCHEMATRON_STYLESHEET);
    StreamSource source = new StreamSource(getResourceAsStream(resourceName));
    initTransformerFactory(transformerFactory);
    schematron = transformerFactory.newTemplates(source);
    InputSource schemaSource = new InputSource(getResourceAsStream(fullResourceName(SCHEMATRON_SCHEMA)));
    PropertyMapBuilder builder = new PropertyMapBuilder();
    ValidateProperty.ERROR_HANDLER.put(builder, new DraconianErrorHandler());
    RngProperty.CHECK_ID_IDREF.add(builder);
    try {
      schematronSchema = CompactSchemaReader.getInstance().createSchema(schemaSource, builder.toPropertyMap());
    }
    catch (SAXException e) {
      throw new IncorrectSchemaException();
    }
    catch (IOException e) {
      throw new IncorrectSchemaException();
    }
  }

  public Option getOption(String uri) {
    return SchematronProperty.getOption(uri);
  }

  private void initTransformerFactory(TransformerFactory factory) {
    String name = factory.getClass().getName();
    try {
      if (name.equals("com.icl.saxon.TransformerFactoryImpl"))
        factory.setAttribute("http://icl.com/saxon/feature/linenumbering",
            Boolean.TRUE);
      else if (name.equals("net.sf.saxon.TransformerFactoryImpl")) {
        factory.setAttribute("http://saxon.sf.net/feature/linenumbering",
            Boolean.TRUE);
        factory.setAttribute("http://saxon.sf.net/feature/version-warning",
            Boolean.FALSE);
      }
      else if (name.equals("org.apache.xalan.processor.TransformerFactoryImpl")) {
        // Try both the documented URI and the URI that the code expects.
        try {
          // This is the URI that the code expects.
          factory.setAttribute("http://xml.apache.org/xalan/properties/source-location",
                               Boolean.TRUE);
        }
        catch (IllegalArgumentException e) {
          // This is the URI that's documented.
          factory.setAttribute("http://apache.org/xalan/features/source_location",
                               Boolean.TRUE);
        }
      }
    }
    catch (IllegalArgumentException e) {
    }
  }

  static class ValidateStage extends XMLReaderImpl {
    private final ContentHandler validator;
    private ContentHandler contentHandler;
    private final XMLReader reader;
    private final CountingErrorHandler ceh;

    ValidateStage(XMLReader reader, Validator validator, CountingErrorHandler ceh) {
      this.reader = reader;
      this.validator = validator.getContentHandler();
      this.ceh = ceh;
    }

    public void parse(InputSource input)
            throws SAXException, IOException {
      reader.parse(input);
      if (ceh.getHadErrorOrFatalError())
        throw new SAXException(new IncorrectSchemaException());
    }

    public void setContentHandler(ContentHandler handler) {
      this.contentHandler = handler;
      reader.setContentHandler(new ForkContentHandler(validator, contentHandler));
    }

    public ContentHandler getContentHandler() {
      return contentHandler;
    }
  }

  static class UserException extends Exception {
    private final SAXException exception;

    UserException(SAXException exception) {
      this.exception = exception;
    }

    SAXException getException() {
      return exception;
    }
  }

  static class UserWrapErrorHandler extends CountingErrorHandler {
    UserWrapErrorHandler(ErrorHandler errorHandler) {
      super(errorHandler);
    }

    public void warning(SAXParseException exception)
            throws SAXException {
      try {
        super.warning(exception);
      }
      catch (SAXException e) {
        throw new SAXException(new UserException(e));
      }
    }

    public void error(SAXParseException exception)
            throws SAXException {
      try {
        super.error(exception);
      }
      catch (SAXException e) {
        throw new SAXException(new UserException(e));
      }
    }

    public void fatalError(SAXParseException exception)
            throws SAXException {
      try {
        super.fatalError(exception);
      }
      catch (SAXException e) {
        throw new SAXException(new UserException(e));
      }
    }
  }

  static class ErrorFilter extends DelegatingContentHandler {
    private final ErrorHandler eh;
    private final Localizer localizer;
    private Locator locator;

    ErrorFilter(ContentHandler delegate, ErrorHandler eh, Localizer localizer) {
      super(delegate);
      this.eh = eh;
      this.localizer = localizer;
    }

    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
      super.setDocumentLocator(locator);
    }

    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
            throws SAXException {
      if (namespaceURI.equals(ERROR_URI) && localName.equals("error"))
        eh.error(new SAXParseException(localizer.message(atts.getValue("", "message"),
                                                         atts.getValue("", "arg")),
                                       locator));
      super.startElement(namespaceURI, localName, qName, atts);
    }
  }

  static class LocationFilter extends DelegatingContentHandler implements Locator {
    private final String systemId;
    private int lineNumber = -1;
    private SAXException exception = null;

    LocationFilter(ContentHandler delegate, String systemId) {
      super(delegate);
      this.systemId = systemId;
    }

    SAXException getException() {
      return exception;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument()
            throws SAXException {
      getDelegate().setDocumentLocator(this);
      super.startDocument();
    }

    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
            throws SAXException {
      String value = atts.getValue(LOCATION_URI, "line-number");
      if (value != null) {
        try {
          lineNumber = Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
          lineNumber = -1;
        }
      }
      else
        lineNumber = -1;
      try {
        super.startElement(namespaceURI, localName, qName, atts);
      }
      catch (SAXException e) {
        this.exception = e;
        setDelegate(null);
      }
      lineNumber = -1;
    }

    public String getPublicId() {
      return null;
    }

    public String getSystemId() {
      return systemId;
    }

    public int getLineNumber() {
      return lineNumber;
    }

    public int getColumnNumber() {
      return -1;
    }
  }

  static class TransformStage extends XMLReaderImpl {
    private ContentHandler contentHandler;
    private final Transformer transformer;
    private final SAXSource transformSource;
    private final String systemId;
    private final CountingErrorHandler ceh;
    private final Localizer localizer;

    TransformStage(Transformer transformer, SAXSource transformSource, String systemId,
                   CountingErrorHandler ceh, Localizer localizer) {
      this.transformer = transformer;
      this.transformSource = transformSource;
      this.systemId = systemId;
      this.ceh = ceh;
      this.localizer = localizer;
    }

    public void parse(InputSource input)
            throws IOException, SAXException {
      try {
        LocationFilter handler = new LocationFilter(new ErrorFilter(contentHandler, ceh, localizer),
                                                                    systemId);
        transformer.transform(transformSource, new SAXResult(handler));
        SAXException exception = handler.getException();
        if (exception != null)
          throw exception;
      }
      catch (TransformerException e) {
        if (e.getException() instanceof IOException)
          throw (IOException)e.getException();
        throw toSAXException(e);
      }
      if (ceh.getHadErrorOrFatalError())
        throw new SAXException(new IncorrectSchemaException());
    }

    public ContentHandler getContentHandler() {
      return contentHandler;
    }

    public void setContentHandler(ContentHandler contentHandler) {
      this.contentHandler = contentHandler;
    }
  }

  static class SAXErrorListener implements ErrorListener {
     private final ErrorHandler eh;
     private final String systemId;
     private boolean hadError = false;
     SAXErrorListener(ErrorHandler eh, String systemId) {
       this.eh = eh;
       this.systemId = systemId;
     }

     boolean getHadError() {
       return hadError;
     }

     public void warning(TransformerException exception)
             throws TransformerException {
       SAXParseException spe = transform(exception);
       try {
         eh.warning(spe);
       }
       catch (SAXException e) {
         throw new TransformerException(new UserException(e));
       }
     }

     public void error(TransformerException exception)
             throws TransformerException {
       hadError = true;
       SAXParseException spe = transform(exception);
       try {
         eh.error(spe);
       }
       catch (SAXException e) {
         throw new TransformerException(new UserException(e));
       }
     }

     public void fatalError(TransformerException exception)
             throws TransformerException {
       hadError = true;
       SAXParseException spe = transform(exception);
       try {
         eh.fatalError(spe);
       }
       catch (SAXException e) {
         throw new TransformerException(new UserException(e));
       }
     }

     SAXParseException transform(TransformerException exception) throws TransformerException {
       Throwable cause = exception.getException();
       // Xalan takes it upon itself to catch exceptions and pass them to the ErrorListener.
       if (cause instanceof RuntimeException)
         throw (RuntimeException)cause;
       if (cause instanceof SAXException
           || cause instanceof IncorrectSchemaException
           || cause instanceof IOException)
         throw exception;
       SourceLocator locator = exception.getLocator();
       if (locator == null)
         return new SAXParseException(exception.getMessage(), null);
       // Xalan sometimes loses the systemId; work around this.
       String s = locator.getSystemId();
       if (s == null)
        s = systemId;
       return new SAXParseException(exception.getMessage(),
                                    null,
                                    s,
                                    locator.getLineNumber(),
                                    -1);
     }
   }


  // This is an alternative approach for implementing createSchema.
  // This approach uses SAXTransformerFactory. We will stick with
  // the original for now since it works and is debugged.  Also
  // Saxon 6.5.2 prints to System.err in TemplatesHandlerImpl.getTemplates().

  private Schema createSchema2(InputSource in, PropertyMap properties)
            throws IOException, SAXException, IncorrectSchemaException {
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    CountingErrorHandler ceh = new CountingErrorHandler(eh);
    String systemId = in.getSystemId();
    try {
      SAXTransformerFactory factory = (SAXTransformerFactory)transformerFactoryClass.newInstance();
      initTransformerFactory(factory);
      TransformerHandler transformerHandler = factory.newTransformerHandler(schematron);
      // XXX set up phase and diagnose
      PropertyMapBuilder builder = new PropertyMapBuilder(properties);
      ValidateProperty.ERROR_HANDLER.put(builder, ceh);
      Validator validator = schematronSchema.createValidator(builder.toPropertyMap());
      Resolver resolver = ResolverFactory.createResolver(properties);
      XMLReader xr = resolver.createXMLReader();
      xr.setContentHandler(new ForkContentHandler(validator.getContentHandler(),
                                                  transformerHandler));
      factory.setErrorListener(new SAXErrorListener(ceh, systemId));
      TemplatesHandler templatesHandler = factory.newTemplatesHandler();
      LocationFilter stage2 = new LocationFilter(new ErrorFilter(templatesHandler, ceh, localizer), systemId);
      transformerHandler.setResult(new SAXResult(stage2));
      xr.setErrorHandler(ceh);
      xr.parse(in);
      SAXException exception = stage2.getException();
      if (exception != null)
        throw exception;
      if (ceh.getHadErrorOrFatalError())
        throw new IncorrectSchemaException();
      return new SchemaImpl(templatesHandler.getTemplates(),
                            transformerFactoryClass,
                            properties,
                            supportedPropertyIds);
    }
    catch (TransformerConfigurationException e) {
      throw new SAXException(localizer.message("unexpected_schema_creation_error"));
    }
    catch (InstantiationException e) {
      throw new SAXException(e);
    }
    catch (IllegalAccessException e) {
      throw new SAXException(e);
    }
  }

  // This implementation was written in ignorance of SAXTransformerFactory.

  public Schema createSchema(SAXSource source, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException {
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    SAXErrorListener errorListener = new SAXErrorListener(eh, source.getSystemId());
    UserWrapErrorHandler ueh1 = new UserWrapErrorHandler(eh);
    UserWrapErrorHandler ueh2 = new UserWrapErrorHandler(eh);
    try {
      PropertyMapBuilder builder = new PropertyMapBuilder(properties);
      ValidateProperty.ERROR_HANDLER.put(builder, ueh1);
      source = createValidatingSource(source, builder.toPropertyMap(), ueh1);
      source = createTransformingSource(source,
                                        SchematronProperty.PHASE.get(properties),
                                        properties.contains(SchematronProperty.DIAGNOSE),
                                        source.getSystemId(),
                                        ueh2);
      TransformerFactory transformerFactory = (TransformerFactory)transformerFactoryClass.newInstance();
      initTransformerFactory(transformerFactory);
      transformerFactory.setErrorListener(errorListener);
      Templates templates = transformerFactory.newTemplates(source);
      return new SchemaImpl(templates, transformerFactoryClass, properties, supportedPropertyIds);
    }
    catch (TransformerConfigurationException e) {
      throw toSAXException(e, errorListener.getHadError()
                              || ueh1.getHadErrorOrFatalError()
                              || ueh2.getHadErrorOrFatalError());
    }
    catch (InstantiationException e) {
      throw new SAXException(e);
    }
    catch (IllegalAccessException e) {
      throw new SAXException(e);
    }
  }

  private SAXSource createValidatingSource(SAXSource source, PropertyMap properties, CountingErrorHandler ceh) throws SAXException {
    Validator validator = schematronSchema.createValidator(properties);
    XMLReader xr = source.getXMLReader();
    if (xr == null)
      xr = ResolverFactory.createResolver(properties).createXMLReader();
    xr.setErrorHandler(ceh);
    return new SAXSource(new ValidateStage(xr, validator, ceh), source.getInputSource());
  }

  private SAXSource createTransformingSource(SAXSource in, String phase, boolean diagnose,
                                             String systemId, CountingErrorHandler ceh) throws SAXException {
    try {
      Transformer transformer = schematron.newTransformer();
      transformer.setErrorListener(new DraconianErrorListener());
      if (phase != null)
        transformer.setParameter("phase", phase);
      if (diagnose)
        transformer.setParameter("diagnose", Boolean.TRUE);
      return new SAXSource(new TransformStage(transformer, in, systemId, ceh, localizer),
                           new InputSource(systemId));
    }
    catch (TransformerConfigurationException e) {
      throw new SAXException(e);
    }
  }

  private SAXException toSAXException(TransformerException e, boolean hadError) throws IOException, IncorrectSchemaException {
      return causeToSAXException(e.getException(), hadError);
    }

  private SAXException causeToSAXException(Throwable cause, boolean hadError) throws IOException, IncorrectSchemaException {
      if (cause instanceof RuntimeException)
        throw (RuntimeException)cause;
      if (cause instanceof IOException)
        throw (IOException)cause;
      if (cause instanceof IncorrectSchemaException)
        throw (IncorrectSchemaException)cause;
      if (cause instanceof SAXException)
        return causeToSAXException(((SAXException)cause).getException(), hadError);
      if (cause instanceof TransformerException)
        return toSAXException((TransformerException)cause, hadError);
      if (cause instanceof UserException)
        return toSAXException((UserException)cause);
      if (hadError)
        throw new IncorrectSchemaException();
      return new SAXException(localizer.message("unexpected_schema_creation_error"),
                              cause instanceof Exception ? (Exception)cause : null);
    }

  private static SAXException toSAXException(UserException e) throws IOException, IncorrectSchemaException {
      SAXException se = e.getException();
      Exception cause = se.getException();
      if (cause instanceof IncorrectSchemaException)
        throw (IncorrectSchemaException)cause;
      if (cause instanceof IOException)
        throw (IOException)cause;
      return se;
    }

  private static String fullResourceName(String name) {
    String className = SchemaReaderImpl.class.getName();
    return className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resources/" + name;
  }

  private static InputStream getResourceAsStream(String resourceName) {
    ClassLoader cl = SchemaReaderImpl.class.getClassLoader();
    // XXX see if we should borrow 1.2 code from Service
    if (cl == null)
      return ClassLoader.getSystemResourceAsStream(resourceName);
    else
      return cl.getResourceAsStream(resourceName);
  }

  static SAXException toSAXException(TransformerException transformerException) {
    // Unwrap where possible
    Throwable wrapped = transformerException.getException();
    if (wrapped instanceof SAXException)
      return (SAXException)wrapped;
    if (wrapped instanceof RuntimeException)
      throw (RuntimeException)wrapped;
    if (wrapped instanceof Exception)
      return new SAXException((Exception)wrapped);
    return new SAXException(transformerException);
  }
}
