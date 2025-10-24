package com.cybozu.labs.langdetect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.cybozu.labs.langdetect.util.LangProfile;
import com.cybozu.labs.langdetect.util.TagExtractor;

/**
 * Load Wikipedia's abstract XML as corpus and
 * generate its language profile in JSON format.
 * 
 * @author Nakatani Shuyo
 * 
 */
public class GenProfile {

    /**
     * Load Wikipedia abstract database file and generate its language profile
     * @param lang target language name
     * @param file target database file path
     * @return Language profile instance
     * @throws LangDetectException 
     */
    public static LangProfile loadFromWikipediaAbstract(String lang, File file) throws LangDetectException {

        LangProfile profile = new LangProfile(lang, null, null);

        BufferedReader br = null;
        try {
            InputStream is = new FileInputStream(file);
            if (file.getName().endsWith(".gz")) is = new GZIPInputStream(is);
            br = new BufferedReader(new InputStreamReader(is, "utf-8"));

            TagExtractor tagextractor = new TagExtractor("abstract", 100);

            XMLStreamReader reader = null;
            try {
                XMLInputFactory factory = XMLInputFactory.newInstance();
                reader = factory.createXMLStreamReader(br);
                while (reader.hasNext()) {
                    switch (reader.next()) {
                    case XMLStreamReader.START_ELEMENT:
                        tagextractor.setTag(reader.getName().toString());
                        break;
                    case XMLStreamReader.CHARACTERS:
                        tagextractor.add(reader.getText());
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        String text = tagextractor.closeTag();
                        if (text != null) profile.update(text);
                        break;
                    }
                }
            } catch (XMLStreamException e) {
                throw new LangDetectException(ErrorCode.TrainDataFormatError, "Training database file '" + file.getName() + "' is an invalid XML.");
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (XMLStreamException e) {}
            }
            System.out.println(lang + ":" + tagextractor.count());

        } catch (IOException e) {
            throw new LangDetectException(ErrorCode.CantOpenTrainData, "Can't open training database file '" + file.getName() + "'");
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {}
        }
        return profile;
    }


    /**
     * Load text file with UTF-8 and generate its language profile
     * @param lang target language name
     * @param file target file path
     * @return Language profile instance
     * @throws LangDetectException 
     */
    public static LangProfile loadFromText(String lang, File file) throws LangDetectException {

        LangProfile profile = new LangProfile(lang, null, null);

        BufferedReader is = null;
        try {
            is = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));

            int count = 0;
            while (is.ready()) {
                String line = is.readLine();
                profile.update(line);
                ++count;
            }

            System.out.println(lang + ":" + count);

        } catch (IOException e) {
            throw new LangDetectException(ErrorCode.CantOpenTrainData, "Can't open training database file '" + file.getName() + "'");
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {}
        }
        return profile;
    }
}
