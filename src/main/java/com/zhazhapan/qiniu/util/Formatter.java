/**
 * 
 */
package com.zhazhapan.qiniu.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.zhazhapan.qiniu.modules.constant.Values;
import com.zhazhapan.qiniu.view.Dialogs;

/**
 * @author pantao
 *
 */
public class Formatter {

	private static Logger logger = Logger.getLogger(Formatter.class);

	public static int stringToInt(String string) {
		if (Checker.isNumber(string)) {
			return Integer.parseInt(string);
		}
		return 0;
	}

	public static final Pattern FILE_NAME_PATTERN = Pattern
			.compile("(([^/\\\\:*\"<>|?]+)\\.)*[^/\\\\:*\"<>|?]+(\\?.*)?$", Pattern.CASE_INSENSITIVE);

	public static String formatSize(long size) {
		if (size < Values.KB) {
			return size + " B";
		} else if (size < Values.MB) {
			return decimalFormat((double) size / Values.KB) + " KB";
		} else if (size < Values.GB) {
			return decimalFormat((double) size / Values.MB) + " MB";
		} else if (size < Values.TB) {
			return decimalFormat((double) size / Values.GB) + " GB";
		} else {
			return decimalFormat((double) size / Values.TB) + " TB";
		}
	}

	public static String decimalFormat(double number) {
		return decimalFormat(number, "#0.00");
	}

	public static String decimalFormat(double number, String format) {
		return new DecimalFormat(format).format(number);
	}

	public static String timeStampToString(long time) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
	}

	public static String jsonFormat(String string) {
		String json;
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(string);
			json = gson.toJson(je);
		} catch (Exception e) {
			Dialogs.showException(Values.FORMAT_JSON_ERROR, e);
			logger.error("format json string error,json: " + string);
			json = string;
		}
		return json;
	}

	public static String dateToString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(checkDate(date));
	}

	public static String datetimeToString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(checkDate(date));
	}

	public static String getFileName(String string) {
		if (Checker.isNotEmpty(string)) {
			try {
				string = URLDecoder.decode(string, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("decode url '" + string + "' error use encoding utf-8, message: " + e.getMessage());
			}
			Matcher matcher = FILE_NAME_PATTERN.matcher(string);
			if (matcher.find() && Checker.isNotEmpty(matcher.group(0))) {
				String name = matcher.group(0).split("\\?")[0];
				if (Checker.isNotEmpty(name)) {
					return name;
				}
			}
		}
		return "undefined";
	}

	private static Date checkDate(Date date) {
		return Checker.isNull(date) ? new Date() : date;
	}
}
