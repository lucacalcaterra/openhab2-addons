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
package org.openhab.binding.riscocloud.internal;

import static org.openhab.binding.riscocloud.internal.RiscoCloudBindingConstants.*;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.riscocloud.internal.handler.RiscoCloudAccountHandler;
import org.openhab.binding.riscocloud.internal.handler.RiscoCloudPartitionHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link RiscoCloudHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Luca Calcaterra - Initial contribution
 */
@Component(configurationPid = "binding.riscocloud", service = ThingHandlerFactory.class)
public class RiscoCloudHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_RISCOCLOUD_ACCOUNT.equals(thingTypeUID)) {
            RiscoCloudAccountHandler handler = new RiscoCloudAccountHandler((Bridge) thing);
            return handler;
        } else if (THING_TYPE_PARTITION.equals(thingTypeUID)) {
            RiscoCloudPartitionHandler handler = new RiscoCloudPartitionHandler(thing);
            return handler;
        }
        // } else if (THING_TYPE_HEATPUMPDEVICE.equals(thingTypeUID)) {
        // MelCloudHeatpumpDeviceHandler handler = new MelCloudHeatpumpDeviceHandler(thing);
        // return handler;
        // }

        return null;
    }
}
