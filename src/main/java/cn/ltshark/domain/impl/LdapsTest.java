package cn.ltshark.domain.impl;

/**
 * Created by ltshark on 15/6/30.
 */

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Properties;

public class LdapsTest {
    public static void main(String[] args) {
        Properties env = new Properties();
//        String adminName = "administrator@yaic.com.cn";//username@domain
        String adminName = "cn=administrator,cn=users,DC=yaic,DC=com,DC=cn";
        String adminPassword = "Yf821010";//password
        String ldapURL = "LDAP://172.16.104.186:636";//ip:port
        String keystore = "/Users/ltshark/Downloads/test.keystore";
//        String keystore = "/Library/Java/JavaVirtualMachines/jdk1.7.0_75.jdk/Contents/Home/jre/lib/security/test.keystore";
        //   String keystore = "E:/project/iam/testADlhj.keystore";
        System.setProperty("javax.net.ssl.trustStore", keystore);
        System.setProperty("javax.net.debug", "debug");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_PROTOCOL,"ssl");
//        env.put(Context.SECURITY_AUTHENTICATION, "simple");//"none","simple","strong"
        env.put(Context.SECURITY_PRINCIPAL, adminName);
        env.put(Context.SECURITY_CREDENTIALS, adminPassword);
        env.put(Context.PROVIDER_URL, ldapURL);
        try {
            LdapContext ctx = new InitialLdapContext(env, null);
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String searchFilter = "(&(objectCategory=person)(objectClass=user)(name=*))";
            String searchBase = "DC=yaic,DC=com,DC=cn";
            String returnedAtts[] = {"memberOf"};
            searchCtls.setReturningAttributes(returnedAtts);
            NamingEnumeration<SearchResult> answer = ctx.search(searchBase, searchFilter,searchCtls);
            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                System.out.println("<<<::[" + sr.getName()+"]::>>>>");
                System.out.println("<<<::[" + sr.getAttributes()+"]::>>>>");
                System.out.println("<<<::[" + sr.getNameInNamespace()+"]::>>>>");
            }
            ctx.close();
        }catch (NamingException e) {
            e.printStackTrace();
            System.err.println("Problem searching directory: " + e);
        }
    }
}
