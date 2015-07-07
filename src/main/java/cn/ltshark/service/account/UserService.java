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

import cn.ltshark.domain.*;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * @author Mattias Hellborg Arthursson
 */
@Component
public class UserService implements BaseLdapNameAware {
    private final UserRepo userRepo;
    private final GroupRepo groupRepo;
    private LdapName baseLdapPath;
    private DirectoryType directoryType;
    private LdapTemplate ldapTemplate;

    @Autowired
    public UserService(UserRepo userRepo, GroupRepo groupRepo) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
    }

    public Group getUserGroup() {
        return groupRepo.findByName(GroupRepo.USER_GROUP);
    }

    public void setDirectoryType(DirectoryType directoryType) {
        this.directoryType = directoryType;
    }

    @Override
    public void setBaseLdapPath(LdapName baseLdapPath) {
        this.baseLdapPath = baseLdapPath;
    }

    public Iterable<User> findAll() {
        LdapQuery query = query().where("objectCategory").is("person").and("objectClass").is("user");
        for (User user : (List<User>) ldapTemplate.search(query, getContextMapper()))
            System.out.println(user);
        System.out.println("---------------------");
        Iterable<User> all = userRepo.findAll(query);
        for (User user : (List<User>) all) {
            System.out.println(user);
        }
//      samaccountname
        return all;
    }

    protected ContextMapper getContextMapper() {
        return new PersonContextMapper();
    }


    private static class PersonContextMapper extends AbstractContextMapper<User> {
        public User doMapFromContext(DirContextOperations context) {
            System.out.println(context);
            User person = new User();
            person.setFullName(context.getStringAttribute("cn"));
            person.setLastName(context.getStringAttribute("sn"));
            return person;
        }
    }

    public Name getUserIdByLoginName(String loginName) {
        LdapQuery query = query().where("objectCategory").is("person").and("objectClass").is("user").and("samaccountname").is(loginName);
        List<Name> search = ldapTemplate.search(query, new UserIdContextMapper());
        if (search.size() != 1)
            return null;
        return search.get(0);
    }

    public User findUser(String userId) {
        LdapName id = LdapUtils.newLdapName(userId);
        return userRepo.findOne(id);
    }

    public User findUser(Name userId) {
        LdapName id = LdapUtils.newLdapName(userId);
        return userRepo.findOne(id);
    }

    public User createUser(User user) {
        User savedUser = userRepo.save(user);

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
        return Sets.newLinkedHashSet(userRepo.findAll(toRelativeIds(absoluteIds)));
    }

    public Iterable<Name> toRelativeIds(Iterable<Name> absoluteIds) {
        return Iterables.transform(absoluteIds, new Function<Name, Name>() {
            @Override
            public Name apply(Name input) {
                return LdapUtils.removeFirst(input, baseLdapPath);
            }
        });
    }

    public User updateUser(Name userId, User user) {
        LdapName originalId = LdapUtils.newLdapName(userId);
        User existingUser = userRepo.findOne(originalId);

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setTitle(user.getTitle());
        existingUser.setDepartment(user.getDepartment());
//        existingUser.setUnit(user.getUnit());

//        return null;
        if (directoryType == DirectoryType.AD) {
            return updateUserAd(originalId, existingUser);
        } else {
            return updateUserStandard(originalId, existingUser);
        }
    }


    /**
     * Update the user and - if its id changed - update all group references to the user.
     *
     * @param originalId   the original id of the user.
     * @param existingUser the user, populated with new data
     * @return the updated entry
     */
    private User updateUserStandard(LdapName originalId, User existingUser) {
        User savedUser = userRepo.save(existingUser);

        if (!originalId.equals(savedUser.getId())) {
            // The user has moved - we need to update group references.
            LdapName oldMemberDn = toAbsoluteDn(originalId);
            LdapName newMemberDn = toAbsoluteDn(savedUser.getId());

            Collection<Group> groups = groupRepo.findByMember(oldMemberDn);
            updateGroupReferences(groups, oldMemberDn, newMemberDn);
        }
        return savedUser;
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

        User savedUser = userRepo.save(existingUser);
        LdapName newMemberDn = toAbsoluteDn(savedUser.getId());

        if (!originalId.equals(savedUser.getId())) {
            // The user has moved - we need to update group references.
//            updateGroupReferences(groups, oldMemberDn, newMemberDn);
        }
        return savedUser;
    }

    private void updateGroupReferences(Collection<Group> groups, Name originalId, Name newId) {
        for (Group group : groups) {
            group.removeMember(originalId);
            group.addMember(newId);

            groupRepo.save(group);
        }
    }

    public boolean authenticate(String userName, String password) {
        String userId = getUserNameInNamespaceByloginName(userName);
        if (StringUtils.isBlank(userId)) return false;

        DirContext ctx = null;
        try {
            ctx = ldapTemplate.getContextSource().getContext(userId, password);
            System.out.println(ctx);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            LdapUtils.closeContext(ctx);
        }
    }

    private String getUserNameInNamespaceByloginName(String userName) {
        LdapQuery query = query().where("objectCategory").is("person").and("objectClass").is("user").and("samaccountname").is(userName);
        List<String> search = ldapTemplate.search(query, new DnContextMapper());
        if (search.size() != 1)
            return null;
        return search.get(0);
    }

    private final static class DnContextMapper extends AbstractContextMapper<String> {
        @Override
        protected String doMapFromContext(DirContextOperations ctx) {
            return ctx.getNameInNamespace();
        }
    }

    private final static class UserIdContextMapper extends AbstractContextMapper<Name> {
        @Override
        protected Name doMapFromContext(DirContextOperations ctx) {
            return ctx.getDn();
        }
    }


    public void modifyPassword(Name name, String newPwd) {
//        String userId = getUserNameInNamespaceByloginName(userName);
//        if (StringUtils.isBlank(userId)) return false;
        Attributes attrs = new BasicAttributes();
        attrs.put(Context.SECURITY_CREDENTIALS, newPwd);
        DirContext ctx = null;
        try {
//            ctx = ldapTemplate.getContextSource().getContext(userId, password);
            ModificationItem[] mods = new ModificationItem[1];
            String newQuotedPassword = "\"" + newPwd + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));
            ldapTemplate.modifyAttributes(name, mods);
