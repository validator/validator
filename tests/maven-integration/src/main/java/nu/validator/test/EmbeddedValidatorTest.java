package nu.validator.test;

import nu.validator.client.EmbeddedValidator;
import org.xml.sax.SAXException;

import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Integration test for the validator Maven artifact.
 * 
 * This test verifies that:
 * 1. The EmbeddedValidator can be instantiated
 * 2. Shaded jing-trang classes are present (nu.validator.vendor.thaiopensource.*)
 * 3. Basic HTML validation works without NoClassDefFoundError
 * 4. The artifact brings in no log4j, and the command-line validator works
 *    without it, with nothing but its results on stderr
 */
public class EmbeddedValidatorTest {

    public static void main(String[] args) {
        System.out.println("Starting Maven Integration Test for nu.validator:validator");
        System.out.println("=============================================================");
        
        try {
            testValidHtml();
            testInvalidHtml();
            testShadedClasses();
            testNoLog4j();
            testCommandLineValidator();
            
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

    private static void testNoLog4j() throws Exception {
        System.out.println("\nTest 5: Verifying log4j is NOT on the classpath...");

        try {
            Class.forName("org.apache.log4j.Logger");
            throw new Exception("Found org.apache.log4j.Logger - the artifact should not bring in log4j or reload4j");
        } catch (ClassNotFoundException e) {
            System.out.println("✓ org.apache.log4j.Logger not found (as expected)");
        }
    }

    private static void testCommandLineValidator() throws Exception {
        System.out.println("\nTest 6: Running the command-line validator from the artifact...");

        String validHtml = "<!DOCTYPE html>\n" +
                          "<html lang=\"en\">\n" +
                          "<head><title>Test</title></head>\n" +
                          "<body><p>Hello World</p></body>\n" +
                          "</html>";
        byte[] body = validHtml.getBytes(StandardCharsets.UTF_8);

        Path file = Files.createTempFile("valid", ".html");
        Files.write(file, body);
        HttpServer server = HttpServer.create(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/valid.html", exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        try {
            runCommandLineValidator(file.toString());
            System.out.println("✓ Checked a file, with nothing on stderr");
            runCommandLineValidator("http://127.0.0.1:"
                    + server.getAddress().getPort() + "/valid.html");
            System.out.println("✓ Checked a document by URL, with nothing on stderr");
        } finally {
            server.stop(0);
            Files.delete(file);
        }
    }

    private static void runCommandLineValidator(String document) throws Exception {
        List<String> command = Arrays.asList(
                Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "-Dnu.validator.servlet.allow-forbidden-hosts=true",
                "-cp", classpath(),
                "nu.validator.client.SimpleCommandLineValidator",
                document);
        Process process = new ProcessBuilder(command)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        if (exitCode != 0 || !stderr.isEmpty()) {
            throw new Exception("Checking " + document + " exited with " + exitCode
                    + " and printed to stderr:\n" + stderr);
        }
    }

    // The classpath Maven runs this test with: the artifact and its dependencies.
    private static String classpath() throws Exception {
        List<String> entries = new ArrayList<>();
        for (URL url : ((URLClassLoader) EmbeddedValidatorTest.class.getClassLoader()).getURLs()) {
            entries.add(Paths.get(url.toURI()).toString());
        }
        return String.join(File.pathSeparator, entries);
    }
}
