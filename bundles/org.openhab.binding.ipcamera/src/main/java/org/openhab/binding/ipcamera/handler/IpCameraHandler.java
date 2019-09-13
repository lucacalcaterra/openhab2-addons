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

package org.openhab.binding.ipcamera.handler;

import static org.openhab.binding.ipcamera.IpCameraBindingConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.soap.SOAPException;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.onvif.ver10.schema.FloatRange;
import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Profile;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;
import org.onvif.ver10.schema.VideoEncoderConfiguration;
import org.openhab.binding.ipcamera.internal.AmcrestHandler;
import org.openhab.binding.ipcamera.internal.DahuaHandler;
import org.openhab.binding.ipcamera.internal.DoorBirdHandler;
import org.openhab.binding.ipcamera.internal.Ffmpeg;
import org.openhab.binding.ipcamera.internal.FoscamHandler;
import org.openhab.binding.ipcamera.internal.HikvisionHandler;
import org.openhab.binding.ipcamera.internal.InstarHandler;
import org.openhab.binding.ipcamera.internal.MyNettyAuthHandler;
import org.openhab.binding.ipcamera.internal.StreamServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.onvif.soap.OnvifDevice;
import de.onvif.soap.devices.PtzDevices;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * The {@link IpCameraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthew Skinner - Initial contribution
 */

public class IpCameraHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_ONVIF, THING_TYPE_HTTPONLY, THING_TYPE_AMCREST, THING_TYPE_DAHUA,
                    THING_TYPE_INSTAR, THING_TYPE_FOSCAM, THING_TYPE_DOORBIRD, THING_TYPE_HIKVISION));

    public final Logger logger = LoggerFactory.getLogger(getClass());
    private final ScheduledExecutorService cameraConnection = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService scheduledMovePTZ = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService pollCamera = Executors.newSingleThreadScheduledExecutor();

    public Configuration config;
    private OnvifDevice onvifCamera;
    public Ffmpeg ffmpegHLS = null;
    public Ffmpeg ffmpegGIF = null;
    private List<Profile> profiles;
    private String username;
    private String password;
    private ScheduledFuture<?> cameraConnectionJob = null;
    private ScheduledFuture<?> pollCameraJob = null;
    private int selectedMediaProfile = 0;
    private Bootstrap mainBootstrap;
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup mainEventLoopGroup = new NioEventLoopGroup();
    private EventLoopGroup serversLoopGroup = new NioEventLoopGroup();
    private FullHttpRequest putRequestWithBody;
    private String nvrChannel;
    private CircularFifoBuffer fifoSnapshotBuffer;
    private int preroll, postroll, snapCount = 0;
    private boolean updateImage = true;
    private byte lowPriorityCounter = 0;
    public String hostIp = "0.0.0.0";

    public ArrayList<String> listOfRequests = new ArrayList<String>(18);
    public ArrayList<Channel> listOfChannels = new ArrayList<Channel>(18);
    // Status can be -2=storing a reply, -1=closed, 0=closing (do not re-use
    // channel), 1=open, 2=open and ok to reuse
    public ArrayList<Byte> listOfChStatus = new ArrayList<Byte>(18);
    public ArrayList<String> listOfReplies = new ArrayList<String>(18);
    public ArrayList<String> lowPriorityRequests = null;
    public ReentrantLock lock = new ReentrantLock();
    // ChannelGroup is thread safe
    final ChannelGroup mjpegChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // basicAuth MUST remain private as it holds the password
    private String basicAuth = null;
    public boolean useDigestAuth = false;

    private String snapshotUri = null;
    public String mjpegUri = null;
    ChannelFuture serverFuture = null;
    public int serverPort = 0;
    Object firstStreamedMsg = null;
    public byte[] currentSnapshot;
    private String rtspUri = null;

    public String ipAddress = "empty";
    private String profileToken = "empty";

    private String updateImageEvents;
    public boolean audioAlarmUpdateSnapshot = false;
    public boolean motionAlarmUpdateSnapshot = false;
    boolean isOnline = false; // Used so only 1 error is logged when a network issue occurs.
    public boolean firstAudioAlarm = false;
    public boolean firstMotionAlarm = false;
    boolean shortAudioAlarm = true; // used for when the alarm is less than the polling amount of time.
    boolean shortMotionAlarm = true; // used for when the alarm is less than the polling amount of time.
    boolean movePTZ = false; // used to delay PTZ movements for when a rule changes all 3 at the same time so
                             // only 1
                             // movement is made.

    private PTZVector ptzLocation;
    private FloatRange panRange;
    private FloatRange tiltRange;
    private PtzDevices ptzDevices;
    // These hold the cameras PTZ position in the range that the camera uses, ie
    // mine is -1 to +1
    private Float currentPanCamValue = 0.0f;
    private Float currentTiltCamValue = 0.0f;
    private Float currentZoomCamValue = 0.0f;
    private Float zoomMin = 0.0f;
    private Float zoomMax = 0.0f;
    // These hold the PTZ values for updating Openhabs controls in 0-100 range
    private Float currentPanPercentage = 0.0f;
    private Float currentTiltPercentage = 0.0f;
    private Float currentZoomPercentage = 0.0f;

    public IpCameraHandler(Thing thing) {
        super(thing);
    }

    // false clears the stored user/pass hash, true creates the hash
    public void setBasicAuth(boolean useBasic) {
        if (useBasic == false) {
            logger.debug("Removing BASIC auth now and making it NULL.");
            basicAuth = null;
            return;
        }
        if (username != null && password != null) {
            String authString = username + ":" + password;
            ByteBuf byteBuf = null;
            try {
                byteBuf = Base64.encode(Unpooled.wrappedBuffer(authString.getBytes(CharsetUtil.UTF_8)));
                basicAuth = byteBuf.getCharSequence(0, byteBuf.capacity(), CharsetUtil.UTF_8).toString();
            } finally {
                if (byteBuf != null) {
                    byteBuf.release();
                    byteBuf = null;
                }
            }
        } else {
            logger.error("Camera is asking for Basic Auth when you have not provided a username and/or password !");
        }
    }

    private String getCorrectUrlFormat(String longUrl) {
        String temp = longUrl;
        URL url;

        try {
            url = new URL(longUrl);
            int port = url.getPort();
            if (port == -1) {
                if (url.getQuery() == null) {
                    temp = url.getPath();
                } else {
                    temp = url.getPath() + "?" + url.getQuery();
                }
            } else {
                if (url.getQuery() == null) {
                    temp = ":" + url.getPort() + url.getPath();
                } else {
                    temp = ":" + url.getPort() + url.getPath() + "?" + url.getQuery();
                }
            }
        } catch (MalformedURLException e) {
            logger.error("A non valid url was given to the binding {} - {}", longUrl, e);
        }
        return temp;
    }

    private void cleanChannels() {
        lock.lock();
        for (byte index = 0; index < listOfRequests.size(); index++) {
            logger.debug("Channel status is {} for URL:{}", listOfChStatus.get(index), listOfRequests.get(index));
            switch (listOfChStatus.get(index)) {
                case 2: // Open and OK to reuse
                case 1: // Open
                case 0: // Closing but still open
                    Channel channel = listOfChannels.get(index);
                    if (channel.isOpen()) {
                        break;
                    } else {
                        listOfChStatus.set(index, (byte) -1);
                        logger.warn("Cleaning the channels has just found a connection with wrong open state.");
                    }
                case -1: // closed
                    listOfRequests.remove(index);
                    listOfChStatus.remove(index);
                    listOfChannels.remove(index);
                    listOfReplies.remove(index);
                    index--;
                    break;
            }
        }
        lock.unlock();
    }

    private void closeChannel(String url) {
        lock.lock();
        try {
            for (byte index = 0; index < listOfRequests.size(); index++) {
                if (listOfRequests.get(index).equals(url)) {
                    switch (listOfChStatus.get(index)) {
                        case 2: // Still open and OK to reuse
                        case 1: // Still open
                        case 0: // Marked as closing but channel still needs to be closed.
                            Channel chan = listOfChannels.get(index);
                            chan.close();// We can't wait as OH kills any handler that takes >5 seconds.
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void closeAllChannels() {
        lock.lock();
        try {
            for (byte index = 0; index < listOfRequests.size(); index++) {
                logger.debug("Channel status is {} for URL:{}", listOfChStatus.get(index), listOfRequests.get(index));
                switch (listOfChStatus.get(index)) {
                    case 2: // Still open and ok to reuse
                    case 1: // Still open
                    case 0: // Marked as closing but channel still needs to be closed.
                        Channel chan = listOfChannels.get(index);
                        chan.close();
                        // Handlers may get shutdown by Openhab if total delay >5 secs so no wait.
                        break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void hikChangeSetting(String httpGetPutURL, String findOldValue, String newValue) {
        String body;
        byte indexInLists;
        lock.lock();
        try {
            indexInLists = (byte) listOfRequests.indexOf(httpGetPutURL);
        } finally {
            lock.unlock();
        }
        if (indexInLists >= 0) {
            lock.lock();
            if (listOfReplies.get(indexInLists) != null) {
                body = listOfReplies.get(indexInLists);
                lock.unlock();
                logger.debug("An OLD reply from the camera was:{}", body);
                body = body.replace(findOldValue, newValue);
                logger.debug("Body for this PUT is going to be:{}", body);
                FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod("PUT"),
                        httpGetPutURL);
                request.headers().set(HttpHeaderNames.HOST, ipAddress);
                request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                request.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/xml; charset=\"UTF-8\"");
                ByteBuf bbuf = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
                request.headers().set(HttpHeaderNames.CONTENT_LENGTH, bbuf.readableBytes());
                request.content().clear().writeBytes(bbuf);
                sendHttpPUT(httpGetPutURL, request);
            } else {
                lock.unlock();
            }
        } else {
            sendHttpGET(httpGetPutURL);
            logger.warn(
                    "Did not have a reply stored before hikChangeSetting was run, try again shortly as a reply has just been requested.");
        }
    }

    public void hikSendXml(String httpPutURL, String xml) {
        logger.trace("Body for PUT:{} is going to be:{}", httpPutURL, xml);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod("PUT"), httpPutURL);
        request.headers().set(HttpHeaderNames.HOST, ipAddress);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/xml; charset=\"UTF-8\"");
        ByteBuf bbuf = Unpooled.copiedBuffer(xml, StandardCharsets.UTF_8);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, bbuf.readableBytes());
        request.content().clear().writeBytes(bbuf);
        sendHttpPUT(httpPutURL, request);
    }

    public void sendHttpPUT(String httpRequestURL, FullHttpRequest request) {
        putRequestWithBody = request; // use Global so the authhandler can use it when resent with DIGEST.
        sendHttpRequest("PUT", httpRequestURL, null);
    }

    public void sendHttpGET(String httpRequestURL) {
        sendHttpRequest("GET", httpRequestURL, null);
    }

    public int getPortFromShortenedUrl(String httpRequestURL) {
        if (httpRequestURL.startsWith(":")) {
            int end = httpRequestURL.indexOf("/");
            return Integer.parseInt(httpRequestURL.substring(1, end));
        }
        return Integer.parseInt(config.get(CONFIG_PORT).toString());
    }

    public String getTinyUrl(String httpRequestURL) {
        if (httpRequestURL.startsWith(":")) {
            int beginIndex = httpRequestURL.indexOf("/");
            return httpRequestURL.substring(beginIndex);
        }
        return httpRequestURL;
    }

    // Always use this as sendHttpGET(GET/POST/PUT/DELETE, "/foo/bar",null,false)//
    // The authHandler will use the url inside a digest string as needed.
    public boolean sendHttpRequest(String httpMethod, String httpRequestURLFull, String digestString) {

        Channel ch;
        ChannelFuture chFuture = null;
        CommonCameraHandler commonHandler;
        MyNettyAuthHandler authHandler;
        AmcrestHandler amcrestHandler;
        InstarHandler instarHandler;

        int port = getPortFromShortenedUrl(httpRequestURLFull);
        String httpRequestURL = getTinyUrl(httpRequestURLFull);

        if (mainBootstrap == null) {
            mainBootstrap = new Bootstrap();
            mainBootstrap.group(mainEventLoopGroup);
            mainBootstrap.channel(NioSocketChannel.class);
            mainBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            mainBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4500);
            mainBootstrap.option(ChannelOption.SO_SNDBUF, 1024 * 8);
            mainBootstrap.option(ChannelOption.SO_RCVBUF, 1024 * 1024);
            mainBootstrap.option(ChannelOption.TCP_NODELAY, true);
            mainBootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel socketChannel) throws Exception {
                    // HIK Alarm stream needs > 9sec idle to stop stream closing
                    socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(18, 0, 0));
                    socketChannel.pipeline().addLast("HttpClientCodec", new HttpClientCodec());
                    socketChannel.pipeline().addLast("authHandler",
                            new MyNettyAuthHandler(username, password, thing.getHandler()));
                    socketChannel.pipeline().addLast("commonHandler", new CommonCameraHandler());

                    switch (thing.getThingTypeUID().getId()) {
                        case "AMCREST":
                            socketChannel.pipeline().addLast("amcrestHandler", new AmcrestHandler(thing.getHandler()));
                            break;
                        case "DAHUA":
                            socketChannel.pipeline().addLast("brandHandler",
                                    new DahuaHandler(thing.getHandler(), nvrChannel));
                            break;
                        case "DOORBIRD":
                            socketChannel.pipeline().addLast("brandHandler", new DoorBirdHandler(thing.getHandler()));
                            break;
                        case "FOSCAM":
                            socketChannel.pipeline().addLast("brandHandler",
                                    new FoscamHandler(thing.getHandler(), username, password));
                            break;
                        case "HIKVISION":
                            socketChannel.pipeline().addLast("brandHandler",
                                    new HikvisionHandler(thing.getHandler(), nvrChannel));
                            break;
                        case "INSTAR":
                            socketChannel.pipeline().addLast("instarHandler", new InstarHandler(thing.getHandler()));
                            break;
                        default:
                            socketChannel.pipeline().addLast("brandHandler",
                                    new HikvisionHandler(thing.getHandler(), nvrChannel));
                            break;
                    }
                }
            });
        }

        FullHttpRequest request;
        if (httpMethod.contentEquals("PUT")) {
            if (useDigestAuth && digestString == null) {
                request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod(httpMethod), httpRequestURL);
                request.headers().set(HttpHeaderNames.HOST, ipAddress);
                request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            } else {
                request = putRequestWithBody;
            }
        } else {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, new HttpMethod(httpMethod), httpRequestURL);
            request.headers().set(HttpHeaderNames.HOST, ipAddress);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        if (basicAuth != null) {
            if (useDigestAuth) {
                logger.warn("Camera at IP:{} had both Basic and Digest set to be used", ipAddress);
                setBasicAuth(false);
            } else {
                request.headers().set(HttpHeaderNames.AUTHORIZATION, "Basic " + basicAuth);
            }
        }

        if (useDigestAuth) {
            if (digestString != null) {
                logger.debug("Resending using a fresh DIGEST \tURL:{}", httpRequestURL);
                request.headers().set(HttpHeaderNames.AUTHORIZATION, "Digest " + digestString);
            }
        }

        logger.debug("Sending camera: {}: http://{}{}", httpMethod, ipAddress, httpRequestURL);
        lock.lock();

        byte indexInLists = -1;
        try {
            for (byte index = 0; index < listOfRequests.size(); index++) {
                boolean done = false;
                if (listOfRequests.get(index).equals(httpRequestURL)) {

                    switch (listOfChStatus.get(index)) {
                        case 2: // Open and ok to reuse
                            ch = listOfChannels.get(index);
                            if (ch.isOpen()) {
                                logger.debug("   Using the already open channel:{} \t{}:{}", index, httpMethod,
                                        httpRequestURL);
                                commonHandler = (CommonCameraHandler) ch.pipeline().get("commonHandler");
                                commonHandler.setURL(httpRequestURLFull);
                                authHandler = (MyNettyAuthHandler) ch.pipeline().get("authHandler");
                                authHandler.setURL(httpMethod, httpRequestURL);
                                ch.writeAndFlush(request);
                                request = null;
                                return true;
                            } else {
                                logger.debug("!!!! Closed Channel was marked as open, channel:{} \t{}:{}", index,
                                        httpMethod, httpRequestURL);
                            }

                        case -1: // Closed
                            indexInLists = index;
                            listOfChStatus.set(indexInLists, (byte) 1);
                            done = true;
                            break;
                    }
                    if (done) {
                        break;
                    }
                }
            }
        } finally {
            lock.unlock();
        }

        chFuture = mainBootstrap.connect(new InetSocketAddress(ipAddress, port));
        // ChannelOption.CONNECT_TIMEOUT_MILLIS means this will not hang here.
        chFuture.awaitUninterruptibly();

        if (!chFuture.isSuccess()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connection Timeout: Check your IP is correct and the camera can be reached.");
            restart();
            if (isOnline) {
                logger.error("Can not connect with HTTP to the camera at {}:{} check your network for issues!",
                        ipAddress, port);
                isOnline = false; // Stop multiple errors when camera takes a while to connect.
                cameraConnectionJob = cameraConnection.schedule(pollingCameraConnection, 8, TimeUnit.SECONDS);
            } else {
                cameraConnectionJob = cameraConnection.schedule(pollingCameraConnection, 56, TimeUnit.SECONDS);
            }
            return false;
        }

        ch = chFuture.channel();
        commonHandler = (CommonCameraHandler) ch.pipeline().get("commonHandler");
        authHandler = (MyNettyAuthHandler) ch.pipeline().get("authHandler");
        commonHandler.setURL(httpRequestURL);
        authHandler.setURL(httpMethod, httpRequestURL);

        switch (thing.getThingTypeUID().getId()) {
            case "AMCREST":
                amcrestHandler = (AmcrestHandler) ch.pipeline().get("amcrestHandler");
                amcrestHandler.setURL(httpRequestURL);
                break;
            case "INSTAR":
                instarHandler = (InstarHandler) ch.pipeline().get("instarHandler");
                instarHandler.setURL(httpRequestURL);
                break;
        }

        if (indexInLists >= 0) {
            lock.lock();
            try {
                listOfChannels.set(indexInLists, ch);
            } finally {
                lock.unlock();
            }
            logger.debug("Have re-opened  the closed channel:{} \t{}:{}", indexInLists, httpMethod, httpRequestURL);
        } else {
            lock.lock();
            try {
                listOfRequests.add(httpRequestURL);
                listOfChannels.add(ch);
                listOfChStatus.add((byte) 1);
                listOfReplies.add(null);
            } finally {
                lock.unlock();
            }
            logger.debug("Have  opened  a  brand NEW channel:{} \t{}:{}", listOfRequests.size() - 1, httpMethod,
                    httpRequestURL);
        }

        ch.writeAndFlush(request);
        // Cleanup
        request = null;
        chFuture = null;
        return true;
    }

    // These methods handle the response from all Camera brands, nothing specific to
    // any brand should be in here //
    private class CommonCameraHandler extends ChannelDuplexHandler {
        private int bytesToRecieve = 0;
        private int bytesAlreadyRecieved = 0;
        private byte[] lastSnapshot;
        private String incomingMessage;
        private String contentType = "empty";
        private Object reply = null;
        private String requestUrl;
        private boolean closeConnection = true;
        private boolean isChunked = false;

        public void setURL(String url) {
            requestUrl = url;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            HttpContent content = null;
            try {
                logger.trace(msg.toString());
                if (msg instanceof HttpResponse) {
                    HttpResponse response = (HttpResponse) msg;
                    if (response.status().code() != 401) {
                        if (!response.headers().isEmpty()) {
                            for (String name : response.headers().names()) {
                                switch (name.toLowerCase()) { // Possible localization issues doing this
                                    case "content-type":
                                        contentType = response.headers().getAsString(name);
                                        break;
                                    case "content-length":
                                        bytesToRecieve = Integer.parseInt(response.headers().getAsString(name));
                                        break;
                                    case "connection":
                                        if (response.headers().getAsString(name).contains("keep-alive")) {
                                            closeConnection = false;
                                            lock.lock();
                                            try {
                                                byte indexInLists = (byte) listOfChannels.indexOf(ctx.channel());
                                                if (indexInLists >= 0) {
                                                    listOfChStatus.set(indexInLists, (byte) 2);
                                                }
                                            } finally {
                                                lock.unlock();
                                            }
                                        }
                                        break;
                                    case "transfer-encoding":
                                        if (response.headers().getAsString(name).contains("chunked")) {
                                            isChunked = true;
                                        }
                                        break;
                                }
                            }
                            if (contentType.contains("multipart")) {
                                closeConnection = false;
                                if (mjpegUri.contains(requestUrl)) {
                                    if (msg instanceof HttpMessage) {
                                        // logger.debug("First stream packet back from camera is HttpMessage:{}",
                                        // msg);
                                        ReferenceCountUtil.retain(msg, 2);
                                        // very start of stream only
                                        firstStreamedMsg = msg;
                                        stream(msg);
                                    }
                                }
                            } else if (closeConnection) {
                                lock.lock();
                                try {
                                    byte indexInLists = (byte) listOfChannels.indexOf(ctx.channel());
                                    if (indexInLists >= 0) {
                                        listOfChStatus.set(indexInLists, (byte) 0);
                                    } else {
                                        logger.debug("!!!! Could not find the ch for a Connection: close URL:{}",
                                                requestUrl);
                                    }
                                } finally {
                                    lock.unlock();
                                }
                            }
                        }
                    }
                }

                if (msg instanceof HttpContent) {
                    if (mjpegUri.contains(requestUrl)) {
                        // multiple MJPEG stream packets come back as this.
                        // logger.trace("Stream packets back from camera is :{}", msg);
                        ReferenceCountUtil.retain(msg, 1);
                        stream(msg);
                    } else {
                        content = (HttpContent) msg;
                        // Found a TP Link camera uses Content-Type: image/jpg instead of image/jpeg
                        if (contentType.contains("image/jp")) {
                            if (bytesToRecieve == 0) {
                                bytesToRecieve = 768000; // 0.768 Mbyte when no Content-Length is sent
                                logger.debug("Camera has no Content-Length header, we have to guess how much RAM.");
                            }
                            for (int i = 0; i < content.content().capacity(); i++) {
                                if (lastSnapshot == null) {
                                    lastSnapshot = new byte[bytesToRecieve];
                                }
                                lastSnapshot[bytesAlreadyRecieved++] = content.content().getByte(i);
                            }
                            if (bytesAlreadyRecieved > bytesToRecieve) {
                                logger.error("We got too much data from the camera, please report this.");
                            }

                            if (content instanceof LastHttpContent) {
                                if (contentType.contains("image/jp") && bytesAlreadyRecieved != 0) {
                                    if (updateImage) {
                                        updateState(CHANNEL_IMAGE, new RawType(lastSnapshot, "image/jpeg"));
                                    }
                                    if (preroll > 0) {
                                        fifoSnapshotBuffer.add(lastSnapshot);
                                    }
                                    currentSnapshot = lastSnapshot;
                                    lastSnapshot = null;
                                    if (closeConnection) {
                                        logger.debug("Snapshot recieved: Binding will now close the channel.");
                                        ctx.close();
                                    } else {
                                        logger.debug("Snapshot recieved: Binding will now keep-alive the channel.");
                                    }
                                }
                            }
                        } else { // incomingMessage that is not an IMAGE
                            if (incomingMessage == null) {
                                incomingMessage = content.content().toString(CharsetUtil.UTF_8);
                            } else {
                                incomingMessage += content.content().toString(CharsetUtil.UTF_8);
                            }
                            bytesAlreadyRecieved = incomingMessage.length();
                            if (content instanceof LastHttpContent) {
                                // If it is not an image send it on to the next handler//
                                if (bytesAlreadyRecieved != 0) {
                                    reply = incomingMessage;
                                    incomingMessage = null;
                                    bytesToRecieve = 0;
                                    bytesAlreadyRecieved = 0;
                                    super.channelRead(ctx, reply);
                                }
                            }
                            // HIKVISION alertStream never has a LastHttpContent as it always stays open//
                            if (contentType.contains("multipart")) {
                                if (!contentType.contains("image/jp") && bytesAlreadyRecieved != 0) {
                                    reply = incomingMessage;
                                    incomingMessage = null;
                                    bytesToRecieve = 0;
                                    bytesAlreadyRecieved = 0;
                                    super.channelRead(ctx, reply);
                                }
                            }
                            // Foscam needs this as will other cameras with chunks//
                            if (isChunked && bytesAlreadyRecieved != 0) {
                                reply = incomingMessage;
                                incomingMessage = null;
                                bytesToRecieve = 0;
                                bytesAlreadyRecieved = 0;
                                super.channelRead(ctx, reply);
                            }
                        }
                    }
                } else { // msg is not HttpContent
                    // logger.debug("Packet back from camera is not matching HttpContent");

                    // Foscam and Amcrest cameras need this
                    if (!contentType.contains("image/jp") && bytesAlreadyRecieved != 0) {
                        reply = incomingMessage;
                        logger.debug("Packet back from camera is {}", incomingMessage);
                        incomingMessage = null;
                        bytesToRecieve = 0;
                        bytesAlreadyRecieved = 0;
                        // TODO: Following line causes NPE that gets safely caught once every few days,
                        // need to debug...
                        super.channelRead(ctx, reply);
                    }
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            lock.lock();
            try {
                byte indexInLists = (byte) listOfChannels.indexOf(ctx.channel());
                if (indexInLists >= 0) {
                    logger.debug("commonCameraHandler closed channel:{} \tURL:{}", indexInLists, requestUrl);
                    listOfChStatus.set(indexInLists, (byte) -1);
                } else {
                    if (listOfChannels.size() > 0) {
                        logger.warn("Can't find ch when removing handler \t\tURL:{}", requestUrl);
                    }
                }
            } finally {
                lock.unlock();
            }
            lastSnapshot = null;
            bytesAlreadyRecieved = 0;
            contentType = null;
            reply = null;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            lock.lock();
            try {
                byte indexInLists = (byte) listOfChannels.indexOf(ctx.channel());
                if (indexInLists >= 0) {
                    listOfChStatus.set(indexInLists, (byte) -1);
                } else {
                    logger.warn("!!!! exceptionCaught could not locate the channel to close it down");
                }
            } finally {
                lock.unlock();
            }
            logger.warn("!!!! Camera has closed the channel \tURL:{} Cause reported is: {}", requestUrl, cause);
            ctx.close();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                // If camera does not use the channel for X amount of time it will close.
                if (e.state() == IdleState.READER_IDLE) {

                    lock.lock();
                    try {
                        byte indexInLists = (byte) listOfChannels.indexOf(ctx.channel());
                        if (indexInLists >= 0) {
                            String urlToKeepOpen;
                            switch (thing.getThingTypeUID().getId()) {
                                case "DAHUA":
                                    urlToKeepOpen = listOfRequests.get(indexInLists);
                                    if ("/cgi-bin/eventManager.cgi?action=attach&codes=[All]"
                                            .contentEquals(urlToKeepOpen)) {
                                        return;
                                    }
                                    break;
                                case "HIKVISION":
                                    urlToKeepOpen = listOfRequests.get(indexInLists);
                                    if ("/ISAPI/Event/notification/alertStream".contentEquals(urlToKeepOpen)) {
                                        return;
                                    }
                                    break;
                                case "DOORBIRD":
                                    urlToKeepOpen = listOfRequests.get(indexInLists);
                                    if ("/bha-api/monitor.cgi?ring=doorbell,motionsensor"
                                            .contentEquals(urlToKeepOpen)) {
                                        return;
                                    }
                                    break;
                            }
                            logger.debug("! Channel was found idle for more than 15 seconds so closing it down. !");
                            listOfChStatus.set(indexInLists, (byte) 0);
                        } else {
                            logger.warn("!?! Channel that was found idle could not be located in our tracking. !?!");
                        }
                    } finally {
                        lock.unlock();
                    }
                    ctx.close();

                } else if (e.state() == IdleState.WRITER_IDLE) {
                    // ctx.writeAndFlush("fakePing\r\n");
                }
            }
        }

    }

    public void startStreamServer(boolean start) {

        if (!start) {
            serversLoopGroup.shutdownGracefully(8, 8, TimeUnit.SECONDS);
            serverBootstrap = null;
        } else {
            if (serverBootstrap == null) {
                InetAddress inet;
                try {
                    inet = InetAddress.getLocalHost();
                    InetAddress[] ipConnections = InetAddress.getAllByName(inet.getCanonicalHostName());
                    if (ipConnections != null) {
                        for (int i = 0; i < ipConnections.length; i++) {
                            if (ipConnections[i].isSiteLocalAddress()) {
                                hostIp = ipConnections[i].getHostAddress();
                                // logger.debug("Stream Server is serving on IP:{}", ip);
                            }
                        }
                    }
                    ipConnections = null;
                } catch (UnknownHostException e2) {
                    logger.error("Stream Server has an error finding an IP:{}", e2);
                }
                inet = null;

                try {
                    serversLoopGroup = new NioEventLoopGroup();
                    serverBootstrap = new ServerBootstrap();
                    serverBootstrap.group(serversLoopGroup);
                    serverBootstrap.channel(NioServerSocketChannel.class);
                    // IP "0.0.0.0" will bind the server to all network connections//
                    serverBootstrap.localAddress(new InetSocketAddress(hostIp, serverPort));
                    serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 10, 0));
                            socketChannel.pipeline().addLast("HttpServerCodec", new HttpServerCodec());
                            socketChannel.pipeline().addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
                            socketChannel.pipeline()
                                    .addLast(new StreamServerHandler((IpCameraHandler) thing.getHandler()));
                        }
                    });
                    serverFuture = serverBootstrap.bind().sync();
                    serverFuture.await(4000);
                    logger.info("IpCamera file server for camera {} has started on port {}", ipAddress, serverPort);
                    updateState(CHANNEL_STREAM_URL,
                            new StringType("http://" + hostIp + ":" + serverPort + "/ipcamera.mjpeg"));
                    updateState(CHANNEL_HLS_URL,
                            new StringType("http://" + hostIp + ":" + serverPort + "/ipcamera.m3u8"));
                } catch (Exception e) {
                    logger.error("Exception occured starting the new streaming server:{}", e);
                }
            }
        }
    }

    // If start is true the CTX is added to the list to stream video to, false stops
    // the stream.
    public void setupMjpegStreaming(boolean start, ChannelHandlerContext ctx) {
        if (start) {
            mjpegChannelGroup.add(ctx.channel());
            if (mjpegChannelGroup.size() == 1) {
                sendHttpGET(mjpegUri);
            } else if (firstStreamedMsg != null) {
                ctx.channel().writeAndFlush(firstStreamedMsg);
            }
        } else {
            mjpegChannelGroup.remove(ctx.channel());
            if (mjpegChannelGroup.isEmpty()) {
                logger.debug("All MJPEG streams have stopped, so closing the MJPEG source stream now.");
                closeChannel(getTinyUrl(mjpegUri));
            }
        }
    }

    public void stream(Object msg) {
        try {
            ReferenceCountUtil.retain(msg, 1);
            mjpegChannelGroup.writeAndFlush(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void storeSnapshots() {
        int count = 0;
        OutputStream fos = null;
        for (Object lastSnapshot : fifoSnapshotBuffer) {
            byte[] foo = (byte[]) lastSnapshot;
            File file = new File(config.get(CONFIG_FFMPEG_OUTPUT).toString() + "snapshot" + count + ".jpg");
            count++;
            try {
                fos = new FileOutputStream(file);
                fos.write(foo);
                fos.close();
            } catch (FileNotFoundException e) {
                logger.error("FileNotFoundException {}", e);
            } catch (IOException e) {
                logger.error("IOException {}", e);
            }
        }
    }

    public void setupFfmpegFormat(String format) {
        // Make sure the folder exists, if not create it.
        new File(config.get(CONFIG_FFMPEG_OUTPUT).toString()).mkdirs();

        switch (format) {
            case "HLS":
                if (ffmpegHLS == null) {
                    String ffmpegInput = (config.get(CONFIG_FFMPEG_INPUT) == null) ? rtspUri
                            : config.get(CONFIG_FFMPEG_INPUT).toString();

                    if (ffmpegInput.contains(":554")) {
                        ffmpegHLS = new Ffmpeg((IpCameraHandler) thing.getHandler(),
                                config.get(CONFIG_FFMPEG_LOCATION).toString(), "-rtsp_transport tcp", ffmpegInput,
                                config.get(CONFIG_FFMPEG_HLS_OUT_ARGUMENTS).toString(),
                                config.get(CONFIG_FFMPEG_OUTPUT).toString() + "ipcamera.m3u8", username, password);
                    } else {
                        ffmpegHLS = new Ffmpeg((IpCameraHandler) thing.getHandler(),
                                config.get(CONFIG_FFMPEG_LOCATION).toString(), "", ffmpegInput,
                                config.get(CONFIG_FFMPEG_HLS_OUT_ARGUMENTS).toString(),
                                config.get(CONFIG_FFMPEG_OUTPUT).toString() + "ipcamera.m3u8", username, password);
                    }
                }
                ffmpegHLS.setFormat(format);
                ffmpegHLS.startConverting();
                break;
            case "GIF":
                if (ffmpegGIF == null) {
                    if (preroll > 0) {
                        ffmpegGIF = new Ffmpeg((IpCameraHandler) thing.getHandler(),
                                config.get(CONFIG_FFMPEG_LOCATION).toString(), "-y -f image2 -framerate 1",
                                config.get(CONFIG_FFMPEG_OUTPUT).toString() + "snapshot%d.jpg",
                                config.get(CONFIG_FFMPEG_GIF_OUT_ARGUMENTS).toString(),
                                config.get(CONFIG_FFMPEG_OUTPUT).toString() + "ipcamera.gif", null, null);
                    } else {

                        String ffmpegInput = (config.get(CONFIG_FFMPEG_INPUT) == null) ? rtspUri
                                : config.get(CONFIG_FFMPEG_INPUT).toString();

                        String inOptions = "-y -t " + postroll + " -rtsp_transport tcp";
                        if (!ffmpegInput.contains("rtsp")) {
                            inOptions = "-y -t " + postroll;
                        }

                        ffmpegGIF = new Ffmpeg((IpCameraHandler) thing.getHandler(),
                                config.get(CONFIG_FFMPEG_LOCATION).toString(), inOptions, ffmpegInput,
                                config.get(CONFIG_FFMPEG_GIF_OUT_ARGUMENTS).toString(),
                                config.get(CONFIG_FFMPEG_OUTPUT).toString() + "ipcamera.gif", username, password);

                    }
                }

                if (preroll > 0) {
                    storeSnapshots();
                }

                ffmpegGIF.setFormat(format);
                ffmpegGIF.startConverting();
                break;
        }
    }

    public void motionDetected(String thisAlarmsChannel) {
        updateState(thisAlarmsChannel.toString(), OnOffType.valueOf("ON"));
        updateState(CHANNEL_LAST_MOTION_TYPE, new StringType(thisAlarmsChannel));
        if (updateImageEvents.contains("2")) {
            if (!firstMotionAlarm) {
                sendHttpGET(snapshotUri);
                firstMotionAlarm = true;
            }
        } else if (updateImageEvents.contains("4")) { // During Motion Alarms
            motionAlarmUpdateSnapshot = true;
            shortMotionAlarm = true; // used for when the alarm is less than the polling amount of time.
        }
    }

    public void audioDetected() {
        updateState(CHANNEL_AUDIO_ALARM, OnOffType.valueOf("ON"));
        if (updateImageEvents.contains("3")) {
            if (!firstAudioAlarm) {
                sendHttpGET(snapshotUri);
                firstAudioAlarm = true;
            }
        } else if (updateImageEvents.contains("5")) {// During audio alarms
            audioAlarmUpdateSnapshot = true;
            shortAudioAlarm = true; // used for when the alarm is less than the polling amount of time.
        }
    }

    public String returnValueFromString(String rawString, String searchedString) {
        String result = "";
        int index = rawString.indexOf(searchedString);
        if (index != -1) // -1 means "not found"
        {
            result = rawString.substring(index + searchedString.length(), rawString.length());
            index = result.indexOf("\r\n"); // find a carriage return to find the end of the value.
            if (index == -1) {
                return result; // Did not find a carriage return.
            } else {
                return result.substring(0, index);
            }
        }
        return null; // Did not find the String we were searching for
    }

    public String searchString(String rawString, String searchedString) {
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

    private PTZVector getPtzPosition() {
        PTZVector pv;
        try {
            pv = ptzDevices.getPosition(profileToken);
            if (pv != null) {
                return pv;
            }
        } catch (NullPointerException e) {
            logger.error("NPE occured when trying to fetch the cameras PTZ position");
        } catch (Exception e) {
            logger.error("Generic Exception occured when trying to fetch the cameras PTZ position. {}", e);
        } catch (Throwable t) {
            logger.error("A Throwable occured when trying to fetch the cameras PTZ position. {}", t);
        }
        logger.warn(
                "Camera did not give a good reply when asked what its position was, possibly due to missing zoom features. Going to fake the position so PTZ still works.");
        pv = new PTZVector();
        pv.setPanTilt(new Vector2D());
        pv.setZoom(new Vector1D());
        return pv;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.toString() == "REFRESH") {
            switch (channelUID.getId()) {
                case CHANNEL_PAN:
                    getAbsolutePan();
                    return;
                case CHANNEL_TILT:
                    getAbsoluteTilt();
                    return;
                case CHANNEL_ZOOM:
                    getAbsoluteZoom();
                    return;
            }
        } // caution "REFRESH" can still progress to brand Handlers below the else.
        else {
            switch (channelUID.getId()) {
                case CHANNEL_UPDATE_IMAGE_NOW:
                    if ("ON".equals(command.toString())) {
                        updateImage = true;
                    } else {
                        updateImage = false;
                    }
                    return;
                case CHANNEL_UPDATE_GIF:
                    if ("ON".equals(command.toString())) {
                        if (preroll > 0) {
                            snapCount = postroll;
                        } else {
                            setupFfmpegFormat("GIF");
                        }
                    }
                    return;
                case CHANNEL_PAN:
                    setAbsolutePan(Float.valueOf(command.toString()));
                    return;
                case CHANNEL_TILT:
                    setAbsoluteTilt(Float.valueOf(command.toString()));
                    return;
                case CHANNEL_ZOOM:
                    setAbsoluteZoom(Float.valueOf(command.toString()));
                    return;
            }
        }
        // commands and refresh now get passed to brand handlers
        switch (thing.getThingTypeUID().getId()) {
            case "AMCREST":
                AmcrestHandler amcrestHandler = new AmcrestHandler(thing.getHandler());
                amcrestHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests == null) {
                    lowPriorityRequests = amcrestHandler.getLowPriorityRequests();
                }
                break;
            case "DAHUA":
                DahuaHandler dahuaHandler = new DahuaHandler(thing.getHandler(), nvrChannel);
                dahuaHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests == null) {
                    lowPriorityRequests = dahuaHandler.getLowPriorityRequests();
                }
                break;
            case "DOORBIRD":
                DoorBirdHandler doorBirdHandler = new DoorBirdHandler(thing.getHandler());
                doorBirdHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests == null) {
                    lowPriorityRequests = doorBirdHandler.getLowPriorityRequests();
                }
                break;
            case "HIKVISION":
                HikvisionHandler hikvisionHandler = new HikvisionHandler(thing.getHandler(), nvrChannel);
                hikvisionHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests == null) {
                    lowPriorityRequests = hikvisionHandler.getLowPriorityRequests();
                }
                break;
            case "FOSCAM":
                FoscamHandler foscamHandler = new FoscamHandler(thing.getHandler(), username, password);
                foscamHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests == null) {
                    lowPriorityRequests = foscamHandler.getLowPriorityRequests();
                }
                break;
            case "INSTAR":
                InstarHandler instarHandler = new InstarHandler(thing.getHandler());
                instarHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests == null) {
                    lowPriorityRequests = instarHandler.getLowPriorityRequests();
                }
                break;
            default:
                HikvisionHandler defaultHandler = new HikvisionHandler(thing.getHandler(), nvrChannel);
                defaultHandler.handleCommand(channelUID, command);
                if (lowPriorityRequests == null) {
                    lowPriorityRequests = new ArrayList<String>(1);
                }
                break;
        }
    }

    public void setChannelState(String channelToUpdate, State valueOf) {
        updateState(channelToUpdate, valueOf);
    }

    void getAbsolutePan() {
        if (ptzDevices != null) {
            ptzLocation = getPtzPosition();
            currentPanPercentage = (((panRange.getMin() - ptzLocation.getPanTilt().getX()) * -1)
                    / ((panRange.getMin() - panRange.getMax()) * -1)) * 100;
            currentPanCamValue = ((((panRange.getMin() - panRange.getMax()) * -1) / 100) * currentPanPercentage
                    + panRange.getMin());
            logger.debug("Pan is updating to:{} and the cam value is {}", Math.round(currentPanPercentage),
                    currentPanCamValue);
            updateState(CHANNEL_PAN, new PercentType(Math.round(currentPanPercentage)));
        }
    }

    void getAbsoluteTilt() {
        if (ptzDevices != null) {
            currentTiltPercentage = (((tiltRange.getMin() - ptzLocation.getPanTilt().getY()) * -1)
                    / ((tiltRange.getMin() - tiltRange.getMax()) * -1)) * 100;
            currentTiltCamValue = ((((tiltRange.getMin() - tiltRange.getMax()) * -1) / 100) * currentTiltPercentage
                    + tiltRange.getMin());
            logger.debug("Tilt is updating to:{} and the cam value is {}", Math.round(currentTiltPercentage),
                    currentTiltCamValue);
            updateState(CHANNEL_TILT, new PercentType(Math.round(currentTiltPercentage)));
        }
    }

    void getAbsoluteZoom() {
        if (ptzDevices != null) {
            currentZoomPercentage = (((zoomMin - ptzLocation.getZoom().getX()) * -1) / ((zoomMin - zoomMax) * -1))
                    * 100;
            currentZoomCamValue = ((((zoomMin - zoomMax) * -1) / 100) * currentZoomPercentage + zoomMin);

            logger.debug("Zoom is updating to:{} and the cam value is {}", Math.round(currentZoomPercentage),
                    currentZoomCamValue);
            updateState(CHANNEL_ZOOM, new PercentType(Math.round(currentZoomPercentage)));
        }
    }

    void setAbsolutePan(Float panValue) {
        if (ptzDevices != null) {
            if (onvifCamera != null && panRange != null && tiltRange != null) {
                try {
                    currentPanCamValue = ((((panRange.getMin() - panRange.getMax()) * -1) / 100) * panValue
                            + panRange.getMin());
                    logger.debug("Cameras Pan  has changed to:{}", currentPanCamValue);
                    movePTZ = true;
                } catch (NullPointerException e) {
                    logger.error("NPE occured when trying to move the cameras Pan with ONVIF");
                }
            }
        }
    }

    void setAbsoluteTilt(Float tiltValue) {
        if (ptzDevices != null) {
            if (onvifCamera != null && panRange != null && tiltRange != null) {
                try {
                    currentTiltCamValue = ((((tiltRange.getMin() - tiltRange.getMax()) * -1) / 100) * tiltValue
                            + tiltRange.getMin());
                    logger.debug("Cameras Tilt has changed to:{}", currentTiltCamValue);
                    movePTZ = true;
                } catch (NullPointerException e) {
                    logger.error("NPE occured when trying to move the cameras Tilt with ONVIF");
                }
            }
        }
    }

    void setAbsoluteZoom(Float zoomValue) {
        if (ptzDevices != null) {
            if (onvifCamera != null && panRange != null && tiltRange != null) {
                try {
                    currentZoomCamValue = ((((zoomMin - zoomMax) * -1) / 100) * zoomValue + zoomMin);
                    logger.debug("Cameras Zoom has changed to:{}", currentZoomCamValue);
                    movePTZ = true;
                } catch (NullPointerException e) {
                    logger.error("NPE occured when trying to move the cameras Zoom with ONVIF");
                }
            }
        }
    }

    public String encodeSpecialChars(String text) {
        String encodedString = null;
        try {
            encodedString = URLEncoder.encode(text, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to encode special characters for URL. {}", e);
        }
        return encodedString;
    }

    Runnable pollingCameraConnection = new Runnable() {
        @Override
        public void run() {

            if (thing.getThingTypeUID().getId().equals("HTTPONLY")) {
                if (!snapshotUri.isEmpty()) {
                    logger.debug("Camera at {} has a snapshot address of:{}:", ipAddress, snapshotUri);
                    if (sendHttpRequest("GET", snapshotUri, null)) {
                        updateStatus(ThingStatus.ONLINE);
                        isOnline = true;
                        logger.info("IP Camera at {} is now online.", ipAddress);
                        pollCameraJob = pollCamera.scheduleAtFixedRate(pollingCamera, 5000,
                                Integer.parseInt(config.get(CONFIG_POLL_CAMERA_MS).toString()), TimeUnit.MILLISECONDS);
                        sendHttpGET(snapshotUri);
                        updateState(CHANNEL_IMAGE_URL, new StringType("http://" + ipAddress + snapshotUri));

                        if (updateImage) {
                            updateState(CHANNEL_UPDATE_IMAGE_NOW, OnOffType.valueOf("ON"));
                        }

                        if (!"-1".contentEquals(config.get(CONFIG_SERVER_PORT).toString())) {
                            startStreamServer(true);
                        }

                        cameraConnectionJob.cancel(false);
                        cameraConnectionJob = null;
                    }
                }
                return;
            }

            if (onvifCamera == null) {
                try {
                    logger.debug("About to connect to the IP Camera using the ONVIF PORT at IP:{}:{}", ipAddress,
                            config.get(CONFIG_ONVIF_PORT).toString());
                    if (username != null && password != null) {
                        if ("FOSCAM".contentEquals(thing.getThingTypeUID().getId())) {
                            // Foscam user/pass has been changed to remove special chars for URL format.
                            onvifCamera = new OnvifDevice(ipAddress + ":" + config.get(CONFIG_ONVIF_PORT).toString(),
                                    config.get(CONFIG_USERNAME).toString(), config.get(CONFIG_PASSWORD).toString());
                        } else {
                            onvifCamera = new OnvifDevice(ipAddress + ":" + config.get(CONFIG_ONVIF_PORT).toString(),
                                    username, password);
                        }
                    } else {
                        onvifCamera = new OnvifDevice(ipAddress + ":" + config.get(CONFIG_ONVIF_PORT).toString());
                    }

                    logger.debug("Fetching the number of Media Profiles this camera supports.");
                    profiles = onvifCamera.getDevices().getProfiles();
                    if (profiles.isEmpty()) {
                        logger.error("Camera replied with NULL when trying to get a list of the media profiles");
                    }
                    logger.debug("Checking the selected Media Profile is a valid number.");
                    if (selectedMediaProfile > profiles.size()) {
                        logger.warn(
                                "The selected Media Profile in the binding is higher than the max supported profiles. Changing to use Media Profile 0.");
                        selectedMediaProfile = 0;
                    }

                    logger.debug("Fetching a Token for the selected Media Profile.");
                    profileToken = profiles.get(selectedMediaProfile).getToken();
                    if (profileToken == null) {
                        logger.error("Camera replied with NULL when trying to get a media profile token.");
                    }
                    if (snapshotUri == null) {
                        logger.debug("Auto fetching the snapshot URL for the selected Media Profile.");
                        snapshotUri = onvifCamera.getMedia().getSnapshotUri(profileToken);
                    }

                    if (logger.isDebugEnabled()) {
                        VideoEncoderConfiguration result = profiles.get(selectedMediaProfile)
                                .getVideoEncoderConfiguration();

                        logger.debug("About to fetch some information about the Media Profiles from the camera");
                        for (int x = 0; x < profiles.size(); x++) {
                            result = profiles.get(x).getVideoEncoderConfiguration();
                            logger.debug("*********** Media Profile {} details reported by camera at IP:{} ***********",
                                    x, ipAddress);
                            if (selectedMediaProfile == x) {
                                logger.debug(
                                        "Camera will use this Media Profile unless you change it in the bindings settings.");
                            }
                            if ("JPEG".equalsIgnoreCase(result.getEncoding().toString())) {
                                logger.info("This can be used to stream MJPEG if it is reachable with a HTTP url.");
                            } else if ("H_264".equalsIgnoreCase(result.getEncoding().toString())) {
                                logger.info("This can be used to stream HLS with low CPU overhead");
                            }
                            logger.debug("Media Profile {} is named:{}", x, result.getName());
                            logger.debug("Media Profile {} uses video encoder\t:{}", x, result.getEncoding());
                            logger.debug("Media Profile {} uses video quality\t:{}", x, result.getQuality());
                            logger.debug("Media Profile {} uses video resoltion\t:{} x {}", x,
                                    result.getResolution().getWidth(), result.getResolution().getHeight());
                            logger.debug("Media Profile {} uses video bitrate\t:{}", x,
                                    result.getRateControl().getBitrateLimit());
                        }
                        logger.debug("About to interrogate the camera to see if it supports PTZ.");
                    }

                    ptzDevices = onvifCamera.getPtz();
                    if (ptzDevices != null) {

                        if (ptzDevices.isPtzOperationsSupported(profileToken)
                                && ptzDevices.isAbsoluteMoveSupported(profileToken)) {

                            logger.debug(
                                    "Camera is reporting that it supports PTZ control with Absolute movement via ONVIF");

                            logger.debug("Checking Pan now.");
                            panRange = ptzDevices.getPanSpaces(profileToken);
                            logger.debug("Checking Tilt now.");
                            tiltRange = ptzDevices.getTiltSpaces(profileToken);
                            logger.debug("Checking Zoom now.");
                            zoomMin = ptzDevices.getZoomSpaces(profileToken).getMin();
                            zoomMax = ptzDevices.getZoomSpaces(profileToken).getMax();

                            if (logger.isDebugEnabled()) {
                                logger.debug("Camera has reported the range of movements it supports via PTZ.");
                                logger.debug("The camera can Pan  from {} to {}", panRange.getMin(), panRange.getMax());
                                logger.debug("The camera can Tilt from {} to {}", tiltRange.getMin(),
                                        tiltRange.getMax());
                                logger.debug("The camera can Zoom from {} to {}", zoomMin, zoomMax);
                                logger.debug("Fetching the cameras current position.");
                            }

                            ptzLocation = getPtzPosition();

                        } else {
                            logger.debug(
                                    "Camera is reporting that it does NOT support Absolute PTZ controls via ONVIF");
                            // null will stop code from running on cameras that do not support PTZ features.
                            ptzDevices = null;
                        }
                    }

                    logger.debug(
                            "Finished with PTZ with no errors, now fetching the Video URL for RTSP from the camera.");
                    rtspUri = onvifCamera.getMedia().getRTSPStreamUri(profileToken);
                    if (rtspUri.contains(":80:")) {
                        rtspUri = rtspUri.replace(":80:", ":");
                    }

                } catch (ConnectException e) {
                    logger.debug(
                            "Can not connect with ONVIF to the camera at {}, check the ONVIF_PORT is correct. Fault was {}",
                            ipAddress, e.toString());
                } catch (SOAPException e) {
                    logger.warn(
                            "SOAP error when trying to connect with ONVIF. This may indicate your camera does not fully support ONVIF, check for an updated firmware for your camera. Will try and connect with HTTP. Camera at IP:{}, fault was {}",
                            ipAddress, e.toString());
                } catch (NullPointerException e) {
                    logger.warn("Following NPE occured when trying to connect to the camera with ONVIF.{}",
                            e.toString());
                    logger.error(
                            "Since an NPE occured when asking the camera about PTZ, the PTZ controls will not work. If the camera does not come online, give the camera the wrong ONVIF port number so it can bypass using ONVIF and still come online.");
                    ptzDevices = null;

                } catch (Exception e) {
                    logger.error("Generic Exception occured when trying to fetch the cameras PTZ ranges. {}", e);
                } catch (Throwable t) {
                    logger.error("A Throwable occured when trying to fetch the cameras PTZ ranges. {}", t);
                }
            }
            // We may be able to skip ONVIF if we have already tried and connected or failed
            // previously.
            if (snapshotUri != null) {
                if (sendHttpRequest("GET", snapshotUri, null)) {
                    updateState(CHANNEL_IMAGE_URL, new StringType("http://" + ipAddress + snapshotUri));
                    if (rtspUri != null) {
                        updateState(CHANNEL_RTSP_URL, new StringType(rtspUri));
                    }
                    if (updateImage) {
                        updateState(CHANNEL_UPDATE_IMAGE_NOW, OnOffType.valueOf("ON"));
                    }
                    pollCameraJob = pollCamera.scheduleAtFixedRate(pollingCamera, 7000,
                            Integer.parseInt(config.get(CONFIG_POLL_CAMERA_MS).toString()), TimeUnit.MILLISECONDS);
                    // Instar needs the host IP before thing comes online.
                    if (!"-1".contentEquals(config.get(CONFIG_SERVER_PORT).toString())) {
                        startStreamServer(true);
                    }
                    updateStatus(ThingStatus.ONLINE);
                    isOnline = true;
                    logger.info("IP Camera at {} is now online.", ipAddress);
                    cameraConnectionJob.cancel(false);
                    cameraConnectionJob = null;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Camera failed to report a valid Snaphot URL, try over-riding the Snapshot URL auto detection by entering a known URL.");
                logger.error(
                        "Camera failed to report a valid Snaphot URL, try over-riding the Snapshot URL auto detection by entering a known URL.");
            }
        }
    };

    boolean streamIsStopped(String url) {
        byte indexInLists = 0;
        lock.lock();
        try {
            indexInLists = (byte) listOfRequests.lastIndexOf(url);
            if (indexInLists < 0) {
                return true; // Stream not found, probably first run.
            }
            // Stream was found in list now to check status
            // Status can be -1=closed, 0=closing (do not re-use channel), 1=open , 2=open
            // and ok to reuse
            else if (listOfChStatus.get(indexInLists) < 1) {
                // may need to check if more than one is in the lists.
                return true; // Stream was open, but not now.
            }
        } finally {
            lock.unlock();
        }
        return false; // Stream is still open
    }

    Runnable pollingCamera = new Runnable() {
        @Override
        public void run() {
            // Snapshot should be first to keep consistent time between shots
            if (snapshotUri != null) {
                if (updateImageEvents.contains("1") || updateImage) {
                    sendHttpGET(snapshotUri);
                } else if (audioAlarmUpdateSnapshot || shortAudioAlarm) {
                    sendHttpGET(snapshotUri);
                    shortAudioAlarm = false;
                } else if (motionAlarmUpdateSnapshot || shortMotionAlarm) {
                    sendHttpGET(snapshotUri);
                    shortMotionAlarm = false;
                }
            }
            // NOTE: Use lowPriorityRequests if get request is not needed every poll.
            switch (thing.getThingTypeUID().getId()) {
                case "HIKVISION":
                    if (streamIsStopped("/ISAPI/Event/notification/alertStream")) {
                        logger.warn("The alarm stream was not running for camera {}, re-starting it now", ipAddress);
                        sendHttpGET("/ISAPI/Event/notification/alertStream");
                    }
                    break;
                case "AMCREST":
                    sendHttpGET("/cgi-bin/eventManager.cgi?action=getEventIndexes&code=VideoMotion");
                    sendHttpGET("/cgi-bin/eventManager.cgi?action=getEventIndexes&code=AudioMutation");
                    break;
                case "DAHUA":
                    // Check for alarms, channel for NVRs appears not to work at filtering.
                    if (streamIsStopped("/cgi-bin/eventManager.cgi?action=attach&codes=[All]")) {
                        logger.warn("The alarm stream was not running for camera {}, re-starting it now", ipAddress);
                        sendHttpGET("/cgi-bin/eventManager.cgi?action=attach&codes=[All]");
                    }
                    break;
                case "DOORBIRD":
                    // Check for alarms, channel for NVRs appears not to work at filtering.
                    if (streamIsStopped("/bha-api/monitor.cgi?ring=doorbell,motionsensor")) {
                        logger.warn("The alarm stream was not running for camera {}, re-starting it now", ipAddress);
                        sendHttpGET("/bha-api/monitor.cgi?ring=doorbell,motionsensor");
                    }
                    break;
            }

            if (!lowPriorityRequests.isEmpty()) {
                if (lowPriorityCounter >= lowPriorityRequests.size()) {
                    lowPriorityCounter = 0;
                }
                sendHttpGET(lowPriorityRequests.get(lowPriorityCounter++));
            }

            if (ffmpegHLS != null) {
                ffmpegHLS.getKeepAlive();
            }
            // Delay movements so when a rule changes all 3, a single movement is made.
            if (movePTZ) {
                movePTZ = false;
                scheduledMovePTZ.schedule(runnableMovePTZ, 50, TimeUnit.MILLISECONDS);
            }
            if (listOfRequests.size() > 12) {
                logger.info(
                        "There are {} channels being tracked, cleaning out old channels now to try and reduce this to 12 or below.",
                        listOfRequests.size());
                cleanChannels();
            }
            if (snapCount > 0) {
                if (--snapCount == 0) {
                    setupFfmpegFormat("GIF");
                }
            }
        }
    };

    Runnable runnableMovePTZ = new Runnable() {
        @Override
        public void run() {
            try {
                ptzDevices.absoluteMove(profileToken, currentPanCamValue, currentTiltCamValue, currentZoomCamValue);
            } catch (SOAPException e) {
                logger.error("SOAP exception occured");
            } catch (NullPointerException e) {
                logger.error("NPE occured when trying to move the cameras with ONVIF");
            }
        }
    };

    @Override
    public void initialize() {
        logger.debug("initialize() called.");
        config = thing.getConfiguration();
        ipAddress = config.get(CONFIG_IPADDRESS).toString();
        username = (config.get(CONFIG_USERNAME) == null) ? null : config.get(CONFIG_USERNAME).toString();
        password = (config.get(CONFIG_PASSWORD) == null) ? null : config.get(CONFIG_PASSWORD).toString();
        preroll = Integer.parseInt(config.get(CONFIG_GIF_PREROLL).toString());
        postroll = Integer.parseInt(config.get(CONFIG_GIF_POSTROLL).toString());
        fifoSnapshotBuffer = new CircularFifoBuffer(preroll + postroll);
        updateImageEvents = config.get(CONFIG_IMAGE_UPDATE_EVENTS).toString();
        updateImage = (boolean) config.get(CONFIG_UPDATE_IMAGE);

        snapshotUri = (config.get(CONFIG_SNAPSHOT_URL_OVERRIDE) == null) ? null
                : getCorrectUrlFormat(config.get(CONFIG_SNAPSHOT_URL_OVERRIDE).toString());

        mjpegUri = (config.get(CONFIG_STREAM_URL_OVERRIDE) == null) ? "noUrlGiven"
                : getCorrectUrlFormat(config.get(CONFIG_STREAM_URL_OVERRIDE).toString());

        nvrChannel = (config.get(CONFIG_NVR_CHANNEL) == null) ? null : config.get(CONFIG_NVR_CHANNEL).toString();

        selectedMediaProfile = (config.get(CONFIG_ONVIF_PROFILE_NUMBER) == null) ? 0
                : Integer.parseInt(config.get(CONFIG_ONVIF_PROFILE_NUMBER).toString());

        serverPort = Integer.parseInt(config.get(CONFIG_SERVER_PORT).toString());
        if (serverPort > -1 && serverPort < 1025) {
            logger.warn(
                    "The streaming server's port is <= 1024 and may cause permission errors under Linux, try using a higher port.");
        }

        // Known cameras will connect quicker if we skip ONVIF questions.
        switch (thing.getThingTypeUID().getId()) {
            case "AMCREST":
            case "DAHUA":
                if (mjpegUri == "noUrlGiven") {
                    mjpegUri = "/cgi-bin/mjpg/video.cgi?channel=" + nvrChannel + "&subtype=1";
                }
                if (snapshotUri == null) {
                    snapshotUri = "/cgi-bin/snapshot.cgi?channel=" + nvrChannel;
                }
                break;
            case "DOORBIRD":
                if (mjpegUri == "noUrlGiven") {
                    mjpegUri = "/bha-api/video.cgi";
                }
                if (snapshotUri == null) {
                    snapshotUri = "/bha-api/image.cgi";
                }
                break;
            case "FOSCAM":
                // Foscam needs any special char like spaces (%20) to be encoded for URLs.
                username = encodeSpecialChars(username);
                password = encodeSpecialChars(password);
                if (mjpegUri == "noUrlGiven") {
                    mjpegUri = "/cgi-bin/CGIStream.cgi?cmd=GetMJStream&usr=" + username + "&pwd=" + password;
                }
                if (snapshotUri == null) {
                    snapshotUri = "/cgi-bin/CGIProxy.fcgi?usr=" + username + "&pwd=" + password + "&cmd=snapPicture2";
                }
                break;
            case "HIKVISION":// The 02 gives you the first sub stream which needs to be set to MJPEG
                if (mjpegUri == "noUrlGiven") {
                    mjpegUri = "/ISAPI/Streaming/channels/" + nvrChannel + "02" + "/httppreview";
                }
                if (snapshotUri == null) {
                    snapshotUri = "/ISAPI/Streaming/channels/" + nvrChannel + "01/picture";
                }
                break;
            case "INSTAR":
                if (snapshotUri == null) {
                    snapshotUri = "/tmpfs/snap.jpg";
                }
                if (mjpegUri == "noUrlGiven") {
                    mjpegUri = "/mjpegstream.cgi?-chn=12";
                }
                break;
        }
        cameraConnectionJob = cameraConnection.schedule(pollingCameraConnection, 1, TimeUnit.SECONDS);
    }

    private void restart() {
        logger.info("ipCamera restarting.");

        basicAuth = null; // clear out stored password hash
        useDigestAuth = false;
        startStreamServer(false);

        if (pollCameraJob != null) {
            pollCameraJob.cancel(true);
            pollCameraJob = null;
        }
        if (cameraConnectionJob != null) {
            cameraConnectionJob.cancel(false);
            cameraConnectionJob = null;
        }

        closeAllChannels();

        if (ffmpegHLS != null) {
            ffmpegHLS.stopConverting();
            ffmpegHLS = null;
        }
        if (ffmpegGIF != null) {
            ffmpegGIF.stopConverting();
            ffmpegGIF = null;
        }

        lock.lock();
        try {
            listOfRequests.clear();
            listOfChannels.clear();
            listOfChStatus.clear();
            listOfReplies.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void dispose() {
        logger.info("ipCamera Dispose() called.");
        onvifCamera = null; // needed in case user edits password.
        restart();
    }
}
