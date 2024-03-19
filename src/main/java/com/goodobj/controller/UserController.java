package com.goodobj.controller;

import com.goodobj.service.UserService;
import com.goodobj.spring.anno.Autowired;
import com.goodobj.spring.anno.Component;

/**
 * @author yuanlei-003
 */
@Component
public class UserController {

    @Autowired
    private UserService userService;
}
