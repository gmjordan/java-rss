package com.github.gmjordan.javarss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.gmjordan.javarss.model.RssChannel;
import com.github.gmjordan.javarss.model.RssFeed;
import com.github.gmjordan.javarss.model.RssImage;
import com.github.gmjordan.javarss.model.RssItem;
import com.github.gmjordan.javarss.util.DateParser;
import com.github.gmjordan.javarss.util.DefaultDateParser;
import com.hp.hpl.sparta.Document;
import com.hp.hpl.sparta.Parser;

public class RssParser {
	public static final int TYPE_RDF = 0;

	public static final int TYPE_RSS = 1;

	public static final int TYPE_ATOM = 2;

	private Document doc;

	private String filename;

	private Integer timeout;

	private String xPath;

	private int rssType;

	private String charset = "UTF-8";

	private String cacheDir;

	private long cacheLifeTime = 0;

	private DateParser dateParser = new DefaultDateParser();

	private Map<String, RssModuleParser> moduleParsers;

	public RssParser() {
		filename = "";
	}

	public RssParser(String filename) {
		this.filename = filename;
	}

	public void setDateParser(DateParser dateParser) {
		this.dateParser = dateParser;
	}

	public void addRssModuleParser(String name, RssModuleParser moduleParser) {
		if (moduleParsers == null) {
			moduleParsers = new HashMap<String, RssModuleParser>();
		}
		moduleParsers.put(name, moduleParser);
	}

	public RssFeed load() throws Exception {
		if ((filename.startsWith("http://")) ||
				(filename.startsWith("https://"))) {
			this.parseFromURL();
		} else {
			this.parseFromReader(this.getReaderFromFile(filename));
		}

		return this.getRssFeed();
	}

	public RssFeed loadString(String xml) throws Exception {
		try {
			doc = Parser.parse(xml);
		}
		catch (Exception e) {
			doc = new Document();
			throw new Exception("Error reading the file " + filename, e);
		}
		this.setChannelXPath();

		return this.getRssFeed();
	}

	public RssFeed load(String filename, int timeout) throws Exception {
		this.filename = filename;
		this.timeout = timeout;
		this.load();
		return this.getRssFeed();
	}

	public RssFeed load(File file) throws Exception {
		InputStream input = new FileInputStream(file);
		this.parseFromReader(this.getReaderFromInputStream(input));
		return this.getRssFeed();
	}

