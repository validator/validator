package com.thaiopensource.xml.dtd.test;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

public class CompareOutputStream extends OutputStream {
  private final InputStream in;
  private long byteIndex = 0;

  public CompareOutputStream(InputStream in) {
    this.in = in;
  }

  public void write(int b) throws IOException {
    if (in.read() != (b & 0xFF))
      throw new CompareFailException(byteIndex);
    byteIndex++;
  }

  public void close() throws IOException {
    if (in.read() != -1)
      throw new CompareFailException(byteIndex);
    in.close();
  }
}
