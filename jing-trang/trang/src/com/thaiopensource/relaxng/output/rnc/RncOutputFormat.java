package com.thaiopensource.relaxng.output.rnc;

import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.OutputDirectoryParamProcessor;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.SchemaDocument;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class RncOutputFormat implements OutputFormat {
  public void output(SchemaCollection sc, OutputDirectory od, String[] params, String inputFormat, ErrorHandler eh)
          throws SAXException, IOException, OutputFailedException, InvalidParamsException {
    new OutputDirectoryParamProcessor(od).process(params, eh);
    try {
      ErrorReporter er = new ErrorReporter(eh, RncOutputFormat.class);
      for (Iterator iter = sc.getSchemaDocumentMap().entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        outputPattern((SchemaDocument)entry.getValue(), (String)entry.getKey(), od, er);
      }
    }
    catch (ErrorReporter.WrappedSAXException e) {
      throw e.getException();
    }
  }

  private static void outputPattern(SchemaDocument sd, String sourceUri, OutputDirectory od, ErrorReporter er) throws IOException {
    Output.output(sd.getPattern(),
                  sd.getEncoding(),
                  sourceUri,
                  od,
                  er);
  }

}
