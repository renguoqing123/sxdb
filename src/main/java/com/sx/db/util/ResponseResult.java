package com.sx.db.util;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Title: Class 所有json返回结构定义
 * Description:
 *
 * @param <T>
 * @author rengq	
 * @version v0.0.1
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ResponseResult<T> extends BaseRespMsg {

    private static final long serialVersionUID = 1L;

    private T data;

    public ResponseResult() {
    }

    public ResponseResult(String respCode, String memo) {
        this.respCode = respCode;
        this.memo = memo;
    }

    public ResponseResult(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return DEFAULT_SUCCESS_RESP_CODE.equals(getRespCode());
    }

}

