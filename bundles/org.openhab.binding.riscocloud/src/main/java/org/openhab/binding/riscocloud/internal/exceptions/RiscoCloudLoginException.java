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
 * Exception to encapsulate any login issues with MELCloud.
 *
 * @author Luca Calcaterra - Initial Contribution
 */
public class RiscoCloudLoginException extends Exception {
    private static final long serialVersionUID = 1L;

    public RiscoCloudLoginException(Throwable cause) {
        super("Error occurred during login to RiscoCloud", cause);
    }

    public RiscoCloudLoginException(String message) {
        super(message);
    }

    public RiscoCloudLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
