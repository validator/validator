package com.thaiopensource.relaxng.input.xml;

import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.input.AbstractMultiInputFormat;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import com.thaiopensource.relaxng.translate.util.ParamProcessor;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;

public class XmlInputFormat extends AbstractMultiInputFormat {
  public SchemaCollection load(String[] uris, String[] params, String outputFormat, ErrorHandler eh)
          throws InputFailedException, InvalidParamsException, IOException, SAXException {
    new ParamProcessor().process(params, eh);
    return Inferrer.infer(uris, eh);
  }
}
