package com.webreach.mirth.server.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public Date getDate(String pattern, String date) throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.parse(date);
	}

	public String formatDate(String pattern, Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(date);
	}
}
