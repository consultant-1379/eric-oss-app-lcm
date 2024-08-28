/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.ae.acm.clients.keycloak.common;

import static com.ericsson.oss.ae.acm.common.constant.AppLcmError.KEYCLOAK_ROLES_NOT_FOUND_ERROR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ericsson.oss.ae.acm.clients.keycloak.dto.ClientRoleDTO;
import com.ericsson.oss.ae.acm.common.exception.AppLcmException;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.Role;

/**
 * Class for comparing roles in Keycloak.
 */
@Slf4j
public class RoleUtil {

    public static final String ADD = "ADD";
    public static final String DEL = "DEL";

    /**
     * Private constructor
     */
    private RoleUtil() {
    }

    /**
     * The method is a utility to verify that all roles are present
     *
     * @param clientRoles
     * @param app
     * @return boolean
     */
    public static boolean areAllRolesPresent(final ClientRoleDTO[] clientRoles, final App app) {
        log.debug("Check all the roles are present or not");
        final List<String> keycloakRoleNameList = Arrays.stream(clientRoles).map(ClientRoleDTO::getName).collect(Collectors.toList());
        final List<String> rolesToBeAssociated = app.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        return new HashSet<>(keycloakRoleNameList).containsAll(rolesToBeAssociated);
    }

    /**
     * The method will accept a list of roles extracted from keycloak and a list of roles that are required to be possibly added.
     * It will verify that all the roles of app descriptor(@RolesDto) exist in keycloak(@roleInKeyclaok) and will
     *
     * @param roles
     * @param rolesInKeycloak
     * @return
     */
    public static List<ClientRoleDTO> validateAndExtractRoles(final List<Role> roles, final ResponseEntity<ClientRoleDTO[]> rolesInKeycloak) {
        log.debug("Validate and extract roles");
        final List<ClientRoleDTO> rolesConverted = Arrays.stream(Optional.ofNullable(rolesInKeycloak.getBody())
                        .orElseThrow())
                .filter(role -> roles
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toList())
                        .contains(role.getName()))
                .collect(Collectors.toList());

        if (roles.size() != rolesConverted.size()) {
            log.error("Roles not found in keycloak");
            throw new AppLcmException(HttpStatus.BAD_REQUEST, KEYCLOAK_ROLES_NOT_FOUND_ERROR);
        }
        return rolesConverted;
    }

    /**
     * The method will create a map that contains all roles that need to be associated and all roles that need to be deassociated
     * @param newAppDescriptorRoles
     * @param oldAppDescriptorRoles
     * @return map of roles to be associated and deassociated
     */
    public static Map<String, List<Role>> roleDiff(final App newAppDescriptorRoles, final App oldAppDescriptorRoles) {
        log.debug("Map new and old roles");
        List<Role> roleToDeAssociate = Optional.ofNullable(oldAppDescriptorRoles.getRoles()).orElse(new ArrayList<>()).stream().filter(role -> !(Optional.of(newAppDescriptorRoles.getRoles()).orElse(new ArrayList<>()).contains(role))).collect(Collectors.toList());
        List<Role> roleToAssociate = Optional.ofNullable(newAppDescriptorRoles.getRoles()).orElse(new ArrayList<>()).stream().filter(role -> !(Optional.of(oldAppDescriptorRoles.getRoles()).orElse(new ArrayList<>()).contains(role))).collect(Collectors.toList());
        Map<String, List<Role>> roleDiffMap = new HashMap<>();
        roleDiffMap.put(DEL, roleToDeAssociate);
        roleDiffMap.put(ADD, roleToAssociate);
        return roleDiffMap;
    }

}
