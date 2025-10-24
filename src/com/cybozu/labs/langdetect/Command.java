package com.cybozu.labs.langdetect;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.cybozu.labs.langdetect.util.LangProfile;

/**
 * 
 * LangDetect Command Line Interface
 * <p>
 * This is a command line interface of Language Detection Library "LandDetect".
 * 
 * 
 * @author Nakatani Shuyo
 *
 */
public class Command {
    /** smoothing default parameter (ELE) */
    private static final double DEFAULT_ALPHA = 0.5;

    /** for Command line easy parser */
    private HashMap<String, String> opt_with_value = new HashMap<String, String>();
    private HashMap<String, String> values = new HashMap<String, String>();
    private HashSet<String> opt_without_value = new HashSet<String>();
    private ArrayList<String> arglist = new ArrayList<String>();

    /**
     * Command line easy parser
     * @param args command line arguments
     */
    private void parse(String[] args) {
        for(int i=0;i<args.length;++i) {
            if (opt_with_value.containsKey(args[i])) {
                String key = opt_with_value.get(args[i]);
                values.put(key, args[i+1]);
                ++i;
            } else if (args[i].startsWith("-")) {
                opt_without_value.add(args[i]);
            } else {
                arglist.add(args[i]);
            }
        }
    }

    private void addOpt(String opt, String key, String value) {
        opt_with_value.put(opt, key);
        values.put(key, value);
    }
    private String get(String key) {
        return values.get(key);
    }
    private Long getLong(String key) {
        String value = values.get(key);
        if (value == null) return null;
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private double getDouble(String key, double defaultValue) {
        try {
            return Double.valueOf(values.get(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean hasOpt(String opt) {
        return opt_without_value.contains(opt);
    }

        
    /**
     * File search (easy glob)
     * @param directory directory path
     * @param pattern   searching file pattern with regular representation
     * @return matched file
     */
    private File searchFile(File directory, String pattern) {
        for(File file : directory.listFiles()) {
            if (file.getName().matches(pattern)) return file;
        }
        return null;
    }


    /**
     * load profiles
     * @return false if load success
     */
    private boolean loadProfile() {
        String profileDirectory = get("directory") + "/"; 
        try {
            DetectorFactory.loadProfile(profileDirectory);
            Long seed = getLong("seed");
            if (seed != null) DetectorFactory.setSeed(seed);
            return false;
        } catch (LangDetectException e) {
            System.err.println("ERROR: " + e.getMessage());
            return true;
        }
    }

    /**
     * Language detection test for each file (--detectlang option)
     * 
     * <pre>
     * usage: --detectlang -d [profile directory] -a [alpha] -s [seed] [test file(s)]
     * </pre>
     * 
     */
    public void detectLang() {
        if (loadProfile()) return;
        for (String filename: arglist) {
            BufferedReader is = null;
            try {
                is = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"));

                Detector detector = DetectorFactory.create(getDouble("alpha", DEFAULT_ALPHA));
                if (hasOpt("--debug")) detector.setVerbose();
                detector.append(is);
                System.out.println(filename + ":" + detector.getProbabilities());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LangDetectException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is!=null) is.close();
                } catch (IOException e) {}
            }

        }
    }

    /**
     * Batch Test of Language Detection (--batchtest option)
     * 
     * <pre>
     * usage: --batchtest -d [profile directory] -a [alpha] -s [seed] [test data(s)]
     * </pre>
     * 
     * The format of test data(s):
     * <pre>
     *   [correct language name]\t[text body for test]\n
     * </pre>
     *  
     */
    public void batchTest() {
        if (loadProfile()) return;
        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        for (String filename: arglist) {
            BufferedReader is = null;
            try {
                is = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"));
                while (is.ready()) {
                    String line = is.readLine();
                    int idx = line.indexOf('\t');
                    if (idx <= 0) continue;
                    String correctLang = line.substring(0, idx);
                    String text = line.substring(idx + 1);
                    
                    Detector detector = DetectorFactory.create(getDouble("alpha", DEFAULT_ALPHA));
                    detector.append(text);
                    String lang = "";
                    try {
                        lang = detector.detect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!result.containsKey(correctLang)) result.put(correctLang, new ArrayList<String>());
                    result.get(correctLang).add(lang);
                    if (hasOpt("--debug")) System.out.println(correctLang + "," + lang + "," + (text.length()>100?text.substring(0, 100):text));
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LangDetectException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is!=null) is.close();
                } catch (IOException e) {}
            }

            ArrayList<String> langlist = new ArrayList<String>(result.keySet());
            Collections.sort(langlist);

            int totalCount = 0, totalCorrect = 0;
            for ( String lang :langlist) {
                HashMap<String, Integer> resultCount = new HashMap<String, Integer>();
                int count = 0;
                ArrayList<String> list = result.get(lang);
                for (String detectedLang: list) {
                    ++count;
                    if (resultCount.containsKey(detectedLang)) {
                        resultCount.put(detectedLang, resultCount.get(detectedLang) + 1);
                    } else {
                        resultCount.put(detectedLang, 1);
                    }
                }
                int correct = resultCount.containsKey(lang)?resultCount.get(lang):0;
                double rate = correct / (double)count;
                System.out.println(String.format("%s (%d/%d=%.2f): %s", lang, correct, count, rate, resultCount));
                totalCorrect += correct;
                totalCount += count;
            }
            System.out.println(String.format("total: %d/%d = %.3f", totalCorrect, totalCount, totalCorrect / (double)totalCount));
            
        }
        
    }

    /**
     * Command Line Interface
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Command command = new Command();
        command.addOpt("-d", "directory", "./");
        command.addOpt("-a", "alpha", "" + DEFAULT_ALPHA);
        command.addOpt("-s", "seed", null);
        command.addOpt("-l", "lang", null);
        command.parse(args);

        if (command.hasOpt("--detectlang")) {
            command.detectLang();
        } else if (command.hasOpt("--batchtest")) {
            command.batchTest();
        }
    }

}
