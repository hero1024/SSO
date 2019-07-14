package com.spj.sso.server.controller;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spj.sso.server.entity.TokenSession;
import com.spj.sso.server.pojo.ssoUser;
import com.spj.sso.server.pojo.User;
import com.spj.sso.server.service.AuthSessionService;
import com.spj.sso.server.service.RedisOperatorService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Controller
public class SsoServerController {
	
	@Autowired
    private RestTemplate restTemplate;
	
	@Autowired
	private AuthSessionService authSessionService;

	@Autowired
	private RedisOperatorService redisOperatorService;

	@RequestMapping("/index")
	public String firstCheck(HttpServletRequest request) {
		String originalUrl = request.getParameter("originalUrl");
		String ticket = request.getParameter("ticket");
		String token = null;
		boolean loginFlag = false;
		if(ticket!=null && ticket.trim().length()>0 && !"null".equals(ticket)) {
			//对用户先判断是否已经登陆过
			token = authSessionService.getUserToken(ticket);
			if(token!=null) {
				TokenSession tokenSession = redisOperatorService.getTokenInfo(token);
				if(tokenSession!=null) {
					if(System.currentTimeMillis()-tokenSession.getDeadline()<0){
						loginFlag = true;
					}
				}
			}
			}else {
			ssoUser ssoUser = (ssoUser) request.getSession().getAttribute("ssoUser");
			if (!"".equals(ssoUser) && ssoUser != null) {
				ticket = ssoUser.getTicket();
				if (ticket != null && ticket.trim().length() > 0) {
					token = authSessionService.getUserToken(ticket);
					if (token != null) {
						//对用户票据先判断是否已经过期
						TokenSession tokenSession = redisOperatorService.getTokenInfo(token);
						if (tokenSession != null) {
							//用户登录状态下票据自动延期
							if (System.currentTimeMillis() - tokenSession.getDeadline() > 0) {
								tokenSession.setDeadline(System.currentTimeMillis() + 10 * 60 * 1000);
								redisOperatorService.putTokenInfo(token, tokenSession);
							}
						}
						if (tokenTrans(request, originalUrl, ticket, token)) {
							if (originalUrl != null && !"".equals(originalUrl)) {
								//sendUser(ssoUser.getUserName(),originalUrl);
								if (originalUrl.contains("?")) {
									originalUrl = originalUrl + "&ticket=" + ticket;
								} else {
									originalUrl = originalUrl + "?ticket=" + ticket;
								}
							}
							return "redirect:" + originalUrl;
						}
					}
				}
			}
		}
		if(loginFlag) {
			//判断如果用户已经在SSO-Server认证过，直接发送token
			if(tokenTrans(request,originalUrl,ticket,token)) {
				if(originalUrl!=null&&!"".equals(originalUrl)) {
					if(originalUrl.contains("?")) {
						originalUrl = originalUrl + "&ticket="+ticket;
					}else {
						originalUrl = originalUrl + "?ticket="+ticket;
					}
				}else {
					return "index";
				}
			}
			return "redirect:"+originalUrl;
		}
			//需要替换成专业点的路径,自己登陆下了
			return "redirect:/loginPage?originalUrl="+request.getParameter("originalUrl");
	}
	
	//登陆界面，返回的是页面地址
	@RequestMapping("/loginPage")
	public String index(HttpServletRequest request) {
		if(request.getParameter("originalUrl")!=null) {
			request.setAttribute("originalUrl", request.getParameter("originalUrl"));
		}
		return "loginIndex";
	}

	private boolean tokenTrans(HttpServletRequest request, String originalUrl,String userName, String token) {
		TokenSession tokenSession = redisOperatorService.getTokenInfo(token);
		String[] paths = originalUrl.split("/");
		String shortAppServerUrl = paths[2];
		String returnUrl = "http://"+shortAppServerUrl+"/receiveToken?ssoToken="+token+"&userName="+userName+"&deadLine="+tokenSession.getDeadline();
		return "success".equals(restTemplate.getForObject(returnUrl, String.class));

	}

