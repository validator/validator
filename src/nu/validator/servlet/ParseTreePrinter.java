package nu.validator.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nu.validator.htmlparser.test.ListErrorHandler;
import nu.validator.htmlparser.test.TreeDumpContentHandler;
import nu.validator.xml.NullEntityResolver;
import nu.validator.xml.PrudentHttpEntityResolver;
import nu.validator.xml.TypedInputSource;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactory;

import fi.iki.hsivonen.gnu.xml.aelfred2.SAXDriver;

public class ParseTreePrinter {
    
    private static final String FORM_HTML = "<!DOCTYPE html><title>Parse Tree Dump</title><form><p><input type='url' name='doc' id='doc' pattern='(?:https?://.+)?'> <input name='submit' value='Print Tree' type='submit' id='submit'></form>";
    
    private final HttpServletRequest request;

    private final HttpServletResponse response;

    /**
     * @param request
     * @param response
     */
    public ParseTreePrinter(final HttpServletRequest request,
            final HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    private String scrubUrl(String urlStr) {
        if (urlStr == null) {
            return null;
        }
        try {
            return IRIFactory.iriImplementation().construct(urlStr).toASCIIString();
        } catch (IRIException e) {
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public void service() throws IOException {
        String document = scrubUrl(request.getParameter("doc"));
        document = ("".equals(document)) ? null : document;
        Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        if (document == null) {
            response.setContentType("text/html; charset=utf-8");
            writer.write(FORM_HTML);
            writer.flush();
            writer.close();
            return;
        } else {
            response.setContentType("text/plain; charset=utf-8");
            try {
            PrudentHttpEntityResolver httpRes = new PrudentHttpEntityResolver(
                    2048 * 1024, false, null);
            httpRes.setAllowGenericXml(false);
            httpRes.setAcceptAllKnownXmlTypes(false);
            httpRes.setAllowHtml(true);
            httpRes.setAllowXhtml(true);
            TypedInputSource documentInput = (TypedInputSource) httpRes.resolveEntity(
                    null, document);
            String type = documentInput.getType();
            XMLReader parser;
            if ("text/html".equals(type)) {
                writer.write("HTML parser\n\n#document\n");
                parser = new nu.validator.htmlparser.sax.HtmlParser();
            } else if ("application/xhtml+xml".equals(type)) {
                writer.write("XML parser\n\n#document\n");
                parser = new SAXDriver();
                parser.setFeature(
                        "http://xml.org/sax/features/external-general-entities",
                        false);
                parser.setFeature(
                        "http://xml.org/sax/features/external-parameter-entities",
                        false);
                parser.setEntityResolver(new NullEntityResolver());
            } else {
                writer.write("Unsupported content type.\n");
                writer.flush();
                writer.close();
                return;
            }
            TreeDumpContentHandler treeDumpContentHandler = new TreeDumpContentHandler(writer, false);
            ListErrorHandler listErrorHandler = new ListErrorHandler();
            parser.setContentHandler(treeDumpContentHandler);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", treeDumpContentHandler);
            parser.setErrorHandler(listErrorHandler);
            parser.parse(documentInput);
            writer.write("#errors\n");
            for (String err : listErrorHandler.getErrors()) {
                writer.write(err);
                writer.write('\n');
            }
            } catch (SAXException e) {
                writer.write("Exception:\n");
                writer.write(e.getMessage());
                writer.write("\n");
            } catch (IOException e) {
                writer.write("Exception:\n");
                writer.write(e.getMessage());
                writer.write("\n");
            } finally {
                writer.flush();
                writer.close();
            }
        }
    }

}