	public RssFeed load(InputStream input) throws Exception {
		this.parseFromReader(this.getReaderFromInputStream(input));
		return this.getRssFeed();
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void enableCache(String cacheDir, long cacheLifeTime) {
		this.cacheDir = cacheDir;
		this.cacheLifeTime = cacheLifeTime;
	}

	public Document getDocument() {
		return doc;
	}

	private RssChannel getChannel() throws Exception {
		if (rssType != TYPE_ATOM) {
			return this.getChannelRss();
		} else {
			return this.getChannelAtom();
		}
	}

	private RssChannel getChannelAtom() throws Exception {
		RssChannel res = new RssChannel();
		String pubDate, updated;

		try {

			res.setTitle(doc.xpathSelectString("/feed/title/text()"));
			res.setDescription(doc.xpathSelectString("/feed/tagline/text()"));
			res.setLink(doc.xpathSelectString("/feed/link/@href"));
			pubDate = doc.xpathSelectString("/feed/modified/text()");
			updated = doc.xpathSelectString("/feed/updated/text()");
			if (pubDate != null) {
				res.setPubDate(this.getDate(pubDate, TYPE_ATOM));
			}
			if (updated != null) {
				res.setLastBuildDate(this.getDate(updated, TYPE_ATOM));
			}

			this.parseAdditionalChannelInfo(res);
		}
		catch (Exception e) {
			throw new Exception("Error reading ATOM channel " + filename, e);
		}
		return res;
	}

	private RssChannel getChannelRss() throws Exception {
		RssChannel res = new RssChannel();
		String datePublish, lastBuildDate;

		try {
			if (rssType == TYPE_RDF) {
				res.setTitle(doc.xpathSelectString(xPath + "/channel/title/text()"));
				res.setLink(doc.xpathSelectString(xPath + "/channel/link/text()"));
				res.setDescription(doc.xpathSelectString(xPath + "/channel/description/text()"));
				datePublish = doc.xpathSelectString(xPath + "/channel/dc:date/text()");
				lastBuildDate = doc.xpathSelectString(xPath + "/lastBuildDate/text()");
			} else {
				res.setTitle(doc.xpathSelectString(xPath + "/title/text()"));
				res.setLink(doc.xpathSelectString(xPath + "/link/text()"));
				res.setDescription(doc.xpathSelectString(xPath + "/description/text()"));
				datePublish = doc.xpathSelectString(xPath + "/pubDate/text()");
				lastBuildDate = doc.xpathSelectString(xPath + "/lastBuildDate/text()");
			}

			if (datePublish != null) {
				res.setPubDate(this.getDate(datePublish, rssType));
			}

			if (lastBuildDate != null) {
				res.setLastBuildDate(this.getDate(lastBuildDate, rssType));
			}

			this.parseAdditionalChannelInfo(res);
		}
		catch (Exception e) {
			throw new Exception("Error reading element channel from " + filename, e);
		}

		return res;
	}

	private RssImage getImage() throws Exception {
		if (rssType != TYPE_ATOM) {
			return this.getImageRss();
		} else {
			return new RssImage();
		}
	}

	private RssImage getImageRss() throws Exception {
		RssImage res = new RssImage();
		String title;
		try {
			title = doc.xpathSelectString(xPath + "/image/title/text()");
			if (title != null) {
				res.setTitle(title);
				res.setLink(doc.xpathSelectString(xPath + "/image/link/text()"));
				res.setUrl(doc.xpathSelectString(xPath + "/image/url/text()"));
			} else {
				title = doc.xpathSelectString(xPath + "/channel/image/title/text()");
				if (title != null) {
					res.setTitle(title);
					res.setLink(doc.xpathSelectString(xPath + "/channel/image/link/text()"));
					res.setUrl(doc.xpathSelectString(xPath + "/channel/image/url/text()"));
				}
			}

			this.parseAdditionalImageInfo(res);
		}
		catch (Exception e) {
			throw new Exception("Error getting RSS image " + filename, e);
		}

		return res;
	}

	private List<RssItem> getItems() throws Exception {
		if (rssType != TYPE_ATOM) {
			return this.getItemsRss();
		} else {
			return this.getItemsAtom();
		}
	}

	@SuppressWarnings("rawtypes")
	private List<RssItem> getItemsAtom() throws Exception {
		List<RssItem> res = new ArrayList<RssItem>();
		try {
			Enumeration list = doc.xpathSelectElements("feed/entry");
			int i = 1;
			while (list.hasMoreElements()) {
				list.nextElement();
				res.add(getItemAtom(i));
				i++;
			}
		}
		catch (Exception e) {
			throw new Exception("Error getting items " + filename, e);
		}
		return res;
	}

	@SuppressWarnings("rawtypes")
	private List<RssItem> getItemsRss() throws Exception {
		List<RssItem> res = new ArrayList<RssItem>();
		try {
			Enumeration list = doc.xpathSelectElements(xPath + "/item");
			int i = 1;
			while (list.hasMoreElements()) {
				list.nextElement();
				res.add(getItemRss(i));
				i++;
			}
		}
		catch (Exception e) {
			throw new Exception("Error getting items " + filename, e);
		}
		return res;
	}

	private RssItem getItemAtom(int index) throws Exception {
		RssItem res = new RssItem();
		String description;
		String datePublish;
		String link;

		try {
			res.setTitle(doc.xpathSelectString("feed/entry[" + index + "]/title/text()"));
			link = doc.xpathSelectString("feed/entry[" + index + "]/link/text()");

			if (link == null) {
				link = doc.xpathSelectString("feed/entry[" + index + "]/link[@rel=\"alternate\"]/@href");
			}
			res.setLink(link);

			description = doc.xpathSelectString("feed/entry[" + index + "]/content/text()");
			if (description == null) {
				description = doc.xpathSelectString("feed/entry[" + index + "]/summary/text()");
			}
			res.setDescription(description);
			res.setAuthor(doc.xpathSelectString("feed/entry[" + index + "]/author/name/text()"));

			datePublish = doc.xpathSelectString("feed/entry[" + index + "]/created/text()");
			// If it is Atom 1.0 the creation date is in "published"
			if ((datePublish == null) || (datePublish.equals(""))) {
				datePublish = doc.xpathSelectString("feed/entry[" + index + "]/published/text()");
			}

			res.setPubDate(this.getDate(datePublish, TYPE_ATOM));

			this.parseAdditionalItemInfo(res, index);
		}
		catch (Exception e) {
			throw new Exception("Error obtaining the entry at position " + index + " of " + filename, e);
		}

		return res;
	}

	private RssItem getItemRss(int index) throws Exception {
		RssItem res = new RssItem();
		String datePublish;
		String author;

		try {
			res.setTitle(doc.xpathSelectString(xPath + "/item[" + index + "]/title/text()"));
			res.setLink(doc.xpathSelectString(xPath + "/item[" + index + "]/link/text()"));
			res.setDescription(doc.xpathSelectString(xPath + "/item[" + index + "]/description/text()"));
			if (rssType == TYPE_RDF) {
				author = doc.xpathSelectString(xPath + "/item[" + index + "]/dc:creator/text()");
				datePublish = doc.xpathSelectString(xPath + "/item[" + index + "]/dc:date/text()");
			} else {
				author = doc.xpathSelectString(xPath + "/item[" + index + "]/author/text()");
				if (author == null) {
					author = doc.xpathSelectString(xPath + "/item[" + index + "]/dc:creator/text()");
				}
				datePublish = doc.xpathSelectString(xPath + "/item[" + index + "]/pubDate/text()");
			}
			res.setAuthor(author);
			res.setPubDate(this.getDate(datePublish, rssType));

			this.parseAdditionalItemInfo(res, index);
		}
		catch (Exception e) {
			throw new Exception("Error obtaining the entry at position " + index + " of " + filename, e);
		}

		return res;
	}

	private void parseAdditionalChannelInfo(RssChannel item) throws Exception {
		if (moduleParsers != null) {
			Iterator<String> keys = moduleParsers.keySet().iterator();
			while (keys.hasNext()) {
				String keyName = keys.next();
				RssModuleParser moduleParser = moduleParsers.get(keyName);
				item.putAdditionalInfo(keyName, moduleParser.parseChannel(rssType, doc));
			}
		}
	}

	private void parseAdditionalImageInfo(RssImage item) throws Exception {
		if (moduleParsers != null) {
			Iterator<String> keys = moduleParsers.keySet().iterator();
			while (keys.hasNext()) {
				String keyName = keys.next();
				RssModuleParser moduleParser = moduleParsers.get(keyName);
				item.putAdditionalInfo(keyName, moduleParser.parseImage(rssType, doc));
			}
		}
	}

	private void parseAdditionalItemInfo(RssItem item, int index) throws Exception {
		if (moduleParsers != null) {
			Iterator<String> keys = moduleParsers.keySet().iterator();
			while (keys.hasNext()) {
				String keyName = keys.next();
				RssModuleParser moduleParser = moduleParsers.get(keyName);
				item.putAdditionalInfo(keyName, moduleParser.parseItem(rssType, doc, index));
			}
		}
	}

	private void parseFromReader(BufferedReader buffer) throws Exception {
		String line;
		StringBuffer xml = new StringBuffer();
		try {
			while ((line = buffer.readLine()) != null) {
				xml.append(line);
			}

			if (xml.charAt(0) == '\uFEFF') {
				xml.setCharAt(0, '\n');
			}

			doc = Parser.parse(xml.toString());
		}
		catch (Exception e) {
			doc = new Document();
			throw new Exception("Error reading the file " + filename, e);
		}
		finally {
			buffer.close();
		}
		this.setChannelXPath();
	}

	private void parseFromFile() throws Exception {
		try {
			File xmlFile = new File(filename);
			doc = Parser.parse(xmlFile);
		}
		catch (Exception e) {
			System.out.println("Error reading the file " + filename);
			System.out.println("RssParser:parseFromFile() ERROR: " + e.getMessage());
			doc = new Document();
		}
		this.setChannelXPath();
	}

	private void parseFromURL() throws Exception {
		if (cacheLifeTime == 0) {
			this.parseFromReader(this.getReaderFromUrl(filename, timeout));
		} else {
			this.parseFromCache();
		}
	}

	private void parseFromCache() throws Exception {
		String encFilename = this.encode(filename);
		if (this.cacheExpired()) {
			this.saveURL(filename, timeout, cacheDir, encFilename);
		}
		filename = cacheDir + "/" + encFilename;
		this.parseFromFile();
	}

	private boolean cacheExpired() throws Exception {
		boolean res = false;
		String filename = this.encode(this.filename);
		File file = new File(cacheDir + "/" + filename);
		boolean exists = file.exists();
		if (exists) {
			long actualTime = System.currentTimeMillis();
			long fileTime = file.lastModified();
			if ((actualTime - fileTime) > cacheLifeTime) {
				res = true;
			}
		} else {
			res = true;
		}
		return res;
	}

	private boolean saveURL(String url, Integer timeout, String path, String filename) throws IOException {
		boolean res = false;
		this.checkPath(path);
		BufferedReader buffer = getReaderFromUrl(url, timeout);
		if (buffer != null) {
			try {
				FileOutputStream f = new FileOutputStream(path + "/" + filename);
				OutputStreamWriter writer = new OutputStreamWriter(f);
				String line;
				while ((line = buffer.readLine()) != null) {
					writer.write(line + "\n", 0, line.length() + 1);
				}
				writer.close();
				res = true;
			}
			catch (IOException e) {
				throw new IOException("Error writing the file " + path + "/" + filename);
			}
		}
		return res;
	}

	private void checkPath(String dir) {
		File f = new File(dir);
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	private BufferedReader getReaderFromFile(String file) throws IOException {
		BufferedReader buffer = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader input = new InputStreamReader(fis, charset);
			buffer = new BufferedReader(input);
			return buffer;
		}
		catch (IOException e) {
			throw new IOException("Error obtaining the reader from " + filename);
		}

	}

	private BufferedReader getReaderFromInputStream(InputStream inputStream) throws IOException {
		BufferedReader buffer = null;
		InputStreamReader input = new InputStreamReader(inputStream, charset);
		buffer = new BufferedReader(input);
		return buffer;
	}

	private BufferedReader getReaderFromUrl(String url, int timeout) throws IOException {
		BufferedReader buffer = null;
		try {
			URL urlConn = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();

			conn.setConnectTimeout(timeout);

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStreamReader input = new InputStreamReader(conn.getInputStream(), charset);
				buffer = new BufferedReader(input);
			}

			return buffer;
		}
		catch (IOException e) {
			throw new IOException("Error obtaining the reader from " + url);
		}
	}

