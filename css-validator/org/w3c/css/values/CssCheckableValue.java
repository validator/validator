// $Id$
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values;

import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

public abstract class CssCheckableValue extends CssValue {

    abstract public boolean isPositive();

    abstract public boolean isStrictlyPositive();

    abstract public boolean isZero();

    boolean contains_variable = false;

    public boolean hasCssVariable() {
        return contains_variable;
    }

    public void markCssVariable() {
        contains_variable = true;
    }

    /**
     * check if the value is positive or null
     *
     * @param ac         the validation context
     * @param callername the String value of the caller (property / media query / ...)
     * @throws InvalidParamException
     */
    public void checkPositiveness(ApplContext ac, String callername)
            throws InvalidParamException {
        if (!isPositive()) {
            throw new InvalidParamException("negative-value",
                    toString(), callername, ac);
        }
    }

    /**
     * check if the value is positive or null
     *
     * @param ac       the validation context
     * @param property the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkPositiveness(ApplContext ac, CssProperty property)
            throws InvalidParamException {
        checkPositiveness(ac, property.getPropertyName());
    }

    /**
     * check if the value is positive or null
     *
     * @param ac           the validation context
     * @param mediafeature the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkPositiveness(ApplContext ac, MediaFeature mediafeature)
            throws InvalidParamException {
        checkPositiveness(ac, mediafeature.getFeatureName());
    }


    /**
     * check if the value is strictly positive
     *
     * @param ac         the validation context
     * @param callername the string value it is defined in
     * @throws InvalidParamException
     */
    public void checkStrictPositiveness(ApplContext ac, String callername)
            throws InvalidParamException {
        if (!isStrictlyPositive()) {
            throw new InvalidParamException("strictly-positive",
                    toString(), callername, ac);
        }
    }

    /**
     * check if the value is strictly positive
     *
     * @param ac           the validation context
     * @param mediafeature the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkStrictPositiveness(ApplContext ac, MediaFeature mediafeature)
            throws InvalidParamException {
        checkStrictPositiveness(ac, mediafeature.getFeatureName());
    }

    /**
     * check if the value is strictly positive
     *
     * @param ac       the validation context
     * @param property the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkStrictPositiveness(ApplContext ac, CssProperty property)
            throws InvalidParamException {
        checkStrictPositiveness(ac, property.getPropertyName());
    }

    /**
     * warn if the value is not positive or null
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     */
    public boolean warnPositiveness(ApplContext ac, String callername) {
        if (!isPositive()) {
            ac.getFrame().addWarning("negative", new String[]{toString(), callername});
            return false;
        }
        return true;
    }

    /**
     * warn if the value is not positive or null
     *
     * @param ac       the validation context
     * @param property the property the value is defined in
     */
    public boolean warnPositiveness(ApplContext ac, CssProperty property) {
        return warnPositiveness(ac, property.getPropertyName());
    }

    /**
     * warn if the value is not positive or null
     *
     * @param ac           the validation context
     * @param mediafeature the property the value is defined in
     */
    public boolean warnPositiveness(ApplContext ac, MediaFeature mediafeature) {
        return warnPositiveness(ac, mediafeature.getFeatureName());
    }


    /**
     * check if the value is equal to zero
     *
     * @param ac       the validation context
     * @param messages an array of Strings
     * @throws InvalidParamException
     */
    public void checkEqualsZero(ApplContext ac, String[] messages)
            throws InvalidParamException {
        if (!isZero()) {
            throw new InvalidParamException("zero", messages, ac);
        }
    }

    /**
     * check if the value is equal to zero
     *
     * @param ac         the validation context
     * @param callername the String value of the object it is defined in
     * @throws InvalidParamException
     */
    public void checkEqualsZero(ApplContext ac, String callername)
            throws InvalidParamException {
        checkEqualsZero(ac, new String[]{toString(), callername});
    }

    /**
     * check if the value is equal to zero
     *
     * @param ac       the validation context
     * @param property the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkEqualsZero(ApplContext ac, CssProperty property)
            throws InvalidParamException {
        checkEqualsZero(ac, property.getPropertyName());
    }

    /**
     * check if the value is equal to zero
     *
     * @param ac           the validation context
     * @param mediafeature the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkEqualsZero(ApplContext ac, MediaFeature mediafeature)
            throws InvalidParamException {
        checkEqualsZero(ac, mediafeature.getFeatureName());
    }


    /**
     * warn if the value is not zero
     *
     * @param ac       the validation context
     * @param messages an array of Strings
     */
    public boolean warnEqualsZero(ApplContext ac, String[] messages) {
        if (!isZero()) {
            ac.getFrame().addWarning("zero", messages);
            return false;
        }
        return true;
    }

    /**
     * warn if the value is not zero
     *
     * @param ac         the validation context
     * @param callername the String value of the object it is defined in
     */
    public boolean warnEqualsZero(ApplContext ac, String callername) {
        return warnEqualsZero(ac, new String[]{callername});
    }

    /**
     * warn if the value is not zero
     *
     * @param ac       the validation context
     * @param property the property the value is defined in
     */
    public boolean warnEqualsZero(ApplContext ac, CssProperty property) {
        return warnEqualsZero(ac, property.getPropertyName());
    }

    /**
     * warn if the value is not zero
     *
     * @param ac           the validation context
     * @param mediafeature the property the value is defined in
     */
    public boolean warnEqualsZero(ApplContext ac, MediaFeature mediafeature) {
        return warnEqualsZero(ac, mediafeature.getFeatureName());
    }

    public boolean isInteger() {
        return false;
    }

    /**
     * check if the value is an integer
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkInteger(ApplContext ac, String callername)
            throws InvalidParamException {
    }

    /**
     * check if the value is an integer
     *
     * @param ac       the validation context
     * @param property the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkInteger(ApplContext ac, CssProperty property)
            throws InvalidParamException {
        checkInteger(ac, property.getPropertyName());
    }

    /**
     * check if the value is an integer
     *
     * @param ac           the validation context
     * @param mediafeature the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkInteger(ApplContext ac, MediaFeature mediafeature)
            throws InvalidParamException {
        checkPositiveness(ac, mediafeature.getFeatureName());
    }

    /**
     * set the native value
     *
     * @param v the BigDecimal
     */
    public abstract void setValue(BigDecimal v);

    /**
     * Get this value as acheckable value
     *
     * @return
     */
    public CssCheckableValue getCheckableValue() {
        return this;
    }
}
