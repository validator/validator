package com.thaiopensource.relaxng.util;

import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;

import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaFactory;
import com.thaiopensource.relaxng.ValidatorHandler;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.mns.MnsSchemaFactory;
import com.thaiopensource.util.UriOrFile;

/**
 * Provides a simplified API for validating XML documents against RELAX NG schemas.
 * This class is neither reentrant nor safe for access from multiple threads.
 *
 * @see SchemaFactory
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class ValidationEngine {
  private final XMLReaderCreator xrc;
  private XMLReader xr;
  private final ErrorHandler eh;
  private final SchemaFactory factory;
  private ValidatorHandler vh;
  private Schema schema;

  /**
   * Flag indicating that ID/IDREF/IDREFS should be checked.
   * @see #ValidationEngine(XMLReaderCreator, ErrorHandler, int)
   */
  public static final int CHECK_ID_IDREF = 01;
  /**
   * Flag indicating that the schema is in the compact syntax rather than the XML syntax.
   * @see #ValidationEngine(XMLReaderCreator, ErrorHandler, int)
   */
  public static final int COMPACT_SYNTAX = 02;
  public static final int FEASIBLE = 04;
  public static final int MNS = 010;

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
   * @see DraconianErrorHandler
   * @see Sax2XMLReaderCreator
   * @see #CHECK_ID_IDREF
   * @see #COMPACT_SYNTAX
   * @see #FEASIBLE
   * @see #MNS
   */
  public ValidationEngine(XMLReaderCreator xrc,
                          ErrorHandler eh,
                          int flags) {
    if (xrc == null)
      xrc = new Sax2XMLReaderCreator();
    if (eh == null)
      eh = new DraconianErrorHandler();
    if ((flags & MNS) != 0)
      factory = new MnsSchemaFactory();
    else
      factory = new SchemaFactory();
    factory.setDatatypeLibraryFactory(new DatatypeLibraryLoader());
    this.xrc = xrc;
    factory.setXMLReaderCreator(xrc);
    this.eh = eh;
    factory.setErrorHandler(eh);
    factory.setCheckIdIdref((flags & CHECK_ID_IDREF) != 0);
    factory.setCompactSyntax((flags & COMPACT_SYNTAX) != 0);
    factory.setFeasible((flags & FEASIBLE) != 0);
  }

  /**
   * Constructs a <code>ValidationEngine</code>.
   *
   * @param xrc the <code>XMLReaderCreator</code> to be used for constructing <code>XMLReader</code>s;
   * if <code>null</code> uses <code>Sax2XMLReaderCreator</code>
   * @param eh the <code>ErrorHandler</code> to be used for reporting errors; if <code>null</code>
   * uses <code>DraconianErrorHandler</code>
   * @param checkIdIdref <code>true</code> if ID/IDREF/IDREFS should be checked; <code>false</code> otherwise
   * @see DraconianErrorHandler
   * @see Sax2XMLReaderCreator
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
   * @param compactSyntax <code>true</code> if the compact syntax should be used to parse the schema;
   * <code>false</code> if the XML syntax should be used
   * @see DraconianErrorHandler
   * @see Sax2XMLReaderCreator
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
      schema = factory.createSchema(in);
      vh = null;
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
    if (vh == null)
      vh = schema.createValidator(eh);
    else
      vh.reset();
    if (xr == null) {
      xr = xrc.createXMLReader();
      if (eh != null)
        xr.setErrorHandler(eh);
    }
    xr.setContentHandler(vh);
    xr.setDTDHandler(vh);
    xr.parse(in);
    return vh.isValidSoFar();
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
