package com.example.nettyspringbootwebsocket.exception;

/**
 * @author nzl
 * @date 2023/6/25
 */
public class WebsocketDeploymentException extends RuntimeException{

    public WebsocketDeploymentException(String message) {
        super(message);
    }
}
