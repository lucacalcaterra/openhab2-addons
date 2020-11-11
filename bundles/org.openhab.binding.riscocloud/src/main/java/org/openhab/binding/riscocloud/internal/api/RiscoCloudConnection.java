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
package org.openhab.binding.riscocloud.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openhab.binding.riscocloud.internal.api.json.AllSitesResponse;
import org.openhab.binding.riscocloud.internal.api.json.AllSitesResponse.Site;
import org.openhab.binding.riscocloud.internal.api.json.CpStateResponse;
import org.openhab.binding.riscocloud.internal.api.json.Login;
import org.openhab.binding.riscocloud.internal.api.json.SiteLoginResponse;
import org.openhab.binding.riscocloud.internal.exceptions.RiscoCloudCommException;
import org.openhab.binding.riscocloud.internal.exceptions.RiscoCloudLoginException;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link RiscoCloudConnection} Manage connection to Risco Cloud .
 *
 * @author Luca Calcaterra - Initial Contribution
 */
public class RiscoCloudConnection {

    private static final String BASE_URL = "https://www.riscocloud.com/webapi/api";
    private static final String LOGIN_URL = BASE_URL + "/auth/login";
    private static final String GETALL_URL = BASE_URL + "/wuws/site/GetAll";
    private static final String LOGINSITEPREFIX_URL = BASE_URL + "/wuws/site";
    private static final String CPSTATEPREFIX_URL = BASE_URL + "/wuws/site";

    private static final int TIMEOUT_MILLISECONDS = 10000;

    // Gson objects are safe to share across threads and are somewhat expensive to
    // construct. Use a single instance.
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    private final Logger logger = LoggerFactory.getLogger(RiscoCloudConnection.class);

    private boolean isConnected = false;
    private String accessToken;
    private String sessionId;
    private Integer siteId;

    public void login(String username, String password, String pinCode, String languageId)
            throws RiscoCloudCommException, RiscoCloudLoginException {
        setConnected(false);
        accessToken = null;
        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("userName", username);
        jsonReq.addProperty("password", password);
        // jsonReq.addProperty("Language", languageId);
        // jsonReq.addProperty("AppVersion", "1.17.5.0");
        // jsonReq.addProperty("Persist", false);
        // jsonReq.addProperty("CaptchaResponse", (String) null);
        InputStream data = new ByteArrayInputStream(jsonReq.toString().getBytes(StandardCharsets.UTF_8));

        // login and access site sequence
        try {
            // login
            String loginResponse = HttpUtil.executeUrl("POST", LOGIN_URL, null, data, "application/json",
                    TIMEOUT_MILLISECONDS);
            logger.debug("Login response: {}", loginResponse);
            Login.Root loginresp = gson.fromJson(loginResponse, Login.Root.class);
            if (loginresp.getStatus() != 200) {
                throw new RiscoCloudLoginException("Login error (status 200 not returned)");
            }
            accessToken = loginresp.getResponse().getAccessToken();
            if (accessToken == null) {
                throw new RiscoCloudLoginException("Accesstoken Empty after Login");
            }
            // fetch first Site ID
            siteId = fetchSitesList().get(0).getId();

            // Site Login
            JsonObject jsonReqLogin = new JsonObject();
            jsonReqLogin.addProperty("pinCode", pinCode);
            jsonReqLogin.addProperty("languageId", languageId);

            InputStream dataSiteLogin = new ByteArrayInputStream(
                    jsonReqLogin.toString().getBytes(StandardCharsets.UTF_8));
            String loginSiteResponse = HttpUtil.executeUrl("POST", LOGINSITEPREFIX_URL + "/" + siteId + "/Login",
                    getHeaderProperties(), dataSiteLogin, "application/json", TIMEOUT_MILLISECONDS);
            logger.debug("Login site response: {}", loginSiteResponse);
            SiteLoginResponse.Root siteLoginResp = gson.fromJson(loginSiteResponse, SiteLoginResponse.Root.class);
            if (siteLoginResp.getStatus() != 200) {
                throw new RiscoCloudLoginException("Site " + siteId + " Login error (status 200 not returned)");
            }
            // get SessionId - token for next requests
            sessionId = siteLoginResp.getResponse().getSessionId();

            // is connected !
            setConnected(true);

            // logger.debug("test");
            // LoginClientResponse resp = gson.fromJson(loginResponse,
            // LoginClientResponse.class);
            // if (resp.getErrorId() != null) {
            // String errorMsg = String.format("Login failed, error code: %s",
            // resp.getErrorId());
            // if (resp.getErrorMessage() != null) {
            // errorMsg = String.format("%s (%s)", errorMsg, resp.getErrorMessage());
            // }
            // throw new RiscoCloudLoginException(errorMsg);
            // }
            // sessionKey = resp.getLoginData().getContextKey();
            // setConnected(true);
        } catch (IOException | JsonSyntaxException e) {
            throw new RiscoCloudCommException(String.format("Login error, reason: %s", e.getMessage()), e);
        }
    }

