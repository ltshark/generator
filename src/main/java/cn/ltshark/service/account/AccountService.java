///*******************************************************************************
// * Copyright (c) 2005, 2014 springside.github.io
// * <p/>
// * Licensed under the Apache License, Version 2.0 (the "License");
// *******************************************************************************/
//package cn.ltshark.service.account;
//
//import java.io.UnsupportedEncodingException;
//import java.util.List;
//import java.util.Map;
//
//import cn.ltshark.repository.KeyTaskDao;
//import cn.ltshark.repository.UserDao;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.shiro.SecurityUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import cn.ltshark.entity.User;
//import cn.ltshark.service.ServiceException;
//import cn.ltshark.service.account.ShiroDbRealm.ShiroUser;
//import org.springside.modules.persistence.DynamicSpecifications;
//import org.springside.modules.persistence.SearchFilter;
//import org.springside.modules.security.utils.Digests;
//import org.springside.modules.utils.Clock;
//import org.springside.modules.utils.Encodes;
//
//import javax.naming.Name;
//
///**
// * 用户管理类.
// *
// * @author calvin
// */
//// Spring Service Bean的标识.
//@Component
//@Transactional
//public class AccountService {
//
//    private static Logger logger = LoggerFactory.getLogger(AccountService.class);
//
//    private UserDao userDao;
//    private KeyTaskDao keyTaskDao;
//    private UserService userService;
//
//    private Clock clock = Clock.DEFAULT;
//
//    public List<User> getAllUser() {
//        return (List<User>) userDao.findAll();
//    }
//
//    public User getUser(Name id) {
//        return userDao.findOne(id);
//    }
//
//    public User findUserByLoginName(String loginName) {
//        return userDao.findByLoginName(loginName);
//    }
//
//    public void updateUser(User user) throws UnsupportedEncodingException {
//        if (StringUtils.isNotBlank(user.getPlainPassword())) {
//            entryptPassword(user);
//            userService.modifyPassword(user);
//        }
//        userDao.save(user);
//    }
//
////    public Page<User> getUsers(Map<String, Object> searchParams, int pageNumber, int pageSize,
////                               String sortType) {
////        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, sortType);
////        Specification<User> spec = buildSpecification(searchParams);
////
////        return userDao.findAll(spec, pageRequest);
////    }
//
//    /**
//     * 创建分页请求.
//     */
//    private PageRequest buildPageRequest(int pageNumber, int pagzSize, String sortType) {
//        Sort sort = null;
//        if ("auto".equals(sortType)) {
//            sort = new Sort(Sort.Direction.DESC, "id");
//        } else if ("name".equals(sortType)) {
//            sort = new Sort(Sort.Direction.ASC, "title");
//        }
//
//        return new PageRequest(pageNumber - 1, pagzSize, sort);
//    }
//
//    /**
//     * 创建动态查询条件组合.
//     */
//    private Specification<User> buildSpecification(Map<String, Object> searchParams) {
//        Map<String, SearchFilter> filters = SearchFilter.parse(searchParams);
//        Specification<User> spec = DynamicSpecifications.bySearchFilter(filters.values(), User.class);
//        return spec;
//    }
//
//    /**
//     * 判断是否超级管理员.
//     */
//    private boolean isSupervisor(Long id) {
//        return id == 1;
//    }
//
//    /**
//     * 判断是否部门管理员.
//     */
//    public boolean isDepartmentAdmin(User user) {
//        if (user == null)
//            return false;
//        return "Network".equals(user.getDepartment());
//    }
//
//    /**
//     * 取出Shiro中的当前用户LoginName.
//     */
//    private String getCurrentUserName() {
//        ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
//        return user.loginName;
//    }
//
//    /**
//     * 设定安全的密码，生成随机的salt并经过1024次 sha-1 hash
//     */
//    private void entryptPassword(User user) throws UnsupportedEncodingException {
//        try {
//            String newQuotedPassword = "\"" + user.getPlainPassword() + "\"";
//            user.setPassword(newQuotedPassword.getBytes("UTF-16LE"));
//        } catch (UnsupportedEncodingException e) {
//            logger.error("entryptPassword error");
//            throw e;
//        }
//    }
//
//    @Autowired
//    public void setUserDao(UserDao userDao) {
//        this.userDao = userDao;
//    }
//
//    @Autowired
//    public void setKeyTaskDao(KeyTaskDao keyTaskDao) {
//        this.keyTaskDao = keyTaskDao;
//    }
//
//    @Autowired
//    public void setUserService(UserService userService) {
//        this.userService = userService;
//    }
//
//    public void setClock(Clock clock) {
//        this.clock = clock;
//    }
//
//
//    public User getCurrentUser() {
//        ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
//        return getUser(user.id);
//    }
//}
