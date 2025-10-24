package org.javacc.parser;

import java.util.List;
import java.util.Map;

public class ParserData {
  public enum LookaheadType {
    TOKEN,
    PRODUCTION,
    SEQUENCE,
    CHOICE,
    ZERORORMORE
  }

  public static class LookaheadInfo {
    public LookaheadType lokaheadType;
    public List<Integer> data;
  }

  Map<Integer, List<LookaheadInfo>> lookaheads;
}
