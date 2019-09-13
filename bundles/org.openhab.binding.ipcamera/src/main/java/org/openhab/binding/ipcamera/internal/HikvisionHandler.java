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

import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_ACTIVATE_ALARM_OUTPUT;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_ENABLE_AUDIO_ALARM;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_ENABLE_FIELD_DETECTION_ALARM;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_ENABLE_LINE_CROSSING_ALARM;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_ENABLE_MOTION_ALARM;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_EXTERNAL_ALARM_INPUT;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_FACE_DETECTED;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_FIELD_DETECTION_ALARM;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_ITEM_LEFT;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_ITEM_TAKEN;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_LINE_CROSSING_ALARM;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_MOTION_ALARM;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_PIR_ALARM;

import java.util.ArrayList;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ipcamera.handler.IpCameraHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

public class HikvisionHandler extends ChannelDuplexHandler {
	IpCameraHandler ipCameraHandler;
	String nvrChannel;
	int lineCount, vmdCount, leftCount, takenCount, faceCount, pirCount, fieldCount = 0;

	public HikvisionHandler(ThingHandler handler, String nvrChannel) {
		ipCameraHandler = (IpCameraHandler) handler;
		this.nvrChannel = nvrChannel;
	}

	// This handles the incoming http replies back from the camera.
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String content = null;
		int debounce = 3;
		try {
			content = msg.toString();
			if (!content.isEmpty()) {
				ipCameraHandler.logger.trace("HTTP Result back from camera is \t:{}:", content);
			} else {
				return;
			}

			// Alarm checking goes in here//
			if (content.contains("<EventNotificationAlert version=\"")) {
				if (content.contains("hannelID>" + nvrChannel + "</")) {// some camera use c or <dynChannelID>

					if (content.contains("<eventType>linedetection</eventType>")) {
						ipCameraHandler.motionDetected(CHANNEL_LINE_CROSSING_ALARM);
						lineCount = debounce;
					}
					if (content.contains("<eventType>fielddetection</eventType>")) {
						ipCameraHandler.motionDetected(CHANNEL_FIELD_DETECTION_ALARM);
						fieldCount = debounce;
					}
					if (content.contains("<eventType>VMD</eventType>")) {
						ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
						vmdCount = debounce;
					}
					if (content.contains("<eventType>facedetection</eventType>")) {
						ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.valueOf("ON"));
						faceCount = debounce;
					}
					if (content.contains("<eventType>unattendedBaggage</eventType>")) {
						ipCameraHandler.setChannelState(CHANNEL_ITEM_LEFT, OnOffType.valueOf("ON"));
						leftCount = debounce;
					}
					if (content.contains("<eventType>attendedBaggage</eventType>")) {
						ipCameraHandler.setChannelState(CHANNEL_ITEM_TAKEN, OnOffType.valueOf("ON"));
						takenCount = debounce;
					}
					if (content.contains("<eventType>PIR</eventType>")) {
						ipCameraHandler.motionDetected(CHANNEL_PIR_ALARM);
						pirCount = debounce;
					}
					if (content.contains("<eventType>videoloss</eventType>\r\n<eventState>inactive</eventState>")) {
						ipCameraHandler.audioAlarmUpdateSnapshot = false;
						ipCameraHandler.motionAlarmUpdateSnapshot = false;
						ipCameraHandler.firstMotionAlarm = false;
						countDown();
						countDown();
					}
				} else if (content.contains("<channelID>0</channelID>")) {// NVR uses channel 0 to say all channels
					if (content.contains("<eventType>videoloss</eventType>\r\n<eventState>inactive</eventState>")) {
						ipCameraHandler.audioAlarmUpdateSnapshot = false;
						ipCameraHandler.motionAlarmUpdateSnapshot = false;
						ipCameraHandler.firstMotionAlarm = false;
						countDown();
						countDown();
					}
				}
				countDown();
			}

