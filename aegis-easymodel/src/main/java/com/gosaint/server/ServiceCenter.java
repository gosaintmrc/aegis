package com.gosaint.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Authgor: gosaint
 * @Description:
 * @Date Created in 21:07 2018/10/3
 * @Modified By:服务中心，主要是服务的远程调用的实现
 */
public class ServiceCenter implements Server {

    private static ServerSocket socket;//服务套接字
    private static SocketAddress endpoint;//远程服务地址
    private String hostname;//远程服务的IP地址
    private int port;//远程服务的端口
    //注册中心注册的服务
    private static final Map<String,Class> serviceRegistry=new HashMap<>();
    private static boolean isRunning = false;

    public ServiceCenter(String hostname,int port) {
        this.hostname=hostname;
        this.port=port;
    }

    /**
     * 1 创建固定数量的线程池
     * 2 Runtime.getRuntime().availableProcessors()作用是获取当前系统中可用CPU的数目
     */
    private static ExecutorService service = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void stop() {
        isRunning=false;
        service.shutdown();
    }

    public void start() throws IOException {
        socket=new ServerSocket();//创建服务套接字
        endpoint=new InetSocketAddress(hostname, port);
        socket.bind(endpoint);
        System.out.println("服务开启====================");
        try {
            while(true){
                service.execute(new ServiceTask(socket.accept()));
            }
        }finally {
            socket.close();
        }
    }

    public void register(final Class<?> serviceInterface, final Class<?> impl) {
        serviceRegistry.put(serviceInterface.getName(),impl);
        System.out.println("服务已经注册,名称为 "+serviceInterface.getSimpleName());
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

    /**
     * 静态内部类
     */
    private static class ServiceTask implements Runnable {
        private Socket client;


        public ServiceTask(final Socket client) {
            this.client=client;
        }

        public void run() {
            //对象输入流和对象输出流
            ObjectInputStream ois=null;
            ObjectOutputStream oos = null;
            try {
                //将客户端发送的码流反序列化成对象，反射调用服务实现者，获取执行结果
                ois=new ObjectInputStream(client.getInputStream());
                //获取服务名称和方法名称
                String serviceName = ois.readUTF();
                String methodName=ois.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) ois.readObject();
                Object[] args=(Object[]) ois.readObject();
                Class<?> serviceClass =serviceRegistry.get(serviceName);
                if(serviceClass==null)
                    throw new ClassNotFoundException(serviceName + " not found");
                //反射获取method
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceClass.newInstance(), args);

                // 将执行结果反序列化，通过socket发送给客户端
                oos = new ObjectOutputStream(client.getOutputStream());
                oos.writeObject(result);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(oos!=null){
                    try {
                        oos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        if(ois!=null){
                            try {
                                ois.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }finally {
                                if(client!=null){
                                    try {
                                        client.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
