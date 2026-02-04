package com.cola.attendance.common.exception;

import com.cola.attendance.common.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理。
 * 将参数校验、权限、非法参数及未知异常统一封装为 {@link Result} 返回。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 请求体 @Valid 校验失败（如 DTO 字段校验） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    /** 请求参数绑定失败（如表单、Query 参数） */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("参数错误");
        return Result.fail(400, msg);
    }

    /** 无权限访问（403） */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDenied() {
        return Result.fail(403, "无权限");
    }

    /** 非法参数（业务层抛出） */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArg(IllegalArgumentException e) {
        return Result.fail(400, e.getMessage());
    }

    /** 其他未捕获异常，返回 500 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleOther(Exception e) {
        return Result.fail(500, e.getMessage());
    }
}
