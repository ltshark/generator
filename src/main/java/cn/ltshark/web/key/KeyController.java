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
import cn.ltshark.web.task.TaskController;
import com.google.common.collect.Maps;
import org.slf4j.LoggerFactory;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springside.modules.web.Servlets;

import javax.servlet.ServletRequest;
import java.util.Date;
import java.util.List;
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
    public String list(ServletRequest request, Model model) {
        Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, "search_");
        searchParams.put("EQ_user.id", String.valueOf(getCurrentUserId()));
        List<KeyTask> keyTasks = keyTaskService.getUserKeyTasks(searchParams);
        boolean canDownload = false;
        for (KeyTask task : keyTasks) {
            if (KeyTask.AGREE_APPLY_STATUS.equals(task.getStatus())) {
                canDownload = true;
                break;
            }
        }
        model.addAttribute("tasks", keyTasks);
        model.addAttribute("canDownload", canDownload);

        // 将搜索条件编码成字符串，用于排序，分页的URL
//		model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, "search_"));

        return "key/applyKey";
    }

    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String createForm(Model model, @RequestParam(value = "keyType", defaultValue = "1") String keyType) {
        model.addAttribute("keyType", keyType);
        model.addAttribute("action", "create");
        User user = accountService.getUser(getCurrentUserId());
        model.addAttribute("user", user);
        if ("1".equals(keyType) || "2".equals(keyType))
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
        keyTask.setApplyDate(new Date());
        keyTaskService.saveKeyTask(keyTask);
        logger.info(keyTask.toString());
        model.addAttribute("action", "done");
        return "key/done";
    }

    @RequestMapping(value = "listTask", method = RequestMethod.GET)
    public String listTask(@RequestParam(value = "page", defaultValue = "1") int pageNumber,
                           @RequestParam(value = "page.size", defaultValue = TaskController.PAGE_SIZE) int pageSize,
                           @RequestParam(value = "sortType", defaultValue = "auto") String sortType,
                           @RequestParam(value = "taskStatus", defaultValue = KeyTask.APPLYING_STATUS) String taskStatus,
                           Model model,
                           ServletRequest request) {
        Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, "search_");
        searchParams.put("EQ_status", taskStatus);
        Page<KeyTask> keyTasks = keyTaskService.getKeyTask(searchParams, pageNumber, pageSize, sortType);
        model.addAttribute("tasks", keyTasks);
        model.addAttribute("sortType", sortType);
        model.addAttribute("sortTypes", sortTypes);
        model.addAttribute("taskStatus", taskStatus);
        // 将搜索条件编码成字符串，用于排序，分页的URL
        model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, "search_"));

        return "key/listTask";
    }

    @RequestMapping(value = "done", method = RequestMethod.GET)
    public String done() {
        return "redirect:/key/";
    }

    @RequestMapping(value = "approval/{id}", method = RequestMethod.GET)
    public String agree(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return handleTask(id, redirectAttributes, KeyTask.AGREE_APPLY_STATUS);
    }

    @RequestMapping(value = "batchHandle", method = RequestMethod.POST)
//    public String batchHandle(RedirectAttributes redirectAttributes) {
    public String batchHandle(@RequestParam("taskId") List<Long> taskIds, @RequestParam("actionType") String actionType, RedirectAttributes redirectAttributes) {
        keyTaskService.batchHandle(taskIds, actionType);
        redirectAttributes.addFlashAttribute("message", "审批任务完成");
        return "redirect:/key/listTask?taskStatus=1";
    }

    private String handleTask(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, String agreeApplyStatus) {
        KeyTask keyTask = keyTaskService.getKeyTask(id);
        keyTask.setStatus(agreeApplyStatus);
        keyTask.setApprovalDate(new Date());
        keyTaskService.saveKeyTask(keyTask);
        redirectAttributes.addFlashAttribute("message", "审批任务完成");
        return "redirect:/key/listTask?taskStatus=1";
    }

    @RequestMapping(value = "refuse/{id}", method = RequestMethod.GET)
    public String refuse(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return handleTask(id, redirectAttributes, KeyTask.REFUSE_APPLY_STATUS);
    }

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
