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

import static org.openhab.binding.ipcamera.IpCameraBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.openhab.binding.ipcamera.handler.IpCameraHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

public class StreamServerHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private IpCameraHandler ipCameraHandler;
    private boolean handlingMjpeg = false;

    public StreamServerHandler(IpCameraHandler ipCameraHandler) {
        this.ipCameraHandler = ipCameraHandler;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof HttpRequest) {
                HttpRequest httpRequest = (HttpRequest) msg;
                // logger.info("{}", msg);
                logger.debug("Stream Server recieved request \t{}:{}", httpRequest.method(), httpRequest.uri());
                String requestIP = "("
                        + ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress() + ")";

                if (!ipCameraHandler.config.get(CONFIG_IP_WHITELIST).toString().contains(requestIP)
                        && !ipCameraHandler.config.get(CONFIG_IP_WHITELIST).toString().contentEquals("DISABLE")) {
                    logger.warn("The request made from {} was not in the whitelist and will be ignored.", requestIP);
                    return;
                } else if ("GET".equalsIgnoreCase(httpRequest.method().toString())) {
                    if (httpRequest.uri().contains("/ipcamera.mjpeg")) {
                        if (ipCameraHandler.mjpegUri != null) {
                            ipCameraHandler.setupMjpegStreaming(true, ctx);
                            handlingMjpeg = true;
                        } else {
                            logger.error(
                                    "MJPEG stream was told to start and there is no STREAM_URL_OVERRIDE supplied.");
                        }
                    } else if (httpRequest.uri().contains("/ipcamera.m3u8")) {
                        ipCameraHandler.setupFfmpegFormat("HLS");
                        ipCameraHandler.ffmpegHLS.setKeepAlive();// setup must come first
                        sendFile(ctx, httpRequest.uri(), "application/x-mpegURL");
                    } else if (httpRequest.uri().contains(".ts")) {
                        sendFile(ctx, httpRequest.uri(), "video/MP2T");
                    } else if (httpRequest.uri().contains("/ipcamera.gif")) {
                        sendFile(ctx, httpRequest.uri(), "image/gif");
                    } else if (httpRequest.uri().contains(".jpg")) {
                        if (httpRequest.uri().contains("ipcamera.jpg")) {
                            sendSnapshotImage(ctx, "image/jpeg");
                        } else {
                            // Allow access to the preroll and postroll jpg files
                            sendFile(ctx, httpRequest.uri(), "image/jpeg");
                        }
                    } else if (httpRequest.uri().contains("/instar")) {
                        InstarHandler instar = new InstarHandler(ipCameraHandler);
                        instar.alarmTriggered(httpRequest.uri().toString());
                    }
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void sendSnapshotImage(ChannelHandlerContext ctx, String contentType) throws IOException {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_CACHE);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().add("Access-Control-Allow-Origin", "*");
        response.headers().add("Access-Control-Expose-Headers", "content-length");

        ByteBuf bbuf = Unpooled.copiedBuffer(ipCameraHandler.currentSnapshot);
        response.headers().add(HttpHeaderNames.CONTENT_LENGTH, bbuf.readableBytes());
        ctx.channel().write(response);
        ctx.channel().writeAndFlush(bbuf);
    }

    private void sendFile(ChannelHandlerContext ctx, String fileUri, String contentType) throws IOException {
        File file = new File(ipCameraHandler.config.get(CONFIG_FFMPEG_OUTPUT).toString() + fileUri);
        ChunkedFile chunkedFile = new ChunkedFile(file);
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_CACHE);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().add(HttpHeaderNames.CONTENT_LENGTH, chunkedFile.length());
        response.headers().add("Access-Control-Allow-Origin", "*");
        response.headers().add("Access-Control-Expose-Headers", "content-length");
        ctx.channel().write(response);
        ctx.channel().writeAndFlush(chunkedFile);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause.toString().contains("Connection reset by peer")) {
            logger.debug("Connection reset by peer.");
        } else if (cause.toString().contains("An established connection was aborted by the software")) {
            logger.debug("An established connection was aborted by the software");
        } else if (cause.toString().contains("An existing connection was forcibly closed by the remote host")) {
            logger.debug("An existing connection was forcibly closed by the remote host");
        } else if (cause.toString().contains("(No such file or directory)")) {
            logger.info(
                    "IpCameras file server could not find the requested file. This may happen if ffmpeg is still creating the file.");
        } else {
            logger.warn("Exception caught from stream server:{}", cause);
        }
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
                logger.debug("Stream server is going to close an idle channel.");
                ctx.close();
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.debug("Closing a StreamServerHandler.");
        if (handlingMjpeg) {
            ipCameraHandler.setupMjpegStreaming(false, ctx);
        }
    }
}
