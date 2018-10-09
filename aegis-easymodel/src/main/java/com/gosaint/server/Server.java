package com.gosaint.server;

import java.io.IOException;

public interface Server {
	
	/**
	 * 关闭服务
	 */
	 void stop();
	/**
	 * 开启服务
	 * @throws IOException
	 */

	 void start() throws IOException;
	
	/**
	 * 注册服务
	 * @param serviceInterface 服务注册接口
	 * @param impl	服务实现类
	 */
	 void register(Class<?> serviceInterface, Class<?> impl);
	/**
	 * 判断服务是否运行
	 * @return
	 */
	 boolean isRunning();
	/**
	 * 获取监听端口
	 * @return
	 */
	 int getPort();
}
