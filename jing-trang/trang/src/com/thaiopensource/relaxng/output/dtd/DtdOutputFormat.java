package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.File;

public class DtdOutputFormat implements OutputFormat {
  public void output(SchemaCollection sc, OutputDirectory od, ErrorHandler eh)
          throws SAXException, IOException, OutputFailedException {
    Simplifier.simplify(sc);
    try {
      ErrorReporter er = new ErrorReporter(eh, DtdOutputFormat.class);
      Analysis analysis = new Analysis(sc, er);
      if (!er.getHadError())
        DtdOutput.output(analysis, od, er);
      if (er.getHadError())
        throw new OutputFailedException();
    }
    catch (ErrorReporter.WrappedSAXException e) {
      throw e.getException();
    }
  }
}
