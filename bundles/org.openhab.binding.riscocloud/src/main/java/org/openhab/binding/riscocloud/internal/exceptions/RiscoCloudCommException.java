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
package org.openhab.binding.riscocloud.internal.exceptions;

/**
 * Exception to encapsulate any issues communicating with MELCloud.
 *
 * @author Luca Calcaterra - Initial Contribution
 */
public class RiscoCloudCommException extends Exception {
    private static final long serialVersionUID = 1L;

    public RiscoCloudCommException(Throwable cause) {
        super("Error occurred when communicating with RiscoCloud", cause);
    }

    public RiscoCloudCommException(String message) {
        super(message);
    }

    public RiscoCloudCommException(String message, Throwable cause) {
        super(message, cause);
    }
}