	//登陆逻辑,返回的是令牌
	@RequestMapping(value="/doLogin",method=RequestMethod.POST)
	public String login(HttpServletRequest request, HttpServletResponse response,
			String userName, String password, String originalUrl) {
		if(authSessionService.verify(userName,DigestUtils.md5Hex(password))) {
			String token = authSessionService.cacheSession(userName);
			ssoUser ssoUser = new ssoUser();
			ssoUser.setUserName(userName);
			ssoUser.setTicket(token);
			if(originalUrl==null||"".equals(originalUrl)) {
				request.setAttribute("ssoUser", userName);
				request.setAttribute("ticket", token);
				request.getSession().setAttribute("ssoUser",ssoUser);
				return "redirect:indexPage";
			}
				if (tokenTrans(request, originalUrl, token, userName)) {
					//跳转到提示成功的页面
					request.setAttribute("ssoUser", userName);
					request.getSession().setAttribute("ssoUser",ssoUser);
					if (originalUrl != null) {
						//sendUser(userName,originalUrl);
						if (originalUrl.contains("?")) {
							originalUrl = originalUrl + "&ticket=" + token;
						} else {
							originalUrl = originalUrl + "?ticket=" + token;
						}
						request.setAttribute("originalUrl", originalUrl);
				}
					return "hello";//TO-DO 三秒跳转
			}
		}
		//验证不通过，重新来吧
		if(originalUrl!=null) {
			request.setAttribute("originalUrl", originalUrl);
		}
		try {
			//转码
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.flush();
			out.println("<script>");
			out.println("alert('用户名密码错误，请重新输入！');");
			out.println("</script>");
		}catch(IOException e){
			e.printStackTrace();
		}
		return "loginIndex";
	}

	//校验token并注册地址
	@RequestMapping(value="/varifyToken",method=RequestMethod.GET)
	@ResponseBody
	public String varifyToken(String token, String address) {
		return String.valueOf(authSessionService.checkAndAddAddress(token, address));
	}
	
	@RequestMapping(value="/logoutByTicket",method=RequestMethod.GET)
	//@ResponseBody
	public String logoutByUser(String ticket) {
		String ssoToken = authSessionService.getUserToken(ticket);
		if(ssoToken!=null) {
			List<String> addressList = authSessionService.logoutByToken(ssoToken);
			if(addressList!=null) {
				addressList.stream().forEach(s -> sendLogout2Client(s,ssoToken));
				return "redirect:loginPage";
			}
			return  "noaddress";
		}
		return "Done";
	}
	
	@RequestMapping(value="/logoutByToken",method=RequestMethod.GET)
	@ResponseBody
	public String logoutByToken(String ssoToken) {
		List<String> addressList = authSessionService.logoutByToken(ssoToken);
		if(addressList!=null) {
			addressList.stream().forEach(s -> sendLogout2Client(s,ssoToken));
		}
		return "redirect:hello";
	}
	
	private void sendLogout2Client(String address,String ssoToken) {
		String returnUrl = "http://"+address+"/ssoDeleteToken?ssoToken="+ssoToken;
		try {
			restTemplate.getForObject(returnUrl, String.class);
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			request.getSession().removeAttribute("ssoUser");
		}catch(Exception e) {
			//Log and do nothing
		}
	}

	@RequestMapping("/")
	public String loginIndex(HttpServletRequest request) {
		return "loginIndex";
	}

	@RequestMapping("/indexPage")
	public String indexPage(HttpServletRequest request,HttpServletResponse response) {
		ssoUser ssoUser = (ssoUser) request.getSession().getAttribute("ssoUser");
		System.out.println(ssoUser);
		String ticket = ssoUser.getTicket();
		System.out.println(ticket);
		if (ticket != null && ticket.trim().length() > 0) {
			String token = authSessionService.getUserToken(ticket);
			if (null == token) {
				request.getSession().removeAttribute("ssoUser");
				request.getSession().invalidate();
			}
		}
		return "index";
	}

	@RequestMapping("/welcome")
	public String welcome(HttpServletRequest request) {
		return "hello";
	}

	@RequestMapping("/registerPage")
	public String registerPage(HttpServletRequest request) {
		return "register";
	}

	@RequestMapping("/register")
	public String register(HttpServletRequest request,HttpServletResponse response,String userName,String password){
		User user = new User();
		user.setUsername(userName);
		user.setPassword(DigestUtils.md5Hex(password));
		authSessionService.register(user);
		try {//转码
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.flush();
			out.println("<script>");
			out.println("alert('注册成功，请登录！');");
			out.println("</script>");
		}catch(IOException e){
			e.printStackTrace();
		}
		return "loginIndex";
	}

	/**
	 * 检查注册账号是否存在
	 *
	 * @param name
	 * 表单中的文本框的name属性
	 * @param param
	 * 表单中对应name属性的文本框的值
	 * @return
	 */
	@RequestMapping("/checkUser")
	@ResponseBody
	public Map<String,Object> checkUser(String name, String param, HttpServletRequest request){
		Map<String,Object> map=new HashMap<String,Object>();
		User user=authSessionService.findByName(param);
		if(user!=null){
			map.put("status", "n");
			map.put("info","账号已经存在！" );
		}else{
			map.put("status", "y");
			map.put("info", " ");

		}
		return  map;
	}

}
