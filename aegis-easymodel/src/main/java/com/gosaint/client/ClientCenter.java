package com.gosaint.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Authgor: gosaint
 * @Description:
 * @Date Created in 22:00 2018/10/3
 * @Modified By:
 */
public class ClientCenter{
    @SuppressWarnings(value = "all")
    public static <T> T getRemoteProxyObj(final Class<?> serviceInterface, final InetSocketAddress addr) {
        //ClassLoader loader, Class<?>[] interfaces, InvocationHandler h
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                                          new Class<?>[] { serviceInterface },
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args)
                            throws Throwable {
                        Socket socket=null;
                        ObjectOutputStream oos = null;
                        ObjectInputStream ois = null;
                        try {
                            // 2.创建Socket客户端，根据指定地址连接远程服务提供者
                            socket = new Socket();
                            socket.connect(addr);

                            // 3.将远程服务调用所需的接口类、方法名、参数列表等编码后发送给服务提供者
                            oos = new ObjectOutputStream(socket.getOutputStream());
                            oos.writeUTF(serviceInterface.getName());
                            oos.writeUTF(method.getName());
                            oos.writeObject(method.getParameterTypes());
                            oos.writeObject(args);

                            // 4.同步阻塞等待服务器返回应答，获取应答后返回
                            ois = new ObjectInputStream(socket.getInputStream());
                            return ois.readObject();
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            if (socket != null) socket.close();
                            if (oos != null) oos.close();
                            if (ois != null) ois.close();
                        }
                        return null;
                    }
                });
    }
}
