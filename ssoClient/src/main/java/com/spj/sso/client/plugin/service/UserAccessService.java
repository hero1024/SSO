package com.spj.sso.client.plugin.service;

public interface UserAccessService {
	
	String getUserToken(String user);
	
	void putUserStatus(String user, String flag,String deadLine);
	
	void deleteToken(String user);

	Boolean getTokenFlag(String user);
}
