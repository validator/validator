// $Id$
//
// (c) COPYRIGHT MIT, ECRIM and Keio University, 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css3.media;

import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

/**
 * @spec http://www.w3.org/TR/2012/REC-view-mode-20120619/#the--view-mode--media-feature
 * @deprecated
 */
@Deprecated
public class ViewMode extends MediaFeature {

	static ArrayList<CssIdent> allowed_values;
	static final String specval[] = {"windowed", "floating", "fullscreen", "maximized", "minimized"};

	static {
		allowed_values = new ArrayList<CssIdent>();
		for (String val : specval) {
			allowed_values.add(CssIdent.getIdent(val));
		}
	}

	/**
	 * Create a new ViewMode
	 */
	public ViewMode() {
	}

	/**
	 * Create a new ViewMode.
	 *
	 * @param expression The expression for this media feature
	 * @throws org.w3c.css.util.InvalidParamException
	 *          Values are incorrect
	 */
	public ViewMode(ApplContext ac, String modifier,
					CssExpression expression, boolean check)
			throws InvalidParamException {

		ac.getFrame().addWarning("deprecatedmediafeature", getFeatureName());
		if (modifier != null) {
			throw new InvalidParamException("nomodifiermedia",
					getFeatureName(), ac);
		}

		if (expression != null) {
			if (expression.getCount() > 1) {
				throw new InvalidParamException("unrecognize", ac);
			}
            if (expression.getCount() == 0) {
                throw new InvalidParamException("few-value", getFeatureName(), ac);
            }
			CssValue val = expression.getValue();

			switch (val.getType()) {
				case CssTypes.CSS_IDENT:
					int idx = allowed_values.indexOf(val.getIdent());
					if (idx != -1) {
						value = allowed_values.get(idx);
						break;
					}
					// let it flow through the exception
				default:
					throw new InvalidParamException("value", expression.getValue(),
							getFeatureName(), ac);
			}
		} else {
			// TODO add a warning for value less mediafeature that makes no sense
		}
	}

	public ViewMode(ApplContext ac, String modifier, CssExpression expression)
			throws InvalidParamException {
		this(ac, modifier, expression, false);
	}

	// just in case someone wants to call it externally...
	public void setModifier(ApplContext ac, String modifier)
			throws InvalidParamException {
		throw new InvalidParamException("nomodifiermedia",
				getFeatureName(), ac);
	}

	/**
	 * Returns the value of this media feature.
	 */

	public Object get() {
		return value;
	}

	/**
	 * Returns the name of this media feature.
	 */
	public final String getFeatureName() {
		return "view-mode";
	}

	/**
	 * Compares two media features for equality.
	 *
	 * @param other The other media features.
	 */
	public boolean equals(MediaFeature other) {
		try {
			ViewMode mo = (ViewMode) other;
			return (((value == null) && (mo.value == null)) || ((value != null) && value.equals(mo.value)));
		} catch (ClassCastException cce) {
			return false;
		}

	}
}
