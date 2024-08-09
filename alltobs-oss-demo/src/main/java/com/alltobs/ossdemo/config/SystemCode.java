package com.alltobs.ossdemo.config.enums;

import com.alltobs.ossdemo.config.util.IResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 枚举 SystemCode
 * </p>
 * 自定义异常code及提示信息
 *
 * @author ChenQi
 * &#064;date 2024/3/12
 */
@Getter
@RequiredArgsConstructor
public enum SystemCode implements IResultCode {

    /**
     * 权限部分异常
     */
    FORBIDDEN_401(SystemCode.LOGIN_CODE, "未经认证!"),

    /**
     * 系统未知异常
     */
    FAILURE(SystemCode.FAILURE_CODE, "系统未知异常"),

    /**
     * 操作成功
     */
    SUCCESS(SystemCode.SUCCESS_CODE, "操作成功"),
    ;

    /**
     * 通用 异常 code
     */
    public static final int FAILURE_CODE = -1;
    public static final int SUCCESS_CODE = 200;
    public static final int LOGIN_CODE = 401;
    public static final int AUTHORIZATION_CODE = 403;

    /**
     * code编码
     */
    private final int code;
    /**
     * 中文信息描述
     */
    private final String msg;
}
