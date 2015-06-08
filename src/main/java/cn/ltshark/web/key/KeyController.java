/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package cn.ltshark.web.key;

import cn.ltshark.entity.KeyTask;
import cn.ltshark.entity.User;
import cn.ltshark.service.account.AccountService;
import cn.ltshark.service.account.ShiroDbRealm.ShiroUser;
import cn.ltshark.service.key.KeyTaskService;
import com.google.common.collect.Maps;
import org.slf4j.LoggerFactory;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Task管理的Controller, 使用Restful风格的Urls:
 * <p/>
 * List page : GET /key/
 * Create page : GET /key/create
 * Create action : POST /key/create
 * Update page : GET /key/update/{id}
 * Update action : POST /key/update
 * Delete action : GET /key/delete/{id}
 *
 * @author calvin
 */
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
    private AccountService accountService;

    @RequestMapping(method = RequestMethod.GET)
    public String list() {
//		Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, "search_");
//		Long userId = getCurrentUserId();
//
//		Page<Task> tasks = taskService.getUserTask(userId, searchParams, pageNumber, pageSize, sortType);
//
//		model.addAttribute("tasks", tasks);
//		model.addAttribute("sortType", sortType);
//		model.addAttribute("sortTypes", sortTypes);
//		// 将搜索条件编码成字符串，用于排序，分页的URL
//		model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, "search_"));

        return "key/applyKey";
    }

    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String createForm(Model model, @RequestParam(value = "keyType", defaultValue = "1") String keyType) {
        model.addAttribute("keyType", keyType);
        model.addAttribute("action", "create");
        User user = accountService.getUser(getCurrentUserId());
        model.addAttribute("user", user);
        if ("1".equals(keyType))
            return "key/keyForm";
        else
            return "key/tempKeyForm";

    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String create(Model model, @RequestParam("keyType") String keyType) {
        KeyTask keyTask = new KeyTask();
        keyTask.setUser(new User(getCurrentUserId()));
        keyTask.setType(keyType);
        keyTask.setStatus(KeyTask.APPLYING_STATUS);
        keyTaskService.saveKeyTask(keyTask);
        logger.info(keyTask.toString());
//		newTask.setUser(user);
//		taskService.saveTask(newTask);
//        getCurrentUserId();
//		redirectAttributes.addFlashAttribute("message", "创建任务成功");
//        taskService.        model.addAttribute("action", "create");
        model.addAttribute("action", "done");
        return "key/done";
    }

    @RequestMapping(value = "done", method = RequestMethod.GET)
    public String done() {
        return "redirect:/key/";
    }

//    @RequestMapping(value = "update/{id}", method = RequestMethod.GET)
//    public String updateForm(@PathVariable("id") Long id, Model model) {
//        model.addAttribute("task", keyService.getTask(id));
//        model.addAttribute("action", "update");
//        return "task/taskForm";
//    }

//    @RequestMapping(value = "update", method = RequestMethod.POST)
//    public String update(@Valid @ModelAttribute("keyTask") KeyTask task, RedirectAttributes redirectAttributes) {
//        keyService.saveTask(task);
//        redirectAttributes.addFlashAttribute("message", "更新任务成功");
//        return "redirect:/task/";
//    }

    @RequestMapping(value = "delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        keyTaskService.deleteKeyTask(id);
        redirectAttributes.addFlashAttribute("message", "删除任务成功");
        return "redirect:/task/";
    }

    /**
     * 所有RequestMapping方法调用前的Model准备方法, 实现Struts2 Preparable二次部分绑定的效果,先根据form的id从数据库查出Task对象,再把Form提交的内容绑定到该对象上。
     * 因为仅update()方法的form中有id属性，因此仅在update时实际执行.
     */
    @ModelAttribute
    public void getKeyTask(@RequestParam(value = "id", defaultValue = "-1") Long id, Model model) {
        if (id != -1) {
            model.addAttribute("task", keyTaskService.getKeyTask(id));
        }
    }

    /**
     * 取出Shiro中的当前用户Id.
     */
    private Long getCurrentUserId() {
        ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
        return user.id;
    }
}
