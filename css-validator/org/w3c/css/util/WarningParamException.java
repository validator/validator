// $Id$

package org.w3c.css.util;

public class WarningParamException extends InvalidParamException {
    private String message = null;
    private String[] messageArgs = null;

    public WarningParamException(String message, String messageArg) {
        this(message, new String[]{messageArg});
    }

    public WarningParamException(String message, String[] messageArgs) {
        this.message = message;
        this.messageArgs = messageArgs;
    }

    public String getMessage() {
        return message;
    }

    public String[] getMessageArgs() {
        return messageArgs;
    }
}
