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

package com.ericsson.oss.ae.acm.core.services.jobs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Class for the ThreadPoolTaskExecutor objects for the App LCM monitoring jobs.
 */
@Configuration
@EnableAsync
public class MonitoringJobExecutor {

    @Bean(name = "threadPoolTaskExecutorForInitializeAppMonitoringJob")
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForInitializeAppMonitoringJob() {
        return this.generateThreadPoolTaskExecutor(
                3,
                3,
                true,
                25,
                "threadPoolTaskExecutorForInitializeAppMonitoringJob"
        );
    }

    @Bean(name = "threadPoolTaskExecutorForDeInitializeAppMonitoringJob")
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForDeInitializeAppMonitoringJob() {
        return this.generateThreadPoolTaskExecutor(
            3,
            3,
            true,
            25,
            "threadPoolTaskExecutorForDeInitializeAppMonitoringJob"
        );
    }
    @Bean(name = "threadPoolTaskExecutorForDeployAppInstanceMonitoringJob")
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForDeployAppInstanceMonitoringJob() {
        return this.generateThreadPoolTaskExecutor(
                3,
                3,
                true,
                25,
                "threadPoolTaskExecutorForDeployAppInstanceMonitoringJob"
        );
    }

    @Bean(name = "threadPoolTaskExecutorForUndeployAppInstanceMonitoringJob")
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForUndeployAppInstanceMonitoringJob() {
        return this.generateThreadPoolTaskExecutor(
                3,
                3,
                true,
                25,
                "threadPoolTaskExecutorForUndeployAppInstanceMonitoringJob"
        );
    }

    @Bean(name = "threadPoolTaskExecutorForDeleteAppInstanceMonitoringJob")
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForDeleteAppInstanceMonitoringJob() {
        return this.generateThreadPoolTaskExecutor(
                3,
                3,
                true,
                25,
                "threadPoolTaskExecutorForDeleteAppInstanceMonitoringJob"
        );
    }

    @Bean(name = "threadPoolTaskExecutorForDeleteAppMonitoringJob")
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForDeleteAppMonitoringJob() {
        return this.generateThreadPoolTaskExecutor(
                3,
                3,
                true,
                25,
                "threadPoolTaskExecutorForDeleteAppMonitoringJob"
        );
    }

    @Bean(name = "threadPoolTaskExecutorForUpgradeAppInstanceMonitoringJob")
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForUpgradeAppInstanceMonitoringJob() {
        return this.generateThreadPoolTaskExecutor(
            3,
            3,
            true,
            25,
            "threadPoolTaskExecutorForUpgradeAppInstanceMonitoringJob"
        );
    }


    @Bean(name = "threadPoolTaskExecutorForUpdateAppInstanceMonitoringJob")
    public ThreadPoolTaskExecutor threadPoolTaskExecutorForUpdateAppInstanceMonitoringJob() {
        return this.generateThreadPoolTaskExecutor(
            3,
            3,
            true,
            25,
            "threadPoolTaskExecutorForUpdateAppInstanceMonitoringJob"
        );
    }

    private ThreadPoolTaskExecutor generateThreadPoolTaskExecutor(final int corePoolSize, final int maxPoolSize,
                                                                  final boolean waitForTasksToCompleteOnShutdown,
                                                                  final int queueCapacity,
                                                                  final String threadPrefixName) {
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setThreadNamePrefix(threadPrefixName);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
