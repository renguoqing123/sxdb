package com.sx.db.util;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Title: Class 系统响应吗枚举类 Description: 此处定义所有的系统内容的错误码
 *
 *
 * @author llk
 * @version v0.0.1
 */
public enum SysRespConstants {

	SUCCESS(ResponseResult.DEFAULT_SUCCESS_RESP_CODE, ResponseResult.DEFAULT_SUCCESS_RESP_MEMO), 
	SYSTEM_ERROR("99999","系统错误");

	@Getter
	@Setter
	private String code;
	@Getter
	@Setter
	private String name;

	SysRespConstants(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public static String getName(String code) {
		if (StringUtils.isEmpty(code)) {
			return null;
		}
		SysRespConstants[] sys = SysRespConstants.values();
		for (SysRespConstants s : sys) {
			if ((s.getCode()).equals(code))
				return s.getName();
		}
		return null;
	}

}
