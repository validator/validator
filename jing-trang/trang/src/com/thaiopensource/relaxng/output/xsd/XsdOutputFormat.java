package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;

public class XsdOutputFormat implements OutputFormat {
  public void output(SchemaCollection sc, OutputDirectory od, ErrorHandler eh) throws SAXException, IOException, OutputFailedException {
    try {
      ErrorReporter er = new ErrorReporter(eh, XsdOutputFormat.class);
      SchemaInfo si = new SchemaInfo(sc, er);
      if (!er.getHadError()) {
        RefChecker.check(si, er);
        if (!er.getHadError()) {
          RestrictionsChecker.check(si, er);
          if (!er.getHadError()) {
            Schema schema = BasicBuilder.buildBasicSchema(si, er);
            if (!er.getHadError()) {
              new Transformer(schema, er).transform();
              if (!er.getHadError())
                BasicOutput.output(schema, new PrefixManager(si), od, er);
            }
          }
        }
      }
      if (er.getHadError())
        throw new OutputFailedException();
    }
    catch (ErrorReporter.WrappedSAXException e) {
      throw e.getException();
    }
  }
}
