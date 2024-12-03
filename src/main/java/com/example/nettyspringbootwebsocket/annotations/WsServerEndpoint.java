package com.example.nettyspringbootwebsocket.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author KangJunJie
 * @date 2024/12/2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WsServerEndpoint {

    String value() default "/ws/{arg}";
}
