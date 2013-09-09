package com.github.gmjordan.javarss.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RssChannel implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;

	private String link;

	private String description;

	private Date pubDate;

	private Date lastBuildDate;

	private Map<String, Object> additionalInfo;

	public RssChannel() {
		title = "";
		link = "";
		description = "";
	}

	public RssChannel(String title, String link, String description) {
		this.title = title;
		this.link = link;
		this.description = description;
	}

	public void putAdditionalInfo(String name, Object object) {
		if (additionalInfo == null) {
			additionalInfo = new HashMap<String, Object>();
		}
		additionalInfo.put(name, object);
	}

	public Object getAdditionalInfo(String name) {
		return additionalInfo.get(name);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getLink() {
		return link;
	}

	public Date getPubDate() {
		return pubDate;
	}

	public Date getLastBuildDate() {
		return lastBuildDate;
	}

	public void setLastBuildDate(Date lastBuildDate) {
		this.lastBuildDate = lastBuildDate;
	}
}
