package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.output.OutputDirectoryParamProcessor;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;

public class XsdOutputFormat implements OutputFormat {
  static private final boolean DEFAULT_ENABLE_ABSTRACT_ELEMENT = true;
  public void output(SchemaCollection sc, OutputDirectory od, String[] params, ErrorHandler eh)
          throws SAXException, IOException, OutputFailedException, InvalidParamsException {
    new OutputDirectoryParamProcessor(od).process(params, eh);
    try {
      ErrorReporter er = new ErrorReporter(eh, XsdOutputFormat.class);
      SchemaInfo si = new SchemaInfo(sc, er);
      if (!er.getHadError()) {
        RefChecker.check(si, er);
        if (!er.getHadError()) {
          RestrictionsChecker.check(si, er);
          if (!er.getHadError()) {
            Guide guide = new Guide(DEFAULT_ENABLE_ABSTRACT_ELEMENT);
            Schema schema = BasicBuilder.buildBasicSchema(si, guide, er);
            if (!er.getHadError()) {
              new Transformer(schema, er).transform();
              if (!er.getHadError())
                BasicOutput.output(schema, guide, new PrefixManager(si), od, er);
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
