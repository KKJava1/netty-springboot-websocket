package com.example.nettyspringbootwebsocket.support;

import com.example.nettyspringbootwebsocket.WebsocketProperties;
import com.example.nettyspringbootwebsocket.annotations.WsServerEndpoint;
import com.example.nettyspringbootwebsocket.netty.WebsocketActionDispatch;
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



    private void registerServerEndpoints() {
        //获取WsServerEndpoint注解的类
        String[] beanNamesForAnnotation = beanFactory.getBeanNamesForAnnotation(WsServerEndpoint.class);
        WebsocketActionDispatch actionDispatch = new WebsocketActionDispatch();
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
}
