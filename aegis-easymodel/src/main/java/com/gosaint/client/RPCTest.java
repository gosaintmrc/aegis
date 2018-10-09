package com.gosaint.client;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.gosaint.server.Server;
import com.gosaint.server.ServiceCenter;
import com.gosaint.service.HelloService;
import com.gosaint.service.impl.HelloServiceImpl;

public class RPCTest {
 
    public static void main(String[] args) throws IOException {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Server serviceServer = new ServiceCenter("localhost",8088);
                    serviceServer.register(HelloService.class, HelloServiceImpl.class);
                    serviceServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        HelloService service = ClientCenter.getRemoteProxyObj(HelloService.class, new InetSocketAddress("localhost", 8088));
        System.out.println(service.sayHi("test"));
    }
}