package com.thaiopensource.xml.dtd.test;

import java.io.IOException;

public class CompareFailException extends IOException {
  private final long byteIndex;

  public CompareFailException(long byteIndex) {
    this.byteIndex = byteIndex;
  }

  public long getByteIndex() {
    return byteIndex;
  }
}
