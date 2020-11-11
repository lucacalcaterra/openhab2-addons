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
package org.openhab.binding.riscocloud.internal.discovery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.riscocloud.internal.RiscoCloudBindingConstants;
import org.openhab.binding.riscocloud.internal.api.json.CpStateResponse.Partition;
import org.openhab.binding.riscocloud.internal.exceptions.RiscoCloudCommException;
import org.openhab.binding.riscocloud.internal.exceptions.RiscoCloudLoginException;
import org.openhab.binding.riscocloud.internal.handler.RiscoCloudAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RiscoCloudDiscoveryService} creates things based on the configured location.
 *
 * @author Luca Calcaterra - Initial Contribution
 * @author Pauli Anttila - Refactoring
 * @author Wietse van Buitenen - Check device type, added heatpump device
 */
public class RiscoCloudDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(RiscoCloudDiscoveryService.class);

    private static final int DISCOVER_TIMEOUT_SECONDS = 10;

    private RiscoCloudAccountHandler riscoCloudHandler;
    private ScheduledFuture<?> scanTask;

    /**
     * Creates a RiscoCloudDiscoveryService with enabled autostart.
     */
    public RiscoCloudDiscoveryService() {
        super(RiscoCloudBindingConstants.DISCOVERABLE_THING_TYPE_UIDS, DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    @Modified
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoverDevices();
    }

    @Override
    protected void startScan() {
        if (this.scanTask != null) {
            scanTask.cancel(true);
        }
        this.scanTask = scheduler.schedule(() -> discoverDevices(), 0, TimeUnit.SECONDS);
    }

    @Override
    protected void stopScan() {
        super.stopScan();

        if (this.scanTask != null) {
            this.scanTask.cancel(true);
            this.scanTask = null;
        }
    }

    private void discoverDevices() {
        logger.debug("Discover devices");

        if (riscoCloudHandler != null) {
            try {
                List<Partition> partitionsList = riscoCloudHandler.getPartitionsList();
                if (partitionsList == null) {
                    logger.debug("No devices found");
                } else {
                    ThingUID bridgeUID = riscoCloudHandler.getThing().getUID();
                    logger.debug("bridge check");
                }
                //
                // if (1 == 1) {
                // // if (deviceList == null) {
                // logger.debug("No devices found");
                // } else {
                // ThingUID bridgeUID = riscoCloudHandler.getThing().getUID();
                //
                // // deviceList.forEach(device -> {
                // // ThingTypeUID thingTypeUid = null;
                // // if (device.getType() == 0) {
                // // thingTypeUid = THING_TYPE_ACDEVICE;
                // // } else if (device.getType() == 1) {
                // // thingTypeUid = THING_TYPE_HEATPUMPDEVICE;
                // // } else {
                // // logger.debug("Unsupported device found: name {} : type: {}", device.getDeviceName(),
                // // device.getType());
                // // return;
                // // }
                // // ThingUID deviceThing = new ThingUID(thingTypeUid, riscoCloudHandler.getThing().getUID(),
                // // device.getDeviceID().toString());
                //
                // // Map<String, Object> deviceProperties = new HashMap<>();
                // // deviceProperties.put("deviceID", device.getDeviceID().toString());
                // // deviceProperties.put("serialNumber", device.getSerialNumber().toString());
                // // deviceProperties.put("macAddress", device.getMacAddress().toString());
                // // deviceProperties.put("deviceName", device.getDeviceName().toString());
                // // deviceProperties.put("buildingID", device.getBuildingID().toString());
                //
                // // String label = createLabel(device);
                // // logger.debug("Found device: {} : {}", label, deviceProperties);
                //
                // // thingDiscovered(DiscoveryResultBuilder.create(deviceThing).withLabel(label)
                // // .withProperties(deviceProperties)
                // // .withRepresentationProperty(device.getDeviceID().toString()).withBridge(bridgeUID)
                // // .build());
                // // });
            } catch (RiscoCloudLoginException e) {
                logger.debug("Login error occurred during device list fetch, reason {}. ", e.getMessage(), e);
            } catch (RiscoCloudCommException e) {
                logger.debug("Error occurred during device list fetch, reason {}. ", e.getMessage(), e);
            }
        }
    }

    // private String createLabel(Device device) {
    // StringBuilder sb = new StringBuilder();
    // if (device.getType() == 0) {
    // sb.append("A.C. Device - ");
    // } else if (device.getType() == 1) {
    // sb.append("Heatpump Device - ");
    // }
    // if (device.getBuildingName() != null && device.getBuildingName() instanceof String) {
    // sb.append(device.getBuildingName()).append(" - ");
    // }
    // sb.append(device.getDeviceName());
    // return sb.toString();
    // }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof RiscoCloudAccountHandler) {
            riscoCloudHandler = (RiscoCloudAccountHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return riscoCloudHandler;
    }
}