			// determine if the motion detection is turned on or off.
			else if (content.contains("<MotionDetection version=\"2.0\" xmlns=\"")) {
				ipCameraHandler.lock.lock();
				try {
					byte indexInLists = (byte) ipCameraHandler.listOfRequests
							.indexOf("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection");
					if (indexInLists >= 0) {
						ipCameraHandler.logger.debug(
								"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Storing new Motion reply {}",
								content);
						ipCameraHandler.listOfReplies.set(indexInLists, content);
					}
				} finally {
					ipCameraHandler.lock.unlock();
				}

				if (content.contains("<enabled>true</enabled>")) {
					ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.valueOf("ON"));
				} else if (content.contains("<enabled>false</enabled>")) {
					ipCameraHandler.setChannelState(CHANNEL_ENABLE_MOTION_ALARM, OnOffType.valueOf("OFF"));
				}
			} else if (content.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<LineDetection>")) {
				ipCameraHandler.lock.lock();
				try {
					byte indexInLists = (byte) ipCameraHandler.listOfRequests
							.indexOf("/ISAPI/Smart/LineDetection/" + nvrChannel + "01");
					if (indexInLists >= 0) {
						ipCameraHandler.logger.debug(
								"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Storing new Line Crossing reply {}",
								content);
						ipCameraHandler.listOfReplies.set(indexInLists, content);
					}
				} finally {
					ipCameraHandler.lock.unlock();
				}
				if (content.contains("<enabled>true</enabled>")) {
					ipCameraHandler.setChannelState(CHANNEL_ENABLE_LINE_CROSSING_ALARM, OnOffType.valueOf("ON"));
				} else if (content.contains("<enabled>false</enabled>")) {
					ipCameraHandler.setChannelState(CHANNEL_ENABLE_LINE_CROSSING_ALARM, OnOffType.valueOf("OFF"));
				}
			} else if (content.contains("<AudioDetection version=\"2.0\" xmlns=\"")) {
				ipCameraHandler.lock.lock();
				try {
					byte indexInLists = (byte) ipCameraHandler.listOfRequests
							.indexOf("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01");
					if (indexInLists >= 0) {
						ipCameraHandler.listOfReplies.set(indexInLists, content);
					}
				} finally {
					ipCameraHandler.lock.unlock();
				}
				if (content.contains("<enabled>true</enabled>")) {
					ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.valueOf("ON"));
				} else if (content.contains("<enabled>false</enabled>")) {
					ipCameraHandler.setChannelState(CHANNEL_ENABLE_AUDIO_ALARM, OnOffType.valueOf("OFF"));
				}
			}
			////////////////// External Alarm Input ///////////////
			else if (content.contains("<requestURL>/ISAPI/System/IO/inputs/" + nvrChannel + "/status</requestURL>")) {
				// Stops checking the external alarm if camera does not have feature.
				if (content.contains("<statusString>Invalid Operation</statusString>")) {
					ipCameraHandler.lowPriorityRequests.remove(0);
					ipCameraHandler.logger
							.debug("Stopping checks for alarm inputs as camera appears to be missing this feature.");
				}
			} else if (content.contains("<IOPortStatus version=\"2.0\" xmlns=\"")) {
				if (content.contains("<ioState>active</ioState>")) {
					ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT, OnOffType.valueOf("ON"));
				} else if (content.contains("<ioState>inactive</ioState>")) {
					ipCameraHandler.setChannelState(CHANNEL_EXTERNAL_ALARM_INPUT, OnOffType.valueOf("OFF"));
				}
			} else if (content.contains("<FieldDetection version=\"2.0\" xmlns=\"")) {
				ipCameraHandler.lock.lock();
				try {
					byte indexInLists = (byte) ipCameraHandler.listOfRequests
							.indexOf("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01");
					if (indexInLists >= 0) {
						ipCameraHandler.logger.debug(
								"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Storing new FieldDetection reply {}",
								content);
						ipCameraHandler.listOfReplies.set(indexInLists, content);
					}
				} finally {
					ipCameraHandler.lock.unlock();
				}
				if (content.contains("<enabled>true</enabled>")) {
					ipCameraHandler.setChannelState(CHANNEL_ENABLE_FIELD_DETECTION_ALARM, OnOffType.valueOf("ON"));
				} else if (content.contains("<enabled>false</enabled>")) {
					ipCameraHandler.setChannelState(CHANNEL_ENABLE_FIELD_DETECTION_ALARM, OnOffType.valueOf("OFF"));
				}
			}
		} finally {
			ReferenceCountUtil.release(msg);
			content = null;
		}
	}

	// This does debouncing of the alarms
	void countDown() {
		if (lineCount > 1) {
			lineCount--;
		} else if (lineCount == 1) {
			ipCameraHandler.setChannelState(CHANNEL_LINE_CROSSING_ALARM, OnOffType.valueOf("OFF"));
			lineCount--;
		}
		if (vmdCount > 1) {
			vmdCount--;
		} else if (vmdCount == 1) {
			ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.valueOf("OFF"));
			vmdCount--;
		}
		if (leftCount > 1) {
			leftCount--;
		} else if (leftCount == 1) {
			ipCameraHandler.setChannelState(CHANNEL_ITEM_LEFT, OnOffType.valueOf("OFF"));
			leftCount--;
		}
		if (takenCount > 1) {
			takenCount--;
		} else if (takenCount == 1) {
			ipCameraHandler.setChannelState(CHANNEL_ITEM_TAKEN, OnOffType.valueOf("OFF"));
			takenCount--;
		}
		if (faceCount > 1) {
			faceCount--;
		} else if (faceCount == 1) {
			ipCameraHandler.setChannelState(CHANNEL_FACE_DETECTED, OnOffType.valueOf("OFF"));
			faceCount--;
		}
		if (pirCount > 1) {
			pirCount--;
		} else if (pirCount == 1) {
			ipCameraHandler.setChannelState(CHANNEL_PIR_ALARM, OnOffType.valueOf("OFF"));
			pirCount--;
		}
		if (fieldCount > 1) {
			fieldCount--;
		} else if (fieldCount == 1) {
			ipCameraHandler.setChannelState(CHANNEL_FIELD_DETECTION_ALARM, OnOffType.valueOf("OFF"));
			fieldCount--;
		}
	}

	// This handles the commands that come from the Openhab event bus.
	public void handleCommand(ChannelUID channelUID, Command command) {
		if (command.toString() == "REFRESH") {
			switch (channelUID.getId()) {
			case CHANNEL_ENABLE_AUDIO_ALARM:
				ipCameraHandler.sendHttpGET("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01");
				return;
			case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
				ipCameraHandler.sendHttpGET("/ISAPI/Smart/LineDetection/" + nvrChannel + "01");
				return;
			case CHANNEL_ENABLE_FIELD_DETECTION_ALARM:
				ipCameraHandler.logger.debug("FieldDetection command");
				ipCameraHandler.sendHttpGET("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01");
				return;
			case CHANNEL_ENABLE_MOTION_ALARM:
				ipCameraHandler.sendHttpGET("/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection");
				return;
			}
			return; // Return as we have handled the refresh command above and don't need to
					// continue further.
		} // end of "REFRESH"
		switch (channelUID.getId()) {
		case CHANNEL_ENABLE_AUDIO_ALARM:
			if ("ON".equals(command.toString())) {
				ipCameraHandler.hikChangeSetting("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01",
						"<enabled>false</enabled>", "<enabled>true</enabled>");
			} else {
				ipCameraHandler.hikChangeSetting("/ISAPI/Smart/AudioDetection/channels/" + nvrChannel + "01",
						"<enabled>true</enabled>", "<enabled>false</enabled>");
			}
			return;
		case CHANNEL_ENABLE_LINE_CROSSING_ALARM:
			if ("ON".equals(command.toString())) {
				ipCameraHandler.hikChangeSetting("/ISAPI/Smart/LineDetection/" + nvrChannel + "01",
						"<enabled>false</enabled>", "<enabled>true</enabled>");
			} else {
				ipCameraHandler.hikChangeSetting("/ISAPI/Smart/LineDetection/" + nvrChannel + "01",
						"<enabled>true</enabled>", "<enabled>false</enabled>");
			}
			return;
		case CHANNEL_ENABLE_MOTION_ALARM:
			if ("ON".equals(command.toString())) {

				ipCameraHandler.hikChangeSetting(
						"/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection",
						"<enabled>false</enabled>", "<enabled>true</enabled>");
			} else {
				ipCameraHandler.hikChangeSetting(
						"/ISAPI/System/Video/inputs/channels/" + nvrChannel + "01/motionDetection",
						"<enabled>true</enabled>", "<enabled>false</enabled>");
			}
			return;
		case CHANNEL_ENABLE_FIELD_DETECTION_ALARM:
			if ("ON".equals(command.toString())) {
				ipCameraHandler.hikChangeSetting("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01",
						"<enabled>false</enabled>", "<enabled>true</enabled>");
			} else {
				ipCameraHandler.hikChangeSetting("/ISAPI/Smart/FieldDetection/" + nvrChannel + "01",
						"<enabled>true</enabled>", "<enabled>false</enabled>");
			}
			return;
		case CHANNEL_ACTIVATE_ALARM_OUTPUT:
			if ("ON".equals(command.toString())) {
				ipCameraHandler.hikSendXml("/ISAPI/System/IO/outputs/" + nvrChannel + "/trigger",
						"<IOPortData version=\"1.0\" xmlns=\"http://www.hikvision.com/ver10/XMLSchema\">\r\n    <outputState>high</outputState>\r\n</IOPortData>\r\n");
			} else {
				ipCameraHandler.hikSendXml("/ISAPI/System/IO/outputs/" + nvrChannel + "/trigger",
						"<IOPortData version=\"1.0\" xmlns=\"http://www.hikvision.com/ver10/XMLSchema\">\r\n    <outputState>low</outputState>\r\n</IOPortData>\r\n");
			}
			return;
		}
	}

	// If a camera does not need to poll a request as often as snapshots, it can be
	// added here. Binding steps through the list.
	public ArrayList<String> getLowPriorityRequests() {
		ArrayList<String> lowPriorityRequests = new ArrayList<String>(1);
		lowPriorityRequests.add("/ISAPI/System/IO/inputs/" + nvrChannel + "/status"); // must stay in element 0.
		return lowPriorityRequests;
	}
}
