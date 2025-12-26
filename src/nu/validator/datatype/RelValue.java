/*
 * Copyright (c) 2011-2025 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package nu.validator.datatype;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.text.similarity.LevenshteinDistance;
import nu.validator.vendor.relaxng.datatype.DatatypeException;

public final class RelValue extends AbstractDatatype {

    private static final LevenshteinDistance LEVENSHTEIN =
        new LevenshteinDistance(3);
    private static final int TYPO_THRESHOLD = 2;

    /**
     * IANA-registered link relation types.
     * Source: https://www.iana.org/assignments/link-relations/link-relations-1.csv
     * Last updated: 2025-12-26
     */
    private static final Set<String> registeredValues = new HashSet<>(Arrays.asList(
        "about",
        "acl",
        "alternate",
        "amphtml",
        "api-catalog",
        "appendix",
        "apple-touch-icon",
        "apple-touch-startup-image",
        "archives",
        "author",
        "blocked-by",
        "bookmark",
        "c2pa-manifest",
        "canonical",
        "chapter",
        "cite-as",
        "collection",
        "compression-dictionary",
        "contents",
        "convertedfrom",
        "copyright",
        "create-form",
        "current",
        "deprecation",
        "describedby",
        "describes",
        "disclosure",
        "dns-prefetch",
        "duplicate",
        "edit",
        "edit-form",
        "edit-media",
        "enclosure",
        "external",
        "first",
        "geofeed",
        "glossary",
        "help",
        "hosts",
        "hub",
        "ice-server",
        "icon",
        "index",
        "intervalafter",
        "intervalbefore",
        "intervalcontains",
        "intervaldisjoint",
        "intervalduring",
        "intervalequals",
        "intervalfinishedby",
        "intervalfinishes",
        "intervalin",
        "intervalmeets",
        "intervalmetby",
        "intervaloverlappedby",
        "intervaloverlaps",
        "intervalstartedby",
        "intervalstarts",
        "item",
        "last",
        "latest-version",
        "license",
        "linkset",
        "lrdd",
        "manifest",
        "mask-icon",
        "me",
        "media-feed",
        "memento",
        "micropub",
        "modulepreload",
        "monitor",
        "monitor-group",
        "next",
        "next-archive",
        "nofollow",
        "noopener",
        "noreferrer",
        "opener",
        "openid2.local_id",
        "openid2.provider",
        "original",
        "p3pv1",
        "payment",
        "pingback",
        "preconnect",
        "predecessor-version",
        "prefetch",
        "preload",
        "prerender",
        "prev",
        "prev-archive",
        "preview",
        "previous",
        "privacy-policy",
        "profile",
        "publication",
        "rdap-active",
        "rdap-bottom",
        "rdap-down",
        "rdap-top",
        "rdap-up",
        "related",
        "replies",
        "restconf",
        "ruleinput",
        "search",
        "section",
        "self",
        "service",
        "service-desc",
        "service-doc",
        "service-meta",
        "sip-trunking-capability",
        "sponsored",
        "start",
        "status",
        "stylesheet",
        "subsection",
        "successor-version",
        "sunset",
        "tag",
        "terms-of-service",
        "timegate",
        "timemap",
        "type",
        "ugc",
        "up",
        "version-history",
        "via",
        "webmention",
        "working-copy",
        "working-copy-of"
    ));

    /**
     * The singleton instance.
     */
    public static final RelValue THE_INSTANCE = new RelValue();

    /**
     * Private constructor
     */
    private RelValue() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        Set<String> tokensSeen = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        int len = literal.length();
        for (int i = 0; i < len; i++) {
            char c = literal.charAt(i);
            if (isWhitespace(c) && builder.length() > 0) {
                checkToken(literal, builder, i, tokensSeen);
                builder.setLength(0);
            } else if (!isWhitespace(c)) {
                builder.append(c);
            }
        }
        if (builder.length() > 0) {
            checkToken(literal, builder, len, tokensSeen);
        }
    }

    private void checkToken(CharSequence literal, StringBuilder builder, int i,
            Set<String> tokensSeen) throws DatatypeException {
        String token = builder.toString();
        if (tokensSeen.contains(token)) {
            throw newDatatypeException(i - 1, "Duplicate keyword ", token, ".");
        }
        tokensSeen.add(token);
        // Strip leading colon (RDFa CURIE syntax) for validation purposes
        String tokenForValidation = token.startsWith(":") ? token.substring(1) : token;
        // Accept all short values (3 characters or less) without checking
        if (tokenForValidation.length() <= 3) {
            return;
        }
        // Check if it's an exact match for a registered value
        if (registeredValues.contains(token.toLowerCase())) {
            return;
        }
        // Check for possible typos using Levenshtein distance
        String closestMatch = findClosestMatch(tokenForValidation);
        if (closestMatch != null) {
            // Found a close match - emit info-level warning
            throw newDatatypeException("Typo for \u201c" + closestMatch
                    + "\u201d?", true);
        }
    }

    private String findClosestMatch(String token) {
        String tokenLower = token.toLowerCase();
        String bestMatch = null;
        int bestDistance = Integer.MAX_VALUE;
        for (String registered : registeredValues) {
            String registeredLower = registered.toLowerCase();
            // Skip very-short registered values, to avoid false positives
            if (registeredLower.length() <= 3) {
                continue;
            }
            Integer distance = LEVENSHTEIN.apply(tokenLower, registeredLower);
            if (distance != null && distance > 0 && distance <= TYPO_THRESHOLD) {
                // Avoid false positives: only suggest if lengths are similar.
                // Allow length difference of at most 2 characters.
                int lengthDiff = Math.abs(tokenLower.length() -
                        registeredLower.length());
                if (lengthDiff > 2) {
                    continue;
                }
                // Additional check: require same first character or same last
                // character; avoids false positives like "cite" -> "item".
                boolean sameStart = tokenLower.charAt(0) == registeredLower.charAt(0);
                boolean sameEnd = tokenLower.charAt(tokenLower.length() - 1) ==
                                  registeredLower.charAt(registeredLower.length() - 1);
                if (!sameStart && !sameEnd) {
                    continue;
                }
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestMatch = registered;
                }
            }
        }
        return bestMatch;
    }

    @Override
    public String getName() {
        return "list of link-type keywords";
    }

}
