package com.cdk8s.code.gen.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;


public final class DatetimeUtil {


	public static long currentEpochMilli() {
		return Instant.now().toEpochMilli();
	}

	public static LocalDateTime toLocalDateTime(long epochMill) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMill), ZoneId.systemDefault());
	}


	public static long between(LocalDateTime time1, LocalDateTime time2, ChronoUnit chronoUnit) {
		return chronoUnit.between(time1, time2);
	}

	public static long between(Long beginTime, Long endTime, ChronoUnit chronoUnit) {
		return between(toLocalDateTime(beginTime), toLocalDateTime(endTime), chronoUnit);
	}


}



