package com.example.nettyspringbootwebsocket.support;

import com.example.nettyspringbootwebsocket.annotations.OnMessage;
import com.example.nettyspringbootwebsocket.netty.AttributeKeyConstant;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.core.MethodParameter;

import java.util.Objects;

/**
 * @author KangJunJie
 * @date 2024/12/6
 */
public class TextMethodArgumentResolver implements MethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getMethod().isAnnotationPresent(OnMessage.class)
                && Objects.equals(parameter.getParameterType(),String.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel) {
        TextWebSocketFrame text = channel.attr(AttributeKeyConstant.textWebSocketFrame).get();
        return text.text();
    }
}
