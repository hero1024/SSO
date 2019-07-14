package com.spj.sso.client.plugin.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.spj.sso.client.plugin.service.UserAccessService;

import java.io.IOException;
import java.io.PrintWriter;

@Controller
public class SsoClientController {
	
	@Autowired
    private RestTemplate restTemplate;
	
	@Value("${sso.server.url}")
	String ssoServerPath;
	
	@Autowired
	private UserAccessService userAccessService;

	@RequestMapping("/receiveToken")
	@ResponseBody
	public String receiveToken(HttpServletRequest request, String ssoToken,String userName,String deadLine) {
		if(ssoToken!=null && ssoToken.toString().trim().length()>0) {
			String realUrl = request.getRequestURL().toString();
			String[] paths = realUrl.split("/");
			String realUrlUrls = paths[2];
			String returnUrl = ssoServerPath+"/varifyToken?address="+realUrlUrls+"&token="+ssoToken;
			String resultStr =  restTemplate.getForObject(returnUrl, String.class);
			if("true".equals(resultStr)) {
				//创建局部会话，保存用户状态为已登陆
				userAccessService.putUserStatus(userName, ssoToken, deadLine);
				return "success";
			}
		}
		return "error";
	}
	
	@RequestMapping("/ssoLogout")
	//@ResponseBody
	public String ssoLogout(String ticket) {
		String userToken = userAccessService.getUserToken(ticket);
		if(userToken!=null) {
			String returnUrl = ssoServerPath+"/logoutByToken?ssoToken="+userToken;
			return restTemplate.getForObject(returnUrl, String.class);
		}
		return "None Token";
	}
	
	@RequestMapping("/ssoDeleteToken")
	@ResponseBody
	public String ssoDeleteToken(String ssoToken) {
		userAccessService.deleteToken(ssoToken);
		ssoLogout("ssoUser");
		return "success";
	}

	/*@RequestMapping("/receiveUser")
	public void receiveUserinfo(HttpServletRequest request,
								HttpServletResponse response) {
		String username = request.getParameter("userinfo");
		userAccessService.putUserStatus("ssoUser",username);
	}

	@RequestMapping("/getloginUser")
	public void getloginUser(HttpServletResponse response) throws IOException {
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		String username = userAccessService.getUserToken("ssoUser");
		out.write(username);
		out.flush();
		out.close();
	}*/

}
