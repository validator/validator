package com.thaiopensource.relaxng.input.dtd;

import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import com.thaiopensource.relaxng.translate.util.ParamProcessor;
import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.parse.DtdParserImpl;
import com.thaiopensource.xml.dtd.parse.ParseException;
import com.thaiopensource.xml.dtd.app.UriEntityManager;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;

public class DtdInputFormat implements InputFormat {
  private boolean inlineAttlistDecls = false;

  public SchemaCollection load(String uri, String[] params, ErrorHandler eh)
          throws InvalidParamsException, IOException, SAXException {
    ParamProcessor pp = new ParamProcessor();
    pp.process(params, eh);
    try {
      Dtd dtd = new DtdParserImpl().parse(uri, new UriEntityManager());
      try {
        return new Converter(dtd, new ErrorReporter(eh, DtdInputFormat.class), inlineAttlistDecls).convert();
      }
      catch (ErrorReporter.WrappedSAXException e) {
        throw e.getException();
      }
    }
    catch (ParseException e) {
      throw new SAXParseException(e.getMessageBody(), null, e.getLocation(), e.getLineNumber(), e.getColumnNumber());
    }
  }

  public boolean isInlineAttlistDecls() {
    return inlineAttlistDecls;
  }

  public void setInlineAttlistDecls(boolean inlineAttlistDecls) {
    this.inlineAttlistDecls = inlineAttlistDecls;
  }
}
