/**
 * 
 */
package com.zhazhapan.qiniu.util;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author pantao
 *
 */
public class Checker {

	public static final Pattern HYPER_LINK_PATTERN = Pattern
			.compile("^(https*://)?([a-z0-9]+\\.)+[a-z0-9]+(/[^\\s]*)*$", Pattern.CASE_INSENSITIVE);

	public static final Pattern DATE_PATTERN = Pattern.compile("^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$");

	public static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+$");

	public static boolean isDate(String date) {
		return isNull(date) ? false : DATE_PATTERN.matcher(date).matches();
	}

	public static boolean isNumber(String string) {
		return isNull(string) ? false : NUMBER_PATTERN.matcher(string).matches();
	}

	public static boolean isNull(Object object) {
		return object == null ? true : false;
	}

	public static boolean isNotNull(Object object) {
		return !isNull(object);
	}

	public static boolean isNullOrEmpty(String string) {
		return isNull(string) ? true : string.isEmpty();
	}

	public static boolean isNotEmpty(String string) {
		return !isNullOrEmpty(string);
	}

	public static String checkNull(String string) {
		return isNull(string) ? "" : string;
	}

	public static boolean isNotEmpty(List<?> list) {
		return !isEmpty(list);
	}

	public static boolean isEmpty(List<?> list) {
		return isNull(list) ? true : list.isEmpty();
	}

	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return isNull(map) ? true : map.isEmpty();
	}

	public static boolean isHyperLink(String string) {
		if (isNotEmpty(string)) {
			return HYPER_LINK_PATTERN.matcher(string).matches();
		}
		return false;
	}
}
