package com.thaiopensource.relaxng.translate;

import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.MultiInputFormat;
import com.thaiopensource.relaxng.input.xml.XmlInputFormat;
import com.thaiopensource.relaxng.input.dtd.DtdInputFormat;
import com.thaiopensource.relaxng.input.parse.compact.CompactParseInputFormat;
import com.thaiopensource.relaxng.input.parse.sax.SAXParseInputFormat;
import com.thaiopensource.relaxng.output.LocalOutputDirectory;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.dtd.DtdOutputFormat;
import com.thaiopensource.relaxng.output.rnc.RncOutputFormat;
import com.thaiopensource.relaxng.output.rng.RngOutputFormat;
import com.thaiopensource.relaxng.output.xsd.XsdOutputFormat;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.OptionParser;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.util.Version;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class Driver {
  static private final Localizer localizer = new Localizer(Driver.class);
  private String inputType;
  private String outputType;
  private final ErrorHandlerImpl eh = new ErrorHandlerImpl();
  private static final String DEFAULT_OUTPUT_ENCODING = "UTF-8";
  private static final int DEFAULT_LINE_LENGTH = 72;
  private static final int DEFAULT_INDENT = 2;

  static public void main(String[] args) {
    System.exit(new Driver().doMain(args));
  }

  private int doMain(String[] args) {
    List inputParams = new Vector();
    List outputParams = new Vector();
    try {
      OptionParser op = new OptionParser("I:O:i:o:", args);
      try {
        while (op.moveToNextOption()) {
          switch (op.getOptionChar()) {
          case 'I':
            inputType = op.getOptionArg();
            break;
          case 'O':
            outputType = op.getOptionArg();
            break;
          case 'i':
            inputParams.add(op.getOptionArg());
            break;
          case 'o':
            outputParams.add(op.getOptionArg());
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
      if (args.length < 2) {
        error(localizer.message("too_few_arguments"));
        eh.print(localizer.message("usage", Version.getVersion(Driver.class)));
        return 2;
      }
      if (inputType == null) {
        inputType = extension(args[0]);
        if (inputType.length() > 0)
          inputType = inputType.substring(1);
      }
      InputFormat inFormat;
      if (inputType.equalsIgnoreCase("rng"))
        inFormat = new SAXParseInputFormat();
      else if (inputType.equalsIgnoreCase("rnc"))
        inFormat = new CompactParseInputFormat();
      else if (inputType.equalsIgnoreCase("dtd"))
        inFormat = new DtdInputFormat();
      else if (inputType.equalsIgnoreCase("xml"))
        inFormat = new XmlInputFormat();
      else {
        error(localizer.message("unrecognized_input_type", inputType));
        return 2;
      }
      OutputFormat of;
      String ext = extension(args[args.length - 1]);
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
      String[] inputParamArray = (String[])inputParams.toArray(new String[0]);
      outputType = outputType.toLowerCase();
      SchemaCollection sc;
      if (args.length > 2) {
        if (!(inFormat instanceof MultiInputFormat)) {
          error(localizer.message("too_many_arguments"));
          return 2;
        }
        String[] uris = new String[args.length - 1];
        for (int i = 0; i < uris.length; i++)
          uris[i] = UriOrFile.toUri(args[i]);
        sc = ((MultiInputFormat)inFormat).load(uris, inputParamArray, outputType, eh);
      }
      else
        sc = inFormat.load(UriOrFile.toUri(args[0]), inputParamArray, outputType, eh);
      if (ext.length() == 0)
        ext = outputType;
      OutputDirectory od = new LocalOutputDirectory(sc.getMainUri(),
                                                    new File(args[args.length - 1]),
                                                    ext,
                                                    DEFAULT_OUTPUT_ENCODING,
                                                    DEFAULT_LINE_LENGTH,
                                                    DEFAULT_INDENT);
      of.output(sc, od, (String[])outputParams.toArray(new String[0]), inputType.toLowerCase(), eh);
      return 0;
    }
    catch (OutputFailedException e) {
    }
    catch (InputFailedException e) {
    }
    catch (InvalidParamsException e) {
    }
    catch (IOException e) {
      eh.printException(e);
    }
    catch (SAXException e) {
      eh.printException(e);
    }
    return 1;
  }

  private void error(String message) {
    eh.printException(new SAXException(message));
  }

  static private String extension(String s) {
    int dot = s.lastIndexOf(".");
    if (dot < 0)
      return "";
    return s.substring(dot);
  }
}
