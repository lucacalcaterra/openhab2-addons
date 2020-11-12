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

import static org.openhab.binding.riscocloud.internal.RiscoCloudBindingConstants.THING_TYPE_PARTITION;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.riscocloud.internal.RiscoCloudBindingConstants;
import org.openhab.binding.riscocloud.internal.api.json.cpstate.DevCollection;
import org.openhab.binding.riscocloud.internal.exceptions.RiscoCloudCommException;
import org.openhab.binding.riscocloud.internal.exceptions.RiscoCloudLoginException;
import org.openhab.binding.riscocloud.internal.handler.RiscoCloudAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
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
                List<DevCollection> devCollectionList = riscoCloudHandler.getDevCollectionList();
                if (devCollectionList == null) {
                    logger.debug("No devices found");
                } else {
                    ThingUID bridgeUID = riscoCloudHandler.getThing().getUID();

                    devCollectionList.forEach(devColl -> {

                        // partition type ? Guessing it...
                        if (devColl.getDevType() == 21) {
                            devColl.getDevList().forEach(partition -> {

                                Integer partID = partition.getNum() + 1;
                                ThingTypeUID partitionThingTypeUid = THING_TYPE_PARTITION;
                                ThingUID partitionThing = new ThingUID(partitionThingTypeUid,
                                        riscoCloudHandler.getThing().getUID(), partID.toString());
                                Map<String, Object> partitionProperties = new HashMap<>();
                                partitionProperties.put("partitionID", partID.toString());

                                String partLabel = partition.getDesc();
                                logger.debug("Found partition: {} : {}", partLabel, partitionProperties);

                                thingDiscovered(DiscoveryResultBuilder.create(partitionThing).withLabel(partLabel)
                                        .withProperties(partitionProperties).withRepresentationProperty("partitionID")
                                        .withBridge(bridgeUID).build());

                            });

                        }

                    });
                }

            } catch (RiscoCloudLoginException e) {
                logger.debug("Login error occurred during partition list fetch, reason {}. ", e.getMessage(), e);
            } catch (RiscoCloudCommException e) {
                logger.debug("Error occurred during partition list fetch, reason {}. ", e.getMessage(), e);
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
