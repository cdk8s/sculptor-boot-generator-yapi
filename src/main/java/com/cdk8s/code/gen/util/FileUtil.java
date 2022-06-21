package com.cdk8s.code.gen.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;


@Slf4j
public final class FileUtil {

	@SneakyThrows
	public static void writeStringToFile(File file, String content) {
		FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
	}

	@SneakyThrows
	public static String readFileToString(String filePath) {
		return FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
	}

	@SneakyThrows
	public static String readFileToStringByClasspath(String resourceLocation) {
		URL url = FileUtil.class.getClassLoader().getResource(resourceLocation);
		if (url == null) {
			return null;
		}
		File file = new File(url.getFile());
		return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	}


	public static File createFile(String filePath) {
		File file;
		try {
			file = new File(filePath);
			File parentDir = file.getParentFile();
			if (!parentDir.exists()) {
				FileUtils.forceMkdir(parentDir);
			}
		} catch (Exception e) {
			log.error("create file failure", e);
			throw new RuntimeException(e);
		}
		return file;
	}
}
