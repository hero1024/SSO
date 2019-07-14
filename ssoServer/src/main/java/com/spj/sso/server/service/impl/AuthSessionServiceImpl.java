package com.spj.sso.server.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.spj.sso.server.dao.UserMapper;
import com.spj.sso.server.entity.TokenSession;
import com.spj.sso.server.pojo.User;
import com.spj.sso.server.service.AuthSessionService;
import com.spj.sso.server.service.RedisOperatorService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthSessionServiceImpl implements AuthSessionService {
		
	@Autowired
	private RedisOperatorService redisOperatorService;

	@Autowired
	private UserMapper userMapper;

	@Override
	public int register(User user) {
		return this.userMapper.addUser(user);
	}

	@Override
	public User findByName(String param) {
		return this.userMapper.findByName(param);
	}

	@Override
	public boolean verify(String userName, String password) {
		// 根据数据库数据来校验
		User user = this.userMapper.findUser(userName, password);
		if(user!=null){
			return true;
		}
		return false;
	}

	@Override
	public String cacheSession(String userName) {
		//创建token
		String token = UUID.randomUUID().toString().toUpperCase();
		redisOperatorService.putUserInfo(token, userName);
		TokenSession tokenSession = new TokenSession(token,userName);
		redisOperatorService.putTokenInfo(userName, tokenSession);
		return token;
	}

	@Override
	public boolean checkAndAddAddress(String token, String address) {
		TokenSession tokenSession = redisOperatorService.getTokenInfo(token);
		if(tokenSession!=null) {
			tokenSession.getAddressList().add(address);
			tokenSession.setTokenFlag(true);
			redisOperatorService.putTokenInfo(token, tokenSession);
			return true;
		}
		return false;
	}

	@Override
	public boolean checkUserLoginStatus(String userName,String address) {
		boolean flag = false;
		String token = redisOperatorService.getUserInfo(userName);
		if(token!=null) {
			TokenSession tokenSession = redisOperatorService.getTokenInfo(token);
			if(tokenSession!=null) {
				if(tokenSession.getAddressList().contains(address)) {
					flag =  true;
				}
			}
		}
		return flag;
	}

	@Override
	public String getUserToken(String userName) {
		String token = redisOperatorService.getUserInfo(userName);
		if(token==null) {
			return null;
		}else {
			if(redisOperatorService.getTokenInfo(token)!=null) {
				return token;
			}else {
				return null;
			}
			
		}
	}

	@Override
	public List<String> logoutByUser(String userName) {
		String ssoToken = redisOperatorService.getUserInfo(userName);
		redisOperatorService.deleteUserInfo(userName);
		if(ssoToken!=null) {
			logoutByToken(ssoToken);
		}
		return null;
	}

	@Override
	public List<String> logoutByToken(String ssoToken) {
		if(ssoToken!=null) {
			TokenSession tokenSession = redisOperatorService.getTokenInfo(ssoToken);
			if(tokenSession!=null) {
				redisOperatorService.deleteTokenInfo(ssoToken);
				return tokenSession.getAddressList();
			}
		}
		return null;
	}

	@Override
	public String makeTicket(String username,String password){
		//设置日期格式
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String time = sdf.format(new Date());
		java.util.Random r = new java.util.Random();
		return DigestUtils.md5Hex(time+username+password+r.nextInt()).toUpperCase();
	}
}
