package com.danielfrak.code.keycloak.providers.rest.remote;

import com.danielfrak.code.keycloak.providers.rest.kafka.KafkaLegacyUser;
import com.danielfrak.code.keycloak.providers.rest.kafka.model.UserProfileDto;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static com.danielfrak.code.keycloak.providers.rest.ConfigurationProperties.*;
import static org.keycloak.models.UserModel.RequiredAction.UPDATE_PASSWORD;

public class UserModelFactory {

    private static final Logger LOG = Logger.getLogger(UserModelFactory.class);

    private final KeycloakSession session;
    private final ComponentModel model;

    /**
     * String format:
     * legacyRole:newRole
     */
    private final Map<String, String> roleMap;
    /**
     * String format:
     * legacyGroup:newGroup
     */
    private final Map<String, String> groupMap;

    public UserModelFactory(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
        this.roleMap = legacyMap(model, ROLE_MAP_PROPERTY);
        this.groupMap = legacyMap(model, GROUP_MAP_PROPERTY);
    }

    /**
     * Returns a map of legacy props to new one
     */
    private Map<String, String> legacyMap(ComponentModel model, String property) {
        Map<String, String> newRoleMap = new HashMap<>();
        List<String> pairs = model.getConfig().getList(property);
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            newRoleMap.put(keyValue[0], keyValue[1]);
        }
        return newRoleMap;
    }

    public UserModel create(LegacyUser legacyUser, RealmModel realm) {
        LOG.infof("Creating user model for: %s", legacyUser.getUsername());

        UserModel userModel;
        if (isEmpty(legacyUser.getId())) {
            userModel = session.userLocalStorage().addUser(realm, legacyUser.getUsername());
        } else {
            userModel = session.userLocalStorage().addUser(
                    realm,
                    legacyUser.getId(),
                    legacyUser.getUsername(),
                    true,
                    false
            );
        }

        validateUsernamesEqual(legacyUser, userModel);

        userModel.setFederationLink(model.getId());
        userModel.setEnabled(legacyUser.isEnabled());
        userModel.setEmail(legacyUser.getEmail());
        userModel.setEmailVerified(legacyUser.isEmailVerified());
        userModel.setFirstName(legacyUser.getFirstName());
        userModel.setLastName(legacyUser.getLastName());

        if (legacyUser.getAttributes() != null) {
            legacyUser.getAttributes()
                    .forEach(userModel::setAttribute);
        }

        getRoleModels(legacyUser, realm)
                .forEach(userModel::grantRole);

        getGroupModels(legacyUser, realm)
                .forEach(userModel::joinGroup);

        if (legacyUser.getLegacyUserData() != null && !legacyUser.getLegacyUserData().isEmpty()) {
            KafkaLegacyUser.publishEvent(getLegacyUser(userModel.getId(), legacyUser));
        }

        userModel.addRequiredAction(UPDATE_PASSWORD);

        return userModel;
    }

    private void validateUsernamesEqual(LegacyUser legacyUser, UserModel userModel) {
        if (!userModel.getUsername().equals(legacyUser.getUsername())) {
            throw new IllegalStateException(String.format("Local and remote users differ: [%s != %s]",
                    userModel.getUsername(),
                    legacyUser.getUsername()));
        }
    }

    private Stream<RoleModel> getRoleModels(LegacyUser legacyUser, RealmModel realm) {
        if (legacyUser.getRoles() == null) {
            return Stream.empty();
        }
        return legacyUser.getRoles().stream()
                .map(r -> getRoleModel(realm, r))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<RoleModel> getRoleModel(RealmModel realm, String role) {
        if (roleMap.containsKey(role)) {
            role = roleMap.get(role);
        } else if (isConfigDisabled(MIGRATE_UNMAPPED_ROLES_PROPERTY)) {
            return Optional.empty();
        }
        if (isEmpty(role)) {
            return Optional.empty();
        }
        RoleModel roleModel = realm.getRole(role);
        return Optional.ofNullable(roleModel);
    }

    private boolean isConfigDisabled(String config) {
        return !Boolean.parseBoolean(model.getConfig().getFirst(config));
    }

    private boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }

    private Stream<GroupModel> getGroupModels(LegacyUser legacyUser, RealmModel realm) {
        if (legacyUser.getGroups() == null) {
            return Stream.empty();
        }

        return legacyUser.getGroups().stream()
                .map(group -> getGroupModel(realm, group))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<GroupModel> getGroupModel(RealmModel realm, String groupName) {
        if (groupMap.containsKey(groupName)) {
            groupName = groupMap.get(groupName);
        } else if (isConfigDisabled(MIGRATE_UNMAPPED_GROUPS_PROPERTY)) {
            return Optional.empty();
        }
        if (isEmpty(groupName)) {
            return Optional.empty();
        }

        final String effectiveGroupName = groupName;
        Optional<GroupModel> group = realm.getGroups().stream()
                .filter(g -> effectiveGroupName.equalsIgnoreCase(g.getName())).findFirst();

        GroupModel realmGroup = group
                .map(g -> {
                    LOG.infof("Found existing group %s with id %s", g.getName(), g.getId());
                    return g;
                })
                .orElseGet(() -> {
                    GroupModel newGroup = realm.createGroup(effectiveGroupName);
                    LOG.infof("Created group %s with id %s", newGroup.getName(), newGroup.getId());
                    return newGroup;
                });

        return Optional.of(realmGroup);
    }

    private UserProfileDto getLegacyUser(String userId, LegacyUser legacyUser) {
        var userProfileDto = new UserProfileDto();
        userProfileDto.setUserId(UUID.fromString(userId));
        userProfileDto.setFirstName(legacyUser.getLegacyUserData().get("firstName"));
        userProfileDto.setLastName(legacyUser.getLegacyUserData().get("lastName"));
        userProfileDto.setPhoneNumber(legacyUser.getLegacyUserData().get("phoneNumber"));

        if (legacyUser.getLegacyUserData().get("birthDate").isEmpty()) {
            userProfileDto.setBirthDate(null);
        } else {
            userProfileDto.setBirthDate(LocalDate.parse(legacyUser.getLegacyUserData().get("birthDate")).toString());
        }

        userProfileDto.setAddress(legacyUser.getLegacyUserData().get("address"));
        userProfileDto.setCity(legacyUser.getLegacyUserData().get("city"));
        userProfileDto.setPhotoPath(legacyUser.getLegacyUserData().get("photoPath"));
        userProfileDto.setLinkedinUrl(legacyUser.getLegacyUserData().get("linkedinUrl"));
        userProfileDto.setZipCode(legacyUser.getLegacyUserData().get("zipCode"));
        userProfileDto.setCvPath(legacyUser.getLegacyUserData().get("cvPath"));
        userProfileDto.setProfession(legacyUser.getLegacyUserData().get("profession"));
        userProfileDto.setLastEducationPlace(legacyUser.getLegacyUserData().get("lastEducationPlace"));
        userProfileDto.setSubscribeNewsletter(Boolean.parseBoolean(legacyUser.getLegacyUserData().get("isSubscribeNewsletter")));
        userProfileDto.setDarkMode(Boolean.parseBoolean(legacyUser.getLegacyUserData().get("darkMode")));
        userProfileDto.setReferralCode(legacyUser.getLegacyUserData().get("referralCode"));
        userProfileDto.setReferredBy(legacyUser.getLegacyUserData().get("referredBy"));
        return userProfileDto;
    }
}
