package com.alltobs.ossdemo.config.util;

import java.io.Serializable;

/**
 * 枚举 IResultCode
 *
 * @author ChenQi
 * &#064;date 2024/3/12
 */
public interface IResultCode extends Serializable {

    /**
     * 返回的code码
     *
     * @return code
     */
    int getCode();

    /**
     * 返回的消息
     *
     * @return 消息
     */
    default String getMsg() {
        return "系统未知异常";
    }
}
