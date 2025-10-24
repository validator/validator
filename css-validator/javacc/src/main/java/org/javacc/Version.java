// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.javacc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Supply the version number.
 */
public class Version {
  private Version() {}

  public static final String majorVersion;
  public static final String minorVersion;
  public static final String patchVersion;


  public static final String majorDotMinor;
  public static final String versionNumber;

  static {
    String major = "??";
    String minor = "??";
    String patch = "??";

    Properties props = new Properties();
    InputStream is = Version.class.getResourceAsStream("/version.properties");
    if (is != null)
    {
      try
      {
        props.load(is);
      }
      catch (IOException e)
      {
        System.err.println("Could not read version.properties: " + e);
      }
      major = props.getProperty("version.major", major);
      minor = props.getProperty("version.minor", minor);
      patch = props.getProperty("version.patch", patch);
    }

    majorVersion = major;
    minorVersion = minor;
    patchVersion = patch;
    majorDotMinor = majorVersion + "." + minorVersion;
    versionNumber = majorVersion + "." + minorVersion +
                    (patch.equals("") ? "" : "." + patch);
  }


}
