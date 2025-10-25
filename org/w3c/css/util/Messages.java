//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version $Revision$
 */
public class Messages {

    /**
     * Message properties
     */
    public Utf8Properties<String, String> properties = null;

    public static final ConcurrentHashMap<String, Utf8Properties<String, String>> languages;
    public static final ArrayList<String> languages_name;
    public Utf8Properties<String, String> default_properties = null;

    /**
     * Creates a new Messages
     */
    public Messages(String lang) {
        if (lang != null) {
            StringTokenizer lanTok = new StringTokenizer(lang, ",");
            int maxTok = lanTok.countTokens();

            String slang[] = new String[maxTok];
            float qlang[] = new float[maxTok];

            // quick and dirty, it would be better to use Jigsaw's classes
            while (lanTok.hasMoreTokens()) {
                String l = lanTok.nextToken().trim().toLowerCase();
                int qualsep = l.indexOf(';');
                float qval = 1;
                if (qualsep != -1) {
                    String p = l.substring(qualsep + 1);
                    l = l.substring(0, qualsep);
                    if (p.startsWith("q=")) {
                        qval = Float.parseFloat(p.substring(2));
                    }
                }
                for (int i = 0; i < maxTok; i++) {
                    if (slang[i] == null) {
                        slang[i] = l;
                        qlang[i] = qval;
                        break;
                    } else if (qval > qlang[i]) {
                        System.arraycopy(slang, i, slang, i + 1, (maxTok - i - 1));
                        System.arraycopy(qlang, i, qlang, i + 1, (maxTok - i - 1));
                        slang[i] = l;
                        qlang[i] = qval;
                        break;
                    }

                }
            }
            for (int i = 0; i < maxTok; i++) {
                String l = slang[i];
                properties = languages.get(l);
                if (properties != null) {
                    break;
                }
                int minusIndex = l.indexOf('-');
                if (minusIndex != -1) {
                    // suppressed -cn in zh-cn (example)
                    l = l.substring(0, minusIndex);
                    properties = languages.get(l);
                }
                if (properties != null) {
                    break;
                }
            }
        }
        default_properties = languages.get("en");
        if (properties == null) {
            properties = default_properties;
        }
    }

    /**
     * Get a property.
     */
    public String getString(String message) {
        String s = properties.getProperty(message);
        if (s != null) {
            return s;
        }
        return default_properties.getProperty(message);
    }

    /**
     * Get a property but not its default when not found
     */
    public String getStringStrict(String message) {
        return properties.getProperty(message);
    }

    /**
     * Get a warning property.
     *
     * @param message the warning property.
     */
    public String getWarningString(String message) {
        return getString("warning." + message);
    }

    /**
     * Get a warning level property.
     *
     * @param message the warning property.
     */
    public String getWarningLevelString(String message) {
        return getString(new StringBuilder("warning.").append(message).append(".level").toString());
    }

    /**
     * Get an error property.
     *
     * @param message the error property.
     */
    public String getErrorString(String message) {
        return getString("error." + message);
    }

    /**
     * Get an generator property.
     *
     * @param message the generator property.
     */
    public String getGeneratorString(String message) {
        return getString("generator." + message);
    }

    /**
     * Get an generator property.
     *
     * @param message the generator property.
     */
    public String getGeneratorString(String message, String param) {
        String str = getString("generator." + message);

        // replace all parameters
        int i = str.indexOf("%s");
        if (i >= 0) {
            str = str.substring(0, i) + param + str.substring(i + 2);
        }
        return str;
    }

    /**
     * Get an generator property.
     *
     * @param message the generator property.
     */
    public String getServletString(String message) {
        return getString("servlet." + message);
    }

    /**
     * escape string
     */
    static public String escapeString(String orig) {
        if (orig != null) {
            int len = orig.length();
            StringBuilder ret = new StringBuilder(len + 16);
            char c;

            for (int i = 0; i < len; i++) {
                switch (c = orig.charAt(i)) {
                    case '&':
                        ret.append("&amp;");
                        break;
                    case '\'':
                        ret.append("&#39;");
                        break;
                    case '"':
                        ret.append("&quot;");
                        break;
                    case '<':
                        ret.append("&lt;");
                        break;
                    case '>':
                        ret.append("&gt;");
                        break;
                    default:
                        ret.append(c);
                }
            }
            return ret.toString();
        }
        return "[empty string]";
    }

    /**
     * Replace curly quotes with HTML code tags
     */
    static public String replaceCurlyQuotesWithHtmlCodeTags(String orig) {
        if (orig != null) {
            int len = orig.length();
            StringBuilder ret = new StringBuilder(len + 16);
            char c;
            for (int i = 0; i < len; i++) {
                switch (c = orig.charAt(i)) {
                    case '\u201C':
                        ret.append("<code>");
                        break;
                    case '\u201D':
                        ret.append("</code>");
                        break;
                    default:
                        ret.append(c);
                }
            }
            return ret.toString();
        }
        return "[empty string]";
    }

