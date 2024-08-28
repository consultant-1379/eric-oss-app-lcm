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

package com.ericsson.oss.ae.clients.apponboarding.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.oss.ae.clients.apponboarding.dto.AppDto;
import com.ericsson.oss.ae.clients.apponboarding.dto.ArtifactDto;
import com.ericsson.oss.ae.clients.apponboarding.model.App;
import com.ericsson.oss.ae.clients.apponboarding.model.Artifact;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { AppMapper.class })
public class AppMapperTest {

    private final AppMapper appMapper = new AppMapper();
    private final ArtifactMapper artifactMapper = new ArtifactMapper();

    @Test
    public void givenAValidApp_WhenMappingToAppDto_thenAppDtoShouldContainCorrectValues() {
        final App expectedApp = createApp();
        final AppDto actualAppDto = appMapper.map(expectedApp, AppDto.class);

        assertThat(actualAppDto.getId()).isEqualTo(1);
        assertThat(actualAppDto.getUsername()).isEqualTo("username");
        assertThat(actualAppDto.getName()).isEqualTo("name");
        assertThat(actualAppDto.getVersion()).isEqualTo("version");
        assertThat(actualAppDto.getSize()).isEqualTo("size");
        assertThat(actualAppDto.getVendor()).isEqualTo("vendor");
        assertThat(actualAppDto.getType()).isEqualTo("type");
        assertThat(actualAppDto.getOnboardedDate()).isEqualTo("onboardedDate");
        assertThat(actualAppDto.getStatus()).isEqualTo("status");
        assertThat(actualAppDto.getArtifacts()).isEqualTo(List.of(createArtifactDto()));
    }

    @Test
    public void givenAValidAppDto_WhenMappingToApp_thenAppShouldContainCorrectValues() {
        final AppDto expectedAppDto = createAppDto();
        final App actualApp = appMapper.map(expectedAppDto, App.class);

        assertThat(actualApp.getId()).isEqualTo(1);
        assertThat(actualApp.getUsername()).isEqualTo("username");
        assertThat(actualApp.getName()).isEqualTo("name");
        assertThat(actualApp.getVersion()).isEqualTo("version");
        assertThat(actualApp.getSize()).isEqualTo("size");
        assertThat(actualApp.getVendor()).isEqualTo("vendor");
        assertThat(actualApp.getType()).isEqualTo("type");
        assertThat(actualApp.getOnboardedDate()).isEqualTo("onboardedDate");
        assertThat(actualApp.getStatus()).isEqualTo("status");
        assertThat(actualApp.getArtifacts()).isEqualTo(List.of(createArtifact()));
    }

    private App createApp() {
        final Artifact artifact = createArtifact();
        return App.builder().id(1L).name("name").size("size").status("status").type("type").descriptorInfo("descriptorInfo").version("version")
                .onboardedDate("onboardedDate").username("username").vendor("vendor").artifacts(List.of(artifact)).build();
    }

    private Artifact createArtifact() {
        return Artifact.builder().id(1L).name("name").type("type").version("version").location("location").status("COMPLETED").build();
    }

    private AppDto createAppDto() {
        final AppDto appDto = new AppDto();
        appDto.setId(1L);
        appDto.setName("name");
        appDto.setSize("size");
        appDto.setStatus("status");
        appDto.setType("type");
        appDto.setVersion("version");
        appDto.setOnboardedDate("onboardedDate");
        appDto.setUsername("username");
        appDto.setVendor("vendor");
        appDto.setArtifacts(List.of(createArtifactDto()));
        return appDto;
    }

    private ArtifactDto createArtifactDto() {
        final ArtifactDto artifactDto = new ArtifactDto();
        return artifactDto.id(1L).name("name").type("type").version("version").location("location").status(ArtifactDto.StatusEnum.COMPLETED);
    }
}
