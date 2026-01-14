package com.goalias.common.core.domain;

import com.goalias.common.core.constant.HttpStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应信息主体
 *
 * @author Goalias
 */
@Data
@NoArgsConstructor
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * 失败
     */
    public static final int FAIL = 500;

    private int code;

    private String message;

    private T data;

    public static <T> R<T> ok() {
        return restResult(null, SUCCESS, "操作成功");
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, SUCCESS, "操作成功");
    }

    public static <T> R<T> ok(String message) {
        return restResult(null, SUCCESS, message);
    }

    public static <T> R<T> ok(String message, T data) {
        return restResult(data, SUCCESS, message);
    }

    public static <T> R<T> fail() {
        return restResult(null, FAIL, "操作失败");
    }

    public static <T> R<T> fail(String message) {
        return restResult(null, FAIL, message);
    }

    public static <T> R<T> fail(T data) {
        return restResult(data, FAIL, "操作失败");
    }

    public static <T> R<T> fail(String message, T data) {
        return restResult(data, FAIL, message);
    }

    public static <T> R<T> fail(int code, String message) {
        return restResult(null, code, message);
    }

    /**
     * 返回警告消息
     *
     * @param message 返回内容
     * @return 警告消息
     */
    public static <T> R<T> warn(String message) {
        return restResult(null, HttpStatus.WARN, message);
    }

    /**
     * 返回警告消息
     *
     * @param message 返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> R<T> warn(String message, T data) {
        return restResult(data, HttpStatus.WARN, message);
    }

    private static <T> R<T> restResult(T data, int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMessage(message);
        return r;
    }

    public static <T> Boolean isError(R<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(R<T> ret) {
        return R.SUCCESS == ret.getCode();
    }
}