    public String getString(String message, ArrayList<String> params) {
        if ((params == null) || params.size() == 0) {
            return getString(message);
        }
        String[] msg_parts = getString(message).split("%s", -1);
        Iterator<String> param_it = params.iterator();
        StringBuilder sb = new StringBuilder(msg_parts[0]);
        for (int i = 1; i < msg_parts.length; i++) {
            if (param_it.hasNext()) {
                sb.append(param_it.next());
            }
            sb.append(msg_parts[i]);
        }
        return sb.toString();
    }

    static {
        languages = new ConcurrentHashMap<String, Utf8Properties<String, String>>();
        languages_name = new ArrayList<String>();

        Utf8Properties<String, String> tmp;
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.de");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("de");
                languages.put("de", tmp);
                languages.put("de_DE", tmp);
                languages.put("de_AT", tmp);
                languages.put("de_CH", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties de");
            System.err.println("  " + e.toString());
        }

        // ------------------------------------------------

        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.en");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("en");
                languages.put("en", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties en");
            System.err.println("  " + e.toString());
        }

        // ------------------------------------------------

        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.es");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("es");
                languages.put("es", tmp);
                languages.put("es_ES", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties es");
            System.err.println("  " + e.toString());
        }

        // -----------------------

        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.fr");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("fr");
                languages.put("fr", tmp);
                languages.put("fr_FR", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties fr");
            System.err.println("  " + e.toString());
        }

        // -----------------------

        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.ko");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("ko");
                languages.put("ko", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties ko");
            System.err.println("  " + e.toString());
        }

        // -----------------------

        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.it");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("it");
                languages.put("it", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties it");
            System.err.println("  " + e.toString());
        }

        // -----------------------

        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.nl");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("nl");
                languages.put("nl", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties nl");
            System.err.println("  " + e.toString());
        }

        // -----------------------

        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.ja");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("ja");
                languages.put("ja", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages:" + " couldn't load properties ja");
            System.err.println("  " + e.toString());
        }

        // -----------------------

        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.pl-PL");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("pl-PL");
                languages.put("pl", tmp);
                languages.put("pl_PL", tmp);
                languages.put("pl-PL", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties pl");
            System.err.println("  " + e.toString());
        }

        // -----------------------

        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.pt-BR");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("pt-BR");
                languages.put("pt-br", tmp);
                languages.put("pt-BR", tmp);
                languages.put("pt_BR", tmp);
                languages.put("pt", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties pt-br");
            System.err.println("  " + e.toString());
        }
        // -----------------------
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.ru");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("ru");
                languages.put("ru", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties ru");
            System.err.println("  " + e.toString());
        }

        // -----------------------
        // disabled for the time being, the properties file has trouble with line breaks
        // and the rtl text makes it difficult to fix with my editor. Ball is in the camp of translator 2009-03 -- olivier
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.fa");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("fa");
                languages.put("fa", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties fa");
            System.err.println("  " + e.toString());
        }

        // -----------------------
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.sv");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("sv");
                languages.put("sv", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties sv");
            System.err.println("  " + e.toString());
        }

        // -----------------------
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.bg");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("bg");
                languages.put("bg", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties bg");
            System.err.println("  " + e.toString());
        }

        // -----------------------
        // Ukrainian
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.uk");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("uk");
                languages.put("uk", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties uk");
            System.err.println("  " + e.toString());
        }

        // -----------------------
        // Czech
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.cs");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("cs");
                languages.put("cs", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties cs");
            System.err.println("  " + e.toString());
        }

        // -----------------------
        // Romanian
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.ro");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("ro");
                languages.put("ro", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties ro");
            System.err.println("  " + e.toString());
        }

        // -----------------------
        // Magyar (Hungarian)
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.hu");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("hu");
                languages.put("hu", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties hu");
            System.err.println("  " + e.toString());
        }

        // -----------------------
        // Greek
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.el");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("el");
                languages.put("el", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties el");
            System.err.println("  " + e.toString());
        }

        // -----------------------
        // Hindi
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.hi");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("hi");
                languages.put("hi", tmp);
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties hi");
            System.err.println("  " + e.toString());
        }

        // -----------------------
        // Chinese
        try {
            java.io.InputStream f = Messages.class.getResourceAsStream("Messages.properties.zh-cn");
            try {
                tmp = new Utf8Properties<String, String>();
                tmp.load(f);
                languages_name.add("zh-cn");
                languages.put("zh-cn", tmp);
                languages.put("zh", tmp); // for now we have no other
                // alternative for chinese
            } finally {
                f.close();
            }
        } catch (Exception e) {
            System.err.println("org.w3c.css.util.Messages: " + "couldn't load properties cn");
            System.err.println("  " + e.toString());
        }
    }
}
