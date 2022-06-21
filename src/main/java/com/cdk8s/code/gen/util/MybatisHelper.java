package com.cdk8s.code.gen.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;
import tk.mybatis.mapper.entity.Config;
import tk.mybatis.mapper.mapperhelper.MapperHelper;

import java.io.IOException;
import java.io.Reader;


public class MybatisHelper {
	private static SqlSessionFactory sqlSessionFactory;

	static {
		try {
			Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			reader.close();
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionFactory.openSession();
				MapperHelper mapperHelper = new MapperHelper();
				Config config = new Config();
				config.setIDENTITY("MYSQL");
				config.setEnableMethodAnnotation(true);
				config.setNotEmpty(true);
				config.setCheckExampleEntityClass(true);
				config.setUseSimpleType(true);
				config.setOrder("AFTER");
				mapperHelper.setConfig(config);
				mapperHelper.registerMapper(Mapper.class);
				mapperHelper.registerMapper(MySqlMapper.class);
				mapperHelper.registerMapper(IdsMapper.class);
				mapperHelper.processConfiguration(sqlSession.getConfiguration());

			} finally {
				if (sqlSession != null) {
					sqlSession.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static SqlSession getSqlSession() {
		return sqlSessionFactory.openSession();
	}
}
