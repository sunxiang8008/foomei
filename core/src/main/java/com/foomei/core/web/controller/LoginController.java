package com.foomei.core.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.foomei.core.service.BaseUserService;
import com.foomei.core.service.TokenService;

/**
 * LoginController负责打开登录页面(GET请求)和登录出错页面(POST请求)，

 * 真正登录的POST请求由Filter完成,
 * 
 * @author walker
 */
@Api(description = "管理员登录管理")
@Controller
public class LoginController {
	
	@Autowired
	private TokenService tokenService;
	@Autowired
	private BaseUserService baseUserService;
		
	@ApiOperation(value = "管理员登录页面", httpMethod = "GET")
	@RequestMapping(value = "/admin/login", method = RequestMethod.GET)
	public String login() {
		return "admin/login";
	}

	@ApiOperation(value = "管理员登录", httpMethod = "POST")
	@RequestMapping(value = "/admin/login", method = RequestMethod.POST)
	public String fail(@RequestParam(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM) String userName, Model model) {
		model.addAttribute(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM, userName);
		return "admin/login";
	}
	
	@RequestMapping(value = "/api", method = RequestMethod.GET)
	public String api() {
		return "api";
	}
	
}
