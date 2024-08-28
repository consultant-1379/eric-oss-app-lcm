/*******************************************************************************
 * COPYRIGHT Ericsson 2021
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.oss.ae.clients.apponboarding.dto.ArtifactDto;
import com.ericsson.oss.ae.clients.apponboarding.model.Artifact;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { ArtifactMapper.class })
public class ArtifactMapperTest {
    private final ArtifactMapper artifactMapper = new ArtifactMapper();

    @Test
    public void givenAValidAppOnboardingArtifactDto_WhenMappingToArtifact_thenArtifactShouldContainCorrectValues() {
        final Artifact actualArtifact = artifactMapper.map(
                new ArtifactDto().id(1L).name("app-lcm").type("HELM").version("1.1.0").location("localhost").status(ArtifactDto.StatusEnum.COMPLETED),
                Artifact.class);

        assertThat(actualArtifact.getId()).isEqualTo(1);
        assertThat(actualArtifact.getName()).isEqualTo("app-lcm");
        assertThat(actualArtifact.getType()).isEqualTo("HELM");
        assertThat(actualArtifact.getVersion()).isEqualTo("1.1.0");
        assertThat(actualArtifact.getStatus()).isEqualTo(ArtifactDto.StatusEnum.COMPLETED.getValue());
        assertThat(actualArtifact.getLocation()).isEqualTo("localhost");
    }

    @Test
    public void givenAValidArtifact_WhenMappingToArtifactDto_thenArtifactDtoShouldContainCorrectValues() {
        final ArtifactDto actualArtifactDto = artifactMapper.map(Artifact.builder().id(1L).name("app-lcm").type("HELM").version("1.1.0")
                .location("localhost").status(ArtifactDto.StatusEnum.COMPLETED.getValue()).build(), ArtifactDto.class);

        assertThat(actualArtifactDto.getId()).isEqualTo(1);
        assertThat(actualArtifactDto.getName()).isEqualTo("app-lcm");
        assertThat(actualArtifactDto.getType()).isEqualTo("HELM");
        assertThat(actualArtifactDto.getVersion()).isEqualTo("1.1.0");
        assertThat(actualArtifactDto.getStatus()).isEqualTo(ArtifactDto.StatusEnum.COMPLETED);
        assertThat(actualArtifactDto.getLocation()).isEqualTo("localhost");
    }
}
