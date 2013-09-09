package com.github.gmjordan.javarss;

import com.hp.hpl.sparta.Document;

public interface RssModuleParser {
	public Object parseChannel(int rssType, Document doc) throws Exception;

	public Object parseImage(int rssType, Document doc) throws Exception;

	public Object parseItem(int rssType, Document doc, int index) throws Exception;
}
