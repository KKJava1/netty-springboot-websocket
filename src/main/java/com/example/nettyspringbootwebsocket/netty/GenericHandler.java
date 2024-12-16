package com.example.nettyspringbootwebsocket.netty;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author KangJunJie
 * @date 2024/12/5
 */

//ChannelInboundHandlerAdapter 是 Netty 提供的一个适配器类，用于处理入站事件
public class GenericHandler extends ChannelInboundHandlerAdapter {

    //导入WebSocket 服务的核心管理类
    private final WebsocketActionDispatch websocketActionDispatch;


    public GenericHandler(WebsocketActionDispatch websocketActionDispatch) {
        this.websocketActionDispatch = websocketActionDispatch;
    }

    /**
     *  channelActive 是 Netty 的生命周期方法，当一个通道（Channel）变为活跃状态（即建立了连接）时会被调用。
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        String uri = ctx.channel().attr(AttributeKeyConstant.PATH_KEY).get();
        ctx.channel().attr(AttributeKeyConstant.idleStateEvent).set(evt);
        websocketActionDispatch.dispatch(uri, WebsocketActionDispatch.Action.EVENT,ctx.channel());
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String uri = ctx.channel().attr(AttributeKeyConstant.PATH_KEY).get();
        ctx.channel().attr(AttributeKeyConstant.throwable).set(cause);
        websocketActionDispatch.dispatch(uri, WebsocketActionDispatch.Action.ERROR,ctx.channel());
        super.exceptionCaught(ctx, cause);
    }
}
