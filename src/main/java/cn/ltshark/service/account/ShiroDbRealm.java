/*******************************************************************************
 * Copyright (c) 2005, 2014 springside.github.io
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 *******************************************************************************/
package cn.ltshark.service.account;

import cn.ltshark.entity.User;
import cn.ltshark.shiro.CaptchaException;
import cn.ltshark.shiro.UsernamePasswordCaptchaToken;
import cn.ltshark.web.account.VerifyCodeServlet;
import com.google.common.base.Objects;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.ldap.UnsupportedAuthenticationMechanismException;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Name;
import javax.naming.NamingException;
import java.io.Serializable;

public class ShiroDbRealm extends JndiLdapRealm {

    private static final Logger log = LoggerFactory.getLogger(ShiroDbRealm.class);

    protected UserService userService;
    protected String rootDN;
    protected String managerDepartment;

    /**
     * 认证回调函数,登录时调用.
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
        UsernamePasswordCaptchaToken token = (UsernamePasswordCaptchaToken) authcToken;

        // 增加判断验证码逻辑
        String captcha = token.getCaptcha();
        String exitCode = (String) SecurityUtils.getSubject().getSession().getAttribute(VerifyCodeServlet.KEY_VALIDATE_CODE);

        if (null == captcha || !captcha.equalsIgnoreCase(exitCode)) {
            throw new CaptchaException("验证码错误");
        }

        try {
            return queryForAuthenticationInfo(token, getContextFactory());
        } catch (AuthenticationNotSupportedException e) {
            String msg = "Unsupported configured authentication mechanism";
            throw new UnsupportedAuthenticationMechanismException(msg, e);
        } catch (javax.naming.AuthenticationException e) {
            String msg = "LDAP authentication failed.";
            throw new AuthenticationException(msg, e);
        } catch (NamingException e) {
            String msg = "LDAP naming error while attempting to authenticate user.";
            throw new AuthenticationException(msg, e);
        } catch (UnknownAccountException e) {
            String msg = "UnknownAccountException";
            throw new UnknownAccountException(msg, e);
        } catch (IncorrectCredentialsException e) {
            String msg = "IncorrectCredentialsException";
            throw new IncorrectCredentialsException(msg, e);
        }
    }

    /**
     * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
        User user = userService.findUserByLoginName(shiroUser.loginName);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        if (managerDepartment.equals(user.getDepartment()))
            info.addRole("admin");
        else
            info.addRole("user");
        return info;
    }

    @Override
    protected AuthenticationInfo queryForAuthenticationInfo(AuthenticationToken token, LdapContextFactory ldapContextFactory) throws NamingException {

        //从页面提交的用户名“lisi”
        Object principal = token.getPrincipal();
        //从页面提交的口令“123456”
        Object credentials = token.getCredentials();

        log.info("Authenticating user '{}' through LDAP", principal);

        String loginName = String.valueOf(principal);
        boolean ok = userService.authenticate(loginName, new String((char[]) credentials));
        if (ok) {
            User user = userService.findUserByLoginName(loginName);
            return new SimpleAuthenticationInfo(new ShiroUser(user.getId(), user.getSamAccountName(), user.getName(), user.getDepartment()), token.getCredentials(), this.getName());
        } else
            throw new NamingException("not find " + principal);
    }

    @Value("${sample.ldap.base}")
    public void setRootDN(String rootDN) {
        this.rootDN = rootDN;
    }

    @Value("${managerDepartment}")
    public void setManagerDepartment(String managerDepartment) {
        this.managerDepartment = managerDepartment;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * 自定义Authentication对象，使得Subject除了携带用户的登录名外还可以携带更多信息.
     */
    public static class ShiroUser implements Serializable {
        private static final long serialVersionUID = -1373760761780840081L;
        public Name id;
        public String loginName;
        public String name;
        public String department;

        public ShiroUser(Name id, String loginName, String name, String department) {
            this.id = id;
            this.loginName = loginName;
            this.name = name;
            this.department = department;
        }

        public String getName() {
            return name;
        }

        /**
         * 本函数输出将作为默认的<shiro:principal/>输出.
         */
        @Override
        public String toString() {
            return loginName;
        }

        /**
         * 重载hashCode,只计算loginName;
         */
        @Override
        public int hashCode() {
            return Objects.hashCode(loginName);
        }

        /**
         * 重载equals,只计算loginName;
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ShiroUser other = (ShiroUser) obj;
            if (loginName == null) {
                if (other.loginName != null) {
                    return false;
                }
            } else if (!loginName.equals(other.loginName)) {
                return false;
            }
            return true;
        }
    }
}
