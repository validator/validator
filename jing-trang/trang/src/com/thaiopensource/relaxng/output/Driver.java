package com.thaiopensource.relaxng.output;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.edit.SchemaBuilderImpl;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.output.dtd.DtdOutputFormat;
import com.thaiopensource.relaxng.output.rng.RngOutputFormat;
import com.thaiopensource.relaxng.output.xsd.XsdOutputFormat;
import com.thaiopensource.relaxng.output.rnc.RncOutputFormat;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.util.ErrorHandlerImpl;
import com.thaiopensource.relaxng.util.Jaxp11XMLReaderCreator;
import com.thaiopensource.relaxng.util.ValidationEngine;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.util.OptionParser;
import com.thaiopensource.util.Version;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

public class Driver {
  static private Localizer localizer = new Localizer(Driver.class);
  private String encoding;
  private String inputType;
  private String outputType;
  private ErrorHandlerImpl eh = new ErrorHandlerImpl();
  private static final String DEFAULT_OUTPUT_ENCODING = "UTF-8";
  private static final int DEFAULT_LINE_LENGTH = 72;

  static public void main(String[] args) throws IncorrectSchemaException, SAXException, IOException {
    System.exit(new Driver().doMain(args));
  }

  private int doMain(String[] args) throws IncorrectSchemaException, SAXException, IOException {
    try {
      OptionParser op = new OptionParser("i:o:e:", args);
      try {
        while (op.moveToNextOption()) {
          switch (op.getOptionChar()) {
          case 'e':
            encoding = op.getOptionArg();
            break;
          case 'i':
            inputType = op.getOptionArg();
            break;
          case 'o':
            outputType = op.getOptionArg();
            break;
          }
        }
      }
      catch (OptionParser.InvalidOptionException e) {
        error(localizer.message("invalid_option", op.getOptionCharString()));
        return 2;
      }
      catch (OptionParser.MissingArgumentException e) {
        error(localizer.message("option_missing_argument", op.getOptionCharString()));
        return 2;
      }
      args = op.getRemainingArgs();
      if (args.length != 2) {
        error(localizer.message("wrong_number_of_arguments"));
        eh.print(localizer.message("usage", Version.getVersion(Driver.class)));
        return 2;
      }
      InputSource in = new InputSource(UriOrFile.toUri(args[0]));
      if (encoding != null)
        in.setEncoding(encoding);
      Parseable parseable;
      if (inputType == null) {
        inputType = extension(args[0]);
        if (inputType.length() > 0)
          inputType = inputType.substring(1);
      }
      if (inputType.equalsIgnoreCase("rng"))
        parseable = new SAXParseable(new Jaxp11XMLReaderCreator(), in, eh);
      else if (inputType.equalsIgnoreCase("rnc"))
        parseable = new CompactParseable(in, eh);
      else {
        error(localizer.message("unrecognized_input_type", inputType));
        return 2;
      }
      OutputFormat of;
      String ext = extension(args[1]);
      if (outputType == null) {
        outputType = ext;
        if (outputType.length() > 0)
          outputType = outputType.substring(1);
      }
      if (outputType.equalsIgnoreCase("dtd"))
        of = new DtdOutputFormat();
      else if (outputType.equalsIgnoreCase("rng"))
        of = new RngOutputFormat();
      else if (outputType.equalsIgnoreCase("xsd"))
        of = new XsdOutputFormat();
      else if (outputType.equalsIgnoreCase("rnc"))
        of = new RncOutputFormat();
      else {
        error(localizer.message("unrecognized_output_type", outputType));
        return 2;
      }
      SchemaCollection sc = SchemaBuilderImpl.parse(parseable,
                                                    eh,
                                                    new DatatypeLibraryLoader());
      if (ext.length() == 0)
        ext = outputType;
      OutputDirectory od = new LocalOutputDirectory(new File(args[1]), ext,
                                                    encoding == null ? DEFAULT_OUTPUT_ENCODING : encoding,
                                                    DEFAULT_LINE_LENGTH);
      of.output(sc, od, eh);
      return 0;
    }
    catch (OutputFailedException e) {
    }
    catch (IncorrectSchemaException e) {
    }
    catch (IOException e) {
      eh.printException(e);
    }
    catch (SAXException e) {
      eh.printException(e);
    }
    return 1;
  }

  void error(String message) {
    eh.printException(new SAXException(message));
  }

  static private String extension(String s) {
    int dot = s.lastIndexOf(".");
    if (dot < 0)
      return "";
    return s.substring(dot);
  }
}
