package com.thaiopensource.relaxng.translate.util;

import com.thaiopensource.relaxng.output.common.ErrorReporter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class ParamProcessor {
  private ErrorReporter er;
  private ParamFactory paramFactory;
  private final Map paramMap = new HashMap();
  private final Set processedParamNames = new HashSet();

  private static class BadParamException extends Exception { }

  static class LocalizedInvalidValueException extends InvalidParamValueException {
    private final String key;

    LocalizedInvalidValueException(String key) {
      this.key = key;
    }
  }

  public void declare(String name, Param param) {
    paramMap.put(name, param);
  }

  public void setParamFactory(ParamFactory factory) {
    this.paramFactory = factory;
  }

  public void process(String[] params, ErrorHandler eh) throws InvalidParamsException, SAXException {
    er = new ErrorReporter(eh, ParamProcessor.class);
    try {
      for (int i = 0; i < params.length; i++)
        processParam(params[i]);
      if (er.getHadError())
        throw new InvalidParamsException();
    }
    catch (ErrorReporter.WrappedSAXException e) {
      throw e.getException();
    }
    finally {
      processedParamNames.clear();
      er = null;
    }
  }

  private void processParam(String param) {
    int off = param.indexOf('=');
    String name = null;
    try {
      if (off < 0) {
        if (param.startsWith("no-")) {
          name = param.substring(3);
          lookupParam(name).set(false);
        }
        else {
          name = param;
          lookupParam(name).set(true);
        }
      }
      else {
        name = param.substring(0, off);
        lookupParam(name).set(param.substring(off + 1));
      }
    }
    catch (BadParamException e) {
    }
    catch (LocalizedInvalidValueException e) {
      er.error("invalid_param_value_detail", name, er.getLocalizer().message(e.key), null);
    }
    catch (InvalidParamValueException e) {
      String detail = e.getMessage();
      if (detail != null)
        er.error("invalid_param_value_detail", name, detail, null);
      else if (off < 0)
	er.error(param.startsWith("no-")
                 ? "param_only_positive"
                 : "param_only_negative",
                 name,
                 null);
      else
        er.error("invalid_param_value", name, null);
    }
    catch (ParamValuePresenceException e) {
      if (off < 0)
        er.error("param_value_required", name, null);
      else
        er.error("param_value_not_allowed", name, null);
    }
  }

  private Param lookupParam(String name) throws BadParamException {
    Param p = (Param)paramMap.get(name);
    if (p == null && paramFactory != null)
      p = paramFactory.createParam(name);
    if (p == null) {
      er.error("unrecognized_param", name, null);
      throw new BadParamException();
    }
    if (processedParamNames.contains(name)) {
      if (!p.allowRepeat()) {
        er.error("duplicate_param", name, null);
        throw new BadParamException();
      }
    }
    else
      processedParamNames.add(name);
    return p;
  }
}
