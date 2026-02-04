package com.cola.attendance.common.result;

import lombok.Data;

/**
 * 统一 API 响应封装。
 *
 * @param <T> 业务数据泛型
 */
@Data
public class Result<T> {
    /** 状态码：0 成功，非 0 失败 */
    private int code;
    /** 提示信息 */
    private String msg;
    /** 业务数据 */
    private T data;

    /** 成功响应，code=0，msg="success" */
    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    /** 失败响应，code=1 */
    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.setCode(1);
        r.setMsg(msg);
        return r;
    }

    /** 失败响应，自定义 code 与 msg（如 400/403/500） */
    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
