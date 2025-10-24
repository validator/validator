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

package org.javacc.jjdoc;

import org.javacc.parser.JavaCCGlobals;

/**
 * Global variables for JJDoc.
 *
 */
public class JJDocGlobals extends JavaCCGlobals {
  /**
   * The name of the input file.
   */
  public static String input_file;
  /**
   * The name of the output file.
   */
  public static String output_file;

  /**
   * The Generator to create output with.
   */
  public static Generator generator;

  /**
   * @param generator
   *        The generator to set.
   */
  public static void setGenerator(Generator generator) {
    JJDocGlobals.generator = generator;
  }

  /**
   * The commandline option is either TEXT or not, but the generator might
   * have been set to some other Generator using the setGenerator method.
   *
   * @return the generator configured in options or set by setter.
   */
  public static Generator getGenerator() {
    if (generator == null) {
      if (JJDocOptions.getText()) {
        generator = new TextGenerator();
      } else if (JJDocOptions.getBNF()) {
	    generator = new BNFGenerator();
	  } else if (JJDocOptions.getXText()){
	      generator = new XTextGenerator();
	  } else {
        generator = new HTMLGenerator();
      }
    } else {
      if (JJDocOptions.getText()) {
        if(generator instanceof HTMLGenerator) {
          generator = new TextGenerator();
        }
      } else if (JJDocOptions.getBNF()) {
		generator = new BNFGenerator();
      } else if (JJDocOptions.getXText()){
          generator = new XTextGenerator();
	  } else {
        if(generator instanceof TextGenerator) {
          generator = new HTMLGenerator();
        }
      }
    }
    return generator;
  }

  /**
   * Log informational messages.
   * @param message the message to log
   */
  public static void debug(String message) {
    getGenerator().debug(message);
  }

  /**
   * Log informational messages.
   * @param message the message to log
   */
  public static void info(String message) {
    getGenerator().info(message);
  }

  /**
   * Log error messages.
   * @param message the message to log
   */
  public static void error(String message) {
    getGenerator().error(message);
  }


}
