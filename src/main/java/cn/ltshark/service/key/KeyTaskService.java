/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package cn.ltshark.service.key;

import cn.ltshark.entity.KeyTask;
import cn.ltshark.repository.KeyTaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.persistence.DynamicSpecifications;
import org.springside.modules.persistence.SearchFilter;
import org.springside.modules.persistence.SearchFilter.Operator;

import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Spring Bean的标识.
@Component
// 类中所有public函数都纳入事务管理的标识.
@Transactional
public class KeyTaskService {

    private KeyTaskDao keyTaskDao;

    public KeyTask getKeyTask(Long id) {
        return keyTaskDao.findOne(id);
    }

    public void saveKeyTask(KeyTask entity) {
        keyTaskDao.save(entity);
    }

    public void deleteKeyTask(Long id) {
        keyTaskDao.delete(id);
    }

    public List<KeyTask> getAllKeyTask() {
        return (List<KeyTask>) keyTaskDao.findAll();
    }

    public Page<KeyTask> getKeyTask(Map<String, Object> searchParams, int pageNumber, int pageSize,
                                    String sortType) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, sortType);
        Specification<KeyTask> spec = buildSpecification(searchParams);

        return keyTaskDao.findAll(spec, pageRequest);
    }

    public List<KeyTask> getUserKeyTasks(Map<String, Object> searchParams) {
        Specification<KeyTask> spec = buildSpecification(searchParams);
        return keyTaskDao.findAll(spec);
    }

    /**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(int pageNumber, int pagzSize, String sortType) {
        Sort sort = null;
        if ("auto".equals(sortType)) {
            sort = new Sort(Direction.DESC, "id");
        } else if ("title".equals(sortType)) {
            sort = new Sort(Direction.ASC, "title");
        }

        return new PageRequest(pageNumber - 1, pagzSize, sort);
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
        List<KeyTask> tasks = (List<KeyTask>) keyTaskDao.findAll(taskIds);
        for (KeyTask task : tasks) {
            task.setStatus(actionType);
        }
        keyTaskDao.save(tasks);
    }
}
