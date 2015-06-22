package cn.ltshark.web.account;

import cn.ltshark.entity.Department;
import cn.ltshark.service.account.DepartmentService;
import cn.ltshark.web.task.TaskController;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springside.modules.web.Servlets;

import javax.servlet.ServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.Map;

/**
 * Created by surfrong on 2015/6/16.
 */
@Controller
@RequestMapping(value = "/admin/department")
public class DepartmentAdminController {

    private static Map<String, String> sortTypes = Maps.newLinkedHashMap();
    static {
        sortTypes.put("auto", "自动");
        sortTypes.put("name", "部门名称");
    }
    @Autowired
    private DepartmentService departmentService;

    @RequestMapping(method = RequestMethod.GET)
    public String list(@RequestParam(value = "page", defaultValue = "1") int pageNumber,
                       @RequestParam(value = "page.size", defaultValue = TaskController.PAGE_SIZE) int pageSize,
                       @RequestParam(value = "sortType", defaultValue = "auto") String sortType, Model model,
                       ServletRequest request) {
        Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, "search_");

        Page<Department> Departments = departmentService.getDepartments(searchParams, pageNumber, pageSize, sortType);

        model.addAttribute("departments", Departments);
        model.addAttribute("sortType", sortType);
        model.addAttribute("sortTypes", sortTypes);
        // 将搜索条件编码成字符串，用于排序，分页的URL
        model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, "search_"));

        return "account/adminDepartmentList";
    }

    @RequestMapping(value = "update/{id}", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("department", departmentService.getDepartment(id));
        model.addAttribute("action", "update");
        return "account/adminDepartmentForm";
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public String update(@Valid @ModelAttribute("department") Department department, RedirectAttributes redirectAttributes) {
        departmentService.updateDepartment(department);
        redirectAttributes.addFlashAttribute("message", "更新部门" + department.getName() + "成功");
        return "redirect:/admin/department";
    }

    @RequestMapping(value = "delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Department department = departmentService.getDepartment(id);
        departmentService.deleteDepartment(id);
        redirectAttributes.addFlashAttribute("message", "删除部门" + department.getName() + "成功");
        return "redirect:/admin/department";
    }

    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String createForm(Model model) {
        model.addAttribute("Department", new Department());
        model.addAttribute("action", "create");
        return "account/adminDepartmentForm";
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String create(@Valid Department newDepartment, RedirectAttributes redirectAttributes) {
        newDepartment.setCreateTime(new Date());
        departmentService.updateDepartment(newDepartment);
        redirectAttributes.addFlashAttribute("message", "创建部门成功");
        return "redirect:/admin/department";
    }

    /**
     * 所有RequestMapping方法调用前的Model准备方法, 实现Struts2 Preparable二次部分绑定的效果,先根据form的id从数据库查出Department对象,再把Form提交的内容绑定到该对象上。
     * 因为仅update()方法的form中有id属性，因此仅在update时实际执行.
     */
    @ModelAttribute
    public void getDepartment(@RequestParam(value = "id", defaultValue = "-1") Long id, Model model) {
        if (id != -1) {
            model.addAttribute("department", departmentService.getDepartment(id));
        }
    }
}
