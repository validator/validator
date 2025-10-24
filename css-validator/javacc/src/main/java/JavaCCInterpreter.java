
import java.io.*;
import java.util.*;
import org.javacc.parser.*;

public class JavaCCInterpreter {
  public static void main(String[] args) throws Exception {
    // Initialize all static state
    Main.reInitAll();
    JavaCCParser parser = null;
    for (int arg = 0; arg < args.length - 2; arg++) {
      if (!Options.isOption(args[arg])) {
        System.out.println("Argument \"" + args[arg] + "\" must be an option setting.");
        System.exit(1);
      }
      Options.setCmdLineOption(args[arg]);
    }

    String input = "";
    String grammar = "";
    try {
      File fp = new File(args[args.length-2]);
      byte[] buf = new byte[(int)fp.length()];
      new DataInputStream(
          new BufferedInputStream(
              new FileInputStream(fp))).readFully(buf);
      grammar = new String(buf);
      File inputFile = new File(args[args.length - 1]);
      buf = new byte[(int)inputFile.length()];
      new DataInputStream(
          new BufferedInputStream(
              new FileInputStream(inputFile))).readFully(buf);
      input = new String(buf);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (Throwable t) {
      System.exit(1);
    }
    long l = System.currentTimeMillis();
    new JavaCCInterpreter().runTokenizer(grammar, input);
    System.err.println("Tokenized in: " + (System.currentTimeMillis()-l));
  }

  public void runTokenizer(String grammar, String input) {
    try {
      JavaCCParser parser = new JavaCCParser(new StringReader(grammar));
      parser.javacc_input();
      Semanticize.start();
      LexGen lg = new LexGen();
      lg.generateDataOnly = true;
      lg.start();
      TokenizerData td = LexGen.tokenizerData;
      if (JavaCCErrors.get_error_count() == 0) {
        tokenize(td, input);
      }
    } catch (MetaParseException e) {
      System.out.println("Detected " + JavaCCErrors.get_error_count() +
                         " errors and "
                         + JavaCCErrors.get_warning_count() + " warnings.");
      System.exit(1);
    } catch (Exception e) {
      System.out.println(e.toString());
      System.out.println("Detected " + (JavaCCErrors.get_error_count()+1) +
                         " errors and "
                         + JavaCCErrors.get_warning_count() + " warnings.");
      System.exit(1);
    }
  }

  public static void tokenize(TokenizerData td, String input) {
    // First match the string literals.
    final int input_size = input.length();
    int curPos = 0;
    int curLexState = td.defaultLexState;
    Set<Integer> curStates = new HashSet<Integer>();
    Set<Integer> newStates = new HashSet<Integer>();
    while (curPos < input_size) {
      int beg = curPos;
      int matchedPos = beg;
      int matchedKind = Integer.MAX_VALUE;
      int nfaStartState = td.initialStates.get(curLexState);

      char c = input.charAt(curPos);
      if (Options.getIgnoreCase()) c = Character.toLowerCase(c);
      int key = curLexState << 16 | (int)c;
      final List<String> literals = td.literalSequence.get(key);
      if (literals != null) {
        // We need to go in order so that the longest match works.
        int litIndex = 0;
        for (String s : literals) {
          int index = 1;
          // See which literal matches.
          while (index < s.length() && curPos + index < input_size) {
            c = input.charAt(curPos + index);
            if (Options.getIgnoreCase()) c = Character.toLowerCase(c);
            if (c != s.charAt(index)) break;
            index++;
          }
          if (index == s.length()) {
            // Found a string literal match.
            matchedKind = td.literalKinds.get(key).get(litIndex);
            matchedPos = curPos + index - 1;
            nfaStartState = td.kindToNfaStartState.get(matchedKind);
            curPos += index;
            break;
          }
          litIndex++;
        }
      }

      if (nfaStartState != -1) {
        // We need to add the composite states first.
        int kind = Integer.MAX_VALUE;
        curStates.add(nfaStartState);
        curStates.addAll(td.nfa.get(nfaStartState).compositeStates);
        do {
          c = input.charAt(curPos);
          if (Options.getIgnoreCase()) c = Character.toLowerCase(c);
          for (int state : curStates) {
            TokenizerData.NfaState nfaState = td.nfa.get(state);
            if (nfaState.characters.contains(c)) {
              if (kind > nfaState.kind) {
                kind = nfaState.kind;
              }
              newStates.addAll(nfaState.nextStates);
            }
          }
          Set<Integer> tmp = newStates;
          newStates = curStates;
          curStates = tmp;
          newStates.clear();
          if (kind != Integer.MAX_VALUE) {
            matchedKind = kind;
            matchedPos = curPos;
            kind = Integer.MAX_VALUE;
          }
        } while (!curStates.isEmpty() && ++curPos < input_size);
      }
      if (matchedPos == beg && matchedKind > td.wildcardKind.get(curLexState)) {
        matchedKind = td.wildcardKind.get(curLexState);
      }
      if (matchedKind != Integer.MAX_VALUE) {
        TokenizerData.MatchInfo matchInfo = td.allMatches.get(matchedKind);
        if (matchInfo.action != null) {
          System.err.println(
              "Actions not implemented (yet) in intererpreted mode");
        }
        if (matchInfo.matchType == TokenizerData.MatchType.TOKEN) {
          System.err.println("Token: " + matchedKind + "; image: \"" +
                             input.substring(beg, matchedPos + 1) + "\"");
        }
        if (matchInfo.newLexState != -1) {
          curLexState = matchInfo.newLexState;
        }
        curPos = matchedPos + 1;
      } else {
        System.err.println("Encountered token error at char: " +
                           input.charAt(curPos));
        System.exit(1);
      }
    }
    System.err.println("Matched EOF");
  }
}
