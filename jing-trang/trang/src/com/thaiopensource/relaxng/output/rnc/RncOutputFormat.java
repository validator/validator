package com.thaiopensource.relaxng.output.rnc;

import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.Pattern;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class RncOutputFormat implements OutputFormat {
  public void output(SchemaCollection sc, OutputDirectory od, ErrorHandler eh)
          throws SAXException, IOException, OutputFailedException {

    try {
      ErrorReporter er = new ErrorReporter(eh, RncOutputFormat.class);
      outputPattern(sc.getMainSchema(), OutputDirectory.MAIN, od, er);
      for (Iterator iter = sc.getSchemas().entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        outputPattern((Pattern)entry.getValue(), (String)entry.getKey(), od, er);
      }
    }
    catch (ErrorReporter.WrappedSAXException e) {
      throw e.getException();
    }
  }

  private void outputPattern(Pattern p, String sourceUri, OutputDirectory od, ErrorReporter er) throws IOException {
    Output.output(p,
                  sourceUri,
                  od,
                  er);
  }

}
