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

package com.ericsson.oss.ae.utils.roleutils;

import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import com.ericsson.oss.ae.clients.keycloak.dto.ClientRoleDTO;
import com.ericsson.oss.ae.clients.keycloak.dto.RoleDto;
import com.ericsson.oss.ae.clients.keycloak.utility.RoleUtility;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoleUtilityTest {

    @Test
    void  given_NoRolesInPreviousAndNewAppDescriptor_when_diffFunctionIsCalled_then_mapIsEmpty() {
        List<RoleDto> roleDtoList = new ArrayList<>();

        AppDto appDtoOldAppDescriptor = new AppDto();
        appDtoOldAppDescriptor.setRoles(roleDtoList);

        AppDto appDtoNewAppDescriptor = new AppDto();
        appDtoNewAppDescriptor.setRoles(roleDtoList);

        Map<String, List<RoleDto>> listMap = RoleUtility.roleDiff(appDtoNewAppDescriptor, appDtoOldAppDescriptor);

        assertThat(listMap.get(RoleUtility.add).size()).isEqualTo(0);
        assertThat(listMap.get(RoleUtility.del).size()).isEqualTo(0);
    }

    @Test
    void  given_RolesInPreviousAndNewAppDescriptor_when_TheyAreEquals_then_mapIsEmpty() {
        RoleDto adminRole = new RoleDto("admin");
        RoleDto userRole = new RoleDto("user");
        List<RoleDto> roleDtoList = new ArrayList<>();
        roleDtoList.add(adminRole);
        roleDtoList.add(userRole);

        AppDto appDtoOldAppDescriptor = new AppDto();
        appDtoOldAppDescriptor.setRoles(roleDtoList);

        AppDto appDtoNewAppDescriptor = new AppDto();
        appDtoNewAppDescriptor.setRoles(roleDtoList);

        Map<String, List<RoleDto>> listMap = RoleUtility.roleDiff(appDtoNewAppDescriptor, appDtoOldAppDescriptor);

        assertThat(listMap.get(RoleUtility.add).size()).isEqualTo(0);
        assertThat(listMap.get(RoleUtility.del).size()).isEqualTo(0);
    }

    @Test
    void given_RolesInPreviousAndNewAppDescriptor_when_OldAppDescriptorHasOneMoreRole_then_mapContainsOneRoleToDisassociate() {
        RoleDto adminRole = new RoleDto("admin");
        RoleDto userRole = new RoleDto("user");
        List<RoleDto> roleDtoListForOldAppDescriptor = new ArrayList<>();
        roleDtoListForOldAppDescriptor.add(adminRole);
        roleDtoListForOldAppDescriptor.add(userRole);

        AppDto appDtoOldAppDescriptor = new AppDto();
        appDtoOldAppDescriptor.setRoles(roleDtoListForOldAppDescriptor);

        List<RoleDto> roleDtoListForNewAppDescriptor = new ArrayList<>();
        roleDtoListForNewAppDescriptor.add(adminRole);

        AppDto appDtoNewAppDescriptor = new AppDto();
        appDtoNewAppDescriptor.setRoles(roleDtoListForNewAppDescriptor);



        Map<String, List<RoleDto>> listMap = RoleUtility.roleDiff(appDtoNewAppDescriptor, appDtoOldAppDescriptor);

        assertThat(listMap.get(RoleUtility.add).size()).isEqualTo(0);
        assertThat(listMap.get(RoleUtility.del).size()).isEqualTo(1);
        assertThat(listMap.get(RoleUtility.del).get(0).getName()).isEqualTo("user");
    }

    @Test
    void given_RolesInPreviousAndNewAppDescriptor_when_OldAppDescriptorHasOneLessRole_then_mapContainsOneRoleToAssociate() {
        RoleDto adminRole = new RoleDto("admin");
        RoleDto userRole = new RoleDto("user");
        List<RoleDto> roleDtoListForOldAppDescriptor = new ArrayList<>();
        roleDtoListForOldAppDescriptor.add(adminRole);


        AppDto appDtoOldAppDescriptor = new AppDto();
        appDtoOldAppDescriptor.setRoles(roleDtoListForOldAppDescriptor);

        List<RoleDto> roleDtoListForNewAppDescriptor = new ArrayList<>();
        roleDtoListForNewAppDescriptor.add(adminRole);
        roleDtoListForNewAppDescriptor.add(userRole);

        AppDto appDtoNewAppDescriptor = new AppDto();
        appDtoNewAppDescriptor.setRoles(roleDtoListForNewAppDescriptor);

        Map<String, List<RoleDto>> listMap = RoleUtility.roleDiff(appDtoNewAppDescriptor, appDtoOldAppDescriptor);

        assertThat(listMap.get(RoleUtility.add).size()).isEqualTo(1);
        assertThat(listMap.get(RoleUtility.del).size()).isEqualTo(0);
        assertThat(listMap.get(RoleUtility.add).get(0).getName()).isEqualTo("user");
    }

    @Test
    void given_listOfRolesFromKeycloakAndListOfRolesFromAppDescriptor_when_AllRoleInAppDescriptorArePresent_then_returnTrue() {
        ClientRoleDTO clientRoleDTO = new ClientRoleDTO();
        clientRoleDTO.setId("id1");
        clientRoleDTO.setName("admin");

        ClientRoleDTO clientRoleDTO2 = new ClientRoleDTO();
        clientRoleDTO2.setId("id2");
        clientRoleDTO2.setName("user");

        ClientRoleDTO[] clientRoleDTOS = new ClientRoleDTO[]{clientRoleDTO,clientRoleDTO2};

        RoleDto role = new RoleDto();
        role.setName("admin");

        RoleDto role2 = new RoleDto();
        role2.setName("user");

        List<RoleDto> roleDtoList = new ArrayList<>();
        roleDtoList.add(role);
        roleDtoList.add(role2);

        AppDto appDto = new AppDto();
        appDto.setRoles(roleDtoList);

        assertThat(RoleUtility.areAllRolesPresent(clientRoleDTOS, appDto)).isTrue();
    }

    @Test
    void given_listOfRolesFromKeycloakAndListOfRolesFromAppDescriptor_when_NotAllRoleInAppDescriptorArePresentInKeycloak_then_returnFalse() {
        ClientRoleDTO clientRoleDTO = new ClientRoleDTO();
        clientRoleDTO.setId("id1");
        clientRoleDTO.setName("admin");

        ClientRoleDTO clientRoleDTO2 = new ClientRoleDTO();
        clientRoleDTO2.setId("id2");
        clientRoleDTO2.setName("user");

        ClientRoleDTO[] clientRoleDTOS = new ClientRoleDTO[]{clientRoleDTO,clientRoleDTO2};

        RoleDto role = new RoleDto();
        role.setName("admin2");

        RoleDto role2 = new RoleDto();
        role2.setName("user2");

        List<RoleDto> roleDtoList = new ArrayList<>();
        roleDtoList.add(role);
        roleDtoList.add(role2);

        AppDto appDto = new AppDto();
        appDto.setRoles(roleDtoList);

        assertThat(RoleUtility.areAllRolesPresent(clientRoleDTOS, appDto)).isFalse();
    }

}
