/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.riscocloud.internal.handler;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.riscocloud.internal.api.RiscoCloudConnection;
import org.openhab.binding.riscocloud.internal.api.json.AllSitesResponse.Site;
import org.openhab.binding.riscocloud.internal.config.AccountConfig;
import org.openhab.binding.riscocloud.internal.exceptions.RiscoCloudCommException;
import org.openhab.binding.riscocloud.internal.exceptions.RiscoCloudLoginException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RiscoCloudAccountHandler} is the handler for RiscoCloud API and connects it
 * to the webservice.
 *
 * @author Luca Calcaterra - Initial contribution
 *
 */
public class RiscoCloudAccountHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(RiscoCloudAccountHandler.class);

    private RiscoCloudConnection connection;
    private List<Site> sites;
    private ScheduledFuture<?> connectionCheckTask;
    private AccountConfig config;
    private boolean loginCredentialError;

    public RiscoCloudAccountHandler(Bridge bridge) {
        super(bridge);
    }

    // @Override
    // public Collection<Class<? extends ThingHandlerService>> getServices() {
    // return Collections.singleton(RiscoCloudDiscoveryService.class);
    // }

    @Override
    public void initialize() {
        logger.debug("Initializing RiscoCloud account handler.");
        config = getConfigAs(AccountConfig.class);
        connection = new RiscoCloudConnection();
        sites = Collections.emptyList();
        loginCredentialError = false;
        startConnectionCheck();
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        stopConnectionCheck();
        connection = null;
        sites = Collections.emptyList();
        config = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public ThingUID getID() {
        return getThing().getUID();
    }

    public List<Site> getSitesList() throws RiscoCloudCommException, RiscoCloudLoginException {
        connectIfNotConnected();
        return connection.fetchSitesList();
    }

    private void connect() throws RiscoCloudCommException, RiscoCloudLoginException {
        if (loginCredentialError) {
            throw new RiscoCloudLoginException("Connection to RiscoCloud can't be opened because of wrong credentials");
        }
        logger.debug("Initializing connection to RiscoCloud");
        updateStatus(ThingStatus.OFFLINE);
        try {
            connection.login(config.username, config.password, config.language);
            sites = connection.fetchSitesList();
            updateStatus(ThingStatus.ONLINE);
        } catch (RiscoCloudLoginException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            loginCredentialError = true;
            throw e;
        } catch (RiscoCloudCommException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw e;
        }
    }

    private synchronized void connectIfNotConnected() throws RiscoCloudCommException, RiscoCloudLoginException {
        if (!isConnected()) {
            connect();
        }
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    // public DeviceStatus sendDeviceStatus(DeviceStatus deviceStatus)
    // throws RiscoCloudCommException, RiscoCloudLoginException {
    // connectIfNotConnected();
    // try {
    // return connection.sendDeviceStatus(deviceStatus);
    // } catch (RiscoCloudCommException e) {
    // logger.debug("Sending failed, retry once with relogin");
    // connect();
    // return connection.sendDeviceStatus(deviceStatus);
    // }
    // }
    //
    // public DeviceStatus fetchDeviceStatus(int deviceId, Optional<Integer> buildingId)
    // throws RiscoCloudCommException, RiscoCloudLoginException {
    // connectIfNotConnected();
    // int bid = buildingId.orElse(findBuildingId(deviceId));
    //
    // try {
    // return connection.fetchDeviceStatus(deviceId, bid);
    // } catch (RiscoCloudCommException e) {
    // logger.debug("Sending failed, retry once with relogin");
    // connect();
    // return connection.fetchDeviceStatus(deviceId, bid);
    // }
    // }

    // private int findBuildingId(int deviceId) throws RiscoCloudCommException {
    // if (devices != null) {
    // return devices.stream().filter(d -> d.getDeviceID() == deviceId).findFirst()
    // .orElseThrow(() -> new RiscoCloudCommException(
    // String.format("Can't find building id for device id %s", deviceId)))
    // .getBuildingID();
    // }
    // throw new RiscoCloudCommException(String.format("Can't find building id for device id %s", deviceId));
    // }

    private void startConnectionCheck() {
        if (connectionCheckTask == null || connectionCheckTask.isCancelled()) {
            logger.debug("Start periodic connection check");
            Runnable runnable = () -> {
                logger.debug("Check RiscoCloud connection");
                if (connection.isConnected()) {
                    logger.debug("Connection to RiscoCloud open");
                } else {
                    try {
                        connect();
                    } catch (RiscoCloudLoginException e) {
                        logger.debug("Connection to RiscoCloud down due to login error, reason: {}.", e.getMessage());
                    } catch (RiscoCloudCommException e) {
                        logger.debug("Connection to RiscoCloud down, reason: {}.", e.getMessage());
                    } catch (RuntimeException e) {
                        logger.warn("Unknown error occurred during connection check, reason: {}.", e.getMessage(), e);
                    }
                }
            };
            connectionCheckTask = scheduler.scheduleWithFixedDelay(runnable, 0, 60, TimeUnit.SECONDS);
        } else {
            logger.debug("Connection check task already running");
        }
    }

    private void stopConnectionCheck() {
        if (connectionCheckTask != null) {
            logger.debug("Stop periodic connection check");
            connectionCheckTask.cancel(true);
            connectionCheckTask = null;
        }
    }
}
