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

package org.openhab.binding.ipcamera;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link IpCameraBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class IpCameraBindingConstants {

    private static final String BINDING_ID = "ipcamera";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HTTPONLY = new ThingTypeUID(BINDING_ID, "HTTPONLY");
    public static final ThingTypeUID THING_TYPE_ONVIF = new ThingTypeUID(BINDING_ID, "ONVIF");
    public static final ThingTypeUID THING_TYPE_AMCREST = new ThingTypeUID(BINDING_ID, "AMCREST");
    public static final ThingTypeUID THING_TYPE_FOSCAM = new ThingTypeUID(BINDING_ID, "FOSCAM");
    public static final ThingTypeUID THING_TYPE_HIKVISION = new ThingTypeUID(BINDING_ID, "HIKVISION");
    public static final ThingTypeUID THING_TYPE_INSTAR = new ThingTypeUID(BINDING_ID, "INSTAR");
    public static final ThingTypeUID THING_TYPE_DAHUA = new ThingTypeUID(BINDING_ID, "DAHUA");
    public static final ThingTypeUID THING_TYPE_DOORBIRD = new ThingTypeUID(BINDING_ID, "DOORBIRD");

    // List of all Thing Config items
    public static final String CONFIG_IPADDRESS = "IPADDRESS";
    public static final String CONFIG_PORT = "PORT";
    public static final String CONFIG_ONVIF_PORT = "ONVIF_PORT";
    public static final String CONFIG_SERVER_PORT = "SERVER_PORT";
    public static final String CONFIG_USERNAME = "USERNAME";
    public static final String CONFIG_PASSWORD = "PASSWORD";
    public static final String CONFIG_ONVIF_PROFILE_NUMBER = "ONVIF_MEDIA_PROFILE";
    public static final String CONFIG_POLL_CAMERA_MS = "POLL_CAMERA_MS";
    public static final String CONFIG_SNAPSHOT_URL_OVERRIDE = "SNAPSHOT_URL_OVERRIDE";
    public static final String CONFIG_IMAGE_UPDATE_EVENTS = "IMAGE_UPDATE_EVENTS";
    public static final String CONFIG_UPDATE_IMAGE = "UPDATE_IMAGE";
    public static final String CONFIG_NVR_CHANNEL = "NVR_CHANNEL";
    public static final String CONFIG_MOTION_URL_OVERRIDE = "MOTION_URL_OVERRIDE";
    public static final String CONFIG_AUDIO_URL_OVERRIDE = "AUDIO_URL_OVERRIDE";
    public static final String CONFIG_STREAM_URL_OVERRIDE = "STREAM_URL_OVERRIDE";
    public static final String CONFIG_IP_WHITELIST = "IP_WHITELIST";
    public static final String CONFIG_FFMPEG_LOCATION = "FFMPEG_LOCATION";
    public static final String CONFIG_FFMPEG_INPUT = "FFMPEG_INPUT";
    public static final String CONFIG_FFMPEG_OUTPUT = "FFMPEG_OUTPUT";
    public static final String CONFIG_FFMPEG_HLS_OUT_ARGUMENTS = "FFMPEG_HLS_OUT_ARGUMENTS";
    public static final String CONFIG_FFMPEG_GIF_OUT_ARGUMENTS = "FFMPEG_GIF_OUT_ARGUMENTS";
    public static final String CONFIG_GIF_PREROLL = "GIF_PREROLL";
    public static final String CONFIG_GIF_POSTROLL = "GIF_POSTROLL";

    // List of all Channel ids
    public static final String CHANNEL_UPDATE_IMAGE_NOW = "updateImageNow";
    public static final String CHANNEL_UPDATE_GIF = "updateGif";
    public static final String CHANNEL_IMAGE = "image";
    public static final String CHANNEL_RTSP_URL = "rtspUrl";
    public static final String CHANNEL_IMAGE_URL = "imageUrl";
    public static final String CHANNEL_STREAM_URL = "streamUrl";
    public static final String CHANNEL_HLS_URL = "hlsUrl";
    public static final String CHANNEL_PAN = "pan";
    public static final String CHANNEL_TILT = "tilt";
    public static final String CHANNEL_ZOOM = "zoom";
    public static final String CHANNEL_MOTION_ALARM = "motionAlarm";
    public static final String CHANNEL_LINE_CROSSING_ALARM = "lineCrossingAlarm";
    public static final String CHANNEL_FACE_DETECTED = "faceDetected";
    public static final String CHANNEL_ITEM_LEFT = "itemLeft";
    public static final String CHANNEL_ITEM_TAKEN = "itemTaken";
    public static final String CHANNEL_AUDIO_ALARM = "audioAlarm";
    public static final String CHANNEL_ENABLE_MOTION_ALARM = "enableMotionAlarm";
    public static final String CHANNEL_ENABLE_LINE_CROSSING_ALARM = "enableLineCrossingAlarm";
    public static final String CHANNEL_ENABLE_AUDIO_ALARM = "enableAudioAlarm";
    public static final String CHANNEL_THRESHOLD_AUDIO_ALARM = "thresholdAudioAlarm";
    public static final String CHANNEL_ACTIVATE_ALARM_OUTPUT = "activateAlarmOutput";
    public static final String CHANNEL_ACTIVATE_ALARM_OUTPUT2 = "activateAlarmOutput2";
    public static final String CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT = "enableExternalAlarmInput";
    public static final String CHANNEL_EXTERNAL_ALARM_INPUT = "externalAlarmInput";
    public static final String CHANNEL_EXTERNAL_ALARM_INPUT2 = "externalAlarmInput2";
    public static final String CHANNEL_AUTO_LED = "autoLED";
    public static final String CHANNEL_ENABLE_LED = "enableLED";
    public static final String CHANNEL_ENABLE_PIR_ALARM = "enablePirAlarm";
    public static final String CHANNEL_PIR_ALARM = "pirAlarm";
    public static final String CHANNEL_ENABLE_FIELD_DETECTION_ALARM = "enableFieldDetectionAlarm";
    public static final String CHANNEL_FIELD_DETECTION_ALARM = "fieldDetectionAlarm";
    public static final String CHANNEL_PARKING_ALARM = "parkingAlarm";
    public static final String CHANNEL_TEXT_OVERLAY = "textOverlay";
    public static final String CHANNEL_API_ACCESS = "apiAccess";
    public static final String CHANNEL_EXTERNAL_LIGHT = "externalLight";
    public static final String CHANNEL_DOORBELL = "doorBell";
    public static final String CHANNEL_LAST_MOTION_TYPE = "lastMotionType";
}
