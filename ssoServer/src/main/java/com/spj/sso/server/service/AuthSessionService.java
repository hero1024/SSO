package com.spj.sso.server.service;

import com.spj.sso.server.pojo.User;

import java.util.List;

public interface AuthSessionService {
	
	/**
	 * 验证用户名密码
	 * @param userName
	 * @param password
	 * @return
	 */
	boolean verify(String userName, String password);

	int register(User user);

	User findByName(String param);

	//创建全局会话，返回令牌
	String cacheSession(String userName);
	
	//判断令牌是否有效，如有效则添加地址
	boolean checkAndAddAddress(String token, String address);
	
	//验证用户是否登陆
	boolean checkUserLoginStatus(String userName, String address);
	
	//获取用户令牌信息，获取不到说明用户没有单点登录过
	String getUserToken(String userName); 
	
	//根据用户名销毁令牌，并返回需要通知的地址列表
	List<String> logoutByUser(String userName);
	
	//根据用户名销毁令牌，并返回需要通知的地址列表
	List<String> logoutByToken(String ssoToken);

	//创建票据
	String makeTicket(String username,String password);

}