	@SuppressWarnings("rawtypes")
	private String getRootXPath() throws Exception {
		String xPath = null;
		try {
			Enumeration list = doc.xpathSelectElements("rdf:RDF");
			if (list.hasMoreElements()) {
				xPath = "rdf:RDF";
			} else {
				list = doc.xpathSelectElements("rss/channel");
				if (list.hasMoreElements()) {
					xPath = "rss/channel";
				} else {
					xPath = "";
				}
			}
		}
		catch (Exception e) {
			throw new Exception("Error obtaining the file XPath root [" + filename + "]", e);
		}
		return xPath;
	}

	private void setChannelXPath() throws Exception {
		xPath = getRootXPath();
		if (xPath.equals("rdf:RDF")) {
			rssType = TYPE_RDF;
		}
		else if (xPath.equals("rss/channel")) {
			rssType = TYPE_RSS;
		} else {
			rssType = TYPE_ATOM;
		}
	}

	private String encode(String filename) throws Exception {
		String res = URLEncoder.encode(filename, charset) + ".cache";
		return res;
	}

	private RssFeed getRssFeed() throws Exception {
		RssFeed rss = new RssFeed();

		rss.setRssChannel(this.getChannel());
		rss.setRssImage(this.getImage());
		rss.setRssItems(this.getItems());
		return rss;
	}

	private Date getDate(String date, int rssType) throws Exception {
		return dateParser.getDate(date, rssType);
	}
}
