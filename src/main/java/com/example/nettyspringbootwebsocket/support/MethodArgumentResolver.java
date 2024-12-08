package com.example.nettyspringbootwebsocket.support;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

/**
 * @author KangJunJie
 * @date 2024/12/6
 */
public interface MethodArgumentResolver {


    boolean supportsParameter(MethodParameter parameter);

    @Nullable
    Object resolveArgument(MethodParameter parameter, Channel channel);
}
