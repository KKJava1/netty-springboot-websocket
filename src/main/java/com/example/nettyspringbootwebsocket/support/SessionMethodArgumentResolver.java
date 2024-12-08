package com.example.nettyspringbootwebsocket.support;

import com.example.nettyspringbootwebsocket.socket.Session;
import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;

import java.util.Objects;

/**
 * @author KangJunJie
 * @date 2024/12/6
 */
public class SessionMethodArgumentResolver implements MethodArgumentResolver{

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Objects.equals(Session.class,parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel) {
        return new Session(channel);
    }
}
