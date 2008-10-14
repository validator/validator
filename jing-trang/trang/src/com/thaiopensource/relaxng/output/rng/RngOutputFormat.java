package com.thaiopensource.relaxng.output.rng;

import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.SchemaDocument;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputDirectoryParamProcessor;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

public class RngOutputFormat implements OutputFormat {
  public void output(SchemaCollection sc, OutputDirectory od, String[] params, String inputFormat, ErrorHandler eh)
          throws IOException, InvalidParamsException, SAXException {
    new OutputDirectoryParamProcessor(od).process(params, eh);
    for (Map.Entry<String, SchemaDocument> entry : sc.getSchemaDocumentMap().entrySet()) {
      outputPattern(entry.getValue(), entry.getKey(), od);
    }
  }

  private static void outputPattern(SchemaDocument sd, String sourceUri, OutputDirectory od) throws IOException {
    Analyzer analyzer = new Analyzer();
    sd.getPattern().accept(analyzer);
    Output.output(sd.getPattern(),
                  sd.getEncoding(),
                  sourceUri,
                  od,
                  analyzer.getDatatypeLibrary(),
                  analyzer.getPrefixMap());
  }
}
