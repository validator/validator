package com.thaiopensource.relaxng.output;

import com.thaiopensource.relaxng.translate.util.ParamProcessor;
import com.thaiopensource.relaxng.translate.util.EncodingParam;
import com.thaiopensource.relaxng.translate.util.IntegerParam;
import com.thaiopensource.relaxng.translate.util.Param;

public class OutputDirectoryParamProcessor extends ParamProcessor {
  private final OutputDirectory od;
  private static final int MAX_INDENT = 16;

  public OutputDirectoryParamProcessor(OutputDirectory od) {
    this.od = od;
    super.declare("encoding",
                  new EncodingParam() {
                    protected void setEncoding(String encoding) {
                      OutputDirectoryParamProcessor.this.od.setEncoding(encoding);
                    }
                  });
    super.declare("indent",
                  new IntegerParam(0, MAX_INDENT) {
                    protected void setInteger(int value) {
                      OutputDirectoryParamProcessor.this.od.setIndent(value);
                    }
                  });
  }
}
