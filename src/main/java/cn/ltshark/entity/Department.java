package cn.ltshark.entity;

import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by ltshark on 15/6/12.
 */
@Entity
@Table(name = "g_department")
public class Department extends IdEntity {
    private String name;
    private Date createTime;
    private List<User> users;
    private List<User> admins;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH,
            CascadeType.MERGE, CascadeType.REMOVE, CascadeType.ALL}, fetch = FetchType.EAGER, mappedBy = "department")
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH,
            CascadeType.MERGE, CascadeType.REMOVE, CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "department")
    @Where(clause="roles = 'departmentAdmin'")
    public List<User> getAdmins() {
        return admins;
    }

    public void setAdmins(List<User> admins) {
        this.admins = admins;
    }
}
