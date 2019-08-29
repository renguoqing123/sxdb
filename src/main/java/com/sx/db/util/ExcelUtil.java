package com.sx.db.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {
	public static XSSFWorkbook exportExcelXlsx(String sheetName,String[] sheetHead,List<Map<String ,String>> dataList) {
		// 声明一个工作薄
        XSSFWorkbook workBook = new XSSFWorkbook();
        // 生成一个表格
        XSSFSheet sheet = workBook.createSheet();
        
        workBook.setSheetName(0, sheetName);
        
        // 创建表格标题行 第一行
        XSSFRow titleRow = sheet.createRow(0);
        
        for (int i = 0; i < sheetHead.length; i++) {
            titleRow.createCell(i).setCellValue(sheetHead[i]);
        }
        //插入需导出的数据
        for (int i = 0; i < dataList.size(); i++) {
        	XSSFRow row = sheet.createRow(i + 1);
        	Map map = dataList.get(i);
        	for (int j = 0; j < sheetHead.length; j++) {
        		XSSFCell cell = row.createCell(j);
        		Object value = map.get(sheetHead[j]);
        		if(null != value) {
            		cell.setCellValue(value.toString());
            	}else {
            		cell.setCellValue(StringUtils.EMPTY);
            	}
        	}
        }
        return workBook;
	}
}
