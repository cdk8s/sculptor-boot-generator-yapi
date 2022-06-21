package com.cdk8s.code.gen.util.id;


public final class GenerateIdUtil {

	private static SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(0, 0);

	public static Long getId() {
		return snowflakeIdWorker.nextId();
	}

}

