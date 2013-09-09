java-rss
========
Java RSS parser based on [horrorss](https://code.google.com/p/horrorss/). Updates include:

* last build date (with ATOM via updated)

It requires [Sparta-XML](http://sparta-xml.sourceforge.net/).

The java-rss jar is available [here](https://github.com/gmjordan/java-rss/raw/master/target/java-rss-1.0.0.jar)

###Usage

```java
...
RssParser rss = new RssParser();

try {
	// WSJ feed
	RssFeed rssFeed = rss.load("http://online.wsj.com/xml/rss/3_7014.xml");

	// Gets the channel information of the feed and display its title
	RssChannel rssChannel = rssFeed.getRssChannel();
	log.info("Feed Title: " + rssChannel.getTitle());
	log.info("Feed Pub Date: " + rssChannel.getPubDate());
	log.info("Feed Last Build Date: " + rssChannel.getLastBuildDate());

	// Gets the image of the feed and display the image URL
	RssImage rssImage = rssFeed.getRssImage();
	log.info("Feed Image: " + rssImage.getUrl());

	// Gets and iterate the items of the feed
	for(RssItem rssItem : rssFeed.getRssItems()){
		log.info("Title: " + rssItem.getTitle());
		log.info("Link : " + rssItem.getLink());
		log.info("Desc.: " + rssItem.getDescription());
	}
	

} catch (Exception e) {
	// Something to do if an exception occurs
	log.error(e);
}
...
```
