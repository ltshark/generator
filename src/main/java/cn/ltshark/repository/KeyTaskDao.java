package cn.ltshark.repository;

import cn.ltshark.entity.KeyTask;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by ltshark on 15/6/7.
 */
public interface KeyTaskDao extends PagingAndSortingRepository<KeyTask, Long>, JpaSpecificationExecutor<KeyTask> {

    void deleteByUserId(Long id);
}
