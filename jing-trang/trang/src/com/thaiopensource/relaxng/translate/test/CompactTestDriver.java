package com.thaiopensource.relaxng.translate.test;

import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.parse.compact.CompactParseInputFormat;
import com.thaiopensource.relaxng.output.LocalOutputDirectory;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.rnc.RncOutputFormat;
import com.thaiopensource.relaxng.output.rng.RngOutputFormat;
import com.thaiopensource.relaxng.output.xsd.XsdOutputFormat;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;
import com.thaiopensource.xml.sax.Jaxp11XMLReaderCreator;
import com.thaiopensource.util.UriOrFile;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class CompactTestDriver {

  private final XMLReaderCreator xrc = new Jaxp11XMLReaderCreator();
  private ErrorHandler eh;
  private final InputFormat inputFormat = new CompactParseInputFormat();
  private OutputFormat outputFormat;
  private OutputFormat compactOutputFormat;
  private String toDir;
  private String toExt;

  private CompactTestDriver() {
  }

  static public void main(String[] args) throws IOException {
    System.exit(new CompactTestDriver().doMain(args));
  }

  private int doMain(String[] args) throws IOException {
    eh = new ErrorHandlerImpl(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[0]))));
    if (args[2].equals("xsd")) {
      outputFormat = new XsdOutputFormat();
      toExt = XSD_EXTENSION;
      toDir = XSD_DIR;
    }
    else {
      outputFormat = new RngOutputFormat();
      compactOutputFormat = new RncOutputFormat();
      toExt = XML_EXTENSION;
      toDir = XML_DIR;
    }
    return runTestSuite(new File(args[1])) ? 0 : 1;
  }

  private boolean runTestSuite(File dir) throws IOException {
    boolean passed = true;
    String[] subdirs = dir.list();
    for (int i = 0; i < subdirs.length; i++) {
      File subdir = new File(dir, subdirs[i]);
      if (subdir.isDirectory()) {
        if (!runTestCase(subdir))
          passed = false;
      }
    }
    return passed;
  }

  static private final String XML_DIR = "xml";
  static private final String XSD_DIR = "xsd";
  static private final String COMPACT_DIR = "compact";
  static private final String OUT_DIR = "out";
  static private final String CORRECT_SCHEMA_NAME = "c";
  static private final String INCORRECT_SCHEMA_NAME = "i";
  static private final String COMPACT_EXTENSION = ".rnc";
  static private final String XML_EXTENSION = ".rng";
  static private final String XSD_EXTENSION = ".xsd";
  static private final String OUTPUT_ENCODING = "UTF-8";
  static private final int LINE_LENGTH = 72;
  static private final int INDENT = 2;

  private boolean runTestCase(File dir) throws IOException {
    File xmlDir = new File(dir, toDir);
    File compactDir = new File(dir, COMPACT_DIR);
    File outputDir = new File(dir, OUT_DIR);
    File correct = new File(compactDir, CORRECT_SCHEMA_NAME + COMPACT_EXTENSION);
    File incorrect = new File(compactDir, INCORRECT_SCHEMA_NAME + COMPACT_EXTENSION);
    boolean passed = true;
    if (correct.exists()) {
      File output = new File(outputDir, CORRECT_SCHEMA_NAME + toExt);
      if (!run(correct, output, outputFormat, toExt)
          || !compareDir(xmlDir, outputDir)) {
        passed = false;
        failed(correct);
      }
      else if (toExt.equals(XML_EXTENSION)) {
        cleanDir(outputDir);
        File output2 = new File(outputDir, CORRECT_SCHEMA_NAME + COMPACT_EXTENSION);
        if (!run(correct, output2, compactOutputFormat, COMPACT_EXTENSION)
            || !run(output2, output, outputFormat, toExt)
            || !compareDir(xmlDir, outputDir)) {
          passed = false;
          failed(correct);
        }
      }
    }
    if (incorrect.exists()) {
      File output = new File(outputDir, INCORRECT_SCHEMA_NAME + toExt);
      if (run(incorrect, output, outputFormat, toExt)) {
        passed = false;
        failed(incorrect);
      }
    }
    return passed;
  }

  private boolean compareDir(File goodDir, File testDir) {
    try {
      String[] files = goodDir.list();
      for (int i = 0; i < files.length; i++) {
        File file = new File(goodDir, files[i]);
        if (file.isDirectory()) {
          if (!compareDir(file, new File(testDir, files[i])))
            return false;
        }
        else if (!Compare.compare(file, new File(testDir, files[i]), xrc))
          return false;
      }
      return true;
    }
    catch (SAXException e) {
    }
    catch (IOException e) {
    }
    return false;
  }

  private void cleanDir(File dir) {
    String[] files = dir.list();
    for (int i = 0; i < files.length; i++) {
      File file = new File(dir, files[i]);
      if (file.isDirectory())
        cleanDir(file);
      file.delete();
    }
  }

  private static void failed(File f) {
    System.err.println(f.toString() + " failed");
  }

  private boolean run(File in, File out, OutputFormat of, String outExt) throws IOException {
    try {
      SchemaCollection sc = inputFormat.load(UriOrFile.fileToUri(in), new String[0], null, eh);
      OutputDirectory od = new LocalOutputDirectory(sc.getMainUri(), out, outExt, null, LINE_LENGTH, INDENT);
      od.setEncoding(OUTPUT_ENCODING);
      of.output(sc, od, new String[0], null, eh);
      return true;
    }
    catch (SAXException e) {
      return false;
    }
    catch (InvalidParamsException e) {
      return false;
    }
    catch (InputFailedException e) {
      return false;
    }
    catch (OutputFailedException e) {
      return false;
    }
  }

}
