package com.thaiopensource.relaxng.input.dtd;

import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.parse.DtdParserImpl;
import com.thaiopensource.xml.dtd.app.UriEntityManager;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;

public class DtdInputFormat implements InputFormat {
  public SchemaCollection load(String uri, String encoding, ErrorHandler eh) throws IOException, SAXException {
    Dtd dtd = new DtdParserImpl().parse(uri, new UriEntityManager());
    return new Converter(new ErrorReporter(eh, DtdInputFormat.class)).convertDtd(dtd);
  }
}
