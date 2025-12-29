package nu.validator.test;

import nu.validator.client.EmbeddedValidator;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Integration test for the validator Maven artifact.
 * 
 * This test verifies that:
 * 1. The EmbeddedValidator can be instantiated
 * 2. Shaded jing-trang classes are present (nu.validator.vendor.thaiopensource.*)
 * 3. Basic HTML validation works without NoClassDefFoundError
 */
public class EmbeddedValidatorTest {

    public static void main(String[] args) {
        System.out.println("Starting Maven Integration Test for nu.validator:validator");
        System.out.println("=============================================================");
        
        try {
            testValidHtml();
            testInvalidHtml();
            testShadedClasses();
            
            System.out.println("\n✅ All tests passed!");
            System.exit(0);
            
        } catch (Exception e) {
            System.err.println("\n❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testValidHtml() throws Exception {
        System.out.println("\nTest 1: Validating valid HTML...");
        
        String validHtml = "<!DOCTYPE html>\n" +
                          "<html lang=\"en\">\n" +
                          "<head><title>Test</title></head>\n" +
                          "<body><p>Hello World</p></body>\n" +
                          "</html>";
        
        EmbeddedValidator validator = new EmbeddedValidator();
        validator.setOutputFormat(EmbeddedValidator.OutputFormat.GNU);
        
        try {
            String output = validator.validate(
                new ByteArrayInputStream(validHtml.getBytes(StandardCharsets.UTF_8))
            );
            
            if (!output.isEmpty()) {
                throw new Exception("Valid HTML should not produce validation errors. Output: " + output);
            }
            
            System.out.println("✓ Valid HTML validated successfully (no errors)");
            
        } catch (SAXException e) {
            throw new Exception("SAXException during valid HTML validation", e);
        } catch (IOException e) {
            throw new Exception("IOException during valid HTML validation", e);
        }
    }

    private static void testInvalidHtml() throws Exception {
        System.out.println("\nTest 2: Validating invalid HTML...");
        
        String invalidHtml = "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<head><title>Test</title></head>\n" +
                            "<body><p>Unclosed paragraph</body>\n" +
                            "</html>";
        
        EmbeddedValidator validator = new EmbeddedValidator();
        validator.setOutputFormat(EmbeddedValidator.OutputFormat.GNU);
        
        try {
            String output = validator.validate(
                new ByteArrayInputStream(invalidHtml.getBytes(StandardCharsets.UTF_8))
            );
            
            if (output.isEmpty()) {
                throw new Exception("Invalid HTML should produce validation errors");
            }
            
            System.out.println("✓ Invalid HTML detected errors as expected");
            System.out.println("  Output: " + output.substring(0, Math.min(100, output.length())) + "...");
            
        } catch (SAXException e) {
            throw new Exception("SAXException during invalid HTML validation", e);
        } catch (IOException e) {
            throw new Exception("IOException during invalid HTML validation", e);
        }
    }

    private static void testShadedClasses() throws Exception {
        System.out.println("\nTest 3: Verifying shaded jing-trang classes are present...");
        
        try {
            Class.forName("nu.validator.vendor.thaiopensource.validate.Schema");
            System.out.println("✓ Found shaded class: nu.validator.vendor.thaiopensource.validate.Schema");
            
            Class.forName("nu.validator.vendor.thaiopensource.datatype.xsd.DatatypeLibraryFactoryImpl");
            System.out.println("✓ Found shaded class: nu.validator.vendor.thaiopensource.datatype.xsd.DatatypeLibraryFactoryImpl");
            
            Class.forName("nu.validator.vendor.relaxng.datatype.DatatypeLibraryFactory");
            System.out.println("✓ Found shaded class: nu.validator.vendor.relaxng.datatype.DatatypeLibraryFactory");
            
        } catch (ClassNotFoundException e) {
            throw new Exception("Shaded class not found - Maven artifact is missing shaded jing-trang classes: " + e.getMessage(), e);
        }
        
        System.out.println("\nTest 4: Verifying unshaded classes are NOT present...");
        
        try {
            Class.forName("com.thaiopensource.validate.Schema");
            throw new Exception("Found unshaded class com.thaiopensource.validate.Schema - shading was not applied correctly!");
        } catch (ClassNotFoundException e) {
            System.out.println("✓ Unshaded class com.thaiopensource.validate.Schema not found (as expected)");
        }
    }
}
