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
package org.openhab.binding.riscocloud.internal.config;

/**
 * Config class for a Partition.
 *
 * @author Luca Calcaterra - Initial Contribution
 *
 */
public class PartitionConfig {

    public Integer partitionID;
    // public Integer buildingID;
    public Integer pollingInterval;

    @Override
    public String toString() {
        return "[partitionID=" + partitionID + "]";
    }
}
