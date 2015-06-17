package cn.ltshark.service.account;

import cn.ltshark.entity.Department;
import cn.ltshark.repository.DepartmentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.persistence.DynamicSpecifications;
import org.springside.modules.persistence.SearchFilter;

import java.util.Map;

/**
 * Created by ltshark on 15/6/12.
 */
@Component
@Transactional
public class DepartmentService {

    private static Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    private DepartmentDao departmentDao;

    @Autowired
    public void setDepartmentDao(DepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }

    public Page<Department> getDepartments(Map<String, Object> searchParams, int pageNumber, int pageSize, String sortType) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, sortType);
        Specification<Department> spec = buildSpecification(searchParams);
        return departmentDao.findAll(spec, pageRequest);
    }

    /**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(int pageNumber, int pagzSize, String sortType) {
        Sort sort = null;
        if ("auto".equals(sortType)) {
            sort = new Sort(Sort.Direction.DESC, "id");
        } else if ("name".equals(sortType)) {
            sort = new Sort(Sort.Direction.ASC, "name");
        }

        return new PageRequest(pageNumber - 1, pagzSize, sort);
    }

    /**
     * 创建动态查询条件组合.
     */
    private Specification<Department> buildSpecification(Map<String, Object> searchParams) {
        Map<String, SearchFilter> filters = SearchFilter.parse(searchParams);
        Specification<Department> spec = DynamicSpecifications.bySearchFilter(filters.values(), Department.class);
        return spec;
    }

    public Department getDepartment(Long id) {
        return departmentDao.findOne(id);
    }

    public void updateDepartment(Department newDepartment) {
        departmentDao.save(newDepartment);
    }

    public void deleteDepartment(Long id) {
        departmentDao.delete(id);
    }
}
