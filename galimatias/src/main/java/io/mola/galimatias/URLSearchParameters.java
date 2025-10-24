/**
 * Copyright (c) 2013-2014 Santiago M. Mola <santi@mola.io>
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package io.mola.galimatias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class URLSearchParameters implements Iterable<NameValue> {

    private static final List<NameValue> EMPTY_NAME_VALUES = Collections.unmodifiableList(new ArrayList<NameValue>(0));

    private final List<NameValue> nameValues;

    URLSearchParameters(final String query) {
        if (query != null && !query.isEmpty()) {
            nameValues = Collections.unmodifiableList(FormURLEncodedParser.parse(query));
        } else {
            nameValues = EMPTY_NAME_VALUES;
        }
    }

    URLSearchParameters(final List<NameValue> nameValues) {
        if (nameValues == null) {
            throw new NullPointerException("nameValues");
        }
        this.nameValues = Collections.unmodifiableList(nameValues);
    }

    public URLSearchParameters withAppended(final String name, final String value) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
        return withAppended(new NameValue(name, value));
    }

    public URLSearchParameters withAppended(final NameValue nameValue) {
        if (nameValue == null) {
            throw new NullPointerException("nameValue");
        }
        final List<NameValue> newNameValuesList = new ArrayList<NameValue>(this.nameValues.size() + 1);
        for (final NameValue nv : nameValues) {
            newNameValuesList.add(nv);
        }
        newNameValuesList.add(nameValue);
        return new URLSearchParameters(newNameValuesList);
    }

    public URLSearchParameters with(final String name, final String value) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
       return with(new NameValue(name, value));
    }

    public URLSearchParameters with(final NameValue nameValue) {
        if (nameValue == null) {
            throw new NullPointerException("nameValue");
        }
        final List<NameValue> newNameValuesList = new ArrayList<NameValue>(this.nameValues.size() + 1);
        final String name = nameValue.name();
        for (final NameValue nv : nameValues) {
            if (!nv.name().equals(name)) {
                newNameValuesList.add(nv);
            }
        }
        newNameValuesList.add(nameValue);
        return new URLSearchParameters(newNameValuesList);
    }

    public URLSearchParameters without(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        final List<NameValue> newNameValuesList = new ArrayList<NameValue>(this.nameValues.size());
        for (final NameValue nv : nameValues) {
            if (!nv.name().equals(name)) {
                newNameValuesList.add(nv);
            }
        }
        return new URLSearchParameters(newNameValuesList);
    }

    public String get(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        for (final NameValue nv : nameValues) {
            if (name.equals(nv.name())) {
                return nv.value();
            }
        }
        return null;
    }

    public List<String> getAll(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        final List<String> result = new ArrayList<String>();
        for (final NameValue nv : nameValues) {
            if (name.equals(nv.name())) {
                result.add(nv.value());
            }
        }
        return result;
    }

    public boolean has(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        for (final NameValue nv : nameValues) {
            if (name.equals(nv.name())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<NameValue> iterator() {
        return nameValues.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        URLSearchParameters that = (URLSearchParameters) o;

        if (!nameValues.equals(that.nameValues)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return nameValues.hashCode();
    }
}