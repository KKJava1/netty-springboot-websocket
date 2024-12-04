package com.example.nettyspringbootwebsocket.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author KangJunJie
 * @date 2024/12/3 17:10
 */
@ChannelHandler.Sharable
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final WebsocketActionDispatch websocketActionDispatch;

    public HttpRequestHandler(WebsocketActionDispatch websocketActionDispatch) {
        this.websocketActionDispatch = websocketActionDispatch;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 校验请求路径
        boolean pass = verifyRequest(request);
        if (!pass) {
            ctx.close();
        }
        /**
         * 在 WebSocket 的握手阶段，通常需要同时读取 HTTP 请求头和请求体的信息,从fullHttpRequest中读取
         * 请求头：Upgrade: websocket、Connection: Upgrade。
         * URI：/socketServer/{userid}。
         * 由于 HttpRequest 和 HttpContent 是分开的，如果你使用它们就需要手动管理它们的组合。
         * 而 FullHttpRequest 是两者的结合，直接包含了完整的请求信息，因此更适合处理 WebSocket 握手流程。
         */
        ctx.channel().attr(AttributeKeyConstant.fullHttpRequest).set(request);
        // 存储请求的 URI
        ctx.channel().attr(AttributeKeyConstant.PATH_KEY).set(request.uri());

        // 根据路径参数（如 /socketServer/niezhiliang9595）解析模板变量
        Map<String, String> uriTemplateVariables = websocketActionDispatch.getUriTemplateVariables(request.uri());
        ctx.channel().attr(AttributeKeyConstant.uriTemplateVariables).set(uriTemplateVariables);
        // 分发握手事件
        websocketActionDispatch.dispatch(request.uri(), WebsocketActionDispatch.Action.HAND_SHAKE, ctx.channel());
        // 准备 WebSocket 的握手
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, true, 65536);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }
        else {
            handshaker.handshake(ctx.channel(), request).addListener(future -> {
                if (future.isSuccess()) {
                    websocketActionDispatch.dispatch(request.uri(), WebsocketActionDispatch.Action.OPEN,ctx.channel());
                } else {
                    handshaker.close(ctx.channel(), new CloseWebSocketFrame());
                }
            });
        }
    }


    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return "ws://" + location;
    }


    /**
     * 验证请求是否是Http升级Websocket
     * 并且验证uri是否合法
     * @param request
     * @return
     */

    /**
     *
     WebSocket 的连接建立分为两个主要步骤：

     客户端发起握手请求：
     客户端向服务器发送一个标准的 HTTP 请求，其中包含 Upgrade 和 Connection 头，表示它希望将当前连接从 HTTP 升级到 WebSocket。
     请求头示例：
     makefile
     {
         GET /path HTTP/1.1
         Host: example.com
         Connection: Upgrade
         Upgrade: websocket
         Sec-WebSocket-Key: x3JJHMbDL1EzLkh9L1+K2Q==
         Sec-WebSocket-Version: 13
     }
     服务器响应握手请求：
     如果服务器支持 WebSocket 协议并且验证通过，它会返回一个 HTTP 101 响应（表示协议切换），并且升级连接至 WebSocket协议。

     响应头示例：
     makefile
     {
         HTTP/1.1 101 Switching Protocols
         Upgrade: websocket
         Connection: Upgrade
         Sec-WebSocket-Accept: x3JJHMbDL1EzLkh9L1+K2Q
     }
     */
    private boolean verifyRequest(FullHttpRequest request) {
        /**
         * 需要Websocket的时候，会首先调用HTTP，通过升级头信息Upgrade，Websocket等操作后，才会升级为websocket
         */
        HttpHeaders headers = request.headers();
        String connection = headers.get("Connection");
        String upgrade = headers.get("Upgrade");
        String host = headers.get("Host");
        if (Objects.isNull(connection) || Objects.isNull(upgrade) || Objects.isNull(host)) {
            return false;
        } else if (!"Upgrade".equalsIgnoreCase(connection) || !"websocket".equalsIgnoreCase(upgrade))  {
            return false;
        } else if (!"GET".equalsIgnoreCase(request.method().name())) {
            return false;
        }
        return websocketActionDispatch.verifyUri(request.uri());
    }
}
