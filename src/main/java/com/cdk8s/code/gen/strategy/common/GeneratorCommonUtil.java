package com.cdk8s.code.gen.strategy.common;


import cn.hutool.core.date.DateUtil;
import com.cdk8s.code.gen.dto.ColumnEntity;
import com.cdk8s.code.gen.dto.TableEntity;
import com.cdk8s.code.gen.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.WordUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public final class GeneratorCommonUtil {

	// =====================================业务 start=====================================
	public static Map<String, Object> buildContextParam(Configuration config, Map<String, String> tableInfo, List<Map<String, Object>> columns) {
		Map<String, Object> contextParam = new HashMap<>(30);

		TableEntity tableEntity = new TableEntity();
		tableEntity.setTableName(tableInfo.get("tableName"));
		tableEntity.setComments(tableInfo.get("tableComment"));

		String tableName = tableEntity.getTableName();
		String ClassName = buildClassName(tableName);
		tableEntity.setUpperClassName(ClassName);
		tableEntity.setLowerClassName(StringUtil.uncapitalize(ClassName));
		tableEntity.setBoolMySQL8(Boolean.valueOf(tableInfo.get("boolMySQL8")));

		List<ColumnEntity> columnList = buildColumnEntityList(config, tableEntity, columns, contextParam);
		tableEntity.setColumns(columnList);

		if (tableEntity.getPk() == null) {
			tableEntity.setPk(tableEntity.getColumns().get(0));
		}

		contextParam.put("tableEntity", tableEntity);
		contextParam.put("tableName", tableName);
		contextParam.put("tableComment", tableEntity.getComments());
		contextParam.put("pk", tableEntity.getPk());
		contextParam.put("ClassName", tableEntity.getUpperClassName());
		contextParam.put("className", tableEntity.getLowerClassName());
		contextParam.put("classname", StringUtil.lowerCase(tableEntity.getLowerClassName()));
		contextParam.put("class_name", StringUtil.lowerCamelToLowerUnderscore(tableEntity.getLowerClassName()));
		contextParam.put("lower_first", StringUtil.lowerFirstFromSeparator(tableName, "_"));
		contextParam.put("pathName", tableEntity.getLowerClassName().toLowerCase());
		contextParam.put("columns", tableEntity.getColumns());
		contextParam.put("datetime", DateUtil.now());
		contextParam.put("moduleName", config.getString("moduleName"));
		contextParam.put("javaRootPackage", config.getString("javaRootPackage"));
		return contextParam;
	}


	public static String buildColumnShortComment(String columnComment) {
		return StringUtil.substringBefore(StringUtil.substringBefore(columnComment, ":"), "(");
	}

	public static String buildUpperAttrName(String columnName) {
		return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
	}

	public static String buildLowerAttrName(String columnName) {
		return StringUtil.uncapitalize(buildUpperAttrName(columnName));
	}

	public static String buildClassName(String tableName) {
		return buildUpperAttrName(tableName);
	}

	public static Configuration getConfig() {
		try {
			return new PropertiesConfiguration("generator.properties");
		} catch (Exception e) {
			throw new RuntimeException("获取配置文件失败，", e);
		}
	}

	// =====================================业务 end=====================================
	// =====================================私有方法 start=====================================


	private static List<ColumnEntity> buildColumnEntityList(Configuration config, TableEntity tableEntity, List<Map<String, Object>> columns, Map<String, Object> contextParam) {
		List<ColumnEntity> columnList = new ArrayList<>();
		for (Map<String, Object> column : columns) {
			ColumnEntity columnEntity = new ColumnEntity();
			String columnName = (String) column.get("columnName");
			columnEntity.setColumnName(columnName);

			String columnComment = (String) column.get("columnComment");
			columnEntity.setComment(columnComment);
			columnEntity.setShortComment(buildColumnShortComment(columnComment));

			String maxValue = StringUtil.substringAfter(columnComment.toLowerCase(), "max=");
			if (StringUtil.isNotBlank(maxValue)) {
				columnEntity.setMaxValue(Integer.valueOf(maxValue));
			}

			if (StringUtil.endsWith(columnName, "_enum") || StringUtil.startsWith(columnName, "bool_")) {
				columnEntity.setBoolIsEnum(true);
			}

			Object columnDefault = column.get("columnDefault");
			if (null != columnDefault) {
				columnEntity.setColumnDefault((String) columnDefault);
			}

			Object isNullable = column.get("isNullable");
			if (null != isNullable && StringUtil.equalsIgnoreCase(isNullable.toString(), "yes")) {
				columnEntity.setBoolIsNullable(true);
			}
			Object characterMaximumLength = column.get("characterMaximumLength");
			if (null != characterMaximumLength) {
				if (tableEntity.getBoolMySQL8()) {
					columnEntity.setCharacterMaximumLength(((Long) characterMaximumLength).intValue());
				} else {
					columnEntity.setCharacterMaximumLength(((BigInteger) characterMaximumLength).intValue());
				}
			}


			columnEntity.setDataType((String) column.get("dataType"));
			columnEntity.setExtra((String) column.get("extra"));

			//列名转换成Java属性名
			columnEntity.setUpperAttrName(buildUpperAttrName(columnEntity.getColumnName()));
			columnEntity.setLowerAttrName(buildLowerAttrName(columnEntity.getColumnName()));

			//列的数据类型，转换成Java类型
			String attrType = config.getString(columnEntity.getDataType(), "unknowType");
			columnEntity.setAttrType(attrType);
			//是否主键
			if ("PRI".equalsIgnoreCase((String) column.get("columnKey")) && tableEntity.getPk() == null) {
				tableEntity.setPk(columnEntity);
			}

			columnList.add(columnEntity);
		}
		return columnList;
	}


	// =====================================私有方法 end=====================================

}
