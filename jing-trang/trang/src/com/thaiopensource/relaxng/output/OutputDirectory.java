package com.thaiopensource.relaxng.output;

import com.thaiopensource.xml.out.CharRepertoire;

import java.io.Writer;
import java.io.IOException;

public interface OutputDirectory {
  static public class Stream {
    private final Writer writer;
    private final String encoding;
    private final CharRepertoire charRepertoire;

    public Stream(Writer writer, String encoding, CharRepertoire charRepertoire) {
      this.writer = writer;
      this.encoding = encoding;
      this.charRepertoire = charRepertoire;
    }

    public Writer getWriter() {
      return writer;
    }

    public String getEncoding() {
      return encoding;
    }

    public CharRepertoire getCharRepertoire() {
      return charRepertoire;
    }
  }
  Stream open(String sourceUri, String encoding) throws IOException;
  String reference(String fromSourceUri, String toSourceUri);
  String getLineSeparator();
  int getLineLength();
  int getIndent();
  void setIndent(int indent);
  /**
   * This overrides the encoding specified with open.
   */
  void setEncoding(String encoding);
}
