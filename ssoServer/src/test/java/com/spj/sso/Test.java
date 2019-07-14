package com.spj.sso;

/**
 * @author songpeijiang (songpeijiang@infosec.com.cn)
 * @org 北京信安世纪科技股份有限公司
 * @since 2019/5/9
 */
public class Test {
    public static void main(String[] args) {
        long a = System.currentTimeMillis()+10*60*1000;
        System.out.println(System.currentTimeMillis());
        System.out.println(System.currentTimeMillis()+10*60*1000);
        System.out.println(System.currentTimeMillis()-a);
    }
}
