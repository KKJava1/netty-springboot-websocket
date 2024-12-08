package com.example.nettyspringbootwebsocket.support;

import com.example.nettyspringbootwebsocket.annotations.PathParam;
import com.example.nettyspringbootwebsocket.netty.AttributeKeyConstant;
import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author KangJunJie
 * @date 2024/12/6
 */
public class PathParaMethodArgumentResolver implements MethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PathParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Channel channel) {
        Map<String, String> uriTemplateVariables = channel.attr(AttributeKeyConstant.uriTemplateVariables).get();
        String name = parameter.getParameterName();
        PathParam annotation = parameter.getParameterAnnotation(PathParam.class);
        if (StringUtils.hasLength(annotation.value())) {
            name = annotation.value();
        }
        return uriTemplateVariables.get(name);
    }
}
