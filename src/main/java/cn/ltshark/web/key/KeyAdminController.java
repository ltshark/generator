/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package cn.ltshark.web.key;

import cn.ltshark.entity.KeyTask;
import cn.ltshark.entity.User;
import cn.ltshark.service.account.UserService;
import cn.ltshark.service.key.KeyTaskService;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Controller
@RequestMapping(value = "/admin/key")
public class KeyAdminController {

    private Logger logger = LoggerFactory.getLogger(KeyAdminController.class);

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

    @RequestMapping(value = "listKeyTask", method = RequestMethod.GET)
    public String listKeyTask(@RequestParam(value = "page", defaultValue = "1") int pageNumber,
                              @RequestParam(value = "page.size", defaultValue = PAGE_SIZE) int pageSize,
                              @RequestParam(value = "sortType", defaultValue = "auto") String sortType,
                              @RequestParam(value = "taskStatus", defaultValue = KeyTask.APPLYING_STATUS) String taskStatus,
                              Model model,
                              ServletRequest request) {
//        Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, "search_");
//        searchParams.put("EQ_status", taskStatus);
        Page<KeyTask> keyTasks = keyTaskService.getKeyTask(pageNumber, pageSize);
        model.addAttribute("tasks", keyTasks);
//        model.addAttribute("sortType", sortType);
//        model.addAttribute("sortTypes", sortTypes);
        model.addAttribute("taskStatus", taskStatus);
        // 将搜索条件编码成字符串，用于排序，分页的URL
//        model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, "search_"));

        return "key/listKeyTask";
    }

    @RequestMapping(value = "approval/{id}", method = RequestMethod.GET)
    public String agree(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        return handleTask(id, redirectAttributes, KeyTask.AGREE_APPLY_STATUS);
    }

    @RequestMapping(value = "batchHandle", method = RequestMethod.POST)
    public String batchHandle(@RequestParam("taskId") List<Long> taskIds, @RequestParam("actionType") String actionType, RedirectAttributes redirectAttributes) {
        keyTaskService.batchHandle(taskIds, actionType);
        redirectAttributes.addFlashAttribute("message", "审批任务完成");
        return "redirect:/admin/key/listKeyTask?taskStatus=1";
    }

    @RequestMapping(value = "listUserKeyTask", method = RequestMethod.GET)
    public String listUserKeyTask(@RequestParam(value = "page", defaultValue = "1") int pageNumber,
                                  @RequestParam(value = "page.size", defaultValue = PAGE_SIZE) int pageSize,
                                  @RequestParam(value = "sortType", defaultValue = "auto") String sortType, Model model,
                                  ServletRequest request) {
        Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, "search_");
        User currentUser = userService.getCurrentUser();
        if (userService.isDepartmentAdmin(currentUser)) {
            searchParams.put("EQ_department.id", currentUser.getDepartment());
        }
//        Page<User> users = userService.findAllMembers(searchParams, pageNumber, pageSize, sortType);
//        model.addAttribute("users", users);
        model.addAttribute("sortType", sortType);
        model.addAttribute("sortTypes", sortTypes);
        return "key/listUserKeyTask";
    }

    @RequestMapping(value = "batchApply", method = RequestMethod.POST)
    public String batchApply(@RequestParam("userId") List<String> userIds, @RequestParam("keyType") String keyType, RedirectAttributes redirectAttributes) {
        keyTaskService.batchApply(userIds, keyType);
        redirectAttributes.addFlashAttribute("message", "申请证书完成");
        return "redirect:/admin/key/listUserKeyTask";
    }

    private String handleTask(@PathVariable("id") String id, RedirectAttributes redirectAttributes, String agreeApplyStatus) {
        KeyTask keyTask = keyTaskService.getUserKeyTask(id);
        keyTask.setStatus(agreeApplyStatus);
        keyTask.setApprovalDate(new Date());
        keyTaskService.saveKeyTask(keyTask);
        redirectAttributes.addFlashAttribute("message", "审批任务完成");
        return "redirect:/admin/key/listKeyTask?taskStatus=1";
    }

    @RequestMapping(value = "refuse/{id}", method = RequestMethod.GET)
    public String refuse(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        return handleTask(id, redirectAttributes, KeyTask.REFUSE_APPLY_STATUS);
    }

//    @RequestMapping(value = "update", method = RequestMethod.POST)
//    public String update(@Valid @ModelAttribute("keyTask") KeyTask task, RedirectAttributes redirectAttributes) {
//        keyService.saveTask(task);
//        redirectAttributes.addFlashAttribute("message", "更新任务成功");
//        return "redirect:/task/";
//    }

    @RequestMapping(value = "deleteAgree/{id}")
    public String deleteAgree(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        keyTaskService.deleteKeyTask(id);
        redirectAttributes.addFlashAttribute("message", "删除申请成功");
        return "redirect:/admin/key/listKeyTask?taskStatus=2";
    }

    @RequestMapping(value = "deleteRefuse/{id}")
    public String deleteRefuse(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        keyTaskService.deleteKeyTask(id);
        redirectAttributes.addFlashAttribute("message", "删除申请成功");
        return "redirect:/admin/key/listKeyTask?taskStatus=3";
    }

    /**
     * 所有RequestMapping方法调用前的Model准备方法, 实现Struts2 Preparable二次部分绑定的效果,先根据form的id从数据库查出Task对象,再把Form提交的内容绑定到该对象上。
     * 因为仅update()方法的form中有id属性，因此仅在update时实际执行.
     */
    @ModelAttribute
    public void getKeyTask(@RequestParam(value = "id", defaultValue = "-1") String id, Model model) {
        if (StringUtils.isNotBlank(id)) {
            model.addAttribute("task", keyTaskService.getUserKeyTask(id));
        }
    }

}
