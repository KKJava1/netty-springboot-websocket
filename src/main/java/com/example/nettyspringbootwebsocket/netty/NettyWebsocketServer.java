package com.example.nettyspringbootwebsocket.netty;

import com.example.nettyspringbootwebsocket.WebsocketProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author KangJunJie
 * @date 2024/12/3 16:47
 */
public class NettyWebsocketServer {

    //导入分发器
    private final WebsocketActionDispatch websocketActionDispatch;

    //导入配置
    private WebsocketProperties websocketProperties;

    //导入构造方法
    public NettyWebsocketServer(WebsocketActionDispatch websocketActionDispatch,WebsocketProperties websocketProperties) {
        this.websocketActionDispatch = websocketActionDispatch;
        this.websocketProperties = websocketProperties;
    }


    public void start() throws InterruptedException {
        //通过配置文件来获取Boss线程和Work线程的数量
        NioEventLoopGroup boss = new NioEventLoopGroup(websocketProperties.getBossThreadNums());
        NioEventLoopGroup work = new NioEventLoopGroup(websocketProperties.getWorkerThreadNums());
        //开启服务端启动类
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,work)
                .channel(NioSctpServerChannel.class)
                /**
                 * 通过匿名内部类来实现ChannelPipeline责任链模式调用
                 */
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        //HTTP协议的编解码器，
                        pipeline.addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(websocketProperties.getMaxContentLength()))
                                .addLast(new IdleStateHandler(websocketProperties.getReaderIdleTimeSeconds()
                                        ,websocketProperties.getWriterIdleTimeSeconds()
                                        ,websocketProperties.getAllIdleTimeSeconds()))
                                //用于处理升级websocket协议的调用、握手和开启时候的调度
                                .addLast(new HttpRequestHandler(websocketActionDispatch))
                                .addLast(new WebSocketFrameAggregator(Integer.MAX_VALUE))
                                // 自定义的通用事件处理器
                                .addLast(new GenericHandler(websocketActionDispatch))
                                .addLast(new WebSocketServerHandler(websocketActionDispatch));
                    }
                })
                // 连接超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,websocketProperties.getConnectTimeout())
                // TCP 连接的请求队列的最大长度
                .option(ChannelOption.SO_BACKLOG,websocketProperties.getBackLog())
                // 消息是否立即发送
                .option(ChannelOption.TCP_NODELAY,websocketProperties.isTcpNoDelay())
                // TCP 建立连接后，每隔一段时间就会对连接做一次探测
                .childOption(ChannelOption.SO_KEEPALIVE,Boolean.TRUE);

                ChannelFuture channelFuture = serverBootstrap.bind(websocketProperties.getPort()).sync();
                Channel serverChannle = channelFuture.channel();
                serverChannle.closeFuture().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        boss.shutdownGracefully();
                        work.shutdownGracefully();
                    }
                });

    }

}
