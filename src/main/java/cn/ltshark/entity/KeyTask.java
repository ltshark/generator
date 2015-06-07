package cn.ltshark.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by ltshark on 15/6/7.
 */
@Entity
@Table(name = "g_key_task")
public class KeyTask {

    private String type;
    private User user;
    private String status;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // JPA 基于USER_ID列的多对一关系定义
    @ManyToOne
    @JoinColumn(name = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
