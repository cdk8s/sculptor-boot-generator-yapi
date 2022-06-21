package com.cdk8s.code.gen.strategy;

import com.cdk8s.code.gen.strategy.common.GeneratorFileCommonUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.velocity.VelocityContext;

import java.io.File;


public class ReadYapiJsonToJavaClassStrategy implements GeneratorStrategy {

	@Override
	public void generatorFile(VelocityContext context, Configuration config) {
		String controllerClassName = (String) context.get("controllerClassName");
		generatorFileByTemplate(context, config, controllerClassName);

	}

	private void generatorFileByTemplate(VelocityContext context, Configuration config, String className) {
		String yapiControllerJavaOutPath = config.getString("yapiControllerJavaOutPath");
		String template = "templates/yapi/YapiController.java.vm";
		String fileSuffix = className + ".java";
		String fileFullPathName = yapiControllerJavaOutPath + File.separator + "controller" + File.separator + fileSuffix;
		GeneratorFileCommonUtil.generatorFile(context, config, template, fileFullPathName);
	}
}
