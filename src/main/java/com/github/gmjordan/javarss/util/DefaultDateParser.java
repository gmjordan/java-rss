package com.github.gmjordan.javarss.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.github.gmjordan.javarss.RssParser;

public class DefaultDateParser implements DateParser {

	public Date getDate(String date, int rssType) throws Exception {
		Date res = null;
		String pattern = null;

		switch (rssType) {
		case RssParser.TYPE_RDF: {
			if (date.indexOf("+") >= 0) {
				pattern = "yyyy-MM-dd'T'HH:mm:ss+HH:mm";
			} else {
				pattern = "yyyy-MM-dd'T'HH:mm:ss-HH:mm";
			}
			break;
		}
		case RssParser.TYPE_RSS: {
			pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
			break;
		}
		case RssParser.TYPE_ATOM: {
			pattern = "yyyy-MM-dd'T'HH:mm:ss";
			break;
		}
		}

		try {
			SimpleDateFormat sd = new SimpleDateFormat(pattern, Locale.ENGLISH);
			res = sd.parse(date);
		}
		catch (Exception e) {
			System.out.println("Error parsing date: " + date + " [Type: " + rssType + "] --" + e.toString());
			// throw e;
		}

		return res;
	}

}
