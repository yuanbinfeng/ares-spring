package com.goodobj.test;

import com.goodobj.service.UserService;
import com.goodobj.spring.AresApplicationContext;
import com.goodobj.spring.conf.AppConfig;

/**
 * @author yuanlei-003
 */
public class Test {
    public static void main(String[] args) {
        AresApplicationContext context = new AresApplicationContext(AppConfig.class);
        System.out.println(context.getBean("userController"));
        System.out.println(context.getBean("userServiceImpl"));
        System.out.println(context.getBean("userServiceImpl"));
        System.out.println(context.getBean("userServiceImpl"));
    }
}
