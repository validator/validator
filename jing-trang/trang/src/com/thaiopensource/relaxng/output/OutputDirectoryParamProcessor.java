package com.thaiopensource.relaxng.output;

import com.thaiopensource.relaxng.translate.util.ParamProcessor;
import com.thaiopensource.relaxng.translate.util.EncodingParam;

public class OutputDirectoryParamProcessor extends ParamProcessor {
  private final OutputDirectory od;

  public OutputDirectoryParamProcessor(OutputDirectory od) {
    this.od = od;
    super.declare("encoding",
                  new EncodingParam() {
                    protected void setEncoding(String encoding) {
                      OutputDirectoryParamProcessor.this.od.setEncoding(encoding);
                    }
                  });
  }
}
