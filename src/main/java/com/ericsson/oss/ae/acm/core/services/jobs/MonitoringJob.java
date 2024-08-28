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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import com.ericsson.oss.ae.acm.persistence.entity.App;
import com.ericsson.oss.ae.acm.persistence.entity.AppInstances;

/**
 * Abstract class for a monitoring job in App LCM.
 */
@Slf4j
@Getter
@Setter
@Component
public abstract class MonitoringJob {
    protected ThreadPoolTaskExecutor threadPoolTaskExecutor;
    protected Long timeout;
    private final Map<UUID, Instant> firstPollingTimes = Maps.newConcurrentMap();
    private final Set<UUID> pollingCache = Sets.newConcurrentHashSet();

    protected abstract List<Object> getDatabaseEntitiesForPolling();

    protected abstract boolean pollAcmForState(final Object databaseEntity);

    /**
     * Scheduled method that kicks off the polling flow.
     */
    public void execute() {
        final List<Object> databaseEntitiesForPolling = this.getDatabaseEntitiesForPolling();

        for (final Object databaseEntity : databaseEntitiesForPolling) {
            if (!this.isPollingCacheFull() && !this.isDatabaseEntityInPollingCache(databaseEntity)) {
                if (this.isFirstPollRequest(databaseEntity)) {
                    this.saveTimeForFirstPollRequest(databaseEntity);
                }

                this.addDatabaseEntityToPollingCache(databaseEntity);
                this.handleAcmPolling(databaseEntity);
            }
        }
    }

    private synchronized boolean isPollingCacheFull() {
        return this.pollingCache.size() >= this.threadPoolTaskExecutor.getMaxPoolSize();
    }

    private synchronized boolean isDatabaseEntityInPollingCache(final Object databaseEntity) {
        final UUID databaseEntityId = this.getDatabaseEntityId(databaseEntity);

        return this.pollingCache.contains(databaseEntityId);
    }

    private synchronized boolean isFirstPollRequest(final Object databaseEntity) {
        final UUID databaseEntityId = this.getDatabaseEntityId(databaseEntity);

        return !this.firstPollingTimes.containsKey(databaseEntityId);
    }

    private synchronized void saveTimeForFirstPollRequest(final Object databaseEntity) {
        final UUID databaseEntityId = this.getDatabaseEntityId(databaseEntity);

        this.firstPollingTimes.put(databaseEntityId, Instant.now());
    }

    private synchronized void removeTimeForFirstPollRequest(final Object databaseEntity) {
        final UUID databaseEntityId = this.getDatabaseEntityId(databaseEntity);

        this.firstPollingTimes.remove(databaseEntityId);
    }

    private synchronized void addDatabaseEntityToPollingCache(final Object databaseEntity) {
        final UUID databaseEntityId = this.getDatabaseEntityId(databaseEntity);
        final String databaseEntityType = this.getDatabaseEntityType(databaseEntity);

        this.pollingCache.add(databaseEntityId);
        log.debug("{} added to the polling cache with id: {}", databaseEntityType, databaseEntityId);
    }

    private void handleAcmPolling(final Object databaseEntity) {
        CompletableFuture.supplyAsync(
                () -> this.pollAcmForState(databaseEntity), this.threadPoolTaskExecutor
        ).thenApply(isCompleted -> {
            if (isCompleted) {
                this.removeTimeForFirstPollRequest(databaseEntity);
            }
            return isCompleted;
        }).thenRun(() -> this.removeDatabaseEntityFromPollingCache(databaseEntity));
    }

    private UUID getDatabaseEntityId(final Object databaseEntity) {
        if (databaseEntity.getClass() == App.class) {
            return ((App) databaseEntity).getId();
        } else {
            return ((AppInstances) databaseEntity).getId();
        }
    }

    private String getDatabaseEntityType(final Object databaseEntity) {
        if (databaseEntity.getClass() == App.class) {
            return "app";
        } else {
            return "app instance";
        }
    }

    private synchronized void removeDatabaseEntityFromPollingCache(final Object databaseEntity) {
        final UUID databaseEntityId = this.getDatabaseEntityId(databaseEntity);
        final String databaseEntityType = this.getDatabaseEntityType(databaseEntity);

        this.pollingCache.remove(databaseEntityId);
        log.debug("{} removed from the polling cache with id: {}", databaseEntityType, databaseEntityId);

    }

    protected boolean hasPollAttemptTimedOut(final Object databaseEntity) {
        final UUID databaseEntityId = this.getDatabaseEntityId(databaseEntity);

        final Instant currentInstant = Instant.now();
        final Instant firstPollingTimeInstant = this.firstPollingTimes.get(databaseEntityId);

        final Duration timeDifference = Duration.between(firstPollingTimeInstant, currentInstant);

        final boolean hasDatabaseEntityTimedOut = timeDifference.toMillis() >= this.timeout;

        if (hasDatabaseEntityTimedOut) {
            this.removeTimeForFirstPollRequest(databaseEntity);
        }

        return hasDatabaseEntityTimedOut;
    }
}
