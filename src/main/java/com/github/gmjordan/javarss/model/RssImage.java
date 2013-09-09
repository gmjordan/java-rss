package com.github.gmjordan.javarss.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RssImage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;

	private String url;

	private String link;

	private Map<String, Object> additionalInfo;

	public RssImage() {
		title = "";
		url = "";
		link = "";
	}

	public RssImage(String title, String url, String link) {
		this.title = title;
		this.url = url;
		this.link = link;
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

	public void setUrl(String url) {
		this.url = url;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getLink() {
		return link;
	}

}
