package com.sx.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SxDbInit implements InitializingBean {
	
	//test
	public static Map<String,List<String>> test_column_DB = new LinkedHashMap<>(128);
	
	public static Map<String,String> test_comment_DB = new LinkedHashMap<>(128);
	
	public static List<String> test_table_DB = new ArrayList<>(128);
	
//	public static Map<String,List<String>> test_ku_table_DB = new LinkedHashMap<>(128);
	
	public static Map<String,Map<String,String>> test_ku_table_comment_DB = new LinkedHashMap<>(128);
	
	//prod
	public static Map<String,List<String>> prod_column_DB = new LinkedHashMap<>(128);
	
	public static List<String> prod_table_DB = new ArrayList<>(128);
	
	public static Map<String,String> prod_comment_DB = new LinkedHashMap<>(128);
	
//	public static Map<String,List<String>> prod_ku_table_DB = new LinkedHashMap<>(128);
	
	public static Map<String,Map<String,String>> prod_ku_table_comment_DB = new LinkedHashMap<>(128);
	
	@Override
	public void afterPropertiesSet(){
		initdb();
	}
	
	
	public void initdb() {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		ResultSet rsc = null;
		try {
			log.info("---初始化initdb----");
			Properties information = PropertiesLoaderUtils.loadAllProperties("information.properties");
			Properties library = PropertiesLoaderUtils.loadAllProperties("library.properties");
			Enumeration<Object> db= information.keys();
			if(db.hasMoreElements()) {
				Object key = db.nextElement();
				log.info("key={}", key);
				Object value = information.get(key);
				String[] val = value.toString().split(",");
				for(int i = 0; i<val.length; i++) {
					String v = val[i];
					String dbBeanId="dataSourceHikari_information_"+v;
					HikariDataSource dataSource = null;
					log.info("环境==dbBeanId={}", dbBeanId);
					try {
						dataSource = (HikariDataSource)SpringContextHolder.getBean(dbBeanId);
					} catch (Exception e) {
						continue;
					}
					boolean test = false;
					if(v.equals("test")) { test=true; }
					String libValue = library.getProperty(v);
					String[] libVal = libValue.split(",");
					for(int j = 0; j<libVal.length; j++) {
						String ku = libVal[j];
						log.info("数据库库名：ku={}",ku);
						connection = dataSource.getConnection();
						statement = connection.createStatement();
						rs = statement.executeQuery("SELECT TABLE_NAME,TABLE_COMMENT FROM TABLES WHERE TABLE_SCHEMA='"+ku+"'");
						if(test) {
							test_table_DB = new ArrayList<>(128);
							test_comment_DB = new LinkedHashMap<>(128);
							while (rs.next()) {
								String tableName = rs.getString("TABLE_NAME");
								String tableComment=rs.getString("TABLE_COMMENT");
								test_comment_DB.put(tableName, tableComment);
								test_table_DB.add(tableName);
							}
							test_ku_table_comment_DB.put(ku, test_comment_DB);
							//test_ku_table_DB.put(ku, test_table_DB);//报存库下的所有表名
//							log.info("ku_table_DB={}", ku_table_DB);
							for(String tableName:test_table_DB) {
								rsc = statement.executeQuery("SELECT COLUMN_NAME,COLUMN_COMMENT FROM COLUMNS WHERE TABLE_SCHEMA='"+ku+"' AND TABLE_NAME='"+tableName+"'");
								List<String> data = new ArrayList<>(128);
								while (rsc.next()) {
									data.add(rsc.getString("COLUMN_NAME"));
								}
								test_column_DB.put(tableName, data);//保存表下所有字段
							}
						} else {
							prod_table_DB = new ArrayList<>(128);
							prod_comment_DB = new LinkedHashMap<>(128);
							while (rs.next()) {
								String tableName = rs.getString("TABLE_NAME");
								String tableComment=rs.getString("TABLE_COMMENT");
								prod_comment_DB.put(tableName, tableComment);
								prod_table_DB.add(tableName);
							}
							prod_ku_table_comment_DB.put(ku, prod_comment_DB);
							//prod_ku_table_DB.put(ku, prod_table_DB);//报存库下的所有表名
							for(String tableName:prod_table_DB) {
								rsc = statement.executeQuery("SELECT COLUMN_NAME,COLUMN_COMMENT FROM COLUMNS WHERE TABLE_SCHEMA='"+ku+"' AND TABLE_NAME='"+tableName+"'");
								List<String> data = new ArrayList<>(128);
								while (rsc.next()) {
									data.add(rsc.getString("COLUMN_NAME"));
								}
								prod_column_DB.put(tableName, data);//保存表下所有字段
							}
						}
					}
//					log.info("prod_ku_table_DB={}", prod_ku_table_DB);
					log.info("prod_table_DB={}", prod_table_DB);
					log.info("prod_comment_DB={}",prod_comment_DB);
					log.info("prod_column_DB={}", prod_column_DB);
					
//					log.info("test_ku_table_DB={}", test_ku_table_DB);
					log.info("test_table_DB={}", test_table_DB);
					log.info("test_comment_DB={}",test_comment_DB);
					log.info("test_column_DB={}", test_column_DB);
				}
			}
		} catch (IOException e) {
			log.info("IOException={}", e);
		} catch (Exception e) {
			log.info("Exception={}", e);
		}finally {
			try {
				rs.close();
				rsc.close();
				connection.close();
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
