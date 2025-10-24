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

    private final int address;

    private IPv4Address(final byte[] addrBytes) {
        int addr = 0;
        addr  = addrBytes[3] & 0xFF;
        addr |= ((addrBytes[2] << 8) & 0xFF00);
        addr |= ((addrBytes[1] << 16) & 0xFF0000);
        addr |= ((addrBytes[0] << 24) & 0xFF000000);
        this.address = addr;
    }

    public static IPv4Address parseIPv4Address(final String input) throws GalimatiasParseException{
        if (input == null) {
            throw new NullPointerException("null input");
        }
        if (input.isEmpty()) {
            throw new GalimatiasParseException("empty input");
        }
        if (input.charAt(input.length() - 1) == '.') { //XXX: This case is not covered by the IPv6-mapped IPv4 case in the spec
            throw new GalimatiasParseException("IPv4 address has trailing dot");
        }
        byte[] addr = new byte[4];
        int dotsSeen = 0;
        int addrIdx = 0;
        int idx = 0;
        boolean isEOF = false;
        while (!isEOF) {
            char c = input.charAt(idx);
            Integer value = null;
            if (!isASCIIDigit(c)) {
                throw new GalimatiasParseException("Non-digit character in IPv4 address");
            }
            while (isASCIIDigit(c)) {
                final int number = c - 0x30;  // 10.3.1
                if (value == null) {          // 10.3.2
                    value = number;
                } else if (value == 0) {
                    throw new GalimatiasParseException("IPv4 address contains a leading zero");
                } else {
                    value = value * 10 + number;
                }
                idx++;                        // 10.3.3
                isEOF = idx >= input.length();
                c = (isEOF)? 0x00 : input.charAt(idx);
                if (value > 255) {            // 10.3.4
                    throw new GalimatiasParseException("Invalid value for IPv4 address");
                }
            }
            if (dotsSeen < 3 && c != '.') {
                throw new GalimatiasParseException("Illegal character in IPv4 address", idx);
            }
            idx++;
            isEOF = idx >= input.length();
            c = (isEOF)? 0x00 : input.charAt(idx);
            if (dotsSeen == 3 && idx < input.length()) {
                throw new GalimatiasParseException("IPv4 address is too long", idx);
            }
            addr[addrIdx] = (byte) (int) value;
            addrIdx++;
            dotsSeen++;
        }
        if (dotsSeen != 4) {
            throw new GalimatiasParseException("Malformed IPv4 address");
        }
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
