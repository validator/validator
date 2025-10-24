package org.javacc.parser;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that implements a table driven code generator for the token manager in
 * java.
 */
public class TableDrivenJavaCodeGenerator implements TokenManagerCodeGenerator {
  private static final String TokenManagerTemplate =
      "/templates/TableDrivenTokenManager.template";
  private final CodeGenerator codeGenerator = new CodeGenerator();

  @Override
  public void generateCode(TokenizerData tokenizerData) {
    String superClass = (String)Options.getOptions().get(
                             Options.USEROPTION__TOKEN_MANAGER_SUPER_CLASS);
    Map<String, Object> options = Options.getOptions();
    options.put("maxOrdinal", tokenizerData.allMatches.size());
    options.put("maxLexStates", tokenizerData.lexStateNames.length);
    options.put("stateSetSize", tokenizerData.nfa.size());
    options.put("parserName", tokenizerData.parserName);
    options.put("maxLongs", tokenizerData.allMatches.size()/64 + 1);
    options.put("parserName", tokenizerData.parserName);
    options.put("charStreamName", CodeGenerator.getCharStreamName());
    options.put("defaultLexState", tokenizerData.defaultLexState);
    options.put("decls", tokenizerData.decls);
    options.put("superClass", (superClass == null || superClass.equals(""))
                      ? "" : "extends " + superClass);
    options.put("noDfa", Options.getNoDfa());
    options.put("generatedStates", tokenizerData.nfa.size());
    try {
      codeGenerator.writeTemplate(TokenManagerTemplate, options);
      dumpDfaTables(codeGenerator, tokenizerData);
      dumpNfaTables(codeGenerator, tokenizerData);
      dumpMatchInfo(codeGenerator, tokenizerData);
    } catch(IOException ioe) {
      assert(false);
    }
  }

  @Override
  public void finish(TokenizerData tokenizerData) {
    // TODO(sreeni) : Fix this mess.
    codeGenerator.genCodeLine("\n}");
    if (!Options.getBuildParser()) return;
    String fileName = Options.getOutputDirectory() + File.separator +
                      tokenizerData.parserName + "TokenManager.java";
    codeGenerator.saveOutput(fileName);
  }

