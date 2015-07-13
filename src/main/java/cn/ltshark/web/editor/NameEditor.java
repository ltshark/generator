package cn.ltshark.web.editor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ldap.support.LdapUtils;

import java.beans.PropertyEditorSupport;

/**
 * Created by surfrong on 2015/7/13.
 */
public class NameEditor extends PropertyEditorSupport{

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if(StringUtils.isNotBlank(text)) {
            setValue(LdapUtils.newLdapName(text));
        }
    }

}
