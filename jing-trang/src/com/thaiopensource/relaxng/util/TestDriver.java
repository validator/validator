package com.thaiopensource.relaxng.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.util.Properties;
import java.util.MissingResourceException;
import java.util.Hashtable;
import java.net.URL;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;

import com.thaiopensource.relaxng.ValidationEngine;
import com.thaiopensource.relaxng.XMLReaderCreator;

import org.relaxng.datatype.DatatypeLibraryFactory;

class TestDriver {
  static public void main(String[] args) throws IOException {
    System.exit(new TestDriver().doMain(args));
  }

  private ValidationEngine engine;

  public int doMain(String[] args) throws IOException {
    long startTime = System.currentTimeMillis();
    ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
    engine = new ValidationEngine();
    engine.setXMLReaderCreator(Driver.createXMLReaderCreator());
    engine.setErrorHandler(eh);
    engine.setDatatypeLibraryFactory(new DatatypeLibraryLoader());
    int result = 0;
    for (int i = 0; i < args.length; i++) {
      int n = runTestSuite(new File(args[i]));
      if (n > result)
	result = n;
    }
    eh.print(eh.format("elapsed_time",
		       new Object[] { new Long(System.currentTimeMillis()
					       - startTime) }));
    return result;
  }

  private static final String CORRECT_SCHEMA_SUFFIX = ".c.rng";
  private static final String INCORRECT_SCHEMA_SUFFIX = ".i.rng";
  private static final String VALID_INSTANCE_SUFFIX = ".v.xml";
  private static final String INVALID_INSTANCE_SUFFIX = ".i.xml";
  private static final char SCHEMA_SEP = '.';
  
  public int runTestSuite(File dir) throws IOException {
    int result = 0;
    String[] files = dir.list();
    Hashtable table = new Hashtable();
    for (int i = 0; i < files.length; i++)
      table.put(files[i], files[i]);
    for (int i = 0; i < files.length; i++) {
      String f = files[i];
      int n = 0;
      if (f.endsWith(CORRECT_SCHEMA_SUFFIX)) {
	String base = f.substring(0,
				  f.length() - CORRECT_SCHEMA_SUFFIX.length());
	n = testCorrect(new File(dir, f), new File(dir, base));
      }
      else if (f.endsWith(INCORRECT_SCHEMA_SUFFIX)) {
	String base = f.substring(0,
				  f.length() - INCORRECT_SCHEMA_SUFFIX.length());
	n = testIncorrect(new File(dir, f), new File(dir, base));
      }
      else if (f.endsWith(VALID_INSTANCE_SUFFIX)) {
	String base = f.substring(0,
				  f.length() - VALID_INSTANCE_SUFFIX.length());
	int k = base.lastIndexOf(SCHEMA_SEP);
	if (k >= 0) {
	  base = base.substring(0, k);
	  String schema = base + CORRECT_SCHEMA_SUFFIX;
	  if (table.get(schema) != null)
	    n = testValid(new File(dir, schema), new File(dir, base), new File(dir, f));
	}
      }
      else if (f.endsWith(INVALID_INSTANCE_SUFFIX)) {
	String base = f.substring(0,
				  f.length() - INVALID_INSTANCE_SUFFIX.length());
	int k = base.lastIndexOf(SCHEMA_SEP);
	if (k >= 0) {
	  base = base.substring(0, k);
	  String schema = base + CORRECT_SCHEMA_SUFFIX;
	  if (table.get(schema) != null)
	    n = testInvalid(new File(dir, schema), new File(dir, base), new File(dir, f));
	}
      }
      if (n > result)
	result = n;
    }
    return result;
  }

  private int testCorrect(File schema, File dir) throws IOException {
    if (!runTest(schema, dir, null)) {
      System.err.println("Failed: " + schema.toString());
      return 1;
    }
    return 0;
  }

  private int testIncorrect(File schema, File dir) throws IOException {
    if (runTest(schema, dir, null)) {
      System.err.println("Failed: " + schema.toString());
      return 1;
    }
    return 0;

  }

  private int testValid(File schema, File dir, File instance) throws IOException {
    if (!runTest(schema, dir, instance)) {
      System.err.println("Failed: " + schema.toString() + "+" + instance.toString());
      return 1;
    }
    return 0;
  }

  private int testInvalid(File schema, File dir, File instance) throws IOException {
    if (runTest(schema, dir, instance)) {
      System.err.println("Failed: " + schema.toString() + "+" + instance.toString());
      return 1;
    }
    return 0;
  }

  private boolean runTest(File schema,
			  File dir,
			  File instance) throws IOException {
    ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
    boolean hadError = false;
    try {
      URL schemaURL = FileURL.fileToURL(schema);
      InputSource in = new InputSource(schemaURL.openStream());
      in.setSystemId(FileURL.fileToURL(dir).toString() + "/");
      if (engine.loadPattern(in)) {
	if (instance != null
	    && !engine.validate(new InputSource(FileURL.fileToURL(instance).toString())))
	  hadError = true;
      }
      else
	hadError = true;
    }
    catch (SAXException e) {
      hadError = true;
      eh.printException(e);
    }
    return !hadError;
  }

}
