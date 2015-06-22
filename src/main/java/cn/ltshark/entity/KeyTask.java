package cn.ltshark.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ltshark on 15/6/7.
 */
@Entity
@Table(name = "g_key_task")
public class KeyTask extends IdEntity {

    public static final String APPLYING_STATUS = "1";
    public static final String AGREE_APPLY_STATUS = "2";
    public static final String REFUSE_APPLY_STATUS = "3";
    private String type;
    private User user;
    private String status;
    private Date applyDate;//申请时间
    private Date approvalDate;//审批时间

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // JPA 基于USER_ID列的多对一关系定义
    @OneToOne
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

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public Date getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(Date applyDate) {
        this.applyDate = applyDate;
    }
}
