package com.example.nettyspringbootwebsocket.netty;

import com.example.nettyspringbootwebsocket.WebsocketProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
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


    public void start(){
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
                                .addLast(new HttpRequestHandler(websocketActionDispatch));
                    }
                });


    }

}
