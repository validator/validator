package com.cybozu.labs.nutch.plugin;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.lucene.LuceneWriter;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.Parse;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

/**
 * 
 * Language Detection Extension for Apache Nutch
 *   using Language Detection Library ( http://code.google.com/p/language-detection/ ).
 * 
 * For HTMLLanguageParser and LanguageQueryFilter, 
 * the extensions of the Nutch's standard language-identifier plugin can be used without modifications, 
 * so it is provides an extension of LanguageIdentifier only.
 * 
 * @author Nakatani Shuyo
 *
 */
public class LanguageDetectionFilter implements IndexingFilter {
	private static final int TEXTSIZE_UPPER_LIMIT_DEFAULT = 10000;
	private Configuration conf = null;
	private LangDetectException cause = null;
	private int textsize_upper_limit;

	/**
	 * Constructor with no parameters (for generation by reflection)
	 */
	public LanguageDetectionFilter() {
	}

	/**
	 * {@inheritDoc}
	 */
	public NutchDocument filter(NutchDocument doc, Parse parse, Text url,
			CrawlDatum datum, Inlinks inlinks) throws IndexingException {
		if (conf == null) {
			throw new IndexingException("Not Yet Initialization.");
		}
		if (cause != null) {
			throw new IndexingException("Initialization Failed.", cause);
		}

		String lang = parse.getData().getParseMeta().get(Metadata.LANGUAGE);
		if (lang == null) {
			StringBuilder text = new StringBuilder();
			text.append(parse.getData().getTitle()).append(" ")
					.append(parse.getText());
			try {
				Detector detector = DetectorFactory.create();
				detector.setMaxTextLength(textsize_upper_limit);
				detector.append(text.toString());
				lang = detector.detect();
			} catch (LangDetectException e) {
				throw new IndexingException("Detection failed.", e);
			}
		}
		if (lang == null) lang = "unknown";

		doc.add("lang", lang);
		return doc;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addIndexBackendOptions(Configuration conf) {
		LuceneWriter.addFieldOptions("lang", LuceneWriter.STORE.YES,
				LuceneWriter.INDEX.UNTOKENIZED, conf);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setConf(Configuration conf) {
		if (this.conf == null) {
			try {
				DetectorFactory.loadProfile(conf.get("langdetect.profile.dir"));
				textsize_upper_limit = conf.getInt("langdetect.textsize", TEXTSIZE_UPPER_LIMIT_DEFAULT);
			} catch (LangDetectException e) {
				// afterward throw when filter() is called
				cause = e;
			}
		}
		this.conf = conf;
	}

	/**
	 * {@inheritDoc}
	 */
	public Configuration getConf() {
		return this.conf;
	}
}
