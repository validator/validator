package com.thaiopensource.relaxng.output.rng;

import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.OutputFormat;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/*
XXX Specify indent
*/
public class RngOutputFormat implements OutputFormat {
  public void output(SchemaCollection sc, OutputDirectory od, ErrorHandler eh) throws IOException {
    outputPattern(sc.getMainSchema(), OutputDirectory.MAIN, od);
    for (Iterator iter = sc.getSchemas().entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      outputPattern((Pattern)entry.getValue(), (String)entry.getKey(), od);
    }
  }

  private void outputPattern(Pattern p, String sourceUri, OutputDirectory od) throws IOException {
    Analyzer analyzer = new Analyzer();
    p.accept(analyzer);
    Output.output(p,
                  sourceUri,
                  od,
                  analyzer.getDatatypeLibrary(),
                  analyzer.getPrefixMap());
  }
}
