package com.thaiopensource.relaxng.util;

import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;

import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.DTDHandler;

import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.xml.sax.Sax2XMLReaderCreator;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.rng.RngProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;

/**
 * Provides a simplified API for validating XML documents against schemas.
 * This class is neither reentrant nor safe for access from multiple threads.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class ValidationEngine {
  private final XMLReaderCreator xrc;
  private XMLReader xr;
  private final CountingErrorHandler eh;
  private final SchemaReader sr;
  private final PropertyMap schemaProperties;
  private final PropertyMap instanceProperties;
  private Validator validator;
  private Schema schema;

  /**
   * Flag indicating that ID/IDREF/IDREFS should be checked.
   * @see #ValidationEngine(com.thaiopensource.xml.sax.XMLReaderCreator, ErrorHandler, int)
   */
  public static final int CHECK_ID_IDREF = 01;
  /**
   * Flag indicating that the schema is in the RELAX NG compact syntax rather than the XML syntax.
   * @see #ValidationEngine(com.thaiopensource.xml.sax.XMLReaderCreator, ErrorHandler, int)
   */
  public static final int COMPACT_SYNTAX = 02;
  public static final int FEASIBLE = 04;

  /**
   * Default constructor.  Equivalent to <code>ValidationEngine(null, null, CHECK_ID_IDREF)</code>.
   */
  public ValidationEngine() {
    this(null, null, CHECK_ID_IDREF);
  }
  /**
   * Constructs a <code>ValidationEngine</code>.
   *
   * @param xrc the <code>XMLReaderCreator</code> to be used for constructing <code>XMLReader</code>s;
   * if <code>null</code> uses <code>Sax2XMLReaderCreator</code>
   * @param eh the <code>ErrorHandler</code> to be used for reporting errors; if <code>null</code>
   * uses <code>DraconianErrorHandler</code>
   * @param flags bitwise OR of flags selected from <code>CHECK_ID_IDREF</code>, <code>COMPACT_SYNTAX</code>,
   * <code>FEASIBLE</code>, <code>MNS</code>
   * @see com.thaiopensource.xml.sax.DraconianErrorHandler
   * @see com.thaiopensource.xml.sax.Sax2XMLReaderCreator
   * @see #CHECK_ID_IDREF
   * @see #COMPACT_SYNTAX
   * @see #FEASIBLE
   */
  public ValidationEngine(XMLReaderCreator xrc,
                          ErrorHandler eh,
                          int flags) {
    PropertyMapBuilder builder = new PropertyMapBuilder();
    if (xrc == null)
      xrc = new Sax2XMLReaderCreator();
    ValidateProperty.XML_READER_CREATOR.put(builder, xrc);
    this.xrc = xrc;
    if (eh == null)
      eh = new DraconianErrorHandler();
    ValidateProperty.ERROR_HANDLER.put(builder, eh);
    this.eh = new CountingErrorHandler(eh);
    instanceProperties = new SinglePropertyMap(ValidateProperty.ERROR_HANDLER,
                                               this.eh);
    RngProperty.DATATYPE_LIBRARY_FACTORY.put(builder,
                                             new DatatypeLibraryLoader());
    if ((flags & CHECK_ID_IDREF) != 0)
      RngProperty.CHECK_ID_IDREF.add(builder);
    if ((flags & FEASIBLE) != 0)
      RngProperty.FEASIBLE.add(builder);
    schemaProperties = builder.toPropertyMap();
    if ((flags & COMPACT_SYNTAX) != 0)
      sr = CompactSchemaReader.getInstance();
    else
      sr = new AutoSchemaReader();
  }

  /**
   * Constructs a <code>ValidationEngine</code>.
   *
   * @param xrc the <code>XMLReaderCreator</code> to be used for constructing <code>XMLReader</code>s;
   * if <code>null</code> uses <code>Sax2XMLReaderCreator</code>
   * @param eh the <code>ErrorHandler</code> to be used for reporting errors; if <code>null</code>
   * uses <code>DraconianErrorHandler</code>
   * @param checkIdIdref <code>true</code> if ID/IDREF/IDREFS should be checked; <code>false</code> otherwise
   * @see com.thaiopensource.xml.sax.DraconianErrorHandler
   * @see com.thaiopensource.xml.sax.Sax2XMLReaderCreator
   * @deprecated
   */
  public ValidationEngine(XMLReaderCreator xrc,
                          ErrorHandler eh,
                          boolean checkIdIdref) {
    this(xrc, eh, checkIdIdref ? CHECK_ID_IDREF : 0);
  }

  /**
   * Constructs a <code>ValidationEngine</code>.
   *
   * @param xrc the <code>XMLReaderCreator</code> to be used for constructing <code>XMLReader</code>s;
   * if <code>null</code> uses <code>Sax2XMLReaderCreator</code>
   * @param eh the <code>ErrorHandler</code> to be used for reporting errors; if <code>null</code>
   * uses <code>DraconianErrorHandler</code>
   * @param checkIdIdref <code>true</code> if ID/IDREF/IDREFS should be checked; <code>false</code> otherwise
   * @param compactSyntax <code>true</code> if the RELAX NG compact syntax should be used to parse the schema;
   * <code>false</code> if the XML syntax should be used
   * @see com.thaiopensource.xml.sax.DraconianErrorHandler
   * @see com.thaiopensource.xml.sax.Sax2XMLReaderCreator
   * @deprecated
   */
  public ValidationEngine(XMLReaderCreator xrc, ErrorHandler eh, boolean checkIdIdref, boolean compactSyntax) {
    this(xrc,
         eh,
         (checkIdIdref ? CHECK_ID_IDREF : 0)
         | (compactSyntax ? COMPACT_SYNTAX : 0));
  }


  /**
   * @deprecated
   */
  public ValidationEngine(XMLReaderCreator xrc, ErrorHandler eh, boolean checkIdIdref, boolean compactSyntax,
                          boolean feasible) {
    this(xrc,
         eh,
         (checkIdIdref ? CHECK_ID_IDREF : 0)
         | (compactSyntax ? COMPACT_SYNTAX : 0)
         | (feasible ? FEASIBLE : 0));
  }

  /**
   * Loads a schema. Subsequent calls to <code>validate</code> will validate with
   * respect the loaded schema. This can be called more than once to allow
   * multiple documents to be validated against different schemas.
   *
   * @param in the InputSource for the schema
   * @return <code>true</code> if the schema was loaded successfully; <code>false</code> otherwise
   * @throws IOException if an I/O error occurred
   * @throws SAXException if an XMLReader or ErrorHandler threw a SAXException
   */
  public boolean loadSchema(InputSource in) throws SAXException, IOException {
    try {
      schema = sr.createSchema(in, schemaProperties);
      validator = null;
      return true;
    }
    catch (IncorrectSchemaException e) {
      return false;
    }
  }

  /**
   * Validates a document against the currently loaded schema. This can be called
   * multiple times in order to validate multiple documents.
   *
   * @param in the InputSource for the document to be validated
   * @return <code>true</code> if the document is valid; <code>false</code> otherwise
   * @throws IllegalStateException if there is no currently loaded schema
   * @throws IOException if an I/O error occurred
   * @throws SAXException if an XMLReader or ErrorHandler threw a SAXException
   */
  public boolean validate(InputSource in) throws SAXException, IOException {
    if (schema == null)
      throw new IllegalStateException("cannot validate without schema");
    if (validator == null)
      validator = schema.createValidator(instanceProperties);
    else
      validator.reset();
    if (xr == null) {
      xr = xrc.createXMLReader();
      xr.setErrorHandler(eh);
    }
    eh.reset();
    xr.setContentHandler(validator.getContentHandler());
    DTDHandler dh = validator.getDTDHandler();
    if (dh != null)
      xr.setDTDHandler(dh);
    xr.parse(in);
    return !eh.getHadErrorOrFatalError();
  }

  /**
   * Returns an <code>InputSource</code> for a filename.
   *
   * @param filename a String specifying the filename
   * @return an <code>InputSource</code> for the filename
   */
  static public InputSource fileInputSource(String filename) throws MalformedURLException {
    return fileInputSource(new File(filename));
  }

  /**
   * Returns an <code>InputSource</code> for a <code>File</code>.
   *
   * @param file the <code>File</code>
   * @return an <code>InputSource</code> for the filename
   */
  static public InputSource fileInputSource(File file) throws MalformedURLException {
    return new InputSource(UriOrFile.fileToUri(file));
  }

  /**
   * Returns an <code>InputSource</code> for a string that represents either a file
   * or an absolute URI. If the string looks like an absolute URI, it will be
   * treated as an absolute URI, otherwise it will be treated as a filename.
   *
   * @param uriOrFile a <code>String</code> representing either a file or an absolute URI
   * @return an <code>InputSource</code> for the file or absolute URI
   */
  static public InputSource uriOrFileInputSource(String uriOrFile) throws MalformedURLException {
    return new InputSource(UriOrFile.toUri(uriOrFile));
  }

}
