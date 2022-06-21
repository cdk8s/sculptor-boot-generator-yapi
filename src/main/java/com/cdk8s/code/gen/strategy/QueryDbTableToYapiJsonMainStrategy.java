package com.cdk8s.code.gen.strategy;


import com.cdk8s.code.gen.dto.ColumnEntity;
import com.cdk8s.code.gen.dto.ResponseBaseEntity;
import com.cdk8s.code.gen.dto.ResponsePageBaseEntity;
import com.cdk8s.code.gen.dto.TableEntity;
import com.cdk8s.code.gen.dto.yapi.YapiEntity;
import com.cdk8s.code.gen.dto.yapi.YapiJsonTemplateEntity;
import com.cdk8s.code.gen.strategy.common.GeneratorCommonUtil;
import com.cdk8s.code.gen.util.*;
import com.cdk8s.code.gen.util.id.GenerateIdUtil;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.Configuration;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryDbTableToYapiJsonMainStrategy implements GeneratorStrategy {

	@Override
	public void generatorFile(VelocityContext context, Configuration config) {
		String tableComment = (String) context.get("tableComment");
		String className = (String) context.get("className");
		List<ColumnEntity> columnList = (List<ColumnEntity>) context.get("columns");

		String yapiJson = readYapiJsonTemplateToJsonString(tableComment, className, columnList);
		String yapiJsonOutPath = config.getString("yapiJsonOutPath");
		yapiJsonOutPath = yapiJsonOutPath + "/" + className + ".json";
		FileUtil.writeStringToFile(new File(yapiJsonOutPath), yapiJson);

	}


	// =====================================业务 end=====================================
	// =====================================私有方法 start=====================================

	private static String readYapiJsonTemplateToJsonString(String tableComment, String className, List<ColumnEntity> columnList) {
		String content = FileUtil.readFileToStringByClasspath("templates/yapi/yapiJsonTemplate.json");
		YapiJsonTemplateEntity yapiJsonTemplateEntity = JsonUtil.toObject(content, YapiJsonTemplateEntity.class);
		List<YapiEntity> yapiImportEntityContent = yapiJsonTemplateEntity.getContent();
		for (YapiEntity entity : yapiImportEntityContent) {
			if (StringUtil.isBlank(tableComment)) {
				tableComment = className;
			}
			entity.setName(tableComment);
			List<YapiEntity.ListBean> entitySubList = entity.getList();
			for (YapiEntity.ListBean entitySub : entitySubList) {
				buildCreate(entitySub, className, columnList);
				buildDelete(entitySub, className, columnList);
				buildUpdate(entitySub, className, columnList);
				buildPage(entitySub, className, columnList);
				buildDetail(entitySub, className, columnList);
			}
		}

		return JsonUtil.toJsonPretty(yapiImportEntityContent);
	}

	private static void buildCreate(YapiEntity.ListBean entitySub, String className, List<ColumnEntity> columnList) {
		String path = entitySub.getPath();
		if (StringUtil.containsIgnoreCase(path, "/create")) {
			String newPath = "/api/" + className + "/create";
			entitySub.getQuery_path().setPath(newPath);
			entitySub.setPath(newPath);
			entitySub.setTitle("创建");
			List<ColumnEntity> columnEntityList = createColumns(columnList);
			Map<String, Object> map = new HashMap<>();
			for (ColumnEntity columnEntity : columnEntityList) {
				buildColumnEntity(columnEntity, map);
			}
			String jsonString = JsonUtil.toJsonPretty(map);
			jsonString = jsonString + buildComment(columnEntityList);
			entitySub.setReq_body_other(jsonString);
		}
	}

	private static void buildDelete(YapiEntity.ListBean entitySub, String className, List<ColumnEntity> columnList) {
		String path = entitySub.getPath();
		if (StringUtil.containsIgnoreCase(path, "/batchDelete")) {
			String newPath = "/api/" + className + "/batchDelete";
			entitySub.getQuery_path().setPath(newPath);
			entitySub.setPath(newPath);
			entitySub.setTitle("删除");
		}
	}

	private static void buildUpdate(YapiEntity.ListBean entitySub, String className, List<ColumnEntity> columnList) {
		String path = entitySub.getPath();
		if (StringUtil.containsIgnoreCase(path, "/update")) {
			String newPath = "/api/" + className + "/update";
			entitySub.getQuery_path().setPath(newPath);
			entitySub.setPath(newPath);
			entitySub.setTitle("更新");
			List<ColumnEntity> columnEntityList = updateColumns(columnList);
			Map<String, Object> map = new HashMap<>();
			map.put("id", String.valueOf(GenerateIdUtil.getId()));
			for (ColumnEntity columnEntity : columnEntityList) {
				buildColumnEntity(columnEntity, map);
			}
			String jsonString = JsonUtil.toJsonPretty(map);
			jsonString = jsonString + buildComment(columnEntityList);
			entitySub.setReq_body_other(jsonString);
		}
	}

	private static void buildPage(YapiEntity.ListBean entitySub, String className, List<ColumnEntity> columnList) {
		String path = entitySub.getPath();
		if (StringUtil.containsIgnoreCase(path, "/page")) {
			String newPath = "/api/" + className + "/page";
			entitySub.getQuery_path().setPath(newPath);
			entitySub.setPath(newPath);
			entitySub.setTitle("分页");

			List<Object> dataList = new ArrayList<>();
			List<ColumnEntity> columnEntityList = pageColumns(columnList);
			Map<String, Object> map = new HashMap<>();
			for (ColumnEntity columnEntity : columnEntityList) {
				buildColumnEntity(columnEntity, map);
			}
			dataList.add(map);

			ResponsePageBaseEntity responsePageBaseEntity = buildResponsePageBaseEntity(dataList);
			String jsonString = JsonUtil.toJsonPretty(responsePageBaseEntity);
			jsonString = jsonString + buildComment(columnEntityList);
			entitySub.setRes_body(jsonString);
		}
	}

	private static void buildDetail(YapiEntity.ListBean entitySub, String className, List<ColumnEntity> columnList) {
		String path = entitySub.getPath();
		if (StringUtil.containsIgnoreCase(path, "/detail")) {
			String newPath = "/api/" + className + "/detail";
			entitySub.getQuery_path().setPath(newPath);
			entitySub.setPath(newPath);
			entitySub.setTitle("详情");

			List<ColumnEntity> columnEntityList = detailColumns(columnList);
			Map<String, Object> map = new HashMap<>();
			map.put("id", String.valueOf(GenerateIdUtil.getId()));
			for (ColumnEntity columnEntity : columnEntityList) {
				buildColumnEntity(columnEntity, map);
			}
			ResponseBaseEntity responseBaseEntity = buildResponseBaseEntity(map);
			String jsonString = JsonUtil.toJsonPretty(responseBaseEntity);
			jsonString = jsonString + buildComment(columnEntityList);
			entitySub.setRes_body(jsonString);
		}
	}


	private static void buildColumnEntity(ColumnEntity columnEntity, Map<String, Object> map) {
		String columnName = columnEntity.getColumnName();
		String lowerAttrName = columnEntity.getLowerAttrName();


		if (StringUtil.equalsIgnoreCase(columnName, "id")) {
			map.put(lowerAttrName, RandomUtil.randomNumeric(18));
			return;
		}
		if (StringUtil.equalsIgnoreCase(columnName, "state_enum")) {
			map.put(lowerAttrName, 1);
			return;
		}
		if (StringUtil.equalsIgnoreCase(columnName, "ranking")) {
			map.put(lowerAttrName, 100);
			return;
		}

		if (StringUtil.containsAny(columnName, "_date")) {
			map.put(lowerAttrName, String.valueOf(DatetimeUtil.currentEpochMilli()));
			return;
		}
		if (StringUtil.containsAny(columnName, "user_id")) {
			map.put(lowerAttrName, "111111111111111111");
			return;
		}
		if (StringUtil.containsAny(columnName, "phone")) {
			map.put(lowerAttrName, "13800000000");
			return;
		}
		if (StringUtil.containsAny(columnName, "email")) {
			map.put(lowerAttrName, "gitnavi@qq.com");
			return;
		}
		if (StringUtil.containsAny(columnName, "parent_id")) {
			map.put(lowerAttrName, "1");
			return;
		}
		if (StringUtil.containsAny(columnName, "parent_ids")) {
			map.put(lowerAttrName, "1");
			return;
		}
		if (StringUtil.containsAny(columnName, "image") && StringUtil.containsAny(columnName, "url")) {
			map.put(lowerAttrName, "https://dummyimage.com/400x200.png");
			return;
		}
		if (StringUtil.containsAny(columnName, "avatar") && StringUtil.containsAny(columnName, "url")) {
			map.put(lowerAttrName, "https://dummyimage.com/100x100.png");
			return;
		}

		String attrType = columnEntity.getAttrType();
		if (StringUtil.equalsIgnoreCase(attrType, "Integer")) {
			map.put(lowerAttrName, RandomUtil.nextInt(1, 3));
			return;
		}
		if (StringUtil.equalsIgnoreCase(attrType, "Long")) {
			map.put(lowerAttrName, RandomUtil.randomNumeric(13));
			return;
		}
		if (StringUtil.equalsIgnoreCase(attrType, "BigDecimal")) {
			map.put(lowerAttrName, RandomUtil.nextBigDecimal(1, 3));
			return;
		}
		if (StringUtil.equalsIgnoreCase(attrType, "String")) {
			map.put(lowerAttrName, "这是" + columnEntity.getShortComment());
			return;
		}
	}

	private static String buildComment(List<ColumnEntity> columnEntityList) {
		StringBuilder stringBuilder = new StringBuilder();
		for (ColumnEntity entity : columnEntityList) {
			stringBuilder.append("\n");
			stringBuilder.append("//<comment>");
			stringBuilder.append(" ");
			stringBuilder.append(entity.getLowerAttrName());
			stringBuilder.append(" == ");
			stringBuilder.append(entity.getComment());
			stringBuilder.append("（");
			stringBuilder.append("允许为空：");
			stringBuilder.append(entity.getBoolIsNullable());
			stringBuilder.append("）");
			stringBuilder.append("</comment>");
		}
		return stringBuilder.toString();
	}


	private static List<ColumnEntity> createColumns(List<ColumnEntity> columnEntityList) {
		List<String> foreachIgnoreColumns = new ArrayList<>();
		foreachIgnoreColumns.add("id");
		foreachIgnoreColumns.add("tenant_id");
		foreachIgnoreColumns.add("delete_enum");
		foreachIgnoreColumns.add("create_date");
		foreachIgnoreColumns.add("create_user_id");
		foreachIgnoreColumns.add("update_date");
		foreachIgnoreColumns.add("update_user_id");
		foreachIgnoreColumns.add("delete_date");
		foreachIgnoreColumns.add("delete_user_id");

		List<ColumnEntity> columns = new ArrayList<>();
		for (ColumnEntity columnEntity : columnEntityList) {
			if (!foreachIgnoreColumns.contains(columnEntity.getColumnName())) {
				columns.add(columnEntity);
			}
		}
		return columns;
	}

	private static List<ColumnEntity> updateColumns(List<ColumnEntity> columnEntityList) {
		List<String> foreachIgnoreColumns = new ArrayList<>();
		foreachIgnoreColumns.add("id");//前面会主动加，保证排序永远在最前面
		foreachIgnoreColumns.add("tenant_id");
		foreachIgnoreColumns.add("delete_enum");
		foreachIgnoreColumns.add("create_date");
		foreachIgnoreColumns.add("create_user_id");
		foreachIgnoreColumns.add("update_date");
		foreachIgnoreColumns.add("update_user_id");
		foreachIgnoreColumns.add("delete_date");
		foreachIgnoreColumns.add("delete_user_id");

		List<ColumnEntity> columns = new ArrayList<>();
		for (ColumnEntity columnEntity : columnEntityList) {
			if (!foreachIgnoreColumns.contains(columnEntity.getColumnName())) {
				columns.add(columnEntity);
			}
		}
		return columns;
	}

	private static List<ColumnEntity> pageColumns(List<ColumnEntity> columnEntityList) {
		List<String> foreachIgnoreColumns = new ArrayList<>();
		foreachIgnoreColumns.add("tenant_id");
		foreachIgnoreColumns.add("delete_enum");
		foreachIgnoreColumns.add("delete_date");
		foreachIgnoreColumns.add("delete_user_id");

		List<ColumnEntity> columns = new ArrayList<>();
		for (ColumnEntity columnEntity : columnEntityList) {
			if (!foreachIgnoreColumns.contains(columnEntity.getColumnName())) {
				columns.add(columnEntity);
			}
		}
		return columns;
	}

	private static List<ColumnEntity> detailColumns(List<ColumnEntity> columnEntityList) {
		List<String> foreachIgnoreColumns = new ArrayList<>();
		foreachIgnoreColumns.add("id");
		foreachIgnoreColumns.add("tenant_id");
		foreachIgnoreColumns.add("delete_enum");
		foreachIgnoreColumns.add("delete_date");
		foreachIgnoreColumns.add("delete_user_id");

		List<ColumnEntity> columns = new ArrayList<>();
		for (ColumnEntity columnEntity : columnEntityList) {
			if (!foreachIgnoreColumns.contains(columnEntity.getColumnName())) {
				columns.add(columnEntity);
			}
		}
		return columns;
	}


	private static ResponseBaseEntity buildResponseBaseEntity(Object data) {
		ResponseBaseEntity entity = new ResponseBaseEntity();
		entity.setCode(200);
		entity.setSuccess(true);
		entity.setMsg("操作成功");
		entity.setTimestamp(1583415963985L);
		entity.setData(data);
		return entity;
	}

	private static ResponsePageBaseEntity buildResponsePageBaseEntity(List<Object> dataList) {
		ResponsePageBaseEntity entity = new ResponsePageBaseEntity();
		entity.setCode(200);
		entity.setSuccess(true);
		entity.setMsg("操作成功");
		entity.setTimestamp(1583415963985L);
		ResponsePageBaseEntity.DataBean dataBean = new ResponsePageBaseEntity.DataBean();
		dataBean.setTotal(1);
		dataBean.setPageNum(1);
		dataBean.setPageSize(10);
		dataBean.setSize(1);
		dataBean.setStartRow(1);
		dataBean.setEndRow(1);
		dataBean.setPages(1);
		dataBean.setPrePage(0);
		dataBean.setNextPage(0);
		dataBean.setIsFirstPage(true);
		dataBean.setIsLastPage(true);
		dataBean.setHasPreviousPage(false);
		dataBean.setHasNextPage(false);
		dataBean.setNavigatePages(8);
		dataBean.setNavigateFirstPage(1);
		dataBean.setNavigateLastPage(1);
		dataBean.setList(dataList);
		dataBean.setNavigatepageNums(Lists.newArrayList(1));
		entity.setData(dataBean);
		return entity;
	}


	private static List<ColumnEntity> buildColumnEntityList(Configuration config, TableEntity tableEntity, List<Map<String, Object>> columns) {
		List<ColumnEntity> columnList = new ArrayList<>();
		for (Map<String, Object> column : columns) {
			ColumnEntity columnEntity = new ColumnEntity();
			String columnName = (String) column.get("columnName");
			columnEntity.setColumnName(columnName);

			String columnComment = (String) column.get("columnComment");
			columnEntity.setComment(columnComment);
			columnEntity.setShortComment(GeneratorCommonUtil.buildColumnShortComment(columnComment));

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
				columnEntity.setCharacterMaximumLength(((BigInteger) characterMaximumLength).intValue());
			}

			columnEntity.setDataType((String) column.get("dataType"));
			columnEntity.setExtra((String) column.get("extra"));

			//列名转换成Java属性名
			columnEntity.setUpperAttrName(GeneratorCommonUtil.buildUpperAttrName(columnEntity.getColumnName()));
			columnEntity.setLowerAttrName(GeneratorCommonUtil.buildLowerAttrName(columnEntity.getColumnName()));


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
