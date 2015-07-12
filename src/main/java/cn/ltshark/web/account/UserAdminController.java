///*******************************************************************************
// * Copyright (c) 2005, 2014 springside.github.io
// * <p/>
// * Licensed under the Apache License, Version 2.0 (the "License");
// *******************************************************************************/
//package cn.ltshark.web.account;
//
//import cn.ltshark.entity.User;
//import cn.ltshark.service.account.AccountService;
//import cn.ltshark.web.task.TaskController;
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.Maps;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import org.springside.modules.web.Servlets;
//
//import javax.servlet.ServletRequest;
//import javax.validation.Valid;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
///**
// * 管理员管理用户的Controller.
// *
// * @author calvin
// */
//@Controller
//@RequestMapping(value = "/admin/user")
//public class UserAdminController {
//
//    private static Map<String, String> sortTypes = Maps.newLinkedHashMap();
//
//    static {
//        sortTypes.put("auto", "自动");
//        sortTypes.put("name", "用户名");
//    }
//
//    @Autowired
//    private AccountService accountService;
//    @Autowired
//    private DepartmentService departmentService;
//
//    @RequestMapping(method = RequestMethod.GET)
//    public String list(@RequestParam(value = "page", defaultValue = "1") int pageNumber,
//                       @RequestParam(value = "page.size", defaultValue = TaskController.PAGE_SIZE) int pageSize,
//                       @RequestParam(value = "sortType", defaultValue = "auto") String sortType, Model model,
//                       ServletRequest request) {
//        Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, "search_");
//        User currentUser = accountService.getCurrentUser();
//        if(accountService.isDepartmentAdmin(currentUser)){
//            searchParams.put("EQ_department.id",currentUser.getDepartment().getId().toString());
//        }
//
//        Page<User> users = accountService.getUsers(searchParams, pageNumber, pageSize, sortType);
//
//        model.addAttribute("users", users);
//        model.addAttribute("sortType", sortType);
//        model.addAttribute("sortTypes", sortTypes);
//        // 将搜索条件编码成字符串，用于排序，分页的URL
//        model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, "search_"));
//
//        return "account/adminUserList";
//    }
//
//    @RequestMapping(value = "update/{id}", method = RequestMethod.GET)
//    public String updateForm(@PathVariable("id") Long id, Model model) {
//        model.addAttribute("user", accountService.getUser(id));
//        model.addAttribute("action", "update");
//        model.addAttribute("departments", getAllDepartment());
//        return "account/adminUserForm";
//    }
//
//    private List<Department> getAllDepartment() {
//        User currentUser = accountService.getCurrentUser();
//        if (accountService.isDepartmentAdmin(currentUser)) {
//            return ImmutableList.of(departmentService.getDepartment(currentUser.getDepartment().getId()));
//        } else {
//            return departmentService.getAllDepartment();
//        }
//    }
//
//    @RequestMapping(value = "update", method = RequestMethod.POST)
//    public String update(@Valid @ModelAttribute("user") User user, @RequestParam(value = "department_id", defaultValue = "-1") Long department, RedirectAttributes redirectAttributes) {
//        if (department != -1) {
//            Department department = new Department();
//            department.setId(department);
//            user.setDepartment(department);
//        }
//        accountService.updateUser(user);
//        redirectAttributes.addFlashAttribute("message", "更新用户" + user.getLoginName() + "成功");
//        return "redirect:/admin/user";
//    }
//
//    @RequestMapping(value = "delete/{id}")
//    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
//        User user = accountService.getUser(id);
//        accountService.deleteUser(id);
//        redirectAttributes.addFlashAttribute("message", "删除用户" + user.getLoginName() + "成功");
//        return "redirect:/admin/user";
//    }
//
//    @RequestMapping(value = "create", method = RequestMethod.GET)
//    public String createForm(Model model) {
//        model.addAttribute("user", new User());
//        model.addAttribute("action", "create");
//        model.addAttribute("departments", getAllDepartment());
//        return "account/adminUserForm";
//    }
//
//    @RequestMapping(value = "create", method = RequestMethod.POST)
//    public String create(@Valid User newUser, @RequestParam(value = "department_id") Long department, RedirectAttributes redirectAttributes) {
//        Department department = new Department();
//        department.setId(department);
//        newUser.setDepartment(department);
//        newUser.setRegisterDate(new Date());
//        accountService.updateUser(newUser);
//        redirectAttributes.addFlashAttribute("message", "创建用户成功");
//        return "redirect:/admin/user";
//    }
//
//    /**
//     * 所有RequestMapping方法调用前的Model准备方法, 实现Struts2 Preparable二次部分绑定的效果,先根据form的id从数据库查出User对象,再把Form提交的内容绑定到该对象上。
//     * 因为仅update()方法的form中有id属性，因此仅在update时实际执行.
//     */
//    @ModelAttribute
//    public void getUser(@RequestParam(value = "id", defaultValue = "-1") Long id, Model model) {
//        if (id != -1) {
//            model.addAttribute("user", accountService.getUser(id));
//        }
//    }
//}
