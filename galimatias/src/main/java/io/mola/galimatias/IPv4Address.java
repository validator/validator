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

import java.net.Inet4Address;
import java.net.UnknownHostException;

import static io.mola.galimatias.URLUtils.isASCIIDigit;

public class IPv4Address extends Host {

    private static final long serialVersionUID = 1L;

    // Sentinel returned by parseIPv4Number for input that is not a number.
    private static final long IPV4_NUMBER_FAILURE = -1L;

    private final int address;

    private IPv4Address(final byte[] addrBytes) {
        int addr = 0;
        addr  = addrBytes[3] & 0xFF;
        addr |= ((addrBytes[2] << 8) & 0xFF00);
        addr |= ((addrBytes[1] << 16) & 0xFF0000);
        addr |= ((addrBytes[0] << 24) & 0xFF000000);
        this.address = addr;
    }

    /**
     * The ends-in-a-number checker from the URL Standard.
     *
     * @see <a href="https://url.spec.whatwg.org/#ends-in-a-number-checker">
     *      https://url.spec.whatwg.org/#ends-in-a-number-checker</a>
     */
    static boolean endsInANumber(final String input) {
        String[] parts = input.split("\\.", -1);
        int size = parts.length;
        if (parts[size - 1].isEmpty()) {
            if (size == 1) {
                return false;
            }
            size--;
        }
        String last = parts[size - 1];
        if (!last.isEmpty() && isAllASCIIDigits(last)) {
            return true;
        }
        return parseIPv4Number(last, new boolean[1]) != IPV4_NUMBER_FAILURE;
    }

    private static boolean isAllASCIIDigits(final String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isASCIIDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * The IPv4 number parser from the URL Standard. Returns the parsed number,
     * or {@link #IPV4_NUMBER_FAILURE} for a part that is not a number. When a
     * non-decimal radix prefix (0x or a leading 0) is used, validationError[0]
     * is set to true.
     *
     * @see <a href="https://url.spec.whatwg.org/#ipv4-number-parser">
     *      https://url.spec.whatwg.org/#ipv4-number-parser</a>
     */
    private static long parseIPv4Number(final String input,
            final boolean[] validationError) {
        if (input.isEmpty()) {
            return IPV4_NUMBER_FAILURE;
        }
        int radix = 10;
        String number = input;
        if (number.length() >= 2 && number.charAt(0) == '0'
                && (number.charAt(1) == 'x' || number.charAt(1) == 'X')) {
            validationError[0] = true;
            number = number.substring(2);
            radix = 16;
        } else if (number.length() >= 2 && number.charAt(0) == '0') {
            validationError[0] = true;
            number = number.substring(1);
            radix = 8;
        }
        if (number.isEmpty()) {
            return 0;
        }
        long value = 0;
        for (int i = 0; i < number.length(); i++) {
            int digit = Character.digit(number.charAt(i), radix);
            if (digit < 0) {
                return IPV4_NUMBER_FAILURE;
            }
            value = value * radix + digit;
            // No valid IPv4 number exceeds 32 bits; stop before overflow.
            if (value > 0xFFFFFFFFL) {
                return IPV4_NUMBER_FAILURE;
            }
        }
        return value;
    }

    /**
     * The IPv4 parser from the URL Standard. In addition to the failures the
     * standard defines, this treats validation errors (non-decimal parts and
     * leading zeros) as failures too, since this parser backs a conformance
     * checker for which a validation error means non-conforming.
     *
     * @see <a href="https://url.spec.whatwg.org/#concept-ipv4-parser">
     *      https://url.spec.whatwg.org/#concept-ipv4-parser</a>
     */
    public static IPv4Address parseIPv4Address(final String input) throws GalimatiasParseException {
        if (input == null) {
            throw new NullPointerException("null input");
        }
        if (input.isEmpty()) {
            throw new GalimatiasParseException("empty input");
        }
        String[] parts = input.split("\\.", -1);
        int size = parts.length;
        if (parts[size - 1].isEmpty()) {
            // A trailing dot is a validation error; reject it in strict mode.
            throw new GalimatiasParseException("IPv4 address has a trailing dot");
        }
        if (size > 4) {
            throw new GalimatiasParseException("IPv4 address has too many parts");
        }
        long[] numbers = new long[size];
        boolean[] validationError = new boolean[1];
        for (int i = 0; i < size; i++) {
            long value = parseIPv4Number(parts[i], validationError);
            if (value == IPV4_NUMBER_FAILURE) {
                throw new GalimatiasParseException(
                        "Invalid number in IPv4 address");
            }
            numbers[i] = value;
        }
        if (validationError[0]) {
            throw new GalimatiasParseException(
                    "IPv4 address contains a non-decimal or leading-zero part");
        }
        for (int i = 0; i < size - 1; i++) {
            if (numbers[i] > 255) {
                throw new GalimatiasParseException(
                        "IPv4 address part out of range");
            }
        }
        long last = numbers[size - 1];
        // The last part fills the bytes the earlier parts did not: it must be
        // less than 256 ** (5 - size), i.e. 2 ** (8 * (5 - size)).
        if (last >= (1L << (8 * (5 - size)))) {
            throw new GalimatiasParseException("IPv4 address part out of range");
        }
        long ipv4 = last;
        for (int i = 0; i < size - 1; i++) {
            ipv4 += numbers[i] << (8 * (3 - i));
        }
        byte[] addr = new byte[] {
                (byte) ((ipv4 >> 24) & 0xFF),
                (byte) ((ipv4 >> 16) & 0xFF),
                (byte) ((ipv4 >> 8) & 0xFF),
                (byte) (ipv4 & 0xFF)
        };
        return new IPv4Address(addr);
    }

    /**
     * Convert to @{java.net.InetAddress}.
     *
     * @return The IPv4 address as a @{java.net.InetAddress}.
     */
    public Inet4Address toInetAddress() throws UnknownHostException {
        return (Inet4Address) Inet4Address.getByAddress(getBytes());
    }

    /**
     * Convert from @{java.net.Inet4Address}.
     *
     * @param inet4Address The IPv4 address as a @{java.net.Inet4Address}.
     * @return The IPv4 address as a @{IPv4Address}.
     */
    public static IPv4Address fromInet4Adress(final Inet4Address inet4Address) {
        return new IPv4Address(inet4Address.getAddress());
    }

    private byte[] getBytes() {
        return new byte[] {
                (byte) (address >> 24 & 0x00FF),
                (byte) (address >> 16 & 0x00FF),
                (byte) (address >> 8 & 0x00FF),
                (byte) (address & 0x00FF)
        };
    }

    @Override
    public String toString() {
        byte[] bytes = getBytes();
        return String.format("%d.%d.%d.%d", bytes[0] & 0x00FF, bytes[1] & 0x00FF, bytes[2] & 0x00FF, bytes[3] & 0x00FF);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IPv4Address)) {
            return false;
        }
        return this.address == ((IPv4Address) obj).address;
    }

    @Override
    public int hashCode() {
        return address;
    }

}
