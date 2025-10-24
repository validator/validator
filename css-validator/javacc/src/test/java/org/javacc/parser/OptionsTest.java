package org.javacc.parser;

import java.io.File;

import junit.framework.TestCase;

/**
 * Test cases to prod at the valitity of Options a little.
 *
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class OptionsTest extends TestCase {
    public void testDefaults() {
        Options.init();
        JavaCCErrors.reInit();

//        assertEquals(49, Options.optionValues.size());

        assertEquals(true, Options.getBuildParser());
        assertEquals(true, Options.getBuildTokenManager());
        assertEquals(false, Options.getCacheTokens());
        assertEquals(false, Options.getCommonTokenAction());
        assertEquals(false, Options.getDebugLookahead());
        assertEquals(false, Options.getDebugParser());
        assertEquals(false, Options.getDebugTokenManager());
        assertEquals(true, Options.getErrorReporting());
        assertEquals(false, Options.getForceLaCheck());
        assertEquals(false, Options.getIgnoreCase());
        assertEquals(false, Options.getJavaUnicodeEscape());
        assertEquals(true, Options.getKeepLineColumn());
        assertEquals(true, Options.getSanityCheck());
        assertEquals(true, Options.getStatic());
        assertEquals(false, Options.getUnicodeInput());
        assertEquals(false, Options.getUserCharStream());
        assertEquals(false, Options.getUserTokenManager());
        assertEquals(false, Options.getTokenManagerUsesParser());

        assertEquals(2, Options.getChoiceAmbiguityCheck());
        assertEquals(1, Options.getLookahead());
        assertEquals(1, Options.getOtherAmbiguityCheck());

        assertEquals("1.5", Options.getJdkVersion());
        assertEquals(new File("."), Options.getOutputDirectory());
        assertEquals("", Options.getTokenExtends());
        assertEquals("", Options.getTokenFactory());
		assertEquals(System.getProperties().get("file.encoding"), Options.getGrammarEncoding());

        assertEquals(0, JavaCCErrors.get_warning_count());
        assertEquals(0, JavaCCErrors.get_error_count());
        assertEquals(0, JavaCCErrors.get_parse_error_count());
        assertEquals(0, JavaCCErrors.get_semantic_error_count());
    }

    public void testSetBooleanOption() {
        Options.init();
        JavaCCErrors.reInit();

        assertEquals(true, Options.getStatic());
        Options.setCmdLineOption("-NOSTATIC");
        assertEquals(false, Options.getStatic());

        assertEquals(false, Options.getJavaUnicodeEscape());
        Options.setCmdLineOption("-JAVA_UNICODE_ESCAPE:true");
        assertEquals(true, Options.getJavaUnicodeEscape());

        assertEquals(true, Options.getSanityCheck());
        Options.setCmdLineOption("-SANITY_CHECK=false");
        assertEquals(false, Options.getSanityCheck());

        assertEquals(0, JavaCCErrors.get_warning_count());
        assertEquals(0, JavaCCErrors.get_error_count());
        assertEquals(0, JavaCCErrors.get_parse_error_count());
        assertEquals(0, JavaCCErrors.get_semantic_error_count());
    }


    public void testIntBooleanOption() {
        Options.init();
        JavaCCErrors.reInit();

        assertEquals(1, Options.getLookahead());
        Options.setCmdLineOption("LOOKAHEAD=2");
        assertEquals(2, Options.getLookahead());
        assertEquals(0, JavaCCErrors.get_warning_count());
        Options.setCmdLineOption("LOOKAHEAD=0");
        assertEquals(2, Options.getLookahead());
        assertEquals(0, JavaCCErrors.get_warning_count());
        Options.setInputFileOption(null, null, Options.USEROPTION__LOOKAHEAD, Integer.valueOf(0));
        assertEquals(2, Options.getLookahead());
        assertEquals(1, JavaCCErrors.get_warning_count());

        assertEquals(0, JavaCCErrors.get_error_count());
        assertEquals(0, JavaCCErrors.get_parse_error_count());
        assertEquals(0, JavaCCErrors.get_semantic_error_count());
    }

    public void testSetStringOption() {
        Options.init();
        JavaCCErrors.reInit();

        assertEquals("", Options.getTokenExtends());
        Options.setCmdLineOption("-TOKEN_EXTENDS=java.lang.Object");
        assertEquals("java.lang.Object", Options.getTokenExtends());
        Options.setInputFileOption(null, null, Options.USEROPTION__TOKEN_EXTENDS, "Object");
        // File option does not override cmd line
        assertEquals("java.lang.Object", Options.getTokenExtends());

        Options.init();
        JavaCCErrors.reInit();

        Options.setInputFileOption(null, null, Options.USEROPTION__TOKEN_EXTENDS, "Object");
        assertEquals("Object", Options.getTokenExtends());
        Options.setCmdLineOption("-TOKEN_EXTENDS=java.lang.Object");
        assertEquals("java.lang.Object", Options.getTokenExtends());
    }

    public void testSetNonexistentOption() {
        Options.init();
        JavaCCErrors.reInit();

        assertEquals(0, JavaCCErrors.get_warning_count());
        Options.setInputFileOption(null, null, "NONEXISTENTOPTION", Boolean.TRUE );
        assertEquals(1, JavaCCErrors.get_warning_count());

        assertEquals(0, JavaCCErrors.get_error_count());
        assertEquals(0, JavaCCErrors.get_parse_error_count());
        assertEquals(0, JavaCCErrors.get_semantic_error_count());
    }

    public void testSetWrongTypeForOption() {
        Options.init();
        JavaCCErrors.reInit();

        assertEquals(0, JavaCCErrors.get_warning_count());
        assertEquals(0, JavaCCErrors.get_error_count());
        Options.setInputFileOption(null, null, Options.USEROPTION__STATIC, Integer.valueOf(8));
        assertEquals(1, JavaCCErrors.get_warning_count());

        assertEquals(0, JavaCCErrors.get_error_count());
        assertEquals(0, JavaCCErrors.get_parse_error_count());
        assertEquals(0, JavaCCErrors.get_semantic_error_count());
    }

    public void testNormalize() {
        Options.init();
        JavaCCErrors.reInit();

        assertEquals(false, Options.getDebugLookahead());
        assertEquals(false, Options.getDebugParser());

        Options.setCmdLineOption("-DEBUG_LOOKAHEAD=TRUE");
        Options.normalize();

        assertEquals(true, Options.getDebugLookahead());
        assertEquals(true, Options.getDebugParser());

        assertEquals(0, JavaCCErrors.get_warning_count());
        assertEquals(0, JavaCCErrors.get_error_count());
        assertEquals(0, JavaCCErrors.get_parse_error_count());
        assertEquals(0, JavaCCErrors.get_semantic_error_count());
    }

    public void testOptionsString() throws ParseException {
      Options.init();
      JavaCCErrors.reInit();

      Options.setCmdLineOption("-STATIC=False");
      Options.setCmdLineOption("-IGNORE_CASE=True");
      String[] options = {
    	   Options.USEROPTION__STATIC,
          Options.USEROPTION__IGNORE_CASE
      };
      String optionString = Options.getOptionsString(options);
      assertEquals("STATIC=false,IGNORE_CASE=true", optionString);
    }
}
