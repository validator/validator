//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.util;


import org.w3c.css.parser.analyzer.ParseException;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @version $Revision$
 */
public class InvalidParamException extends ParseException {
    String errorType = null;

    private static HashMap<String, String[]> exceptionMessages;
    private static HashMap<String, int[]> exceptionOrder;

    static {
        exceptionMessages = new HashMap<>();
        exceptionOrder = new HashMap<>();
    }

    /**
     * Create a new InvalidParamException.
     */
    public InvalidParamException() {
        super();
    }

    /**
     * Create a new InvalidParamException with an error message.
     *
     * @param error the error message
     */
    public InvalidParamException(String error, ApplContext ac) {
        super(ac.getMsg().getErrorString((error != null) ? error : ""));
        errorType = error;
    }

    /**
     * Create a new InvalidParamException with an error message class.
     *
     * @param error   the error message class.
     * @param message a message to add
     */
    public InvalidParamException(String error, Object message, ApplContext ac) {
        super(processError(error, new String[]{(message != null) ? message.toString() : null}, ac));
        errorType = error;
    }

    /**
     * Create a new InvalidParamException with an error message class.
     *
     * @param error the error message class.
     * @param args  a string array of messages to add
     */
    public InvalidParamException(String error, String[] args, ApplContext ac) {
        super(processError(error, args, ac));
        errorType = error;
    }

    /**
     * Create a new InvalidParamException.
     *
     * @param error    the error message class
     * @param message1 the first message to add
     * @param message1 the second message to add
     */
    public InvalidParamException(String error, Object message1,
                                 Object message2, ApplContext ac) {
        super(processError(error, new String[]{
                        (message1 != null) ? message1.toString() : null,
                        (message2 != null) ? message2.toString() : null},
                ac));
        errorType = error;
    }

    /**
     * Get the error type if defined
     *
     * @return a String or null if undefined
     */
    public String getErrorType() {
        return errorType;
    }

    private static String processError(String error, String[] args, ApplContext ac) {
        StringBuilder sb = new StringBuilder();
        String str = null;

        if (error != null) {
            str = ac.getMsg().getErrorString(error);
        }
        if (str == null) {
            return "can't find the error message for " + error;
        }
        int order[] = null;
        String msg_parts[] = null;
        boolean paramgenericorder = true;

        msg_parts = exceptionMessages.get(str);
        if (msg_parts != null) {
            order = exceptionOrder.get(str);
            if (order != null) {
                paramgenericorder = false;
            }
        } else {
            // replace all parameters
            try {
                Pattern p = Pattern.compile("%s\\d?");
                msg_parts = p.split(str, -1);
                Matcher m = p.matcher(str);
                int nbparam = 0;
                order = new int[10];
                paramgenericorder = true;
                while (m.find()) {
                    String group = m.group();
                    if (group.length() > 2) {
                        if (nbparam != 0 && paramgenericorder) {
                            // we got a mix of %s and %s\d, stick to %s only
                        } else {
                            paramgenericorder = false;
                            int o = Integer.parseInt(group.substring(2));
                            order[nbparam] = o;
                        }
                    } else {
                        if (!paramgenericorder) {
                            // we got a mix of %s and %s\d, stick to %s only
                            paramgenericorder = true;
                        }
                    }
                    nbparam++;
                }
                if (!paramgenericorder) {
                    // let's do extra checks
                    for (int i = 0; i < nbparam; i++) {
                        if (order[i] > nbparam || order[i] == 0) {
                            // too high or too low -> use %s only
                            paramgenericorder = true;
                            break;
                        }
                        for (int j = i + 1; j < nbparam; j++) {
                            if (order[i] == order[j]) {
                                // two times the same value... -> use %s only
                                paramgenericorder = true;
                                break;
                            }
                        }
                        if (paramgenericorder) {
                            break;
                        }
                    }
                    exceptionOrder.put(str, order);
                }
                exceptionMessages.put(str, msg_parts);
            } catch (PatternSyntaxException pex) {
            }
        }
        int j = 0;
        sb.append(msg_parts[0]);
        for (int i = 1; i < msg_parts.length; i++) {
            if (j < args.length) {
                if (paramgenericorder) {
                    sb.append(args[j++]);
                } else {
                    sb.append(args[order[j++] - 1]);
                }
            }
            sb.append(msg_parts[i]);
        }

        return sb.toString();
    }


} // InvalidParamException
