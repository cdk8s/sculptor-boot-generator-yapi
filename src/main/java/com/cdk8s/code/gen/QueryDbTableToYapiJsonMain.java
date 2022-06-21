package com.cdk8s.code.gen;

import cn.hutool.core.bean.BeanUtil;
import com.cdk8s.code.gen.dto.ColumnEntity;
import com.cdk8s.code.gen.dto.TableEntity;
import com.cdk8s.code.gen.mapper.SysGeneratorMapper;
import com.cdk8s.code.gen.strategy.QueryDbTableToYapiJsonMainStrategy;
import com.cdk8s.code.gen.strategy.StrategyContext;
import com.cdk8s.code.gen.strategy.common.GeneratorCommonUtil;
import com.cdk8s.code.gen.util.CollectionUtil;
import com.cdk8s.code.gen.util.MybatisHelper;
import com.cdk8s.code.gen.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

@Slf4j
public class QueryDbTableToYapiJsonMain {


	//=====================================业务处理 start=====================================


	@SneakyThrows
	public static void main(String[] args) {
		log.info("------zch------根据数据库表生成 YApi JSON 文件 start");
		SqlSession sqlSession = MybatisHelper.getSqlSession();
		SysGeneratorMapper sysGeneratorMapper = sqlSession.getMapper(SysGeneratorMapper.class);

		Configuration config = GeneratorCommonUtil.getConfig();
		String tableName = config.getString("tableName");
		List<String> tableNames = StringUtil.split(tableName, ":");

		generatorMain(sysGeneratorMapper, config, tableNames);
		log.info("------zch------根据数据库表生成 YApi JSON 文件 end");

	}

	//=====================================业务处理  end=====================================
	//=====================================私有方法 start=====================================


	private static void generatorMain(SysGeneratorMapper sysGeneratorMapper, Configuration config, List<String> tableNames) {
		Map<String, String> mysqlVersion = sysGeneratorMapper.selectMySQLVersion();
		String boolMySQL8 = "false";
		if (StringUtil.startsWith(mysqlVersion.get("version"), "8.0")) {
			boolMySQL8 = "true";
		}

		for (String tableNameItem : tableNames) {
			Map<String, String> tableInfo = sysGeneratorMapper.queryTable(tableNameItem);
			if (null == tableInfo) {
				continue;
			}
			tableInfo.put("boolMySQL8", boolMySQL8);
			List<Map<String, Object>> columns = sysGeneratorMapper.queryColumns(tableNameItem);
			if (CollectionUtil.isNotEmpty(columns)) {
				for (Map<String, Object> map : columns) {
					String columnName = (String) map.get("columnName");
					if (checkIncludeUpperLetter(columnName)) {
						throw new RuntimeException("数据库字段命名不能使用驼峰命名：" + columnName);
					}
				}
			}

			generatorCode(config, tableInfo, columns);
		}
	}


	private static void generatorCode(Configuration config, Map<String, String> tableInfo, List<Map<String, Object>> columns) {

		Map<String, Object> contextParam = GeneratorCommonUtil.buildContextParam(config, tableInfo, columns);
		TableEntity tableEntity = (TableEntity) contextParam.get("tableEntity");

		contextParam.put("requestParamColumns", createRequestParamColumns(tableEntity.getColumns()));
		contextParam.put("responseDTOColumns", responseDTOColumns(tableEntity.getColumns()));


		Properties prop = new Properties();
		prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		Velocity.init(prop);

		VelocityContext context = new VelocityContext(contextParam);
		StrategyContext strategyContext = new StrategyContext();


		strategyContext.setGeneratorStrategy(new QueryDbTableToYapiJsonMainStrategy());
		strategyContext.executeStrategy(context, GeneratorCommonUtil.getConfig());

	}


	private static List<ColumnEntity> createRequestParamColumns(List<ColumnEntity> columnEntityList) {
		List<String> foreachIgnoreColumns = new ArrayList<>();
		foreachIgnoreColumns.add("tenant_id");
		foreachIgnoreColumns.add("parent_ids");
		foreachIgnoreColumns.add("delete_enum");
		foreachIgnoreColumns.add("delete_date");
		foreachIgnoreColumns.add("delete_user_id");
		foreachIgnoreColumns.add("create_date");
		foreachIgnoreColumns.add("create_user_id");
		foreachIgnoreColumns.add("update_date");
		foreachIgnoreColumns.add("update_user_id");

		List<ColumnEntity> columns = new ArrayList<>();
		for (ColumnEntity columnEntity : columnEntityList) {
			if (!foreachIgnoreColumns.contains(columnEntity.getColumnName())) {
				columns.add(columnEntity);
			}
		}
		return columns;
	}

	private static List<ColumnEntity> responseDTOColumns(List<ColumnEntity> columnEntityList) {
		List<String> foreachIgnoreColumns = new ArrayList<>();
		foreachIgnoreColumns.add("tenant_id");
		foreachIgnoreColumns.add("create_date");
		foreachIgnoreColumns.add("create_user_id");
		foreachIgnoreColumns.add("update_date");
		foreachIgnoreColumns.add("update_user_id");
		foreachIgnoreColumns.add("delete_enum");
		foreachIgnoreColumns.add("delete_date");
		foreachIgnoreColumns.add("delete_user_id");

		List<ColumnEntity> columns = new ArrayList<>();
		for (ColumnEntity columnEntity : columnEntityList) {
			String columnName = columnEntity.getColumnName();
			if (!foreachIgnoreColumns.contains(columnName)) {
				if (StringUtil.containsAny(columnName, "password", "pwd")) {
					continue;
				}
				columns.add(columnEntity);
				if (StringUtil.endsWith(columnName, "_enum") || StringUtil.startsWith(columnName, "bool_")) {
					ColumnEntity enumStringColumn = new ColumnEntity();
					BeanUtil.copyProperties(columnEntity, enumStringColumn);
					enumStringColumn.setColumnName(columnEntity.getColumnName() + "_string");
					enumStringColumn.setUpperAttrName(columnEntity.getUpperAttrName() + "String");
					enumStringColumn.setLowerAttrName(columnEntity.getLowerAttrName() + "String");
					enumStringColumn.setAttrType("String");
					columns.add(enumStringColumn);
				}
			}
		}

		return columns;
	}

	private static boolean checkIncludeUpperLetter(String str) {
		String regex = ".*[A-Z]+.*";
		return Pattern.matches(regex, str);
	}


	//=====================================私有方法  end=====================================


}
