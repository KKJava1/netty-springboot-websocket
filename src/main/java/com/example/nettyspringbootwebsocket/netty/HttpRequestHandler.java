package com.example.nettyspringbootwebsocket.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

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
        // 参数传递到WebsocketHandler
        ctx.channel().attr(AttributeKeyConstant.fullHttpRequest).set(request);

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
