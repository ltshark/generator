/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.ltshark.service.account;

import cn.ltshark.entity.Group;
import cn.ltshark.entity.User;
import cn.ltshark.repository.GroupRepo;
import cn.ltshark.repository.UserDao;
import cn.ltshark.service.ServiceException;
import cn.ltshark.service.account.ShiroDbRealm.ShiroUser;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.springframework.ldap.query.LdapQueryBuilder.query;


/**
 * @author Mattias Hellborg Arthursson
 */
@Component
public class UserService implements BaseLdapNameAware {
    private final UserDao userDao;
    private final GroupRepo groupRepo;
    private LdapName baseLdapPath;
    private LdapTemplate ldapTemplate;
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserDao userDao, GroupRepo groupRepo) {
        this.userDao = userDao;
        this.groupRepo = groupRepo;
    }

    public Group getUserGroup() {
        return groupRepo.findByName(GroupRepo.USER_GROUP);
    }

    @Override
    public void setBaseLdapPath(LdapName baseLdapPath) {
        this.baseLdapPath = baseLdapPath;
    }

    public Iterable<User> findAll() {
        LdapQuery query = query().where("objectCategory").is("person").and("objectClass").is("user");
        for (User user : (List<User>) ldapTemplate.search(query, new UserMapper()))
            System.out.println(user);
        System.out.println("---------------------");
        Iterable<User> all = userDao.findAll(query);
        for (User user : (List<User>) all) {
            System.out.println(user);
        }
        return all;
    }

    public User findUserByLoginName(String loginName) {
        LdapQuery query = query().where("objectCategory").is("person").and("objectClass").is("user").and("samaccountname").is(loginName);
        List<User> search = ldapTemplate.search(query, new UserMapper());
        if (search.size() != 1)
            return null;
        return search.get(0);
    }

    public User findUser(String userId) {
        LdapName id = LdapUtils.newLdapName(userId);
        return userDao.findOne(id);
    }

    public User findUser(Name userId) {
        LdapName id = LdapUtils.newLdapName(userId);
        return userDao.findOne(id);
    }

    public User createUser(User user) {
        User savedUser = userDao.save(user);

        Group userGroup = getUserGroup();

        // The DN the member attribute must be absolute
        userGroup.addMember(toAbsoluteDn(savedUser.getId()));
        groupRepo.save(userGroup);

        return savedUser;
    }

    public LdapName toAbsoluteDn(Name relativeName) {
        return LdapNameBuilder.newInstance(baseLdapPath)
                .add(relativeName)
                .build();
    }

    /**
     * This method expects absolute DNs of group members. In order to find the actual users
     * the DNs need to have the base LDAP path removed.
     *
     * @param absoluteIds
     * @return
     */
    public Set<User> findAllMembers(Iterable<Name> absoluteIds) {
        return Sets.newLinkedHashSet(userDao.findAll(toRelativeIds(absoluteIds)));
    }

    public Iterable<Name> toRelativeIds(Iterable<Name> absoluteIds) {
        return Iterables.transform(absoluteIds, new Function<Name, Name>() {
            @Override
            public Name apply(Name input) {
                return LdapUtils.removeFirst(input, baseLdapPath);
            }
        });
    }

    public void updateUser(User user) {
//        LdapName originalId = LdapUtils.newLdapName(user.getId());
//        User existingUser = userDao.findOne(originalId);
//
//        existingUser.setFirstName(user.getFirstName());
//        existingUser.setLastName(user.getLastName());
//        existingUser.setFullName(user.getFullName());
//        existingUser.setEmail(user.getEmail());
//        existingUser.setPhone(user.getPhone());
//        existingUser.setTitle(user.getTitle());
//        existingUser.setDepartment(user.getDepartment());
//        existingUser.setDisplayName(user.getDisplayName());
//        existingUser.setDepartment(user.getDepartment());
//        existingUser.setName(user.getName());
//        existingUser.setUserPrincipalName(user.getUserPrincipalName());
//        existingUser.setSamAccountName(user.getSamAccountName());
//        existingUser.setDescription(user.getDescription());
        if (StringUtils.isNotBlank(user.getPlainPassword())) {
            modifyPassword(user);
        }
        userDao.save(user);
    }

    /**
     * Special behaviour in AD forces us to get the group membership before the user is updated,
     * because AD clears group membership for removed entries, which means that once the user is
     * update we've lost track of which groups the user was originally member of, preventing us to
     * update the membership references so that they point to the new DN of the user.
     * <p/>
     * This is slightly less efficient, since we need to get the group membership for all updates
     * even though the user may not have been moved. Using our knowledge of which attributes are
     * part of the distinguished name we can do this more efficiently if we are implementing specifically
     * for Active Directory - this approach is just to highlight this quite significant difference.
     *
     * @param originalId   the original id of the user.
     * @param existingUser the user, populated with new data
     * @return the updated entry
     */
    private User updateUserAd(LdapName originalId, User existingUser) {
        LdapName oldMemberDn = toAbsoluteDn(originalId);
        Collection<Group> groups = groupRepo.findByMember(oldMemberDn);

        User savedUser = userDao.save(existingUser);
        LdapName newMemberDn = toAbsoluteDn(savedUser.getId());

        if (!originalId.equals(savedUser.getId())) {
            // The user has moved - we need to update group references.
//            updateGroupReferences(groups, oldMemberDn, newMemberDn);
        }
        return savedUser;
    }

    public boolean authenticate(String loginName, String password) {
//        User user = findUserByLoginName(loginName);
//        if (user == null) return false;
//        DirContext ctx = null;
        try {
            LdapQuery query = query().where("samaccountname").is(loginName);
            ldapTemplate.authenticate(query, password);
//            ctx = ldapTemplate.getContextSource().getContext(user.getId().toString(), password);
//            System.out.println(ctx);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
//            LdapUtils.closeContext(ctx);
        }
    }

    public User findUserByLoginName(Name userId) {
        return findUserByLoginName(userId.toString());
    }

    /**
     * 取出Shiro中的当前用户Id.
     */
    public User getCurrentUser() {
        ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
        return findUser(user.id);
    }

    public boolean isDepartmentAdmin(User currentUser) {
        if (currentUser == null)
            return false;
        return "Network".equals(currentUser.getDepartment());
    }

    private final static class UserMapper extends AbstractContextMapper<User> {
        @Override
        protected User doMapFromContext(DirContextOperations ctx) {
            User user = new User();
            user.setId(ctx.getDn());
//            user.setFullName(ctx.getNameInNamespace());
            user.setFullName(ctx.getStringAttribute("cn"));
//            user.setEmployeeNumber(Integer.valueOf(ctx.getObjectAttribute("employeeNumber")));
            user.setFirstName(ctx.getStringAttribute("givenName"));
            user.setLastName(ctx.getStringAttribute("sn"));
            user.setTitle(ctx.getStringAttribute("title"));
            user.setEmail(ctx.getStringAttribute("mail"));
            user.setPhone(ctx.getStringAttribute("telephoneNumber"));
            user.setName(ctx.getStringAttribute("name"));
            user.setDepartment(ctx.getStringAttribute("department"));
            user.setUserPrincipalName(ctx.getStringAttribute("userprincipalname"));
            user.setSamAccountName(ctx.getStringAttribute("samaccountname"));
            user.setDisplayName(ctx.getStringAttribute("displayname"));
            user.setDescription(ctx.getStringAttribute("description"));
            System.out.println(ctx);
            return user;
        }
    }

    public void modifyPassword(User user) {
//        Hashtable env = new Hashtable();
//        String adminName = "cn=administrator,cn=users,DC=yaic,DC=com,DC=cn";
//
//        String adminpassword = "Yf821010";
//        String userName = "CN=qware4（快威4）,CN=users,DC=yaic,DC=com,DC=cn";
//        String userName = "CN=qware4（快威4）,CN=users,DC=yaic,DC=com,DC=cn";
        //old password Ab123456
//        String newPassword = "yaic32@";
//        String keystore = "C:\\Program Files\\Java\\jdk1.7.0_45\\jre\\lib\\security\\cacerts2";
        //   String keystore = "E:/project/iam/testADlhj.keystore";
//        System.setProperty("javax.net.ssl.trustStore", keystore);
//        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
//        String ldapURL = "ldap://192.168.134.129:636";
//        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//        env.put(Context.SECURITY_AUTHENTICATION, "simple");
//        env.put(Context.SECURITY_PRINCIPAL, adminName);
//        env.put(Context.SECURITY_CREDENTIALS, adminpassword);
//        env.put(Context.SECURITY_PROTOCOL, "ssl");
//        env.put(Context.PROVIDER_URL, ldapURL);
//        LdapContext ctx = null;
        try {
//            ctx = new InitialLdapContext(env, null);
            ModificationItem[] mods = new ModificationItem[2];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", user.getPassword()));
            mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", user.getPassword()));
            ldapTemplate.modifyAttributes(user.getId(), mods);
            System.out.println("Reset Password for: " + user.getId());
            System.out.println("Problem encoding password222: " + user.getPlainPassword());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Problem encoding password222: " + e);
            throw new ServiceException(e.getMessage());
        }
//        finally {
//            LdapUtils.closeContext(ctx);
//        }
    }


    public List<User> searchByNameName(String lastName) {
        return userDao.findByFullNameContains(lastName);
    }

    @Autowired
    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public static void main(String[] args) throws InvalidNameException {
        ApplicationContext applicationContext = new FileSystemXmlApplicationContext("classpath*:/applicationContext.xml");
        UserService userService = (UserService) applicationContext.getBean("userService");
//        List<User> users = (List<User>) userService.findAll();
//        System.out.println(users);
        boolean ok = userService.authenticate("qware4", "yaic32!");
        System.out.println(ok);
//        Name qware4 = userService.getUserIdByLoginName("qware4");
//        System.out.println(qware4);
//        System.out.println(userService.findUser("CN=qware4（快威4）,CN=Users"));
//        User user = userService.findUser(qware4);
//        System.out.println(user);

//        user.setPhone("111111111111");
//        userService.updateUser(qware4, user);

        if (!ok) {
            System.out.println("authenticate error");
            return;

        }
        User user = userService.findUserByLoginName("qware4");
        System.out.println("-------------mapper--------------");
        System.out.println(user);
        user = userService.findUser("CN=qware4（快威4）,CN=Users");
        System.out.println("-------------orm--------------");
        System.out.println(user);
        user = userService.findUser(user.getId());
        System.out.println(user);
        user.setPhone("22222");
        user.setPlainPassword("");
        userService.updateUser(user);
        user = userService.findUser(user.getId());
        System.out.println(user);
        user.setPlainPassword("yaic32@");
        userService.modifyPassword(user);
        ok = userService.authenticate(user.getSamAccountName(), user.getPlainPassword());
        System.out.println(ok);

    }
}
