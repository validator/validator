package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.OutputDirectoryParamProcessor;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.File;

public class DtdOutputFormat implements OutputFormat {
  public void output(SchemaCollection sc, final OutputDirectory od, String[] params, String inputFormat, ErrorHandler eh)
          throws SAXException, IOException, OutputFailedException, InvalidParamsException {
    new OutputDirectoryParamProcessor(od).process(params, eh);
    Simplifier.simplify(sc);
    try {
      ErrorReporter er = new ErrorReporter(eh, DtdOutputFormat.class);
      Analysis analysis = new Analysis(sc, er);
      if (!er.getHadError())
        DtdOutput.output(!inputFormat.equals("xml"), analysis, od, er);
      if (er.getHadError())
        throw new OutputFailedException();
    }
    catch (ErrorReporter.WrappedSAXException e) {
      throw e.getException();
    }
  }
}
