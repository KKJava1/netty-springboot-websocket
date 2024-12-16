package com.example.nettyspringbootwebsocket.support;

import com.example.nettyspringbootwebsocket.WebsocketProperties;
import com.example.nettyspringbootwebsocket.annotations.WsServerEndpoint;
import com.example.nettyspringbootwebsocket.netty.NettyWebsocketServer;
import com.example.nettyspringbootwebsocket.netty.WebsocketActionDispatch;
import lombok.SneakyThrows;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

/**
 * @author KangJunJie
 * @date 2024/12/2
 */
public class WebSocketAnnotationPostProcessor implements SmartInitializingSingleton {


    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private WebsocketProperties websocketProperties;

    //实现Spring的后置处理器，在Spring容器加载完后执行的方法
    @Override
    public void afterSingletonsInstantiated() {
        //1.获取带有SpringBoot注解的类
        String[] beanNamesForAnnotation = beanFactory.getBeanNamesForAnnotation(SpringBootApplication.class);
        String applicationStartBean = beanNamesForAnnotation[0];
        //2.获取主启动类 Bean
        Object bean = beanFactory.getBean(applicationStartBean);
        //3.获取主启动类所在的包路径
        String basePackage = ClassUtils.getPackageName(bean.getClass());
        scanWebsocketServiceBeans(basePackage, beanFactory);

        registerServerEndpoints();
    }



    @SneakyThrows
    private void registerServerEndpoints() {
        /**
         * 开发者只需要标注注解即可，无需手动配置端点路径和方法，简化了开发流程。
         * 动态发现的机制也方便后续扩展，如果新增了端点，只需添加新的类，不需要修改框架代码。
         */
        String[] beanNamesForAnnotation = beanFactory.getBeanNamesForAnnotation(WsServerEndpoint.class);

        WebsocketActionDispatch actionDispatch = new WebsocketActionDispatch();
        for (String beanName : beanNamesForAnnotation) {
            // 获取 bean 的类型
            Class<?> beanType = beanFactory.getType(beanName);
            // 获取目标类（处理代理类的情况）
            Class<?> targetClass = getTargetClass(beanType);
            //提取 @WsServerEndpoint 注解信息
            WsServerEndpoint wsServerEndpoint = targetClass.getAnnotation(WsServerEndpoint.class);
            /**
             * 创建 WebsocketServerEndpoint 对象，包含服务端点的：
             * 目标类（pojoClazz）：targetClass
             * 类实例（object）：从 Spring 容器中获取
             * 路径（path）：wsServerEndpoint.value()从注解中提取的路径
             */
            WebsocketServerEndpoint websocketServerEndpoint = new WebsocketServerEndpoint(targetClass
                    ,beanFactory.getBean(targetClass),wsServerEndpoint.value());
            //将每个端点及其方法绑定到 WebsocketActionDispatch 分发器中。
            actionDispatch.addWebsocketServerEndpoint(websocketServerEndpoint);
        }
        //传入事件分发器actionDispatch和websocketProperties默认配置
        NettyWebsocketServer websocketServer = new NettyWebsocketServer(actionDispatch,websocketProperties);
        // 启动websocket
        websocketServer.start();
    }


    /**
     *  自动发现并注册所有标注了 @WsServerEndpoint 的类，使它们能够作为 WebSocket 服务端点被管理和使用。
     * @param packagesToScan 扫描包路径
     * @param registry
     */
    private void scanWebsocketServiceBeans(String packagesToScan, DefaultListableBeanFactory registry) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        // 扫描 @WsServerEndpoint标注的类
        scanner.addIncludeFilter(new AnnotationTypeFilter(WsServerEndpoint.class));
        scanner.scan(packagesToScan);
    }

    /**
     * 获取类型的目标类型
     * @param clazz
     * @return
     */
    public Class<?> getTargetClass(Class<?> clazz) {
        if (AopUtils.isCglibProxy(clazz)) {
            return clazz.getSuperclass();
        }
        return clazz;
    }
}
