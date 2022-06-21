package com.cdk8s.code.gen;

import cn.hutool.core.io.file.FileWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cdk8s.code.gen.dto.yapi.ApiControllerBO;
import com.cdk8s.code.gen.dto.yapi.YapiEntity;
import com.cdk8s.code.gen.strategy.ReadYapiJsonToJavaClassStrategy;
import com.cdk8s.code.gen.strategy.StrategyContext;
import com.cdk8s.code.gen.strategy.common.GeneratorCommonUtil;
import com.cdk8s.code.gen.util.CollectionUtil;
import com.cdk8s.code.gen.util.FileUtil;
import com.cdk8s.code.gen.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Slf4j
public class ReadYapiJsonToJavaClassMain {

	//=====================================业务处理 start=====================================

	@SneakyThrows
	public static void main(String[] args) {
		log.info("------zch------根据 YApi JSON 生成 Java 文件 start");
		generatorFile();
		log.info("------zch------根据 YApi JSON 生成 Java 文件 end");
	}


	@SneakyThrows
	public static void generatorFile() {
		Configuration config = GeneratorCommonUtil.getConfig();
		String yapiJSONFilePath = config.getString("yapiJSONFilePath");
		String yapiParamJavaOutPath = config.getString("yapiParamJavaOutPath");
		String yapiResponseJavaOutPath = config.getString("yapiResponseJavaOutPath");
		String yapiParamPackage = config.getString("yapiParamPackage");
		String yapiResponsePackage = config.getString("yapiResponsePackage");

		String readFileToString = FileUtil.readFileToString(yapiJSONFilePath);

		List<YapiEntity> userListResult = JSON.parseObject(readFileToString, new TypeReference<List<YapiEntity>>() {
		});

		if (CollectionUtil.isNotEmpty(userListResult)) {


			List<List<ApiControllerBO>> resultList = new ArrayList<>();

			for (YapiEntity entity : userListResult) {
				List<YapiEntity.ListBean> listBean = entity.getList();
				if (CollectionUtil.isNotEmpty(listBean)) {
					List<ApiControllerBO> apiControllerBOList = new ArrayList<>();

					for (YapiEntity.ListBean bean : listBean) {
						String responseBody = bean.getRes_body();
						String requestBody = bean.getReq_body_other();

						YapiEntity.ListBean.QueryPathBean queryPath = bean.getQuery_path();
						String apiPath = queryPath.getPath();

						if (StringUtil.isBlank(responseBody)) {
							log.error("apiPath={}, responseBody 不能为空，所有接口都必须有返回值", apiPath);
							return;
						}

						List<String> apiPathStringList = StringUtil.split(apiPath, "/");
						if (apiPathStringList.size() != 3) {
							log.error("apiPath={}, apiPath 地址必须是 3 段式", apiPath);
							return;
						}


						String classNamePath = apiPathStringList.get(1);
						String actionPath = apiPathStringList.get(2);
						String upperClassName = StringUtil.lowerCamelToUpperCamel(classNamePath);

						ApiControllerBO apiControllerBO = new ApiControllerBO();
						apiControllerBO.setApiClassName(upperClassName);
						apiControllerBO.setApiPath(apiPath);
						apiControllerBO.setApiComment(bean.getTitle());
						apiControllerBO.setApiMethodName(actionPath);

						responseBody = removeComment(responseBody);
						requestBody = removeComment(requestBody);

						if (StringUtil.containsAny(responseBody, "<comment>")) {
							log.error("apiPath={}, responseBody 还存在 <comment>", apiPath);
							return;
						}

						if (StringUtil.containsAny(requestBody, "<comment>")) {
							log.error("apiPath={}, requestBody 还存在 <comment>", apiPath);
							return;
						}


						String rootClassName = WordUtils.capitalize(classNamePath, new char[]{'_'}).replace("_", "") + WordUtils.capitalize(actionPath, new char[]{'_'}).replace("_", "");
						String requestParamName = "App" + rootClassName + "RequestParam";
						String responseDTOName = "App" + rootClassName + "ResponseDTO";

						apiControllerBO.setRequestParamClassName(requestParamName);

						String requestFileContent = buildRequest(requestBody, yapiParamPackage, requestParamName);

						if (StringUtil.isNotBlank(requestFileContent)) {
							writeFile(requestFileContent, yapiParamJavaOutPath, requestParamName);
							apiControllerBO.setResponseDTOClassName(requestParamName);
						}


						String responseFileContent = buildResponse(responseBody, yapiResponsePackage, responseDTOName);
						if (StringUtil.isNotBlank(responseFileContent)) {
							writeFile(responseFileContent, yapiResponseJavaOutPath, responseDTOName);
							apiControllerBO.setResponseDTOClassName(responseDTOName);
						}

						apiControllerBOList.add(apiControllerBO);
					}

					resultList.add(apiControllerBOList);
				}

			}


			if (CollectionUtil.isNotEmpty(resultList)) {
				for (List<ApiControllerBO> apiControllerBOList : resultList) {
					Map<String, Object> contextParam = new HashMap<>(30);
					String apiClassName = apiControllerBOList.get(0).getApiClassName();
					String controllerClassName = "App" + apiClassName + "Controller";
					contextParam.put("controllerClassName", controllerClassName);
					contextParam.put("ClassName", apiClassName);
					contextParam.put("className", StringUtil.upperCamelToLowerCamel(apiClassName));
					contextParam.put("javaRootPackage", config.getString("javaRootPackage"));
					contextParam.put("apiControllerBOList", apiControllerBOList);

					Properties prop = new Properties();
					prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
					Velocity.init(prop);

					VelocityContext context = new VelocityContext(contextParam);
					StrategyContext strategyContext = new StrategyContext();

					strategyContext.setGeneratorStrategy(new ReadYapiJsonToJavaClassStrategy());
					strategyContext.executeStrategy(context, config);
				}
			}

		}
	}


