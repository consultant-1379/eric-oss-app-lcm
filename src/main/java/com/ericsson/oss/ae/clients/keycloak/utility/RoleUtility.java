/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.ae.clients.keycloak.utility;

import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import com.ericsson.oss.ae.clients.keycloak.KeycloakClient;
import com.ericsson.oss.ae.clients.keycloak.dto.ClientDto;
import com.ericsson.oss.ae.clients.keycloak.dto.ClientRoleDTO;
import com.ericsson.oss.ae.clients.keycloak.dto.RoleDto;
import com.ericsson.oss.ae.clients.keycloak.dto.ServiceAccountDto;
import com.ericsson.oss.ae.presentation.exceptions.AppLcmError;
import com.ericsson.oss.ae.presentation.exceptions.KeycloakException;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;

public class RoleUtility {

    public static final String add = "ADD";
    public static final String del = "DEL";

    /**
     * The method will create a map that contain all roles that will need to be associated and all roles  that need to be deassociated
     * @param newAppDescriptorRolesDto
     * @param oldAppDescriptorRolesDto
     * @return
     */
    public static Map<String, List<RoleDto>> roleDiff(AppDto newAppDescriptorRolesDto, AppDto oldAppDescriptorRolesDto) {
        List<RoleDto> roleToAssociate = Optional.ofNullable(newAppDescriptorRolesDto.getRoles()).orElse(new ArrayList<>()).stream().filter(role -> !(Optional.of(oldAppDescriptorRolesDto.getRoles()).orElse(new ArrayList<>()).contains(role))).collect(Collectors.toList());
        List<RoleDto> roleToDeAssociate = Optional.ofNullable(oldAppDescriptorRolesDto.getRoles()).orElse(new ArrayList<>()).stream().filter(role -> !(Optional.of(newAppDescriptorRolesDto.getRoles()).orElse(new ArrayList<>()).contains(role))).collect(Collectors.toList());
        Map<String, List<RoleDto>> roleDiffMap = new HashMap<>();
        roleDiffMap.put(add, roleToAssociate);
        roleDiffMap.put(del, roleToDeAssociate);
        return roleDiffMap;
    }

    /**
     * The method is used to associate all new roles present in appdescriptor but not in keyclaok and to remove all roles previously associated not present anymore in appdescriptor
     * @param roleDiff
     * @param keycloakClient
     * @param clientId
     */
    public static void updateClientRoles(Map<String, List<RoleDto>> roleDiff, KeycloakClient keycloakClient, String clientId ) {
        List<RoleDto> rolesToAssociate = roleDiff.get(add);
        List<RoleDto> rolesToDeAssociate = roleDiff.get(del);
        ResponseEntity<ClientDto[]> clientArray = keycloakClient.getClients();
        String id = Arrays.stream(Optional.ofNullable(clientArray.getBody()).orElse(new ClientDto[]{})).filter(cl -> clientId.equals(cl.getClientId())).map(ClientDto::getId).findFirst().orElse("");
        ResponseEntity<ClientRoleDTO[]> clientRoleDtoArrayResponseEntity = keycloakClient.extractRoles();
        List<ClientRoleDTO> clientRoleToAssociateList = validateAndExtractRoles(rolesToAssociate, clientRoleDtoArrayResponseEntity);
        List<ClientRoleDTO> clientRoleToDeAssociateList = validateAndExtractRoles(rolesToDeAssociate, clientRoleDtoArrayResponseEntity);
        ResponseEntity<ServiceAccountDto> serviceAccountDtoResponseEntity = keycloakClient.getServiceAccount(id);
        String serviceAccount = Optional.ofNullable(serviceAccountDtoResponseEntity.getBody()).orElseThrow(() -> new KeycloakException(AppLcmError.APP_LCM_GET_SERVICE_ACCOUNT, "Error retriving service account", "")).getId();
        if (clientRoleToAssociateList.size() > 0) {
            keycloakClient.associateRoles(clientRoleToAssociateList, serviceAccount);
        }

        if (rolesToDeAssociate.size() > 0) {
            keycloakClient.deassociateRoles(clientRoleToDeAssociateList, serviceAccount);
        }
    }

    /**
     * The method is a utility to verify that all roles are present
     * @param clientRoleDTOS
     * @param appDto
     * @return
     */
    public static boolean areAllRolesPresent(ClientRoleDTO[] clientRoleDTOS, AppDto appDto) {
        List<String> keycloakRoleNameList = Arrays.stream(clientRoleDTOS).map(ClientRoleDTO::getName).collect(Collectors.toList());
        List<String> rolesToBeAssociated = appDto.getRoles().stream().map(RoleDto::getName).collect(Collectors.toList());
        return new HashSet<>(keycloakRoleNameList).containsAll(rolesToBeAssociated);
    }

    /**
     * The method will accept a list of role extracted from keycloak and a list of role that are required to be possibily added.
     * It will verify that all the roles of app descriptor(@RolesDto) exist in keycloak(@roleInKeyclaok) and will
     * @param rolesDto
     * @param rolesInKeycloak
     * @return
     */
    public static List<ClientRoleDTO> validateAndExtractRoles(List<RoleDto> rolesDto, ResponseEntity<ClientRoleDTO[]> rolesInKeycloak) {
        List<ClientRoleDTO> rolesConverted = Arrays.stream(Optional.ofNullable(rolesInKeycloak.getBody())
                        .orElseThrow())
                .filter(role -> rolesDto
                        .stream()
                        .map(RoleDto::getName)
                        .collect(Collectors.toList())
                        .contains(role.getName()))
                .collect(Collectors.toList());

        if (rolesDto.size() != rolesConverted.size()) {
            throw new KeycloakException(AppLcmError.APP_LCM_ROLE_CONVERTED_NOT_FOUND, "One of the roles supplied in app descriptor does not exist in keyclaok", "");
        }
        return rolesConverted;
    }
}