  private void dumpDfaTables(
      CodeGenerator codeGenerator, TokenizerData tokenizerData) {
    Map<Integer, int[]> startAndSize = new HashMap<Integer, int[]>();
    int i = 0;

    codeGenerator.genCodeLine(
        "private static final int[] stringLiterals = {");
    for (int key : tokenizerData.literalSequence.keySet()) {
      int[] arr = new int[2];
      List<String> l = tokenizerData.literalSequence.get(key);
      List<Integer> kinds = tokenizerData.literalKinds.get(key);
      arr[0] = i;
      arr[1] = l.size();
      int j = 0;
      if (i > 0) codeGenerator.genCodeLine(", ");
      for (String s : l) {
        if (j > 0) codeGenerator.genCodeLine(", ");
        codeGenerator.genCode(s.length());
        for (int k = 0; k < s.length(); k++) {
          codeGenerator.genCode(", ");
          codeGenerator.genCode((int)s.charAt(k));
          i++;
        }
        int kind = kinds.get(j);
        codeGenerator.genCode(", " + kind);
        codeGenerator.genCode(
            ", " + tokenizerData.kindToNfaStartState.get(kind));
        i += 3;
        j++;
      }
      startAndSize.put(key, arr);
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine(
        "private static final java.util.Map<Integer, int[]> startAndSize =\n" +
        "    new java.util.HashMap<Integer, int[]>();");

    // Static block to actually initialize the map from the int array above.
    codeGenerator.genCodeLine("static {");
    for (int key : tokenizerData.literalSequence.keySet()) {
      int[] arr = startAndSize.get(key);
      codeGenerator.genCodeLine("startAndSize.put(" + key + ", new int[]{" +
                                 arr[0] + ", " + arr[1] + "});");
    }
    codeGenerator.genCodeLine("}");
  }

  private void dumpNfaTables(
      CodeGenerator codeGenerator, TokenizerData tokenizerData) {
    // WE do the following for java so that the generated code is reasonable
    // size and can be compiled. May not be needed for other languages.
    codeGenerator.genCodeLine("private static final long[][] jjCharData = {");
    Map<Integer, TokenizerData.NfaState> nfa = tokenizerData.nfa;
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) codeGenerator.genCodeLine(",");
      if (tmp == null) {
        codeGenerator.genCode("{}");
        continue;
      }
      codeGenerator.genCode("{");
      BitSet bits = new BitSet();
      for (char c : tmp.characters) {
        bits.set(c);
      }
      long[] longs = bits.toLongArray();
      for (int k = 0; k < longs.length; k++) {
        int rep = 1;
        while (k + rep < longs.length && longs[k + rep] == longs[k]) rep++;
        if (k > 0) codeGenerator.genCode(", ");
        codeGenerator.genCode(rep + ", ");
        codeGenerator.genCode("0x" + Long.toHexString(longs[k]) + "L");
        k += rep - 1;
      }
      codeGenerator.genCode("}");
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine(
         "private static final long[][] jjChars = ");
    codeGenerator.genCodeLine(
         "    new long[" + tokenizerData.nfa.size() +
         "][(Character.MAX_VALUE >> 6) + 1]; ");
    codeGenerator.genCodeLine(
         "static { ");
    codeGenerator.genCodeLine(
         "  for (int i = 0; i < " + tokenizerData.nfa.size() + "; i++) { ");
    codeGenerator.genCodeLine(
         "    int ind = 0; ");
    codeGenerator.genCodeLine(
         "    for (int j = 0; j < jjCharData[i].length; j += 2) { ");
    codeGenerator.genCodeLine(
         "      for (int k = 0; k < (int)jjCharData[i][j]; k++) { ");
    codeGenerator.genCodeLine(
         "        jjChars[i][ind++] = jjCharData[i][j + 1]; ");
    codeGenerator.genCodeLine(
         "      } ");
    codeGenerator.genCodeLine(
         "    } ");
    codeGenerator.genCodeLine(
         "  } ");
    codeGenerator.genCodeLine(
         "} ");


    codeGenerator.genCodeLine(
        "private static final int[][] jjcompositeState = {");
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) codeGenerator.genCodeLine(", ");
      if (tmp == null) {
        codeGenerator.genCode("{}");
        continue;
      }
      codeGenerator.genCode("{");
      int k = 0;
      for (int st : tmp.compositeStates) {
        if (k++ > 0) codeGenerator.genCode(", ");
        codeGenerator.genCode(st);
      }
      codeGenerator.genCode("}");
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine("private static final int[] jjmatchKinds = {");
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) codeGenerator.genCodeLine(", ");
      // TODO(sreeni) : Fix this mess.
      if (tmp == null) {
        codeGenerator.genCode(Integer.MAX_VALUE);
        continue;
      }
      codeGenerator.genCode(tmp.kind);
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine(
        "private static final int[][]  jjnextStateSet = {");
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) codeGenerator.genCodeLine(", ");
      if (tmp == null) {
        codeGenerator.genCode("{}");
        continue;
      }
      int k = 0;
      codeGenerator.genCode("{");
      for (int s : tmp.nextStates) {
        if (k++ > 0) codeGenerator.genCode(", ");
        codeGenerator.genCode(s);
      }
      codeGenerator.genCode("}");
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine(
        "private static final int[] jjInitStates  = {");
    int k = 0;
    for (int i : tokenizerData.initialStates.keySet()) {
      if (k++ > 0) codeGenerator.genCode(", ");
      codeGenerator.genCode(tokenizerData.initialStates.get(i));
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine(
        "private static final int[] canMatchAnyChar = {");
    k = 0;
    for (int i = 0; i < tokenizerData.wildcardKind.size(); i++) {
      if (k++ > 0) codeGenerator.genCode(", ");
      codeGenerator.genCode(tokenizerData.wildcardKind.get(i));
    }
    codeGenerator.genCodeLine("};");
  }

  private void dumpMatchInfo(
      CodeGenerator codeGenerator, TokenizerData tokenizerData) {
    Map<Integer, TokenizerData.MatchInfo> allMatches =
        tokenizerData.allMatches;

    // A bit ugly.

    BitSet toSkip = new BitSet(allMatches.size());
    BitSet toSpecial = new BitSet(allMatches.size());
    BitSet toMore = new BitSet(allMatches.size());
    BitSet toToken = new BitSet(allMatches.size());
    int[] newStates = new int[allMatches.size()];
    toSkip.set(allMatches.size() + 1, true);
    toToken.set(allMatches.size() + 1, true);
    toMore.set(allMatches.size() + 1, true);
    toSpecial.set(allMatches.size() + 1, true);
    // Kind map.
    codeGenerator.genCodeLine(
        "public static final String[] jjstrLiteralImages = {");

    int k = 0;
    for (int i : allMatches.keySet()) {
      TokenizerData.MatchInfo matchInfo = allMatches.get(i);
      switch(matchInfo.matchType) {
        case SKIP: toSkip.set(i); break;
        case SPECIAL_TOKEN: toSpecial.set(i); break;
        case MORE: toMore.set(i); break;
        case TOKEN: toToken.set(i); break;
      }
      newStates[i] = matchInfo.newLexState;
      String image = matchInfo.image;
      if (k++ > 0) codeGenerator.genCodeLine(", ");
      if (image != null) {
        codeGenerator.genCode("\"");
        for (int j = 0; j < image.length(); j++) {
          if (image.charAt(j) <= 0xff) {
            codeGenerator.genCode(
                "\\" + Integer.toOctalString((int)image.charAt(j)));
          } else {
            String hexVal = Integer.toHexString((int)image.charAt(j));
            if (hexVal.length() == 3)
              hexVal = "0" + hexVal;
            codeGenerator.genCode("\\u" + hexVal);
          }
        }
        codeGenerator.genCode("\"");
      } else {
        codeGenerator.genCodeLine("null");
      }
    }
    codeGenerator.genCodeLine("};");

    // Now generate the bit masks.
    generateBitVector("jjtoSkip", toSkip, codeGenerator);
    generateBitVector("jjtoSpecial", toSpecial, codeGenerator);
    generateBitVector("jjtoMore", toMore, codeGenerator);
    generateBitVector("jjtoToken", toToken, codeGenerator);

    codeGenerator.genCodeLine("private static final int[] jjnewLexState = {");
    for (int i = 0; i < newStates.length; i++) {
      if (i > 0) codeGenerator.genCode(", ");
      codeGenerator.genCode("0x" + Integer.toHexString(newStates[i]));
    }
    codeGenerator.genCodeLine("};");

    // Action functions.

    final String staticString = Options.getStatic() ? "static " : "";
    // Token actions.
    codeGenerator.genCodeLine(
        staticString + "void TokenLexicalActions(Token matchedToken) {");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.TOKEN,
                       "matchedToken.kind", codeGenerator);
    codeGenerator.genCodeLine("}");

    // Skip actions.
    // TODO(sreeni) : Streamline this mess.

    codeGenerator.genCodeLine(
        staticString + "void SkipLexicalActions(Token matchedToken) {");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.SKIP,
                       "jjmatchedKind", codeGenerator);
    dumpLexicalActions(allMatches, TokenizerData.MatchType.SPECIAL_TOKEN,
                       "jjmatchedKind", codeGenerator);
    codeGenerator.genCodeLine("}");

    // More actions.
    codeGenerator.genCodeLine(
        staticString + "void MoreLexicalActions() {");
    codeGenerator.genCodeLine(
        "jjimageLen += (lengthOfMatch = jjmatchedPos + 1);");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.MORE,
                       "jjmatchedKind", codeGenerator);
    codeGenerator.genCodeLine("}");

    codeGenerator.genCodeLine("public String[] lexStateNames = {");
    for (int i = 0; i < tokenizerData.lexStateNames.length; i++) {
      if (i > 0) codeGenerator.genCode(", ");
      codeGenerator.genCode("\"" + tokenizerData.lexStateNames[i] + "\"");
    }
    codeGenerator.genCodeLine("};");
  }

  private void dumpLexicalActions(
      Map<Integer, TokenizerData.MatchInfo> allMatches,
      TokenizerData.MatchType matchType, String kindString,
      CodeGenerator codeGenerator) {
    codeGenerator.genCodeLine("  switch(" + kindString + ") {");
    for (int i : allMatches.keySet()) {
      TokenizerData.MatchInfo matchInfo = allMatches.get(i);
      if (matchInfo.action == null ||
          matchInfo.matchType != matchType) {
        continue;
      }
      codeGenerator.genCodeLine("    case " + i + ": {\n");
      codeGenerator.genCodeLine("      " + matchInfo.action);
      codeGenerator.genCodeLine("      break;");
      codeGenerator.genCodeLine("    }");
    }
    codeGenerator.genCodeLine("    default: break;");
    codeGenerator.genCodeLine("  }");
  }

  private static void generateBitVector(
      String name, BitSet bits, CodeGenerator codeGenerator) {
    codeGenerator.genCodeLine("private static final long[] " + name + " = {");
    long[] longs = bits.toLongArray();
    for (int i = 0; i < longs.length; i++) {
      if (i > 0) codeGenerator.genCode(", ");
      codeGenerator.genCode("0x" + Long.toHexString(longs[i]) + "L");
    }
    codeGenerator.genCodeLine("};");
  }
}