	//=====================================业务处理  end=====================================
	//=====================================私有方法 start=====================================
	private static void writeFile(String content, String outPath, String className) {
		String src = System.getProperty("user.dir") + File.separator + outPath + File.separator + className + ".java";
		FileUtil.createFile(src);
		FileWriter writer = new FileWriter(src);
		writer.write(content);
	}

	private static String removeComment(String content) {
		if (StringUtil.containsAny(content, "<comment>")) {
			content = StringUtil.removeRangeString(content, "//<comment>", "</comment>");
		}
		if (StringUtil.containsAny(content, "<comment>")) {
			content = StringUtil.removeRangeString(content, "// <comment>", "</comment>");
		}
		return content;
	}

	private static String buildRequest(String requestBody, String yapiParamPackage, String requestParamName) {
		Object jsonObjectByRequest;
		try {
			jsonObjectByRequest = JSON.parse(requestBody);
		} catch (Exception e) {
			log.error("requestBody={}, error={}", requestBody, e.getMessage());
			e.printStackTrace();
			return null;
		}
		if (null != jsonObjectByRequest) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("package " + yapiParamPackage + ";\n");
			buildParamPackage(stringBuilder);
			stringBuilder.append("public class " + requestParamName + " implements Serializable {\n");
			stringBuilder.append("private static final long serialVersionUID = -1L;\n");
			stringBuilder.append("\n");
			try {
				buildParamAttribute(jsonObjectByRequest, stringBuilder);
			} catch (Exception e) {
				log.error("requestBody={}, error={}", requestBody, e.getMessage());
				e.printStackTrace();
				return null;
			}
			stringBuilder.append("\n");
			stringBuilder.append("}");

			return stringBuilder.toString();
		}
		return null;
	}

	private static String buildResponse(String responseBody, String yapiResponsePackage, String responseDTOName) {
		JSONObject jsonObjectByResponse = null;
		try {
			jsonObjectByResponse = (JSONObject) JSON.parse(responseBody);
		} catch (Exception e) {
			log.error("responseBody={}, error={}", responseBody, e.getMessage());
			e.printStackTrace();
			return null;
		}
		Object dataByResponse = jsonObjectByResponse.get("data");
		if (null != dataByResponse) {
			if (dataByResponse instanceof JSONObject) {
				JSONObject responseObject = (JSONObject) dataByResponse;
				Object navigateFirstPage = responseObject.get("navigateFirstPage");
				if (null != navigateFirstPage) {
					dataByResponse = responseObject.get("list");
				}
			}

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("package " + yapiResponsePackage + ";\n");
			buildResponsePackage(stringBuilder);
			stringBuilder.append("public class " + responseDTOName + " implements Serializable {\n");
			stringBuilder.append("private static final long serialVersionUID = -1L;\n");
			stringBuilder.append("\n");
			try {
				buildResponseAttribute(dataByResponse, stringBuilder);
			} catch (Exception e) {
				log.error("responseBody={}, error={}", responseBody, e.getMessage());
				e.printStackTrace();
				return null;
			}
			stringBuilder.append("\n");
			stringBuilder.append("}");

			return stringBuilder.toString();
		}
		return null;
	}


	private static void buildParamPackage(StringBuilder stringBuilder) {
		stringBuilder.append("\n");
		stringBuilder.append("import javax.validation.constraints.NotNull;\n");
		stringBuilder.append("import javax.validation.constraints.NotBlank;\n");
		stringBuilder.append("import javax.validation.constraints.NotEmpty;\n");
		stringBuilder.append("import javax.validation.constraints.Size;\n");
		stringBuilder.append("import org.hibernate.validator.constraints.Length;\n");
		stringBuilder.append("import org.hibernate.validator.constraints.Range;\n");
		stringBuilder.append("import com.cdk8s.sculptor.validator.SafeHtml;\n");
		stringBuilder.append("import lombok.NoArgsConstructor;\n");
		stringBuilder.append("import lombok.Getter;\n");
		stringBuilder.append("import lombok.Setter;\n");
		stringBuilder.append("import lombok.ToString;\n");
		stringBuilder.append("import java.io.Serializable;\n");
		stringBuilder.append("import java.math.BigDecimal;\n");
		stringBuilder.append("import java.util.List;\n");
		stringBuilder.append("import com.fasterxml.jackson.annotation.JsonInclude;\n");
		stringBuilder.append("\n");
		stringBuilder.append("@NoArgsConstructor\n");
		stringBuilder.append("@Setter\n");
		stringBuilder.append("@Getter\n");
		stringBuilder.append("@ToString(callSuper = true)\n");
		stringBuilder.append("@JsonInclude(JsonInclude.Include.NON_NULL)\n");
	}

	private static void buildResponsePackage(StringBuilder stringBuilder) {
		stringBuilder.append("\n");
		stringBuilder.append("import com.fasterxml.jackson.annotation.JsonInclude;\n");
		stringBuilder.append("import lombok.NoArgsConstructor;\n");
		stringBuilder.append("import lombok.Getter;\n");
		stringBuilder.append("import lombok.Setter;\n");
		stringBuilder.append("import lombok.ToString;\n");
		stringBuilder.append("import java.io.Serializable;\n");
		stringBuilder.append("import java.math.BigDecimal;\n");
		stringBuilder.append("import java.util.List;\n");
		stringBuilder.append("\n");
		stringBuilder.append("@NoArgsConstructor\n");
		stringBuilder.append("@Setter\n");
		stringBuilder.append("@Getter\n");
		stringBuilder.append("@ToString(callSuper = true)\n");
		stringBuilder.append("@JsonInclude(JsonInclude.Include.NON_NULL)\n");
	}

	private static void buildParamAttribute(Object data, StringBuilder stringBuilder) {
		JSONObject jsonObject = null;
		if (data instanceof JSONArray) {
			JSONArray dataObject = (JSONArray) data;
			if (dataObject.isEmpty()) {
				log.error("------zch------Param 根元素 dataObject 不能为空");
				return;
			}
			Object obj = dataObject.get(0);
			if (obj instanceof JSONObject) {
				jsonObject = (JSONObject) obj;
			} else {
				throw new RuntimeException("元素不符合可转换类型");
			}

		} else if (data instanceof JSONObject) {
			jsonObject = (JSONObject) data;
		}

		if (null == jsonObject) {
			return;
		}

		Set<Map.Entry<String, Object>> entrySet = jsonObject.entrySet();
		for (Map.Entry<String, Object> map : entrySet) {
			String key = map.getKey();
			Object value = map.getValue();
			if (value instanceof JSONObject || value instanceof JSONArray) {
				if (value instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) value;
					if (jsonArray.isEmpty()) {
						continue;
					}
					Object valueSub = jsonArray.get(0);
					if (!(valueSub instanceof JSONObject) && !(valueSub instanceof JSONArray)) {
						if (valueSub instanceof Integer) {
							stringBuilder.append("@NotEmpty(message = \"" + key + " 不能为空\")\n");
							stringBuilder.append("@Size(min = 1, message = \"" + key + " 至少需要一个元素\")\n");
							stringBuilder.append("private List<Integer> " + key + ";\n");
							stringBuilder.append("\n");
						} else if (valueSub instanceof BigDecimal) {
							stringBuilder.append("@NotEmpty(message = \"" + key + " 不能为空\")\n");
							stringBuilder.append("@Size(min = 1, message = \"" + key + " 至少需要一个元素\")\n");
							stringBuilder.append("private List<BigDecimal> " + key + ";\n");
							stringBuilder.append("\n");
						} else if (valueSub instanceof Long) {
							stringBuilder.append("@NotEmpty(message = \"" + key + " 不能为空\")\n");
							stringBuilder.append("@Size(min = 1, message = \"" + key + " 至少需要一个元素\")\n");
							stringBuilder.append("private List<Long> " + key + ";\n");
							stringBuilder.append("\n");
						} else if (valueSub instanceof String) {
							if (StringUtil.containsIgnoreCase(key, "id") && !StringUtil.containsIgnoreCase(key, "ids")) {
								stringBuilder.append("@NotEmpty(message = \"" + key + " 不能为空\")\n");
								stringBuilder.append("@Size(min = 1, message = \"" + key + " 至少需要一个元素\")\n");
								stringBuilder.append("private List<Long> " + key + ";\n");
							} else {
								stringBuilder.append("@NotEmpty(message = \"" + key + " 不能为空\")\n");
								stringBuilder.append("@Size(min = 1, message = \"" + key + " 至少需要一个元素\")\n");
								stringBuilder.append("private List<String> " + key + ";\n");
							}
							stringBuilder.append("\n");
						}
						continue;
					}
				}

				String className = WordUtils.capitalize(key) + "Bean";
				stringBuilder.append("private " + className + " " + key + ";\n");
				stringBuilder.append("@NoArgsConstructor\n");
				stringBuilder.append("@Setter\n");
				stringBuilder.append("@Getter\n");
				stringBuilder.append("@ToString(callSuper = true)\n");
				stringBuilder.append("@JsonInclude(JsonInclude.Include.NON_NULL)\n");
				stringBuilder.append("public static class " + className + " {\n");
				StringBuilder builder = new StringBuilder();
				buildParamAttribute(value, builder);
				stringBuilder.append(builder.toString());
				stringBuilder.append("}\n");
			} else {
				if (value instanceof Boolean) {
					stringBuilder.append("@NotNull(message = \"" + key + " 不能为空\")\n");
					stringBuilder.append("private Boolean " + key + ";\n");
					stringBuilder.append("\n");
				} else if (value instanceof Integer) {
					if (StringUtil.equalsIgnoreCase(key, "pageSize")) {
						stringBuilder.append("@NotNull(message = \"" + key + " 不能为空\")\n");
						stringBuilder.append("@Range(min = 1, max = 1000, message = \"" + key + " 数值不正确\")\n");
						stringBuilder.append("private Integer " + key + ";\n");
						stringBuilder.append("\n");
					} else {
						stringBuilder.append("@NotNull(message = \"" + key + " 不能为空\")\n");
						stringBuilder.append("@Range(min = 1, message = \"" + key + " 数值不正确\")\n");
						stringBuilder.append("private Integer " + key + ";\n");
						stringBuilder.append("\n");
					}
				} else if (value instanceof BigDecimal) {
					stringBuilder.append("@NotNull(message = \"" + key + " 不能为空\")\n");
					stringBuilder.append("@DecimalMin(value = \"0.00\", inclusive = true, message = \"" + key + " 数值不正确\")\n");
					stringBuilder.append("private BigDecimal " + key + ";\n");
					stringBuilder.append("\n");
				} else if (value instanceof Long || value instanceof BigInteger) {
					stringBuilder.append("@NotNull(message = \"" + key + " 不能为空\")\n");
					stringBuilder.append("@Range(min = 1, message = \"" + key + " 数值不正确\")\n");
					stringBuilder.append("private Long " + key + ";\n");
					stringBuilder.append("\n");
				} else if (value instanceof String) {
					if (StringUtil.containsIgnoreCase(key, "id") && !StringUtil.containsIgnoreCase(key, "ids")) {
						stringBuilder.append("@NotNull(message = \"" + key + " 不能为空\")\n");
						stringBuilder.append("@Range(min = 1, message = \"" + key + " 数值不正确\")\n");
						stringBuilder.append("private Long " + key + ";\n");
					} else {
						stringBuilder.append("@NotBlank(message = \"" + key + " 不能为空\")\n");
						stringBuilder.append("@Length(max = 250, message = \"" + key + " 长度不正确\")\n");
						stringBuilder.append("@SafeHtml\n");

						if (StringUtil.endsWith(key, "Url")) {
							stringBuilder.append("@Pattern(regexp = \"[a-zA-z]+://[^\\\\s]*\", message = \"链接地址必须包含：http:// 或 https:// 等前缀\")\n");
						} else if (StringUtil.endsWith(key, "Email")) {
							stringBuilder.append("@Email(message = \"邮箱地址格式不正确\")\n");
						} else if (StringUtil.endsWith(key, "Phone")) {
							stringBuilder.append("@Pattern(regexp = \"^[1][3,4,5,6,7,8,9][0-9]{9}$\", message = \"手机号格式有误\")\n");
						}

						stringBuilder.append("private String " + key + ";\n");
					}
					stringBuilder.append("\n");
				} else {
					throw new RuntimeException("找不到该类型转换");
				}
			}
		}
	}

	private static void buildResponseAttribute(Object data, StringBuilder stringBuilder) {
		JSONObject jsonObject = null;
		if (data instanceof JSONArray) {
			JSONArray dataObject = (JSONArray) data;
			if (dataObject.isEmpty()) {
				log.error("------zch------Response 根元素 dataObject 不能为空");
				return;
			}
			Object obj = dataObject.get(0);
			if (obj instanceof JSONObject) {
				jsonObject = (JSONObject) obj;
			} else {
				throw new RuntimeException("元素不符合可转换类型");
			}

		} else if (data instanceof JSONObject) {
			jsonObject = (JSONObject) data;
		}

		if (null == jsonObject) {
			return;
		}

		Set<Map.Entry<String, Object>> entrySet = jsonObject.entrySet();
		for (Map.Entry<String, Object> map : entrySet) {
			String key = map.getKey();
			Object value = map.getValue();
			if (value instanceof JSONObject || value instanceof JSONArray) {
				if (value instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) value;
					if (jsonArray.isEmpty()) {
						continue;
					}
					Object valueSub = jsonArray.get(0);
					if (!(valueSub instanceof JSONObject) && !(valueSub instanceof JSONArray)) {
						if (valueSub instanceof Integer) {
							stringBuilder.append("private List<Integer> " + key + ";\n");
						} else if (valueSub instanceof BigDecimal) {
							stringBuilder.append("private List<BigDecimal> " + key + ";\n");
						} else if (valueSub instanceof Long) {
							stringBuilder.append("private List<Long> " + key + ";\n");
						} else if (valueSub instanceof String) {
							if (StringUtil.containsIgnoreCase(key, "id") && !StringUtil.containsIgnoreCase(key, "ids")) {
								stringBuilder.append("private List<Long> " + key + ";\n");
							} else {
								stringBuilder.append("private List<String> " + key + ";\n");
							}
						}
						continue;
					} else if (valueSub instanceof JSONArray) {
						Object v = ((JSONArray) valueSub).get(0);
						if (v instanceof Integer) {
							stringBuilder.append("private List<List<Integer>> " + key + ";\n");
						} else if (v instanceof BigDecimal) {
							stringBuilder.append("private List<List<BigDecimal>> " + key + ";\n");
						} else if (v instanceof Long) {
							stringBuilder.append("private List<List<Long>> " + key + ";\n");
						} else if (v instanceof String) {
							if (StringUtil.containsIgnoreCase(key, "id") && !StringUtil.containsIgnoreCase(key, "ids")) {
								stringBuilder.append("private List<List<Long>> " + key + ";\n");
							} else {
								stringBuilder.append("private List<List<String>> " + key + ";\n");
							}
						} else if (v instanceof JSONObject) {
							String className = WordUtils.capitalize(key) + "Bean";
							stringBuilder.append("private List<" + className + "> " + key + ";\n");
							stringBuilder.append("@NoArgsConstructor\n");
							stringBuilder.append("@Setter\n");
							stringBuilder.append("@Getter\n");
							stringBuilder.append("@ToString(callSuper = true)\n");
							stringBuilder.append("@JsonInclude(JsonInclude.Include.NON_NULL)\n");
							stringBuilder.append("public static class " + className + " {\n");
							StringBuilder builder = new StringBuilder();
							buildResponseAttribute(v, builder);
							stringBuilder.append(builder.toString());
							stringBuilder.append("}\n");
						} else {
							stringBuilder.append("private List<List> " + key + ";\n");
						}
						continue;
					}
				}

				String className = WordUtils.capitalize(key) + "Bean";
				if (value instanceof JSONArray) {
					stringBuilder.append("private " + "List<" + className + ">" + " " + key + ";\n");
				} else {
					stringBuilder.append("private " + className + " " + key + ";\n");
				}
				stringBuilder.append("@NoArgsConstructor\n");
				stringBuilder.append("@Setter\n");
				stringBuilder.append("@Getter\n");
				stringBuilder.append("@ToString(callSuper = true)\n");
				stringBuilder.append("@JsonInclude(JsonInclude.Include.NON_NULL)\n");
				stringBuilder.append("public static class " + className + " {\n");
				StringBuilder builder = new StringBuilder();
				buildResponseAttribute(value, builder);
				stringBuilder.append(builder.toString());
				stringBuilder.append("}\n");
			} else {
				if (value instanceof Boolean) {
					stringBuilder.append("private Boolean " + key + ";\n");
				} else if (value instanceof Integer) {
					stringBuilder.append("private Integer " + key + ";\n");
				} else if (value instanceof BigDecimal) {
					stringBuilder.append("private BigDecimal " + key + ";\n");
				} else if (value instanceof Long || value instanceof BigInteger) {
					stringBuilder.append("private Long " + key + ";\n");
				} else if (value instanceof String) {
					if (StringUtil.containsIgnoreCase((String) value, ",")) {
						stringBuilder.append("private String " + key + ";\n");
					} else if (StringUtil.containsIgnoreCase(key, "id") && !StringUtil.containsIgnoreCase(key, "ids")) {
						stringBuilder.append("private Long " + key + ";\n");
					} else if (StringUtil.containsAny(key, "Date")) {
						stringBuilder.append("private Long " + key + ";\n");
					} else {
						stringBuilder.append("private String " + key + ";\n");
					}
				} else {
					throw new RuntimeException("找不到该类型转换");
				}
			}
		}
	}


	//=====================================私有方法  end=====================================


}
