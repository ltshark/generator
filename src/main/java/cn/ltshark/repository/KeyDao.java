package cn.ltshark.repository;

import cn.ltshark.entity.KeyTask;
import cn.ltshark.entity.Task;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by ltshark on 15/6/7.
 */
public interface KeyDao extends PagingAndSortingRepository<KeyTask, Long>, JpaSpecificationExecutor<Task> {
}
