package com.cdk8s.code.gen.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.math.BigDecimal;


public final class RandomUtil {
	public static int nextInt(final int startInclusive, final int endExclusive) {
		return RandomUtils.nextInt(startInclusive, endExclusive);
	}

	public static double nextDouble(final int startInclusive, final int endExclusive) {
		return nextDouble(startInclusive, endExclusive, 2);
	}

	public static BigDecimal nextBigDecimal(final int startInclusive, final int endExclusive) {
		BigDecimal result = new BigDecimal(nextDouble(startInclusive, endExclusive));
		return result.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public static double nextDouble(final int startInclusive, final int endExclusive, Integer scale) {
		if (null == scale) {
			scale = 2;
		}
		double nextDouble = RandomUtils.nextDouble(startInclusive, endExclusive);
		return new BigDecimal(nextDouble).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static String randomNumeric(final int count) {
		return RandomStringUtils.randomNumeric(count);
	}

}







