package com.github.gmjordan.javarss.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RssItem implements Serializable {

	private static final long serialVersionUID = 1L;

	// For all specifications
	private String title;

	private String link;

	private String description;

	private String author;

	private Date pubDate;

	// Only for RSS 2.0
	private String category;

	private Map<String, Object> additionalInfo;

	public RssItem() {
		title = "";
		link = "";
		description = "";
		author = "";
		category = "";
		pubDate = new Date();
	}

	public RssItem(String title, String link, String description) {
		this.title = title;
		this.link = link;
		this.description = description;
		author = "";
		category = "";
		pubDate = new Date();
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

	public void setLink(String link) {
		this.link = link;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	public String getDescription() {
		return description;
	}

	public String getAuthor() {
		return author;
	}

	public Date getPubDate() {
		return pubDate;
	}

	public String getCategory() {
		return category;
	}
}
