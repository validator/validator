package autotest;

// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2003.
// Please first read the full copyright statement in file COPYRIGHT.html

/**
 * Result<br />
 * Created: Jul 28, 2005 5:25:19 PM<br />
 */
public class Result {
    
    /**
     * Validity of the CSS document
     */
    boolean valid = true;
    
    /**
     * Errors count
     */
    int errors;
    
    /**
     * Warnings count
     */
    int warnings;
    
    /**
     * Returns the number of errors
     * @return Returns the errors.
     */
    public int getErrors() {
        return errors;
    }
    /**
     * Sets the number of errors
     * @param errors The errors to set.
     */
    public void setErrors(int errors) {
        this.errors = errors;
    }
    /**
     * Returns the number of warnings
     * @return Returns the warnings.
     */
    public int getWarnings() {
        return warnings;
    }
    /**
     * Sets the number of warnings
     * @param warnings The warnings to set.
     */
    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }
    /**
     * Returns the validity of the document
     * @return Returns true if the document is valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }
    /**
     * Sets the validity of the document
     * @param valid The valid to set.
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
	String res = "";
	if(valid) {
	    res += "Valid!";
	    res += "\nWarnings: " + warnings;
	}
	else {
	    res += "Errors: " + errors;
	    res += "\nWarnings: " + warnings;
	}
	return res;
    }
}