    public List<Site> fetchSitesList() throws RiscoCloudCommException {
        try {
            String getAllSitesResponse = HttpUtil.executeUrl("POST", GETALL_URL, getHeaderProperties(), null,
                    "application/json", TIMEOUT_MILLISECONDS);
            logger.debug("Login response: {}", getAllSitesResponse);

            AllSitesResponse.Root getAllSitesResp = gson.fromJson(getAllSitesResponse, AllSitesResponse.Root.class);
            if (getAllSitesResp.getStatus() != 200) {
                throw new RiscoCloudCommException("Get Site info error (status 200 not returned)");
            }
            List<Site> sites = new ArrayList<>();
            sites = getAllSitesResp.getResponse();

            logger.debug("check");

            return sites;
        } catch (IOException | JsonSyntaxException e) {
            setConnected(false);
            throw new RiscoCloudCommException("Error occurred during device list poll", e);
        }
    }

    public CpStateResponse.State fetchCpState() throws RiscoCloudCommException {

        try {
            JsonObject jsonCpState = new JsonObject();
            jsonCpState.addProperty("sessionToken", sessionId);

            InputStream cpStatedata = new ByteArrayInputStream(jsonCpState.toString().getBytes(StandardCharsets.UTF_8));
            String cpStateResponse = HttpUtil.executeUrl("POST",
                    CPSTATEPREFIX_URL + "/" + siteId + "/ControlPanel/GetState", getHeaderProperties(), cpStatedata,
                    "application/json", TIMEOUT_MILLISECONDS);
            logger.debug("Get ControlPanel State response: {}", cpStateResponse);

            CpStateResponse.State cpStateResp = gson.fromJson(cpStateResponse, CpStateResponse.State.class);
            return cpStateResp;

        } catch (IOException | JsonSyntaxException e) {
            setConnected(false);
            throw new RiscoCloudCommException("Error occurred during device list poll", e);
        }
    }

    // public List<Device> fetchDeviceList() throws RiscoCloudCommException {
    // assertConnected();
    // try {
    // String response = HttpUtil.executeUrl("GET", DEVICE_LIST_URL,
    // getHeaderProperties(), null, null,
    // TIMEOUT_MILLISECONDS);
    // logger.debug("Device list response: {}", response);
    // List<Device> devices = new ArrayList<>();
    // ListDevicesResponse[] buildings = gson.fromJson(response,
    // ListDevicesResponse[].class);
    // Arrays.asList(buildings).forEach(building -> {
    // if (building.getStructure().getDevices() != null) {
    // devices.addAll(building.getStructure().getDevices());
    // }
    // building.getStructure().getAreas().forEach(area -> {
    // if (area.getDevices() != null) {
    // devices.addAll(area.getDevices());
    // }
    // });
    // building.getStructure().getFloors().forEach(floor -> {
    // if (floor.getDevices() != null) {
    // devices.addAll(floor.getDevices());
    // }
    // floor.getAreas().forEach(area -> {
    // if (area.getDevices() != null) {
    // devices.addAll(area.getDevices());
    // }
    // });
    // });
    // });
    // logger.debug("Found {} devices", devices.size());
    //
    // return devices;
    // } catch (IOException | JsonSyntaxException e) {
    // setConnected(false);
    // throw new RiscoCloudCommException("Error occurred during device list poll",
    // e);
    // }
    // }
    //
    // public DeviceStatus fetchDeviceStatus(int deviceId, int buildingId) throws
    // RiscoCloudCommException {
    // assertConnected();
    // String url = DEVICE_URL + String.format("/Get?id=%d&buildingID=%d", deviceId,
    // buildingId);
    // try {
    // String response = HttpUtil.executeUrl("GET", url, getHeaderProperties(),
    // null, null, TIMEOUT_MILLISECONDS);
    // logger.debug("Device status response: {}", response);
    // DeviceStatus deviceStatus = gson.fromJson(response, DeviceStatus.class);
    // return deviceStatus;
    // } catch (IOException | JsonSyntaxException e) {
    // setConnected(false);
    // throw new RiscoCloudCommException("Error occurred during device status
    // fetch", e);
    // }
    // }
    //
    // public DeviceStatus sendDeviceStatus(DeviceStatus deviceStatus) throws
    // RiscoCloudCommException {
    // assertConnected();
    // String content = gson.toJson(deviceStatus, DeviceStatus.class);
    // logger.debug("Sending device status: {}", content);
    // InputStream data = new
    // ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    // try {
    // String response = HttpUtil.executeUrl("POST", DEVICE_URL + "/SetAta",
    // getHeaderProperties(), data,
    // "application/json", TIMEOUT_MILLISECONDS);
    // logger.debug("Device status sending response: {}", response);
    // return gson.fromJson(response, DeviceStatus.class);
    // } catch (IOException | JsonSyntaxException e) {
    // setConnected(false);
    // throw new RiscoCloudCommException("Error occurred during device command
    // sending", e);
    // }
    // }

    public synchronized boolean isConnected() {
        return isConnected;
    }

    private synchronized void setConnected(boolean state) {
        isConnected = state;
    }

    private Properties getHeaderProperties() {
        Properties headers = new Properties();
        headers.put("Authorization", "Bearer " + accessToken);
        return headers;
    }

    private void assertConnected() throws RiscoCloudCommException {
        if (!isConnected) {
            throw new RiscoCloudCommException("Not connected to RiscoCloud");
        }
    }
}
