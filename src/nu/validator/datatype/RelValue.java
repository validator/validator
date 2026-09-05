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

import nu.validator.vendor.relaxng.datatype.DatatypeException;

public final class RelValue extends AbstractDatatype {

    private static final int TYPO_THRESHOLD = 2;

    /**
     * Shortest token for which two edits still count as a typo. Below this,
     * two edits reach an unrelated word — "json" reaches "icon", "atom"
     * reaches "item", and "share" reaches "start" — so only one edit does.
     */
    private static final int MIN_LENGTH_FOR_TWO_EDITS = 7;

    /**
     * IANA-registered link relation types.
     * Source: https://www.iana.org/assignments/link-relations/link-relations-1.csv
     * plus "sitemap"
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
        "sitemap",
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
        // Check for possible typos using edit distance
        String closestMatch = findClosestMatch(tokenForValidation);
        if (closestMatch != null) {
            // Found a close match - emit info-level warning
            throw newDatatypeException(" Typo for “" + closestMatch
                    + "”?", true);
        }
    }

    private String findClosestMatch(String token) {
        String tokenLower = token.toLowerCase();
        int threshold = tokenLower.length() < MIN_LENGTH_FOR_TWO_EDITS ? 1
                : TYPO_THRESHOLD;
        String bestMatch = null;
        int bestDistance = Integer.MAX_VALUE;
        for (String registered : registeredValues) {
            String registeredLower = registered.toLowerCase();
            // Skip very-short registered values, to avoid false positives
            if (registeredLower.length() <= 3) {
                continue;
            }
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
            int distance = editDistance(tokenLower, registeredLower);
            if (distance > 0 && distance <= threshold
                    && distance < bestDistance) {
                bestDistance = distance;
                bestMatch = registered;
            }
        }
        return bestMatch;
    }

    /**
     * Optimal string alignment distance between two strings: the number of
     * insertions, deletions, substitutions, and transpositions of adjacent
     * characters needed to turn one into the other.
     *
     * A transposition costs one edit, not the two that plain Levenshtein
     * distance charges for it — so "iocn" stays within one edit of "icon",
     * which keeps swapped characters detectable at the tighter threshold that
     * short tokens get.
     */
    private static int editDistance(String a, String b) {
        int aLength = a.length();
        int bLength = b.length();
        int[] twoRowsBack = new int[bLength + 1];
        int[] previousRow = new int[bLength + 1];
        int[] currentRow = new int[bLength + 1];
        for (int j = 0; j <= bLength; j++) {
            previousRow[j] = j;
        }
        for (int i = 1; i <= aLength; i++) {
            currentRow[0] = i;
            for (int j = 1; j <= bLength; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                currentRow[j] = Math.min(
                        Math.min(previousRow[j] + 1, currentRow[j - 1] + 1),
                        previousRow[j - 1] + cost);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2)
                        && a.charAt(i - 2) == b.charAt(j - 1)) {
                    currentRow[j] = Math.min(currentRow[j],
                            twoRowsBack[j - 2] + 1);
                }
            }
            int[] scratch = twoRowsBack;
            twoRowsBack = previousRow;
            previousRow = currentRow;
            currentRow = scratch;
        }
        return previousRow[bLength];
    }

    @Override
    public String getName() {
        return "list of link-type keywords";
    }

}
