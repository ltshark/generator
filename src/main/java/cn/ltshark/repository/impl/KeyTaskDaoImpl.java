package cn.ltshark.repository.impl;

import cn.ltshark.entity.KeyTask;
import cn.ltshark.repository.KeyTaskDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by surfrong on 2015/7/13.
 */
@Component
public class KeyTaskDaoImpl implements KeyTaskDao {
    @Override
    public void deleteByUserId(Long id) {

    }

    @Override
    public void save(List<KeyTask> keyTasks) {

    }

    @Override
    public KeyTask findOne(Long id) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public List<KeyTask> findAll() {
        return null;
    }

    @Override
    public void save(KeyTask entity) {

    }

    @Override
    public List<KeyTask> findAll(List<Long> taskIds) {
        return null;
    }

    @Override
    public KeyTask findOne(Specification<KeyTask> spec) {
        return null;
    }

    @Override
    public List<KeyTask> findAll(Specification<KeyTask> spec) {
        return null;
    }

    @Override
    public Page<KeyTask> findAll(Specification<KeyTask> spec, PageRequest pageRequest) {
        return null;
    }
}
