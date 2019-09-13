/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.ipcamera.internal;

import java.security.MessageDigest;
import java.util.Random;

import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.ipcamera.handler.IpCameraHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;

/**
 * The {@link MyNettyAuthHandler} is responsible for handling the basic and digest auths
 *
 *
 * @author Matthew Skinner - Initial contribution
 */

public class MyNettyAuthHandler extends ChannelDuplexHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private IpCameraHandler myHandler;
    private String username, password;
    private String httpMethod, httpUrl;
    private byte ncCounter = 0;
    String nonce = "empty", opaque = "empty", qop = "empty", realm = "empty";

    public MyNettyAuthHandler(String user, String pass, String method, String url, ThingHandler handle) {
        myHandler = (IpCameraHandler) handle;
        username = user;
        password = pass;
        httpUrl = url;
        httpMethod = method;
    }

    public MyNettyAuthHandler(String user, String pass, ThingHandler handle) {
        myHandler = (IpCameraHandler) handle;
        username = user;
        password = pass;
    }

    public void setURL(String method, String url) {
        httpUrl = url;
        httpMethod = method;
        logger.trace("MyNettyAuthHandler is now setup for \t{}:{}", method, url);
    }

    private String calcMD5Hash(String toHash) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] array = messageDigest.digest(toHash.getBytes());
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                stringBuffer.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            messageDigest = null;
            array = null;
            return stringBuffer.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            logger.error("NoSuchAlgorithmException error when calculating MD5 hash");
        }
        return null;
    }

    private String searchString(String rawString, String searchedString) {
        String result = "";
        int index = 0;
        index = rawString.indexOf(searchedString);
        if (index != -1) // -1 means "not found"
        {
            result = rawString.substring(index + searchedString.length(), rawString.length());
            index = result.indexOf(',');
            if (index == -1) {
                index = result.indexOf('"');
                if (index == -1) {
                    index = result.indexOf('}');
                    if (index == -1) {
                        return result;
                    } else {
                        return result.substring(0, index);
                    }
                } else {
                    return result.substring(0, index);
                }
            } else {
                result = result.substring(0, index);
                index = result.indexOf('"');
                if (index == -1) {
                    return result;
                } else {
                    return result.substring(0, index);
                }
            }
        }
        return null;
    }

    // Method can be used a few ways. processAuth(null, string,string, false) to return the digest on demand, and
    // processAuth(challString, string,string, true) to auto send new packet
    // First run it should not have authenticate as null
    // nonce is reused if authenticate is null so the NC needs to increment to allow this//
    public String processAuth(String authenticate, String httpMethod, String requestURI, boolean reSend) {
        if (authenticate != null) {
            if (authenticate.contains("Basic realm=\"")) {
                if (myHandler.useDigestAuth == true) {
                    return "Error:Downgrade authenticate avoided";
                }
                logger.debug("Setting up the camera to use Basic Auth and resending last request with correct auth.");
                myHandler.setBasicAuth(true);
                myHandler.sendHttpRequest(httpMethod, requestURI, null);
                return "Using Basic";
            }

            /////// Fresh Digest Authenticate method follows as Basic is already handled and returned ////////
            realm = searchString(authenticate, "realm=\"");
            if (realm == null) {
                logger.warn("Could not find a valid WWW-Authenticate response in :{}", authenticate);
                return "Error";
            }
            nonce = searchString(authenticate, "nonce=\"");
            opaque = searchString(authenticate, "opaque=\"");
            qop = searchString(authenticate, "qop=\"");

            if (!qop.isEmpty() && !realm.isEmpty()) {
                myHandler.useDigestAuth = true;
            } else {
                logger.warn(
                        "!!!! Something is wrong with the reply back from the camera. WWW-Authenticate header: qop:{}, realm:{}",
                        qop, realm);
            }

            String stale = searchString(authenticate, "stale=\"");
            if (stale == null) {
            } else if (stale.equalsIgnoreCase("true")) {
                logger.debug("Camera reported stale=true which normally means the NONCE has expired.");
            }
        }

        // create the MD5 hashes
        String ha1 = username + ":" + realm + ":" + password;
        ha1 = calcMD5Hash(ha1);
        Random random = new Random();
        String cnonce = Integer.toHexString(random.nextInt());
        random = null;
        ncCounter = (ncCounter > 125) ? 1 : ++ncCounter;
        String nc = String.format("%08X", ncCounter); // 8 digit hex number
        String ha2 = httpMethod + ":" + requestURI;
        ha2 = calcMD5Hash(ha2);

        String response = ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2;
        response = calcMD5Hash(response);

        String digestString = "username=\"" + username + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\""
                + requestURI + "\", cnonce=\"" + cnonce + "\", nc=" + nc + ", qop=\"" + qop + "\", response=\""
                + response + "\", opaque=\"" + opaque + "\"";

        if (reSend) {
            myHandler.sendHttpRequest(httpMethod, requestURI, digestString);
            return null;
        }
        return digestString;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean closeConnection = true;
        String authenticate = null;
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            if (response.status().code() == 401) {
                if (!response.headers().isEmpty()) {
                    for (CharSequence name : response.headers().names()) {
                        for (CharSequence value : response.headers().getAll(name)) {
                            if (name.toString().equalsIgnoreCase("WWW-Authenticate")) {
                                authenticate = value.toString();
                            }
                            if (name.toString().equalsIgnoreCase("Connection")
                                    && value.toString().contains("keep-alive")) {
                                closeConnection = false;
                            }
                        }
                    }

                    myHandler.lock.lock();
                    try {
                        byte indexInLists = (byte) myHandler.listOfChannels.indexOf(ctx.channel());
                        if (indexInLists >= 0) {
                            if (closeConnection) {
                                // Need to mark the channel as closing so the digest gets a new ch
                                myHandler.listOfChStatus.set(indexInLists, (byte) 0);
                                logger.debug("401: Mark as closing, the  channel:{} \t{}:{}", indexInLists, httpMethod,
                                        httpUrl);
                            } else {
                                myHandler.listOfChStatus.set(indexInLists, (byte) 2);
                                logger.debug("401: Mark to re-use,  the channel:{} \t{}:{}", indexInLists, httpMethod,
                                        httpUrl);
                            }

                        } else {
                            logger.warn("!!!! 401: Could not find the channel to mark as closing or reusable");
                        }
                    } finally {
                        myHandler.lock.unlock();
                    }

                    if (authenticate != null) {
                        processAuth(authenticate, httpMethod, httpUrl, true);
                    } else {
                        logger.warn("Camera gave a 401 reply and did not provide a WWW-Authenticate header");
                    }
                    if (closeConnection) {
                        ctx.close();// needs to be here
                    }
                }
            }
        }
        // Pass the Message back to the pipeline for the next handler to process//
        authenticate = null;
        super.channelRead(ctx, msg);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger = null;
        myHandler = null;
        username = password = httpMethod = httpUrl = null;
        nonce = opaque = qop = realm = null;
    }
}
