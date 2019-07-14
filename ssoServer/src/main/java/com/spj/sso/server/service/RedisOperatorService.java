package com.spj.sso.server.service;


import com.spj.sso.server.entity.TokenSession;

public interface RedisOperatorService {
	
	void putUserInfo(String userName, String token);
	
	void putTokenInfo(String tokenKey, TokenSession tokenSession);
	
	String getUserInfo(String userName);
	
	TokenSession getTokenInfo(String tokenKey);
	
	void deleteUserInfo(String userName);
	
	void deleteTokenInfo(String tokenKey);

}
