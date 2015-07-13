/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package cn.ltshark.web.account;

import javax.naming.Name;
import javax.validation.Valid;

import cn.ltshark.service.account.UserService;
import cn.ltshark.web.editor.NameEditor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import cn.ltshark.entity.User;
import cn.ltshark.service.account.ShiroDbRealm.ShiroUser;

/**
 * 用户修改自己资料的Controller.
 *
 * @author calvin
 */
@Controller
@RequestMapping(value = "/profile")
public class ProfileController {

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        NameEditor nameEditor = new NameEditor();
        binder.registerCustomEditor(Name.class, nameEditor);
    }
    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public String updateForm(Model model) {
        Name id = getCurrentUserId();
        model.addAttribute("user", userService.findUser(id));
        return "account/profile";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String update(@Valid @ModelAttribute("user") User user) {
        userService.updateUser(user);
        updateCurrentUserName(user.getName());
        return "redirect:/";
    }

    /**
     * 所有RequestMapping方法调用前的Model准备方法, 实现Struts2 Preparable二次部分绑定的效果,先根据form的id从数据库查出User对象,再把Form提交的内容绑定到该对象上。
     * 因为仅update()方法的form中有id属性，因此仅在update时实际执行.
     */
    @ModelAttribute
    public void getUser(@RequestParam(value = "id", defaultValue = "") String id, Model model) {
        if (StringUtils.isNotBlank(id)) {
            model.addAttribute("user", userService.findUser(id));
        }
    }

    /**
     * 取出Shiro中的当前用户Id.
     */
    private Name getCurrentUserId() {
        ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
        return user.id;
    }

    /**
     * 更新Shiro中当前用户的用户名.
     */
    private void updateCurrentUserName(String userName) {
        ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
        user.name = userName;
    }
}
