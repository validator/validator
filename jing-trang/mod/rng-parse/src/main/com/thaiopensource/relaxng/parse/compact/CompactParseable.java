package com.thaiopensource.relaxng.parse.compact;

import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.IncludedGrammar;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.parse.SubParseable;
import com.thaiopensource.xml.util.EncodingMap;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;

public class CompactParseable implements SubParseable {
  private final InputSource in;
  private final UriOpener opener;
  private final ErrorHandler eh;

  public CompactParseable(InputSource in, UriOpener opener, ErrorHandler eh) {
    this.in = in;
    this.opener = opener;
    this.eh = eh;
  }

  public ParsedPattern parse(SchemaBuilder sb, Scope scope) throws BuildException, IllegalSchemaException {
    return new CompactSyntax(makeReader(in), in.getSystemId(), sb, eh).parse(scope);
  }

  public SubParseable createSubParseable(String href, String base) throws BuildException {
    return new CompactParseable(opener.resolve(href, base), opener, eh);
  }

  public ParsedPattern parseAsInclude(SchemaBuilder sb, IncludedGrammar g)
          throws BuildException, IllegalSchemaException {
    return new CompactSyntax(makeReader(in), in.getSystemId(), sb, eh).parseInclude(g);
  }

  public String getUri() {
    return in.getSystemId();
  }

  private static final String UTF8 = EncodingMap.getJavaName("UTF-8");
  private static final String UTF16 = EncodingMap.getJavaName("UTF-16");

  private Reader makeReader(InputSource in) throws BuildException {
    in = opener.open(in);
    try {
      Reader reader = in.getCharacterStream();
      if (reader == null) {
        InputStream byteStream = in.getByteStream();
        String encoding = in.getEncoding();
        if (encoding == null) {
          PushbackInputStream pb = new PushbackInputStream(byteStream, 2);
          encoding = detectEncoding(pb);
          byteStream = pb;
        }
        reader = new InputStreamReader(byteStream, encoding);
      }
      return reader;
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }

  static private String detectEncoding(PushbackInputStream in) throws IOException {
    String encoding = UTF8;
    int b1 = in.read();
    if (b1 != -1) {
      int b2 = in.read();
      if (b2 != -1) {
        in.unread(b2);
        if ((b1 == 0xFF && b2 == 0xFE) || (b1 == 0xFE && b2 == 0xFF))
          encoding = UTF16;
      }
      in.unread(b1);
    }
    return encoding;
  }
}
