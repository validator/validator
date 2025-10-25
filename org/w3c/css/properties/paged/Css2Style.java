//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 */
package org.w3c.css.properties.paged;


/**
 * @version $Revision$
 */
public class Css2Style extends org.w3c.css.properties.css2.Css2Style {

    Page page;
    PageATSC pageATSC;

    /**
     * Get the page property
     */
    public final Page getPage() {
        if (page == null) {
            page =
                    (Page) style.CascadingOrder(new Page(),
                            style, selector);
        }
        return page;
    }

    public final PageATSC getPageATSC() {
        if (pageATSC == null) {
            pageATSC =
                    (PageATSC) style.CascadingOrder(new PageATSC(),
                            style, selector);
        }
        return pageATSC;
    }
}
