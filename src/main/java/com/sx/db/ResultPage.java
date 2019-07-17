package com.sx.db;

import lombok.Data;

@Data
public class ResultPage<T> {
	
	private int pageNum;
	private int pageSize;
	private int total;
	
	private T data;
	
	public ResultPage() {
	}
	
	public ResultPage(T data) {
		this.data=data;
	}
}
