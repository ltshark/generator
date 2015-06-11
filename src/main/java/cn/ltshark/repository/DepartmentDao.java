package cn.ltshark.repository;

import cn.ltshark.entity.Department;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by ltshark on 15/6/12.
 */
public interface DepartmentDao extends PagingAndSortingRepository<Department, Long>, JpaSpecificationExecutor<Department> {
}
