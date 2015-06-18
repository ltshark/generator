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
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.ldap.UnsupportedAuthenticationMechanismException;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.utils.Encodes;

import javax.annotation.PostConstruct;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import java.io.Serializable;

public class ShiroDbRealm extends JndiLdapRealm {

    private static final Logger log = LoggerFactory.getLogger(ShiroDbRealm.class);

    protected AccountService accountService;
    protected String rootDN;
    private boolean enableLDAP = false;

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

        User user = accountService.findUserByLoginName(token.getUsername());
        if (user == null) {
            return null;
        }

        if (enableLDAP) {
            AuthenticationInfo info;
            try {
                info = queryForAuthenticationInfo(token, getContextFactory());
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
            if (info == null) {
                return null;
            }
        }
        byte[] salt = Encodes.decodeHex(user.getSalt());
        return new SimpleAuthenticationInfo(new ShiroUser(user.getId(), user.getLoginName(), user.getName()),
                user.getPassword(), ByteSource.Util.bytes(salt), getName());

    }

    /**
     * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
        User user = accountService.findUserByLoginName(shiroUser.loginName);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addRoles(user.getRoleList());
        return info;
    }

    @Override
    protected AuthenticationInfo queryForAuthenticationInfo(AuthenticationToken token, LdapContextFactory ldapContextFactory) throws NamingException {
        Object principal = token.getPrincipal();
        Object credentials = token.getCredentials();
        LdapContext systemCtx = null;
        LdapContext ctx = null;
        try {
            systemCtx = ldapContextFactory.getSystemLdapContext();
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = systemCtx.search(rootDN, "cn=" + principal, constraints);
            if (results != null && !results.hasMore()) {
                throw new UnknownAccountException();
            } else {
                while (results.hasMore()) {
                    SearchResult si = (SearchResult) results.next();
                    principal = si.getName() + "," + rootDN;
                }
                log.info("DN=" + principal);
                try {
                    ctx = ldapContextFactory.getLdapContext(principal, credentials);
                } catch (NamingException e) {
                    throw new IncorrectCredentialsException();
                }
                return createAuthenticationInfo(token, principal, credentials, ctx);
            }
        } finally {
            LdapUtils.closeContext(systemCtx);
            LdapUtils.closeContext(ctx);
        }
    }

    /**
     * 设定Password校验的Hash算法与迭代次数.
     */
    @PostConstruct
    public void initCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(AccountService.HASH_ALGORITHM);
        matcher.setHashIterations(AccountService.HASH_INTERATIONS);

        setCredentialsMatcher(matcher);
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public void setRootDN(String rootDN) {
        this.rootDN = rootDN;
    }

    public void setEnableLDAP(boolean enableLDAP) {
        this.enableLDAP = enableLDAP;
    }

    /**
     * 自定义Authentication对象，使得Subject除了携带用户的登录名外还可以携带更多信息.
     */
    public static class ShiroUser implements Serializable {
        private static final long serialVersionUID = -1373760761780840081L;
        public Long id;
        public String loginName;
        public String name;

        public ShiroUser(Long id, String loginName, String name) {
            this.id = id;
            this.loginName = loginName;
            this.name = name;
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
