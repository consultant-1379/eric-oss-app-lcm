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

package com.ericsson.oss.ae.acm.core.services;

import static com.ericsson.oss.ae.acm.TestConstants.APP_VERSION_1_1_1;
import static com.ericsson.oss.ae.acm.TestConstants.POLICY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ericsson.oss.ae.AppLcmApplication;
import com.ericsson.oss.ae.acm.TestUtils;
import com.ericsson.oss.ae.acm.clients.acmr.AcmService;
import com.ericsson.oss.ae.acm.clients.acmr.common.AcmFileGenerator;
import com.ericsson.oss.ae.acm.clients.acmr.dto.AcCommissionResponse;
import com.ericsson.oss.ae.acm.clients.acmr.dto.ToscaIdentifier;
import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.repository.AppRepository;
import com.ericsson.oss.ae.acm.presentation.mapper.AppDetailsMapper;
import com.ericsson.oss.ae.v3.api.model.AppDetails;
import com.ericsson.oss.ae.v3.api.model.CreateAppRequest;

@SpringBootTest(classes = {AppLcmApplication.class, AppServiceImpl.class})
@ExtendWith(SpringExtension.class)
public class AppsServiceImplTest {

    private static final String TEMPLATE = "template";

    @Mock
    private AcmFileGenerator acmFileGenerator;

    @Mock private AppRepository appRepository;

    @Mock
    private AppDetailsMapper appDetailsMapper;

    @Mock
    private AcmService acmService;

    @InjectMocks
    private AppServiceImpl appsServiceImplUnderTest;

    @Test
    public void testCreateApp() {

        final CreateAppRequest createAppRequest = TestUtils.generateCreateAppRequest();
        final App app = TestUtils.generateAppEntity();
        final AppDetails expectedResult = TestUtils.createApp();

        when(appDetailsMapper.toApp(createAppRequest)).thenReturn(app);
        when(appDetailsMapper.createAppResponseFromApp(any())).thenReturn(expectedResult);
        when(acmFileGenerator.generateToscaServiceTemplate(app)).thenReturn(TEMPLATE);

        ToscaIdentifier affectedAutomationComposition = new ToscaIdentifier(POLICY, APP_VERSION_1_1_1);
        List<ToscaIdentifier> affectedAutomationCompositions = List.of(affectedAutomationComposition);
        AcCommissionResponse acmCreateCompositionResponse = new AcCommissionResponse(UUID.randomUUID(), affectedAutomationCompositions);

        when(acmService.commissionAutomationCompositionType("template")).thenReturn(acmCreateCompositionResponse);
        final AppDetails result = appsServiceImplUnderTest.createApp(createAppRequest);

        assertThat(result).isEqualTo(expectedResult);
    }
}
