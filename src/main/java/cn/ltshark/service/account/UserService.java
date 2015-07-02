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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import javax.naming.Name;
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
        LdapQueryBuilder query1 = query();
//        query1.attributes("memberOf");
        LdapQuery query = (LdapQuery) query1
                .where("objectCategory").is("person").and("objectClass").is("user");
//                .where("objectClass").is("user");
        for(User user : (List<User>)ldapTemplate.search(query1, getContextMapper()))
            System.out.println(user);
        System.out.println("---------------------");
        Iterable<User> all = userRepo.findAll(query);
        for(User user : (List<User>)all){
            System.out.println(user);

        }
        return all;
    }
    protected ContextMapper getContextMapper() {
        return new PersonContextMapper();
    }


    private static class PersonContextMapper extends AbstractContextMapper<User> {
        public User doMapFromContext(DirContextOperations context) {
            User person = new User();
            person.setFullName(context.getStringAttribute("cn"));
            person.setLastName(context.getStringAttribute("sn"));
            return person;
        }
    }

    public User findUser(String userId) {
        return userRepo.findOne(LdapUtils.newLdapName(userId));
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

    public User updateUser(String userId, User user) {
        LdapName originalId = LdapUtils.newLdapName(userId);
        User existingUser = userRepo.findOne(originalId);

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setTitle(user.getTitle());
        existingUser.setDepartment(user.getDepartment());
        existingUser.setUnit(user.getUnit());

        if (directoryType == DirectoryType.AD) {
            return updateUserAd(originalId, existingUser);
        } else {
            return updateUserStandard(originalId, existingUser);
        }
    }


    /**
     * Update the user and - if its id changed - update all group references to the user.
     *
     * @param originalId the original id of the user.
     * @param existingUser the user, populated with new data
     *
     * @return the updated entry
     */
    private User updateUserStandard(LdapName originalId, User existingUser) {
        User savedUser = userRepo.save(existingUser);

        if(!originalId.equals(savedUser.getId())) {
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
     *
     * This is slightly less efficient, since we need to get the group membership for all updates
     * even though the user may not have been moved. Using our knowledge of which attributes are
     * part of the distinguished name we can do this more efficiently if we are implementing specifically
     * for Active Directory - this approach is just to highlight this quite significant difference.
     *
     * @param originalId the original id of the user.
     * @param existingUser the user, populated with new data
     *
     * @return the updated entry
     */
    private User updateUserAd(LdapName originalId, User existingUser) {
        LdapName oldMemberDn = toAbsoluteDn(originalId);
        Collection<Group> groups = groupRepo.findByMember(oldMemberDn);

        User savedUser = userRepo.save(existingUser);
        LdapName newMemberDn = toAbsoluteDn(savedUser.getId());

        if(!originalId.equals(savedUser.getId())) {
            // The user has moved - we need to update group references.
            updateGroupReferences(groups, oldMemberDn, newMemberDn);
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

    public List<User> searchByNameName(String lastName) {
        return userRepo.findByFullNameContains(lastName);
    }

    @Autowired
    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new FileSystemXmlApplicationContext("classpath*:/applicationContext.xml");
        UserService userService = (UserService)applicationContext.getBean("userService");
        userService.findAll();
    }
}
