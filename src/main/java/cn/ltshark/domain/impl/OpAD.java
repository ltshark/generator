package cn.ltshark.domain.impl;

/**
 * Created by ltshark on 15/7/7.
 */

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class OpAD {

    private LdapContext ctx = null;

    private String adminName = "administrator@yaic.com.cn";

    private String adminpassword = "Yf821010";

    private String keystore = "/Users/ltshark/Downloads/test.keystore";

    private String keyPassword = "123456";

    private String ldapURL = "ldaps://172.16.104.186:636";//ip:port

    private String searchBase = "DC=yaic,DC=com,DC=cn";

    private String returnedAtts[] = {"distinguishedName"};

    private boolean initial_Ldap() {

        Hashtable env = new Hashtable();
        System.setProperty("javax.net.ssl.trustStore", keystore);
        System.setProperty("javax.net.ssl.trustStorePassword", keyPassword);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, adminName);
        env.put(Context.SECURITY_CREDENTIALS, adminpassword);
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        env.put(Context.PROVIDER_URL, ldapURL);

        try {
            System.out.println("Start InitialLdapContext");
            ctx = new InitialLdapContext(env, null);
            System.out.println("InitialLdapContext succeed");
        } catch (NamingException e) {
            e.printStackTrace();
            System.out.println("Problem initial_Ldap NamingException: " + e);
            return false;
        }

        return true;
    }

    private boolean close_Ldap() {
        System.out.println("Close Ldap");
        try {
            ctx.close();
        } catch (NamingException e) {
            System.out.println("Problem close_Ldap NamingException: " + e);
            return false;
        }
        return true;
    }

    private String search_distinguishedName(String username) {
        String searchFilter = "(&(objectClass=user)(cn=" + username + "))";

        try {
            System.out.println("Start search " + username + "'s distinguishedName");
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchCtls.setReturningAttributes(returnedAtts);
            NamingEnumeration answer = ctx.search(searchBase, searchFilter,
                    searchCtls);
            if (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                Attributes attrs = sr.getAttributes();
                if (attrs != null) {
                    NamingEnumeration ae = attrs.getAll();
                    Attribute attr = (Attribute) ae.next();
                    NamingEnumeration e = attr.getAll();
                    return (String) e.next();
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
            System.out
                    .println("Problem search_distinguishedName NamingException: " + e);
            return "error";
        }

        return "none";
    }

//    private boolean mod_Pwd(String username, String password) {
//        ModificationItem[] mods = new ModificationItem[1];
//        String newQuotedPassword = "/" " + password + " / "";
//
//        try {
//            System.out.println("Start reset password");
//            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
//            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
//                    new BasicAttribute("unicodePwd", newUnicodePassword));
//            ctx.modifyAttributes(username, mods);
//            System.out.println("Finish reset password" + username);
//        } catch (UnsupportedEncodingException e) {
//            System.out.println("Problem mod_Pwd UnsupportedEncodingException: " + e);
//            return false;
//        } catch (NamingException e) {
//            System.out.println("Problem mod_Pwd NamingException: " + e);
//            return false;
//        }
//
//        return true;
//    }

    public static void main(String args[]) {
        OpAD inst = new OpAD();
        inst.initial_Ldap();
        String username = inst.search_distinguishedName("testuser");
        System.out.println(username);
//        inst.mod_Pwd(username, "1234AbcD");
        inst.close_Ldap();

    }

}