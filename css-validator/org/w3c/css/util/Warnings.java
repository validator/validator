//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Controls all warnings in the validator
 *
 * @version $Revision$
 * @see java.util.Vector
 */
public final class Warnings {
    private ArrayList<Warning> warningData = new ArrayList<Warning>(16);
    private int ignoredWarningCount = 0;
    private int warningLevel = 0;

    public Warnings() {
    }

    public Warnings(int level) {
        this.warningLevel = level;
    }

    public int getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(int warningLevel) {
        this.warningLevel = warningLevel;
    }

    /**
     * Add a warning.
     *
     * @param warn the warning
     */
    public final void addWarning(Warning warn) {
        if (warn.getLevel() > warningLevel) {
            ignoredWarningCount++;
        } else {
            warningData.add(warn);
        }
    }

    /**
     * Add warnings.
     *
     * @param warnings All warnings
     */
    public final void addWarnings(Warnings warnings) {
        //resize(warnings.warningCount);
        warningData.addAll(warnings.warningData);
    }

    /**
     * Get the number of warnings
     *
     * @return the number of warnings
     */
    public final int getWarningCount() {
        return warningData.size();
    }

    /**
     * Get the number of ignored warnings
     * (not corresponding to the warning level)
     *
     * @return the number of ignored warnings
     */
    public final int getIgnoredWarningCount() {
        return ignoredWarningCount;
    }

    /**
     * Get an array with all warnings.
     */
    public final Warning[] getWarnings() {
        Warning out[] = new Warning[warningData.size()];
        warningData.toArray(out);
        return out;
    }

    /**
     * Sort all warnings by line and level
     */
    public final void sort() {
        Collections.sort(warningData);
    }

    /**
     * Get a warning with an index.
     *
     * @param index the warning index.
     */
    public final Warning getWarningAt(int index) {
        return warningData.get(index);
    }
}
