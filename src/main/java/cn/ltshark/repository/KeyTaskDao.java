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

    void deleteByUserId(Long id);

    void save(List<KeyTask> keyTasks);

    KeyTask findOne(Long id);

    void delete(Long id);

    List<KeyTask> findAll();

    void save(KeyTask entity);

    List<KeyTask> findAll(List<Long> taskIds);

    KeyTask findOne(Specification<KeyTask> spec);

    List<KeyTask> findAll(Specification<KeyTask> spec);

    Page<KeyTask> findAll(Specification<KeyTask> spec, PageRequest pageRequest);
}
