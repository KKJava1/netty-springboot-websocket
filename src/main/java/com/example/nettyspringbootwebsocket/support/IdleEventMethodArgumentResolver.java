package com.example.nettyspringbootwebsocket.support;


import com.example.nettyspringbootwebsocket.annotations.OnEvent;
import com.example.nettyspringbootwebsocket.netty.AttributeKeyConstant;
import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;

import java.util.Objects;

/**
 * @author KangJunJie
 * @date 2024/12/6
 */
public class IdleEventMethodArgumentResolver implements MethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getMethod().isAnnotationPresent(OnEvent.class) && Objects.equals(parameter.getParameterType(),Object.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel) {
        return channel.attr(AttributeKeyConstant.idleStateEvent).get();
    }
}
