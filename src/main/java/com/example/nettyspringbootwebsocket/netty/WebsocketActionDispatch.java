package com.example.nettyspringbootwebsocket.netty;


import com.example.nettyspringbootwebsocket.support.WebsocketServerEndpoint;
import io.netty.channel.Channel;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KangJunJie
 * @date 2024/12/2
 * 这是 WebSocket 服务的核心管理类，主要职责是 存储、匹配、验证和分发 WebSocket 请求到具体的服务端点。
 * 负责维护路径和端点之间的映射关系
 */

public class WebsocketActionDispatch {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 示例：{
     *     "/websocket/{uid}/{arg}": WebsocketServerEndpoint 实例
     * }
     */
    private final static Map<String, WebsocketServerEndpoint> endpointMap = new ConcurrentHashMap<>(16);


    /**
     * 验证请求路径是否合法
     * @param uri
     * @return
     */
    protected boolean verifyUri(String uri) {
        return endpointMap.keySet().stream().anyMatch(e -> antPathMatcher.match(e, uri));
    }

    /**
     * 添加websocket服务
     * @param endpoint
     */
     public void addWebsocketServerEndpoint(WebsocketServerEndpoint endpoint) {
        endpointMap.putIfAbsent(endpoint.getPath(),endpoint);
    }


    /**
     * uri匹配对应的websocket服务
     * @param uri
     * @return
     */
    protected WebsocketServerEndpoint matchServerEndpoint(String uri) {
        for (Map.Entry<String, WebsocketServerEndpoint> entry : endpointMap.entrySet()) {
            if (antPathMatcher.match(entry.getKey(),uri)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 事件分发到具体的方法
     * @param uri
     * @param action
     */
    protected void dispatch(String uri, Action action, Channel channel) {
        WebsocketServerEndpoint websocketServerEndpoint = matchServerEndpoint(uri);
        if (Objects.nonNull(websocketServerEndpoint)) {
            Method method = null;
            Object obj = websocketServerEndpoint.getObject();
            switch (action) {
                case HAND_SHAKE:
                    method = websocketServerEndpoint.getOnHandShake();
                    break;
                case OPEN:
                    method = websocketServerEndpoint.getOnOpen();
                    break;
                case CLOSE:
                    method = websocketServerEndpoint.getOnClose();
                    break;
                case MESSAGE:
                    method = websocketServerEndpoint.getOnMessage();
                    break;
                case EVENT:
                    method = websocketServerEndpoint.getOnEvent();
                    break;
                case ERROR:
                    method = websocketServerEndpoint.getOnError();
                    break;
                default:
                    break;
            }
            if (Objects.nonNull(method)) {
                /**
                 * https://blog.csdn.net/weixin_51360020/article/details/142994610 深入理解ReflectionUtils反射工具的使用
                 * 简单来说ReflectionUtils是在原生反射API的基础上又套了一层缓存declaredMethodsCache，类型是ConcurrentReferenceHashMap类型，比起原生的反射的效率回更快
                 */
//                Object[] args = new MethodParamsBuild().getMethodArgumentValues(method,channel);
                Object args = new Object();
                //执行某个对象的指定无/有参方法   args拿到获取的请求头和参数
                ReflectionUtils.invokeMethod(method,obj,args);
            }
        }

    }

    public Map<String,String> getUriTemplateVariables(String lookupPath) {
        WebsocketServerEndpoint websocketServerEndpoint = matchServerEndpoint(lookupPath);
        return antPathMatcher.extractUriTemplateVariables(websocketServerEndpoint.getPath(), lookupPath);
    }



     enum Action {
        HAND_SHAKE,
        OPEN,
        CLOSE,
        MESSAGE,
        EVENT,
        ERROR
    }
}
