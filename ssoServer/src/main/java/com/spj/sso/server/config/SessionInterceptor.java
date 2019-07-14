package com.spj.sso.server.config;

import com.spj.sso.server.pojo.ssoUser;
import com.spj.sso.server.service.AuthSessionService;
import com.spj.sso.server.service.impl.AuthSessionServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionInterceptor extends HandlerInterceptorAdapter {

    private static String[] IGNORE_URI = {"/index", "/loginPage","/doLogin","/varifyToken", "/welcome","/registerPage","/register","/checkUser","/logoutByToken"};
    private static Logger log = LoggerFactory.getLogger(SessionInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        boolean flag = false;
        String url = request.getRequestURL().toString();

        for (String s : IGNORE_URI) {

            if (url.contains(s)) {
                flag = true;
                break;
            }
        }

        if (!flag) {
            Object obj = request.getSession().getAttribute("ssoUser");
            if (null == obj) {
                //未登录
                String servletPath = request.getServletPath();
                log.error("session失效，当前url：" + url+";module Path:"+servletPath);

                if (request.getHeader("x-requested-with") != null &&
                        request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")){
                    //在响应头设置session状态
                    response.setHeader("sessionstatus", "timeout");
                    response.setCharacterEncoding("utf-8");
                    response.setContentType("text/html;charset=utf-8");
                    response.getWriter().print("error");
                } else {
                    response.sendRedirect(request.getContextPath()+"/loginPage");
                }

                return false;

            } else {
            	/*ssoUser user = (ssoUser)obj;
                String ticket = user.getTicket();
                if (ticket != null && ticket.trim().length() > 0) {
                    AuthSessionService authSessionService = new AuthSessionServiceImpl();
                    String token = authSessionService.getUserToken(ticket);
                    if (null == token) {
                        request.getSession().removeAttribute("ssoUser");
                        request.getSession().invalidate();
                        response.sendRedirect(request.getContextPath()+"/loginPage");
                        return false;
                    }
                }
*/
            }
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

}
