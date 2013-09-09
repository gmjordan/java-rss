package com.github.gmjordan.javarss.util;

import java.util.Date;

public interface DateParser {

	public Date getDate(String date, int rssType) throws Exception;
}
