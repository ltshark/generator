package cn.ltshark.domain;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Active Derictory 操作类
 *
 * @author Administrator
 */
public class ADOperator {

    private static Log log = LogFactory.getLog(ADOperator.class);

    private DirContext ctx;

    private static final String CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    // private static final int UF_ACCOUNTDISABLE = 0x0002;// 帐户禁用

    private static final int UF_PASSWD_NOTREQD = 0x0020;// 密码可以不设（为空）

    // private static final int UF_PASSWD_CANT_CHANGE = 0x0040;// 用户不能更改密码

    private static final int UF_NORMAL_ACCOUNT = 0x0200;// 普通帐户

    private static final int UF_DONT_EXPIRE_PASSWD = 0x10000;// 密码永不过期

    // private static final int UF_PASSWORD_EXPIRED = 0x800000;// 用户下次登陆时须更改密码

    private static String ldap_url; // ldap地址

    private static String auth_type;// 认证类型，一般为simple

    private static String manager_dn; // ldap管理员的dn

    private static String manager_password;// ldap管理员密码

    private static String certficationPath;// JDK中导入的证书路径

    private static String certficationPwd;// 证书密码

    private static String domain; // 域名，包括@符号

    private static String baseUserDN;// 用户的DN基础路径

    private static String enabledLCS; // 是否开户用户的LCS功能

    private static String LCServerDN; // LCS服务器的ldap DN

    static {
        readLdapInfo();
    }

    public static ADOperator getInstance() {
        return new ADOperator();
    }

    public ADOperator() {
        if (log.isDebugEnabled()) {
            readLdapInfo();
        }
    }

    private static void readLdapInfo() {
        Properties p = new Properties();
        try {
            p.load(ADOperator.class.getResourceAsStream("/conf/ldap.properties"));
            ldap_url = p.getProperty("ldap_url");
            auth_type = p.getProperty("auth_type");
            manager_dn = p.getProperty("manager_dn");
            manager_password = p.getProperty("manager_password");
            domain = "@" + p.getProperty("domain");
            enabledLCS = p.getProperty("enabledLCS");
            LCServerDN = p.getProperty("LCServerDN");
            baseUserDN = p.getProperty("baseUserDN");
            certficationPath = p.getProperty("certficationPath");
            certficationPwd = p.getProperty("certficationPwd");
        } catch (Exception e) {
            throw new RuntimeException("ldap.properties文件读取失败.", e);
            //log.error("数据读取失败!!!!!");
        }
    }

    /**
     * 关闭LDAP连接
     *
     * @param dirContext DirContext 已连接的LDAP的Context实例
     */
    private void closeDirContext() {
        try {
            if (ctx != null)
                ctx.close();
        } catch (Exception ex) {
            log.error("not close DirContext", ex);
        }
    }

