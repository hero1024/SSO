# SSO毕设项目说明
校园网统一身份认证系统总共创建了4个项目，核心的是ssoServer和ssoClient。
ssoServer：身份认证服务中心，校园网统一身份认证系统门户。
ssoClient：身份认证服务客户端，它是可以单独打包的代码插件而不是独立的web应用。
SSOapp1：安装了ssoClient插件的独立web应用，用来模拟图书馆系统。
SSOapp2：安装了ssoClient插件的独立web应用，用来模拟物理实验教学系统。
启动调试：
ssoServer，独立部署，端口使用8080，服务地址：https://localhost:8080/loginPage
SSOapp1，植入ssoClient插件包，端口使用8081，服务地址：http://localhost:8081/hello
SSOapp2，植入ssoClient插件包，端口使用8082，服务地址：http://localhost:8082/hello
