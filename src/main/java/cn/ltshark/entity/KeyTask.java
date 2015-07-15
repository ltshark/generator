package cn.ltshark.entity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ltshark on 15/6/7.
 */
public class KeyTask {

    public static final String APPLYING_STATUS = "1";
    public static final String AGREE_APPLY_STATUS = "2";
    public static final String REFUSE_APPLY_STATUS = "3";

    public static final String HARDWARE_TYPE = "1";
    public static final String SOFTWARE_TYPE = "2";
    public static final String TEMP_TYPE = "3";

    private String type;
    private String userLoginName;
    private String status;
    private Date applyDate;//申请时间
    private Date approvalDate;//审批时间

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserLoginName() {
        return userLoginName;
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName = userLoginName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return StringUtils.join(new String[]{type, userLoginName, status, simpleDateFormat.format(applyDate), simpleDateFormat.format(approvalDate)}, ",");
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

    public static KeyTask toKeyTask(String s) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String[] strings = StringUtils.split(s);
        KeyTask keyTask = new KeyTask();
        int i = 0;
        keyTask.setType(strings[i++]);
        keyTask.setUserLoginName(strings[i++]);
        keyTask.setStatus(strings[i++]);
        try {
            if (strings.length > 3)
                keyTask.setApplyDate(simpleDateFormat.parse(strings[i++]));
            if (strings.length > 4)
                keyTask.setApprovalDate(simpleDateFormat.parse(strings[i++]));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return keyTask;
    }

    public static void main(String[] args) {
        KeyTask keyTask = new KeyTask();
        keyTask.setType("a");
        System.out.println(keyTask.toString());
    }
}