    /**
     * 获取 LDAP 服务器连接的方法
     *
     * @param env 连接LDAP的连接信息
     * @return DirContext - LDAP server的连接
     */
    private void initDirContext() {
        try {
            //设置系统属性中ssl连接的证书和密码
            System.setProperty("javax.net.ssl.trustStore", certficationPath);
            System.setProperty("javax.net.ssl.trustStorePassword", certficationPwd);

            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, ldap_url);
            env.put(Context.SECURITY_AUTHENTICATION, auth_type);
            env.put(Context.SECURITY_PRINCIPAL, manager_dn);
            env.put(Context.SECURITY_CREDENTIALS, manager_password);
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            // 通过参数连接LDAP/AD
            ctx = new InitialDirContext(env);

            if (log.isDebugEnabled()) {
                log.debug("ldap 连接 AD 成功");
            }
        } catch (NamingException ex) {
            log.error("CONTEXT_FACTORY:" + CONTEXT_FACTORY + ",ldap_url:"
                    + ldap_url + ",auth_type:" + auth_type + ",manager_dn:"
                    + manager_dn + ",manager_password:" + manager_password, ex);
            throw new RuntimeException(ex);
        }
    }

    public void createUser(ADUserInfo user) throws Exception {
        if (this.ctx == null) {
            initDirContext();
        }

        if (this.isUserexist(user.getSamAccountName())) {
            return;
        }

        Attributes attrs = getCommonUserAttrs(user);
        addObjclassAttrs(attrs);
        // 添加用户节点
        putAttribute(attrs, "userPrincipalName", user.getSamAccountName()
                + domain);
        putAttribute(attrs, "sAMAccountName", user.getSamAccountName());
        putAttribute(attrs, "userAccountControl", Integer
                .toString(UF_NORMAL_ACCOUNT | UF_DONT_EXPIRE_PASSWD
                        | UF_PASSWD_NOTREQD));
        ctx
                .createSubcontext(getDN(user.getSamAccountName(), baseUserDN),
                        attrs);
        this.closeDirContext();
    }

    /**
     * 修改密码需要证书，请确认已经导入了证书JDK中
     *
     * @param user
     * @throws Exception
     */
    public void changePassword(ADUserInfo user) throws Exception {
        if (this.ctx == null) {
            initDirContext();
        }

        ModificationItem[] mods = new ModificationItem[1];
        String newQuotedPassword = "\"" + user.getPassword() + "\"";
        byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                new BasicAttribute("unicodePwd", newUnicodePassword));
        ctx.modifyAttributes(getDN(user.getSamAccountName(), baseUserDN), mods);

        this.closeDirContext();
    }

    public void modifyUser(ADUserInfo user) throws Exception {
        // 代码添加------------------------------------------------------->
        // System.out.println("数据库中的用户DN:"+user.getUserBaseDn());
        // System.out.println("LDAP中的用户 DN:"+ldapDN);
        // 将lDAP中的与数据库中不同的DN修改成以数据库为基准的DN
        // if (!ldapDN.equals(user.getUserBaseDn())) {
        // ctx.rename(getDN(user.getSamAccountName(), ldapDN), getDN(user
        // .getSamAccountName(), user.getUserBaseDn()));
        // }
        // 如nj1111@abcd:将其前面的nj1111不改变,但可换后缀@abcd信息,即先截取前名,将后缀改变(+domain)
        // String upn = (String) ctx.getAttributes(
        // getDN(user.getSamAccountName(), user.getUserBaseDn())).get(
        // "userPrincipalName").get();
        // String upn=ctx.getAttributes(getDN(user.getSamAccountName(),
        // user.getUserBaseDn())).get("userPrincipalName").toString();
        // String uname = upn.substring(0, upn.indexOf("@"));
        if (this.ctx == null) {
            initDirContext();
        }

        if (!this.isUserexist(user.getSamAccountName())) {
            this.createUser(user);
        }

        String uname = user.getSamAccountName();
        Attributes attrs = getCommonUserAttrs(user);
        putAttribute(attrs, "userPrincipalName", uname + domain);
        ctx.modifyAttributes(getDN(user.getSamAccountName(), baseUserDN),
                DirContext.REPLACE_ATTRIBUTE, attrs);

        this.closeDirContext();
    }

    public void destroyUser(String userName) throws NamingException {
        if (this.ctx == null) {
            initDirContext();
        }

        if (this.isUserexist(userName, baseUserDN)) {
            ctx.destroySubcontext(this.getDN(userName, baseUserDN));
        }

        this.closeDirContext();
    }

    // public void removeUser(String cn) throws NamingException {
    // if (!isUserexist(cn)) {
    // throw new NamingException("The user(cn: " + cn
    // + ") does not exist!");
    // }
    // ctx.destroySubcontext(getDN(cn));
    // }
    //
    // public void prohibitUser(String cn) throws NamingException {
    // ModificationItem[] mods = new ModificationItem[1];
    // mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
    // new BasicAttribute("userAccountControl", Integer
    // .toString(UF_NORMAL_ACCOUNT + UF_PASSWORD_EXPIRED)));
    // ctx.modifyAttributes(getDN(cn), mods);
    // }
    //
    // public void userMove(String cn, ADUserInfo user) throws
    // NamingException {
    // ModificationItem[] mods = new ModificationItem[1];
    // mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
    // new BasicAttribute("description", user.getDescription()));
    // ctx.modifyAttributes(getDN(cn), mods);
    // }

    private String getDN(String cn, String baseDn) {
        return "cn=" + cn + "," + baseDn;
    }

    // 查找用户
    private Attributes findUser(String cn, String baseDn) {
        Attributes attr = null;
        try {
            attr = ctx.getAttributes(getDN(cn, baseDn));
        } catch (NameNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("AD中用户" + cn + "不存在。");
            }
        } catch (Exception e) {
            log.error("ldap查找AD中的用户出错", e);
            throw new RuntimeException(e);
        }
        return attr;
    }

    // 验证用户是否存在
    public boolean isUserexist(String cn, String baseDn) {
        boolean isThisOpen = false;//标志是否是本方法里打开的上下文
        if (this.ctx == null) {
            initDirContext();
            isThisOpen = true;
        }

        boolean rtn = true;

        Attributes attrs = findUser(cn, baseDn);
        if (attrs == null) {
            rtn = false;
        }

        if (isThisOpen) {
            //如果是本方法打开的上下文则关掉上下文
            this.closeDirContext();
        }

        return rtn;
    }

    // 验证用户是否存在
    public boolean isUserexist(String cn) {
        return isUserexist(cn, baseUserDN);
    }

    // 设置属性
    private void putAttribute(Attributes attrs, String attrName,
                              Object attrValue) {
        if (attrValue != null && attrValue.toString().length() != 0) {
            Attribute attr = new BasicAttribute(attrName, attrValue);
            attrs.put(attr);
        }
    }

    private Attributes addObjclassAttrs(Attributes attrs) {
        Attribute objclass = new BasicAttribute("objectclass");
        objclass.add("top");
        objclass.add("person");
        objclass.add("organizationalPerson");
        objclass.add("user");
        attrs.put(objclass);
        return attrs;
    }

    /**
     * 下面这些是用户中LCS的属性
     * msRTCSIP-ArchivingEnabled 1   开启存档
     * msRTCSIP-FederationEnabled FALSE
     * msRTCSIP-InternetAccessEnabled FALSE
     * msRTCSIP-OptionFlags 0  此属性在老版本的lcs2005中不存在
     * msRTCSIP-PrimaryHomeServer CN=LC Services,CN=Microsoft,CN=LCS182,CN=Pools,CN=RTC Service,CN=Microsoft,CN=System,DC=bxxk,DC=com,DC=cn
     * msRTCSIP-PrimaryUserAddress sip:lcs1@bxxk.com.cn   lcs的帐户名称
     * msRTCSIP-UserEnabled TRUE    是否启用lcs帐户
     */
    private Attributes getCommonUserAttrs(ADUserInfo user) throws Exception {
        Attributes attrs = new BasicAttributes();

        // 设置属性
        putAttribute(attrs, "userPrincipalName", user.getSamAccountName()
                + domain);
        putAttribute(attrs, "sAMAccountName", user.getSamAccountName());
        putAttribute(attrs, "mail", user.getEmail());

        if (StringUtils.isNotEmpty(user.getGivenName())) {
            putAttribute(attrs, "givenname", user.getGivenName());
        }
        if (StringUtils.isNotEmpty(user.getSn())) {
            putAttribute(attrs, "sn", user.getSn());
        }
        if (StringUtils.isNotEmpty(user.getDescription())) {
            putAttribute(attrs, "description", user.getDescription());
        }
        if (StringUtils.isNotEmpty(user.getCompany())) {
            putAttribute(attrs, "company", user.getCompany());
        }
        if (StringUtils.isNotEmpty(user.getDepartment())) {
            putAttribute(attrs, "department", user.getDepartment());
        }
        if (StringUtils.isNotEmpty(user.getDisplayName())) {
            putAttribute(attrs, "displayName", user.getDisplayName());
        }

        putAttribute(attrs, "unicodePwd", ("\"" + user.getPassword() +
                "\"").getBytes("UTF-16LE"));

        if ("true".equalsIgnoreCase(enabledLCS) || "on".equals(enabledLCS)) {
            putAttribute(attrs, "msRTCSIP-ArchivingEnabled", "1");
            putAttribute(attrs, "msRTCSIP-FederationEnabled", "FALSE");
            putAttribute(attrs, "msRTCSIP-InternetAccessEnabled", "FALSE");
            //putAttribute(attrs, "msRTCSIP-OptionFlags", "0");
            putAttribute(attrs, "msRTCSIP-PrimaryHomeServer", LCServerDN);
            putAttribute(attrs, "msRTCSIP-PrimaryUserAddress", "sip:"
                    + user.getSamAccountName() + domain);
            putAttribute(attrs, "msRTCSIP-UserEnabled", "TRUE");
        }

        return attrs;
    }

    /**
     * 以防万一
     */
    protected void finalize() {
        closeDirContext();
    }

    public static void main(String[] args) throws Exception {
        ADUserInfo info = new ADUserInfo();
        info.setCompany("修改公司科技");
        info.setDepartment("修改技术部");
        info.setDescription("测试修改用户");
        info.setDisplayName("测试修改用户显示名称");
        info.setEmail("wls.wei@bxxk.com.cn");
        info.setGivenName("试");
        info.setSamAccountName("wls");
        info.setSn("测");
        info.setPassword("test+abcd123");

        ADOperator op = new ADOperator();
        boolean bool = op.isUserexist(info.getSamAccountName(),
                "cn=Users,dc=bxxk,dc=com,dc=cn");
        if (bool) {
            System.out.println("用户存在，准备修改用户。");
            op.modifyUser(info);
             op.changePassword(info);
            System.out.println("用户修改成功。");
        } else {
            System.out.println("用户不存在，准备创建用户。");
            op.createUser(info);
            System.out.println("创建用户成功。");
        }

        op.closeDirContext();
    }
}
