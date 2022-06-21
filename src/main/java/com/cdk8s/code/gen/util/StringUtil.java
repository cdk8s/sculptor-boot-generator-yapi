package com.cdk8s.code.gen.util;

import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


@Slf4j
public final class StringUtil {

	public static boolean isNotBlank(final String str) {
		return StringUtils.isNotBlank(str);
	}

	public static boolean isBlank(final String str) {
		return StringUtils.isBlank(str);
	}

	public static boolean containsAny(final String str, final CharSequence... search) {
		return StringUtils.containsAny(str, search);
	}

	public static boolean containsIgnoreCase(final String str, final String search) {
		return StringUtils.containsIgnoreCase(str, search);
	}

	public static String substringAfter(final String str, final String search) {
		return StringUtils.substringAfter(str, search);
	}


	public static String substringBefore(final String str, final String search) {
		return StringUtils.substringBefore(str, search);
	}

	public static boolean equalsIgnoreCase(final String str1, final String str2) {
		return StringUtils.equalsIgnoreCase(str1, str2);
	}


	public static String lowerCase(final String str1) {
		return StringUtils.lowerCase(str1);
	}

	public static String upperCase(final String str1) {
		return StringUtils.upperCase(str1);
	}


	public static boolean startsWith(final String str, final String prefix) {
		return StringUtils.startsWith(str, prefix);
	}

	public static boolean endsWith(final String str, final String suffix) {
		return StringUtils.endsWith(str, suffix);
	}

	public static String replace(final String text, final String searchString, final String replacement) {
		return StringUtils.replace(text, searchString, replacement);
	}


	public static String replace(final String text, final String searchString, final String replacement, final int max) {
		return StringUtils.replace(text, searchString, replacement, max);
	}


	public static String remove(final String text, final String searchString) {
		return StringUtils.remove(text, searchString);
	}


	public static List<String> split(String str, String separator) {
		return CollectionUtil.toList(StringUtils.split(str, separator));
	}

	public static String uncapitalize(String str) {
		return StringUtils.uncapitalize(str);
	}


	public static String upperCamelToLowerCamel(String str) {
		CaseFormat fromFormat = CaseFormat.UPPER_CAMEL;
		CaseFormat toFormat = CaseFormat.LOWER_CAMEL;
		return fromFormat.to(toFormat, str);
	}


	public static String lowerCamelToUpperCamel(String str) {
		CaseFormat fromFormat = CaseFormat.LOWER_CAMEL;
		CaseFormat toFormat = CaseFormat.UPPER_CAMEL;
		return fromFormat.to(toFormat, str);
	}


	public static String lowerCamelToLowerUnderscore(String str) {
		CaseFormat fromFormat = CaseFormat.LOWER_CAMEL;
		CaseFormat toFormat = CaseFormat.LOWER_UNDERSCORE;
		return fromFormat.to(toFormat, str);
	}

	public static String removeRangeString(String body, String str1, String str2) {
		while (true) {
			int index1 = body.indexOf(str1);
			if (index1 != -1) {
				int index2 = body.indexOf(str2, index1);
				if (index2 != -1) {
					String str3 = body.substring(0, index1) + body.substring(index2 + str2.length());
					body = str3;
				} else {
					return body;
				}
			} else {
				return body;
			}
		}
	}

	public static String lowerFirstFromSeparator(String str, String separator) {
		if (isNotBlank(str) && isNotBlank(separator)) {
			List<String> split = split(str, separator);
			StringBuilder stringBuilder = new StringBuilder();

			for (String temp : split) {
				stringBuilder.append(Character.toLowerCase(temp.charAt(0)));
			}

			return stringBuilder.toString();
		}
		return null;
	}


}