//            System.out.println(ctx);
//            ctx.modifyAttributes(userId, DirContext.REPLACE_ATTRIBUTE, attrs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LdapUtils.closeContext(ctx);
        }
    }

    public void m() {
        // java.net.Socket sock = new java.net.Socket("10.110.180.50",636);
        // boolean b = sock.isConnected();
        Hashtable env = new Hashtable();
        String adminName = "cn=administrator,cn=users,DC=yaic,DC=com,DC=cn";

        String adminpassword = "Yf821010";
        String userName = "CN=qware4（快威4）,CN=users,DC=yaic,DC=com,DC=cn";
        //old password Ab123456
        String newPassword = "yaic32@";
        String keystore = "C:\\Program Files\\Java\\jdk1.7.0_45\\jre\\lib\\security\\cacerts2";
        //   String keystore = "E:/project/iam/testADlhj.keystore";
        System.setProperty("javax.net.ssl.trustStore", keystore);
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, adminName);
        env.put(Context.SECURITY_CREDENTIALS, adminpassword);
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        String ldapURL = "ldap://192.168.134.129:636";
        env.put(Context.PROVIDER_URL, ldapURL);
        try {
            LdapContext ctx = new InitialLdapContext(env, null);
            ModificationItem[] mods = new ModificationItem[1];
            String newQuotedPassword = "\"" + newPassword + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));
            ctx.modifyAttributes(userName, mods);
            System.out.println("Reset Password for: " + userName);
            ctx.close();
            System.out.println("Problem encoding password222: ");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Problem encoding password222: " + e);
        }
    }

    public List<User> searchByNameName(String lastName) {
        return userRepo.findByFullNameContains(lastName);
    }

    @Autowired
    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new FileSystemXmlApplicationContext("classpath*:/applicationContext.xml");
        UserService userService = (UserService) applicationContext.getBean("userService");
//        List<User> users = (List<User>) userService.findAll();
//        System.out.println(users);
        userService.setDirectoryType(DirectoryType.AD);
        boolean ok = userService.authenticate("qware4", "yaic32!");
        System.out.println(ok);
//        Name qware4 = userService.getUserIdByLoginName("qware4");
//        System.out.println(qware4);
//        System.out.println(userService.findUser("CN=qware4（快威4）,CN=Users"));
//        User user = userService.findUser(qware4);
//        System.out.println(user);

//        user.setPhone("111111111111");
//        userService.updateUser(qware4, user);

//        Name qware4 = userService.getUserIdByLoginName("qware4");
//        userService.modifyPassword(qware4, "yaic32");
        userService.m();
        ok = userService.authenticate("qware4", "yaic32@");
        System.out.println(ok);

    }
}
