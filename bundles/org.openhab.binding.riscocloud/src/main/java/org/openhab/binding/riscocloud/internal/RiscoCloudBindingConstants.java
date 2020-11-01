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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RiscoCloudBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Luca Calcaterra - Initial contribution
 */
@NonNullByDefault
public class RiscoCloudBindingConstants {

    private static final String BINDING_ID = "riscocloud";

    // List of Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_RISCOCLOUD_ACCOUNT = new ThingTypeUID(BINDING_ID, "riscocloudaccount");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_RISCOCLOUD_ACCOUNT).collect(Collectors.toSet()));

}
