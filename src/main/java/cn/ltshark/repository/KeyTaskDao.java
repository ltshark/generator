package cn.ltshark.repository;

import cn.ltshark.entity.KeyTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by ltshark on 15/6/7.
 */
public interface KeyTaskDao {

    void deleteByUserLoginName(String userLoginName);

    void save(List<KeyTask> keyTasks);

    KeyTask findOne(String id);

    Page<KeyTask> findAll(int pageNumber, int pageSize);

    void save(KeyTask entity);

    List<KeyTask> findAll(List<Long> taskIds);

    KeyTask findOne(Specification<KeyTask> spec);

    List<KeyTask> findAll(Specification<KeyTask> spec);

    Page<KeyTask> findAll(Specification<KeyTask> spec, PageRequest pageRequest);
}
