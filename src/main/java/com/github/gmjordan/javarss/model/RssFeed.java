package com.github.gmjordan.javarss.model;

import java.io.Serializable;
import java.util.List;

public class RssFeed implements Serializable {

	private static final long serialVersionUID = 1L;

	private RssChannel rssChannel;

	private RssImage rssImage;

	private List<RssItem> rssItems;

	public RssChannel getRssChannel() {
		return rssChannel;
	}

	public void setRssChannel(RssChannel rssChannel) {
		this.rssChannel = rssChannel;
	}

	public RssImage getRssImage() {
		return rssImage;
	}

	public void setRssImage(RssImage rssImage) {
		this.rssImage = rssImage;
	}

	public List<RssItem> getRssItems() {
		return rssItems;
	}

	public void setRssItems(List<RssItem> rssItems) {
		this.rssItems = rssItems;
	}

}
