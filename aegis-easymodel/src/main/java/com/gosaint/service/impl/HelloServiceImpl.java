package com.gosaint.service.impl;

import com.gosaint.service.HelloService;

/**
 * 服务的实现类
 */
public class HelloServiceImpl implements HelloService {
 
    public String sayHi(String name) {
        return "Hi, " + name;
    }
 
}