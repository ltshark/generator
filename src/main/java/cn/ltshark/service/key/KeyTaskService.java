/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package cn.ltshark.service.key;

import cn.ltshark.entity.KeyTask;
import cn.ltshark.repository.KeyTaskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springside.modules.persistence.DynamicSpecifications;
import org.springside.modules.persistence.SearchFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

// Spring Bean的标识.
@Component
public class KeyTaskService {

    private static Logger logger = LoggerFactory.getLogger(KeyTaskService.class);

    private KeyTaskDao keyTaskDao;

    public void saveKeyTask(KeyTask entity) {
        keyTaskDao.save(entity);
    }

    public void deleteKeyTask(String userLoginName) {
        keyTaskDao.deleteByUserLoginName(userLoginName);
    }

    public Page<KeyTask> getKeyTask(int pageNumber, int pageSize, Map<String, Object> searchParams) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
//        Specification<KeyTask> spec = buildSpecification(searchParams);
        return keyTaskDao.findAll(searchParams, pageRequest);
    }

//    public List<KeyTask> getUserKeyTasks(Map<String, Object> searchParams) {
//        Specification<KeyTask> spec = buildSpecification(searchParams);
//        return keyTaskDao.findAll(spec);
//    }

    /**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(int pageNumber, int pagzSize) {
        return new PageRequest(pageNumber - 1, pagzSize);
    }

    /**
     * 创建动态查询条件组合.
     */
    private Specification<KeyTask> buildSpecification(Map<String, Object> searchParams) {
        Map<String, SearchFilter> filters = SearchFilter.parse(searchParams);
//        filters.put("user.id", new SearchFilter("user.id", Operator.EQ, userId));
        Specification<KeyTask> spec = DynamicSpecifications.bySearchFilter(filters.values(), KeyTask.class);
        return spec;
    }

    @Autowired
    public void setKeyTaskDao(KeyTaskDao keyTaskDao) {
        this.keyTaskDao = keyTaskDao;
    }

    public void batchHandle(List<Long> taskIds, String actionType) {
        List<KeyTask> tasks = keyTaskDao.findAll(taskIds);
        for (KeyTask task : tasks) {
            task.setStatus(actionType);
        }
        keyTaskDao.save(tasks);
    }

    public void batchApply(List<String> userIds, String keyType) {
        List<KeyTask> keyTasks = new ArrayList<KeyTask>();
        for (String userid : userIds) {
            KeyTask keyTask = new KeyTask();
            keyTask.setUserLoginName(userid);
            keyTask.setType(keyType);
            keyTask.setStatus(KeyTask.APPLYING_STATUS);
            keyTask.setApplyDate(new Date());
            keyTasks.add(keyTask);
        }
        if (!keyTasks.isEmpty())
            keyTaskDao.save(keyTasks);
    }

    public KeyTask getUserKeyTask(Map<String, Object> searchParams) {
        Specification<KeyTask> spec = buildSpecification(searchParams);
        return keyTaskDao.findOne(spec);
    }

    public KeyTask getUserKeyTask(String userLoginName) {
        return keyTaskDao.findOne(userLoginName);
    }
}
