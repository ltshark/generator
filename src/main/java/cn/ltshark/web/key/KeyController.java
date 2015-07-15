/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package cn.ltshark.web.key;

import cn.ltshark.entity.KeyTask;
import cn.ltshark.entity.User;
import cn.ltshark.service.account.ShiroDbRealm.ShiroUser;
import cn.ltshark.service.account.UserService;
import cn.ltshark.service.key.KeyTaskService;
import com.google.common.collect.Maps;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping(value = "/key")
public class KeyController {

    private Logger logger = LoggerFactory.getLogger(KeyController.class);

    private static final String PAGE_SIZE = "3";

    private static Map<String, String> sortTypes = Maps.newLinkedHashMap();

    static {
        sortTypes.put("auto", "自动");
        sortTypes.put("title", "标题");
    }

    @Autowired
    private KeyTaskService keyTaskService;
    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model) {
        KeyTask keyTask = keyTaskService.getUserKeyTask(getCurrentUserLoginName());
        boolean canDownload = false;
        if (keyTask != null && KeyTask.AGREE_APPLY_STATUS.equals(keyTask.getStatus())) {
            canDownload = true;
        }
        model.addAttribute("task", keyTask);
        model.addAttribute("canDownload", canDownload);
        model.addAttribute("user", userService.findUserByLoginName(getCurrentUserLoginName()));

        // 将搜索条件编码成字符串，用于排序，分页的URL
//		model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, "search_"));

        return "key/applyKey";
    }

    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String createForm(Model model, @RequestParam(value = "keyType", defaultValue = "1") String keyType) {
        model.addAttribute("keyType", keyType);
        model.addAttribute("action", "create");
        User user = userService.findUser(getCurrentUserLoginName());
        model.addAttribute("user", user);
        if ("1".equals(keyType) || "2".equals(keyType))
            return "key/keyForm";
        else
            return "key/tempKeyForm";

    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String create(Model model, @RequestParam("keyType") String keyType) {
        KeyTask keyTask = new KeyTask();
        keyTask.setUserLoginName(getCurrentUserLoginName());
        keyTask.setType(keyType);
        keyTask.setStatus(KeyTask.APPLYING_STATUS);
        keyTask.setApplyDate(new Date());
        keyTaskService.saveKeyTask(keyTask);
        logger.info(keyTask.toString());
        model.addAttribute("action", "done");
        return "key/done";
    }

    @RequestMapping(value = "done", method = RequestMethod.GET)
    public String done() {
        return "redirect:/key/";
    }

    /**
     * 取出Shiro中的当前用户Id.
     */
    private String getCurrentUserLoginName() {
        ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
        return user.loginName;
    }
}
