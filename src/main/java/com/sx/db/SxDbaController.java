package com.sx.db;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sx.db.util.ExcelUtil;
import com.sx.db.util.ResponseResult;
import com.sx.db.util.SysRespConstants;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@ImportResource(locations={"classpath:spring-a*.xml"})
@Slf4j
@RestController
public class SxDbaController {
	
	@Autowired
	protected HttpServletRequest request;
	
	@SuppressWarnings("resource")
	@RequestMapping(value = "/queryTable/{db}/{table}", method = RequestMethod.GET)
	public ResponseResult<ResultPage<List<Map<String ,String>>>> queryTable(@PathVariable("db") String db,@PathVariable("table") String table,@RequestParam(required=false) String condition,
			@RequestParam Integer pageNum, @RequestParam Integer pageSize,@RequestParam(value = "sort", required = false) String sort){
		ResponseResult<ResultPage<List<Map<String ,String>>>> res=new ResponseResult<>();
		ResultPage<List<Map<String ,String>>> result = new ResultPage<>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
//			Properties properties = PropertiesLoaderUtils.loadAllProperties(""+db+".properties");
//			String param = properties.getProperty(table);
			db = db.replaceAll("-", "_");
			Map<String, List<String>> data = getList(db);
			List<String> lists = data.get(table);
			String param = StringUtils.join(lists.toArray(),",");
			String dbku="dataSourceHikari_"+db;
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
			HikariDataSource dataSource = (HikariDataSource)applicationContext.getBean(dbku);
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			String sql = "SELECT "+param+" FROM "+table+" ";
			String sqlConunt = "SELECT count(*) as record FROM "+table+" ";
			if(null != condition) {
				condition = condition.toUpperCase();
			}
			if(!ObjectUtils.isEmpty(condition) && condition.contains("SELECT")) {
				sql = condition;
				if(condition.contains("FROM")) {
					sqlConunt ="SELECT count(*) as record FROM"+condition.split("FROM")[1];
				}else {
					log.info("unKonw condition:{}",condition);
				}
				param = condition.split("FROM")[0];
				param = param.split("SELECT")[1].trim();
			}else if(!ObjectUtils.isEmpty(condition) && !condition.contains("AND") && (condition.contains("ORDER BY"))) {
				sql = sql + condition;
				sqlConunt = sqlConunt + condition;
			}else if(!ObjectUtils.isEmpty(condition)) {
				sql = sql + condition;
				sqlConunt = sqlConunt + condition;
			}
			//总记录数
			rs= statement.executeQuery(sqlConunt);
			int rowCount = 0;
			if(rs.next()){
				rowCount=rs.getInt("record");
			}
			if(pageNum==-1) {//升序
				pageNum=1;
				if(null == sort) {throw new RuntimeException("sort value is null");}
				sql = sql+" ORDER BY "+sort+" ASC";
			}else if(pageNum==-2) {//降序
				pageNum=1;
				if(null == sort) {throw new RuntimeException("sort value is null");}
				sql = sql+" ORDER BY "+sort+" DESC";;
			}
			if(pageNum==0) {pageNum=1;}
			int pageNo = ((pageNum-1)*pageSize);
			sql = sql+" LIMIT "+pageNo+","+pageSize;
			log.info("db={}", db);
			log.info("sql={}", sql);
			rs = statement.executeQuery(sql);
			String[] str = param.split(",");
			List<Map<String ,String>> dataList = new ArrayList<Map<String, String>>();
			while (rs.next()) {
				Map<String ,String> map=new LinkedHashMap<String, String>();
				for(String key:str) {
					String value = rs.getString(key);
					map.put(key, value);
				}
				dataList.add(map);
			}
			result.setPageNum(pageNum);
			result.setPageSize(pageSize);
			result.setTotal(rowCount);
			result.setData(dataList);
		} catch (Exception e) {
			log.error("sql = error:{}", e.getMessage());
			res.setMemo(e.getMessage());
			res.setRespCode(SysRespConstants.SYSTEM_ERROR.getCode());
		}finally {
			try {
				connection.close();
				statement.close();
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		res.setData(result);
		return res;
	}
	
	//导出
	@SuppressWarnings("resource")
	@RequestMapping(value = "/exportTable/{db}/{table}", method = RequestMethod.GET)
	public void exportTable(@PathVariable("db") String db,@PathVariable("table") String table,@RequestParam(required=false) String condition,
			@RequestParam(value = "sort", required = false) String sort,HttpServletResponse response){
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
//			Properties properties = PropertiesLoaderUtils.loadAllProperties(""+db+".properties");
//			String param = properties.getProperty(table);
			db = db.replaceAll("-", "_");
			Map<String, List<String>> data = getList(db);
			List<String> lists = data.get(table);
			String param = StringUtils.join(lists.toArray(),",");
			String dbku="dataSourceHikari_"+db;
			ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
			HikariDataSource dataSource = (HikariDataSource)applicationContext.getBean(dbku);
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			String sql = "SELECT "+param+" FROM "+table+" ";
			String sqlConunt = "SELECT count(*) as record FROM "+table+" ";
			if(null != condition) {
				condition = condition.toUpperCase();
			}
			if(!ObjectUtils.isEmpty(condition) && condition.contains("SELECT")) {
				sql = condition;
				if(condition.contains("FROM")) {
					sqlConunt ="SELECT count(*) as record FROM"+condition.split("FROM")[1];
				}else {
					log.info("unKonw condition:{}",condition);
				}
				param = condition.split("FROM")[0];
				param = param.split("SELECT")[1].trim();
			}else if(!ObjectUtils.isEmpty(condition) && !condition.contains("AND") && (condition.contains("ORDER BY"))) {
				sql = sql + condition;
				sqlConunt = sqlConunt + condition;
			}else if(!ObjectUtils.isEmpty(condition)) {
				sql = sql + condition;
				sqlConunt = sqlConunt + condition;
			}
			//总记录数
			rs= statement.executeQuery(sqlConunt);
			int rowCount = 0;
			if(rs.next()){
				rowCount=rs.getInt("record");
			}
			log.info("db={}", db);
			log.info("sql={}", sql);
			log.info("rowCount:{}",rowCount);
			rs = statement.executeQuery(sql);
			String[] str = param.split(",");
			List<Map<String ,String>> dataList = new ArrayList<Map<String, String>>();
			while (rs.next()) {
				Map<String ,String> map=new LinkedHashMap<String, String>();
				for(String key:str) {
					String value = rs.getString(key);
					map.put(key, value);
				}
				dataList.add(map);
			}
			String fileName = "数据导出.xlsx";
			XSSFWorkbook workbook = ExcelUtil.exportExcelXlsx("数据导出", str, dataList);
			OutputStream out = response.getOutputStream();
			response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
			response.setContentType("application/msexcel;charset=UTF-8");
			workbook.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
	         e.printStackTrace();
	    } catch (Exception e) {
			log.error("export xlsx sql = error:{}", e.getMessage());
			e.printStackTrace();
		}finally {
			try {
				connection.close();
				statement.close();
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	//查询表
	@RequestMapping(value = "/queryTableList/{db}", method = RequestMethod.GET)
	public Map<String, String> queryTableList(@PathVariable("db") String db) throws SQLException, IOException {
//		Properties properties = PropertiesLoaderUtils.loadAllProperties(""+db+".properties");
//		db = db.replaceAll("-", "_");
		return getDB(db);
	}
	
	//查询表字段
	@RequestMapping(value = "/queryTableParamList/{db}/{table}", method = RequestMethod.GET)
	public List<String> queryTableParamList(@PathVariable("db") String db,@PathVariable("table") String table) throws SQLException, IOException {
//		db = db.replaceAll("-", "_");
		Map<String, List<String>> data = getList(db);
		return data.get(table);
	}
	
	private Map<String, List<String>> getList(String db) {//查询字段
		Map<String, List<String>> data = new LinkedHashMap<>();
		String dbSource="prod";
		if(db.contains("test")) {dbSource="test";}
		dbSource = (dbSource+"_column_DB");
		try {
			Class<SxDbInit> clazz = SxDbInit.class;
			Field field = clazz.getDeclaredField(dbSource);
			data = (Map<String,List<String>>)field.get(dbSource);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	private Map<String,String> getDB(String db) {//查询表
		String dbSource="prod";
		if(db.contains("test")) {dbSource="test";db=db.substring(0, db.indexOf("-test"));}
		dbSource = (dbSource+"_ku_table_comment_DB");
		try {
			Class<SxDbInit> clazz = SxDbInit.class;
			Field field = clazz.getDeclaredField(dbSource);
			Map<String,Map<String,String>> res = (Map<String,Map<String,String>>)field.get(dbSource);
			Map<String,String> mapRes = res.get(db);
			return mapRes;
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new LinkedHashMap<>(128);
	}
	
	//查询DB
	@RequestMapping(value = "/queryDbList", method = RequestMethod.GET)
	public List<String> queryDbList() throws SQLException, IOException {
		Properties properties = PropertiesLoaderUtils.loadAllProperties("db.properties");
		String param = properties.getProperty("db");
		if(!ObjectUtils.isEmpty(param)) {
			String[] str=param.split(",");
			return Arrays.asList(str);
		}
		return new ArrayList<String>();
	}
	
}
