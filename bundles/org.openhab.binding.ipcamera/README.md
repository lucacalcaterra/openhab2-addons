# IP Camera Binding

This binding allows you to use IP cameras in openHAB 2 and has multiple features and ways to work around common issues that cameras present. Please take the time to read through this guide as it will show hidden features and different ways to work with cameras that you may not know about. I highly recommend purchasing a brand of camera that has an open API to give you easy access to alarms and many other advanced features that the binding has implemented. To see what each brand has implemented from the API, please see this post:

<https://community.openhab.org/t/ipcamera-new-ip-camera-binding/42771>

Each brand that has an API is listed below in alphabetical order with a link to where the API is located:

* [AMCREST](https://s3.amazonaws.com/amcrest-files/Amcrest+HTTP+API+3.2017.pdf)

* DAHUA _(see the Amcrest API link above as they work the same)_

* [DOORBIRD](https://www.doorbird.com/api)

* [FOSCAM](https://www.foscam.es/descarga/Foscam-IPCamera-CGI-User-Guide-AllPlatforms-2015.11.06.pdf)

* [GRANDSTREAM](https://www.grandstream.com/sites/default/files/Resources/grandstream_http_api_1.0.0.54.pdf)
_(not implemented, but should be possible to add)_

* [HIKVISION](https://www.hikvision.com)

* [INSTAR](https://wikiold.instar.de/index.php/List_of_CGI_commands_(HD))



## Supported Things

If using openHAB's textual configuration or when needing to setup HABPANEL/sitemaps, you are going to need to know what your camera is as a "thing type". These are listed in CAPS below. Example: The thing type for a generic onvif camera is "ONVIF".

| Thing Type ID | Description |
|-|-|
| `AMCREST` | Use for all Amcrest Cameras that do not work as a Dahua thing. This uses an older polling method for alarm detection which is not as efficient as the newer method used in Dahua. Amcrest are made by Dahua and hence their cameras can be setup as a Dahua thing. |
| `DAHUA` | Use for all current Dahua and Amcrest cameras as they support an API as well as ONVIF. |
| `DOORBIRD` | Use for all current DOORBIRD cameras as they support an API as well as ONVIF. |
| `FOSCAM` | Use for all current FOSCAM HD Cameras as they support an API as well as ONVIF. |
| `HIKVISION` | Use for all current HIKVISION Cameras as they support an API as well as ONVIF. |
| `HTTPONLY` | For any camera that is not ONVIF compatible yet still has the ability to fetch a snapshot or stream with a url. |
| `INSTAR` | Use for all current INSTAR Cameras as they support an API as well as ONVIF. |
| `ONVIF` | Use for all ONVIF Cameras from any brand that do not have an API. You gain Pan Tilt and Zoom controls and auto discovery of the snapshot and rtsp urls over a basic HTTPONLY thing. If your camera does not have PTZ you may prefer to set it up as HTTPONLY due to the camera connecting faster if it skips the extra Onvif functions. |


## Discovery

Auto discovery is not supported currently and I would love a PR if someone has experience finding cameras on a network. ONVIF documents a way to use UDP multicast to find cameras, however some cameras have this feature disabled by default for security reasons in their firmware or default settings hence why this is not high on the list to do. Currently you need to manually add the IP camera either via PaperUI or textual configuration which both are covered below in more detail.

## Binding Configuration

The binding can be configured with PaperUI by clicking on the pencil icon of any of the cameras that you have manually added via the PaperUI inbox. To add a camera just press on the PLUS (+) icon in the INBOX of PaperUI.

Cameras can also be manually configured with text files by doing the following. DO NOT try and change a setting using PaperUI after using textual configuration as the two will conflict as the text file locks the settings preventing them from changing. Because the binding is changing so much at the moment I would recommend you use textual configuration, as each time openHAB restarts it removes and adds the camera so you automatically gain any extra channels or abilities that I add. If using PaperUI, each time I add a new channel you will need to remove and re-add the camera which then gives it a new UID number (Unique ID number), which in turn can break your sitemap and HABPanel setups. Textual configuration has its advantages and locks the camera to use a simple UID which can be a plain text name like "DrivewayCamera".

The configuration parameters that can be used in textual configuration are in CAPS, descriptions can be seen in PaperUI to help guide you on what each one does:

| Parameter | Description |
|-|-|
| `IPADDRESS`| Local address of your camera or NVR |
| `PORT`| This port will be used for HTTP calls for fetching the snapshot and alarm states. |
| `ONVIF_PORT`| The port your camera uses for ONVIF connections. This is needed for PTZ movement and auto discovery of RTSP and snapshot URLs. Giving the wrong port will cause camera to connect faster if you manually provide the URLs. |
| `SERVER_PORT`| The port that will serve the video streams and images back to openHAB without authentication. It must be unique and unused for each camera that you setup. Setting the port to -1 (default), will turn all file serving off and some features may fail to work. |
| `USERNAME`| Username used to connect to your camera via Basic auth. Leave blank if your camera does not use login details. |
| `PASSWORD`| Leave blank if your camera does not use login details. |
| `ONVIF_MEDIA_PROFILE`| 0 is your cameras Mainstream and the numbers above 0 are the substreams if your camera has any. |
| `POLL_CAMERA_MS`| Time in milliseconds between checking camera states and fetching a JPG/Image. |
| `IMAGE_UPDATE_EVENTS`| The `Image` channel and JPG served on request can be set to update in a number of ways to help reduce network traffic. |
| | `0` - JPG and Image only update when updateImageNow is ON |
| | `1` - Always update JPG, Image channel follows `updateImageNow` |
| | `2` - Start Motion Alarm |
| | `3` - Start Audio Alarm |
| | `23` - Start of Motion and Audio Alarms |
| | `4` - During Motion Alarm |
| | `5` - During Audio Alarm |
| | `45` - During Motion and Audio Alarms |
| `UPDATE_IMAGE`| The startup default behavior of updating the image channel until the channel `updateImageNow` overrides. When switched OFF the image channel will NOT update unless you override this with the updateImageNow channel. |
| `NVR_CHANNEL`| Set this to `1` if it is a standalone camera, or to the input channel number of your NVR that the camera is connected to. |
| `SNAPSHOT_URL_OVERRIDE`| Leave this empty to auto detect the snapshot URL. Enter a HTTP address if you wish to override with a different address. This also makes the camera connect quicker. |
| `MOTION_URL_OVERRIDE`| Foscam only, for custom enable motion alarm use. More info found in Foscam setup below. |
| `AUDIO_URL_OVERRIDE`| Foscam only, for custom enable audio alarm use. More info found in foscam setup below. |
| `STREAM_URL_OVERRIDE`| A HTTP URL for MJPEG format streams only, RTSP not supported. |
| `FFMPEG_INPUT`| Best if this stream is in H264 format and can be RTSP or HTTP urls. Leave this blank to use the auto detected RTSP address, or enter a URL for any type of stream that ffmpeg can use as an input. |
| `FFMPEG_LOCATION`| The full path including the filename for where you have installed ffmpeg. For windows use e.g. this format: `c:\ffmpeg\bin\ffmpeg.exe` |
| `FFMPEG_OUTPUT`| The full path where ffmpeg has the ability to write files to ending with a slash. For windows use e.g. this format: `c:\openhabconf\html\ipcamera\` |
| | If you would like to expose the GIF files to your static server, you can set `FFMPEG_OUTPUT="/etc/openhab2/html/cameras/camera-name/"` |
| `FFMPEG_HLS_OUT_ARGUMENTS`| This gives you direct access to specify your own ffmpeg options to be used. Default: ``` -f lavfi -i aevalsrc=0 -acodec aac -vcodec copy -hls_flags delete_segments -segment_list_flags live -flags -global_header ``` |
| `FFMPEG_GIF_OUT_ARGUMENTS`| This gives you direct access to specify your own ffmpeg options to be used for animated GIF files. Default: ``` -filter_complex fps=4,scale=480:-2:flags=lanczos,setpts=0.25*PTS,split[o1][o2];[o1]palettegen[p];[o2]fifo[o3];[o3][p]paletteuse ``` |
| `GIF_PREROLL`| Store this many snapshots from BEFORE you trigger a GIF creation. Default: `0` |
| `GIF_POSTROLL`| How long in seconds to create a GIF from a stream. Alternatively if `GIF_PREROLL` is set to value greater than `0`, this is how many snapshots to use AFTER you trigger a GIF creation. |
| `IP_WHITELIST`| Enter any IPs inside brackets that you wish to allow to access the video stream. `DISABLE` value will turn this feature off.  Example: `IP_WHITELIST="(127.0.0.1)(192.168.0.99)"` |

Create a file called `ipcamera.things` and save it to your things folder. Inside this file enter this in plain text and modify it to your needs.

```java
// Defined a custom HLS setting to allow audio to be casted.
// Uses Onvif to fetch the urls,
hence why they are not defined here.
Thing ipcamera:DAHUA:BabyCamera "Baby Monitor" @ "Cameras"
[
    IPADDRESS="192.168.1.5",
    USERNAME="admin",
    PASSWORD="suitcase123456",
    POLL_CAMERA_MS=2000,
    SERVER_PORT=50001,
    IP_WHITELIST="DISABLE",
    IMAGE_UPDATE_EVENTS=1,
    UPDATE_IMAGE=false,
    GIF_PREROLL=0,
    GIF_POSTROLL=6,
    FFMPEG_OUTPUT="/tmpfs/babymonitor/",
    FFMPEG_HLS_OUT_ARGUMENTS="-acodec copy -vcodec copy -hls_flags delete_segments -segment_list_flags live -flags -global_header"
]

// Update JPG every second and dont update the image channel to save on CPU.
// Uses 3rd stream of camera to feed openHAB with less data than 4k on mainstream.
Thing ipcamera:HIKVISION:DrivewayCam "DrivewayCam" @ "Cameras"
[
    IPADDRESS="192.168.1.6",
    PASSWORD="suitcase123456",
    USERNAME="admin",
    POLL_CAMERA_MS=1000,
    SERVER_PORT=50002,
    IP_WHITELIST="DISABLE",
    IMAGE_UPDATE_EVENTS=1,
    UPDATE_IMAGE=false,
    GIF_PREROLL=0,
    GIF_POSTROLL=6,
    FFMPEG_OUTPUT="/tmpfs/DrivewayCam/",
    FFMPEG_INPUT="rtsp://192.168.1.62:554/Streaming/Channels/103?transportmode=unicast&profile=Profile_1"
]

// Will autofetch the urls from Onvif so they are not defined here.
// Other settings will use the defaults if they are missing.
// Example of the IP_WHITELIST is used here.
Thing ipcamera:ONVIF:003
[
    IPADDRESS="192.168.1.21",
    PASSWORD="suitcase123456",
    USERNAME="admin",
    ONVIF_PORT=80,
    PORT=80,
    SERVER_PORT=50003,
    POLL_CAMERA_MS=2000,
    FFMPEG_OUTPUT="/tmpfs/camera3/",
    IP_WHITELIST="(192.168.2.8)(192.168.2.83)(192.168.2.99)"
]

// ESP32 Cameras have the stream on a different port 81 to snapshots, this can be setup easily.
// Use JPG files as the source for animated Gifs as the camera has no rtsp stream.
Thing ipcamera:HTTPONLY:TTGoCamera "TTGo Camera" @ "Cameras"
[
    IPADDRESS="192.168.1.181",
    POLL_CAMERA_MS=1000,
    SERVER_PORT=54321,
    IP_WHITELIST="DISABLE",
    IMAGE_UPDATE_EVENTS=1,
    UPDATE_IMAGE=true,
    GIF_PREROLL=1,
    GIF_POSTROLL=6,
    SNAPSHOT_URL_OVERRIDE="http://192.168.1.181/capture",
    STREAM_URL_OVERRIDE="http://192.168.1.181:81/stream",
    FFMPEG_OUTPUT="/tmpfs/TTGoCamera/",
    FFMPEG_INPUT="http://192.168.1.181:81/stream"
]

```

Here you see the format is: `bindingID:THINGTYPE:UID` [param1="string",param2=x,param3=x]


BindingID: is always ipcamera.

THINGTYPE: is found listed above under the heading "supported things"

UID: Can be made up but it must be UNIQUE, hence why it is called uniqueID. If you use PaperUI you will notice the UID will be something like "0A78687F" which is not very nice when using it in sitemaps and rules. PaperUI will choose a new random ID each time you remove and add the camera causing you to edit your rules, items and sitemaps to make them match. You can use text to name it something useful like "DrivewayCamera" if you wish.


## Thing Configuration

Not all the configuration controls will be explained here, only the ones which are hard to understand by reading the description in PaperUI.

**IMAGE_UPDATE_EVENTS**

If you look in PaperUI you will notice that there are numbers in brackets after each option. These numbers represents the number for textual config that you can enter into the thing file which is described above. Cameras with supported alarms have more options compared to generic cameras. The channel updateImageNow can work with this setting to allow you to manually start and stop the image from updating.

**UPDATE_IMAGE**

The config `UPDATE_IMAGE` sets the default value of the switch updateImageNow when openHAB restarts.


## Channels

See PaperUI for a full list of channels and the descriptions as most are easy to understand. Any which need further explanations will be added here. Each camera brand will have different channels depending on how much of the support for an API has been added. The channels are kept consistent as much as possible from brand to brand to make upgrading to a different branded camera easier and to help when sharing rules with other users in the forum.

**updateImageNow**

This control can be used to manually start and stop updating the image if the `IMAGE_UPDATE_EVENTS` and `UPDATE_IMAGE` configs means the camera will not be updating. When ON the image will update at the `POLL_CAMERA_MS` rate. When OFF the image will update as is described by the `IMAGE_UPDATE_EVENTS` setting.

**updateGif**

When this control is turned ON it will trigger an animated Gif to be created by ffmpeg, which you will need to install on your server manually. Once the file is created the control will auto turn itself back OFF which can be used to trigger a rule to email or use Pushover/Telegram to send the file to your mobile phone. When `GIF_PREROLL` is set to a value higher than 0, the binding will create and use snapshots (jpg) instead of using the RTSP feed from the camera which is the default behavior when the `GIF_PREROLL` is set to 0 or not defined. `IMAGE_UPDATE_EVENTS` must be set to always update the image and `POLL_CAMERA_MS` sets how often the snapshot is added to the FIFO buffer that creates the animated GIF. The snapshot files are not deleted and this can be used to create and email Jpeg files also giving you a number to choose from in case your camera has delayed footage. The files are placed into the folder specified by the config `FFMPEG_OUTPUT`.

**lastMotionType**

Cameras with multiple alarm types will update this with which alarm detected motion.ie a lineCrossing, faceDetection or item stolen alarm. You can use this to create a timestamp of when the last motion was detected by creating a rule when this channel is updated.

items:

```java
String BabyCamLastMotionType "Last Motion Type" { channel="ipcamera:DAHUA:BabyCamera:lastMotionType" }
DateTime BabyCamLastMotionTime "Last Update [%1$ta %1$tR]"
```

rules:

```java
rule "Create timestamp of last movement"
    when
    Item BabyCamLastMotionType received update
    then
    BabyCamLastMotionTime.postUpdate( new DateTimeType() )
end
```


## API Access Channel

A special String channel has been added that allows you to send any GET request to Dahua cameras only. This is due to the HTTP binding currently not supporting the DIGEST method that these cameras must use in the latest firmwares. For other brands you can use the HTTP binding should a feature not have direct support in this binding. It is far better to add or request a feature so that it gets added to the binding so that all future users benefit. One goal of this binding is to save all users from needing to learn an API, instead they can use that time saved to automate with openHAB.

The reply from the camera is not captured nor returned, so this is only a 1 way GET request.
To use this feature you can simply use this command inside any rule at any time and with as many url Strings as you wish.

item:

```java
String CamAPIAccess "Access the API" { channel="ipcamera:DAHUA:001:apiAccess" }
```

Command to use in rules:

```java
CamAPIAccess.sendCommand('/cgi-bin/configManager.cgi?action=setConfig&Lighting[0][0].Mode=Off')
```

The URL must be in this format without the IP:Port info and the binding will handle the user and password for you making it far simpler to change a password on a camera without the need to update countless lines in your openHAB files.

## Full Example

Use the following examples to base your setup on to save some time. In the example below I believe older versions of OpenHAB needed a fake address in the "Image url=" line, however Openhab 2.4 and newer do not need this to work but for backwards compatibility reasons it was left in the examples. The item= overrides the url. 

NNOTE: If you used PaperUI to create the camera thing instead of textual config, you will need to ensure the 001 is replaced with the cameras UID which may look like "0A78687F". Also replace AMCREST or HIKVISION with the name of the supported thing you are using from the list above.


*.things

```java
Thing ipcamera:DAHUA:001 [
    IPADDRESS="192.168.1.5", PASSWORD="suitcase123456",
    USERNAME="admin",
    POLL_CAMERA_MS=2000,
    SERVER_PORT=50001,
    FFMPEG_OUTPUT="/tmpfs/camera1/"
]

Thing ipcamera:HIKVISION:002 [
    IPADDRESS="192.168.1.6", PASSWORD="suitcase123456",
    USERNAME="admin",
    POLL_CAMERA_MS=2000,
    SERVER_PORT=50002,
    FFMPEG_OUTPUT="/tmpfs/camera2/"
]

```

*.items

```java

Image BabyCamImage { channel="ipcamera:DAHUA:001:image" }
Switch BabyCamUpdateImage "Get new picture" { channel="ipcamera:DAHUA:001:updateImageNow" }
Switch BabyCamCreateGif "Create animated GIF" { channel="ipcamera:DAHUA:001:updateGif" }
Number BabyCamDirection "Camera Direction"
Dimmer BabyCamPan "Pan [%d] left/right" { channel="ipcamera:DAHUA:001:pan" }
Dimmer BabyCamTilt "Tilt [%d] up/down" { channel="ipcamera:DAHUA:001:tilt" }
Dimmer BabyCamZoom "Zoom [%d] in/out" { channel="ipcamera:DAHUA:001:zoom" }
Switch BabyCamEnableMotion "MotionAlarm on/off" { channel="ipcamera:DAHUA:001:enableMotionAlarm" }
Switch BabyCamMotionAlarm "Motion detected" { channel="ipcamera:DAHUA:001:motionAlarm" }
Switch BabyCamEnableAudioAlarm "AudioAlarm on/off" { channel="ipcamera:DAHUA:001:enableAudioAlarm" }
Switch BabyCamAudioAlarm "Audio detected" { channel="ipcamera:DAHUA:001:audioAlarm" }
Dimmer BabyCamAudioThreshold "Audio Threshold [%d]" { channel="ipcamera:DAHUA:001:thresholdAudioAlarm" }
Dimmer BabyCamLED "IR LED [%d]" { channel="ipcamera:DAHUA:001:enableLED" }
Switch BabyCamAutoLED "Auto IR LED" { channel="ipcamera:DAHUA:001:autoLED" }
String BabyCamTextOverlay "Text to overlay" { channel="ipcamera:DAHUA:001:textOverlay" }
String BabyCamAPIAccess "Access the API" { channel="ipcamera:DAHUA:001:apiAccess" }
String BabyCamStreamUrl "Mjpeg Stream" { channel="ipcamera:DAHUA:BabyCamera:streamUrl" }
String BabyCamHlsStreamUrl "HLS Stream" { channel="ipcamera:DAHUA:BabyCamera:hlsUrl" }
String BabyCamRTSPStreamUrl "RTSP Stream" { channel="ipcamera:DAHUA:BabyCamera:rtspUrl" }
DateTime BabyCamLastMotionTime "Time motion was last detected [%1$ta %1$tR]"
String BabyCamLastMotionType "Last Motion Type" { channel="ipcamera:DAHUA:BabyCamera:lastMotionType" }

Image CamImage { channel="ipcamera:HIKVISION:002:image" }
Switch CamUpdateImage "Get new picture" { channel="ipcamera:HIKVISION:002:updateImageNow" }
Switch CamEnableMotionAlarm "MotionAlarm on/off" { channel="ipcamera:HIKVISION:002:enableMotionAlarm" }
Switch CamMotionAlarm "Motion detected" { channel="ipcamera:HIKVISION:002:motionAlarm" }
Switch CamEnableLineAlarm "LineAlarm on/off" { channel="ipcamera:HIKVISION:002:enableLineCrossingAlarm" }
Switch CamLineAlarm "Line Alarm detected" { channel="ipcamera:HIKVISION:002:lineCrossingAlarm" }

```


*.sitemap

```java
    Text label="BabyMonitor" icon="camera"{
        Image url="http://google.com/leaveLinkAsThis" item=BabyCamImage refresh=2000
        Switch item=BabyCamDirection icon=movecontrol label="Camera Direction" mappings=[0="Room", 1="Cot", 2="Door"]
        Switch item=BabyCamUpdateImage
        Default item=BabyCamMotionAlarm icon=siren
        Default item=BabyCamAudioAlarm icon=siren
        Text label="Advanced Controls" icon="settings"{
            Switch item=BabyCamEnableMotion
            Default item=BabyCamEnableAudioAlarm
            Default item=BabyCamAudioThreshold icon=recorder
            Slider item=BabyCamLED
            Default item=BabyCamAutoLED
            Slider item=BabyCamPan icon=movecontrol
            Slider item=BabyCamTilt icon=movecontrol
            Slider item=BabyCamZoom icon=zoom
        }
    }

```

*.rules

```java
rule "Move cameras direction"
when
    Item BabyCamDirection changed
then
    switch (BabyCamDirection.state as DecimalType) {
        case 0 :{
        //Room
        BabyCamPan.sendCommand(22)
        BabyCamTilt.sendCommand(60)
        BabyCamZoom.sendCommand(0)
        }
        case 1 :{
        //Cot
        BabyCamPan.sendCommand(22)
        BabyCamTilt.sendCommand(0)
        BabyCamZoom.sendCommand(0)
        }
        case 2 : {
        //Door
        BabyCamPan.sendCommand(15)
        BabyCamTilt.sendCommand(75)
        BabyCamZoom.sendCommand(1)
        }
    }
end

rule "Camera detected crying"
when
    Item BabyCamAudioAlarm changed from OFF to ON
then
if(BabyMonitor.state==ON){

    if(MumAlerts.state==ON){
    sendNotification("mum@parentCo.com", "Mum, the baby is awake.")
    }

    if(DadAlerts.state==ON){
    sendNotification("dad@parentCo.com", "Dad, the baby is awake.")
    }

    if(TvAlerts.state==ON){
    myKodi_notification.sendCommand("Baby is crying.")
    }
}
end

```

For the above notifications to work you will need to setup multiple users with the correct email address's at the openHAB cloud.

## Image Snapshots

There are a number of ways to use snapshots with this binding, however the best way is to always request the snapshot directly from the camera unless there is a reason why this does not work. The reason for this is to keep network traffic to a minimum and to prevent creating a bottleneck with loads of traffic in and out of your openHAB servers network port.

Ways to use snapshots are:

+ Use the cameras URL and fetch it directly so it passes from the camera to your end device ie Tablet without passing any data through the openHAB server. For cameras like Dahua that refuse to allow DIGEST to be turned off this is not an option, plus the binding has some advantages which are explained below so even if your camera can work directly you may not wish to do so.
+ Request a snapshot with the url ``http://192.168.xxx.xxx:54321/ipcamera.jpg`` this will return the current snapshot which only works if the binding is setup to fetch jpg snapshots. This file does not exist on disk and is served out of ram to keep disk writes to a minimum with this binding. It also means the binding can serve a jpg file much faster than a camera can directly as a camera usually waits for a keyframe, then compresses the data, before it can be sent which all takes time.
+ Use the Create GIF feature (explained in more detail below) and use a preroll value >0. This creates a number of snapshots in the ffmpeg output folder called snapshotXXX.jpg where XXX starts at 0 and increases each poll amount of time. This means you can get a snapshot from an exact amount of time before, on or after triggering the GIF to be created. Handy for cameras which lag due to slow processors and buffering. These snapshots can be fetched either directly as they exist on disk, or via this url format. ``http://192.168.xxx.xxx:54321/snapshot0.jpg`` Where the IP is your Openhab server and the port is what is setup in the binding as the SERVER_PORT.
+ You can also read the image data directly and use it in rules, there are some examples on the forum how to do this, however it is far easier to use the above methods.
+ Also worth a mention is that you can off load cameras to a software and hardware server. These have their advantages but can be overkill depending on what you plan to do with your cameras.



## How to get working video streams

IMPORTANT:
The binding has its own file server that works by allowing access to the snapshot and video streams with no user/password for requests that come from an IP located in the white list. Requests from outside IP's or internal requests not on the white list will fail to get any answer. If you prefer to use your own firewall instead, you can also choose to make the ip whitelist equal "DISABLE" to turn this feature off and then all internal IP's will have access. All external IP access should still be blocked.

There are now multiple ways to get a moving picture:

+ Animated GIF.
+ HLS (Http Live Streaming) which uses h264 that can be used to cast to Chromecast devices and works well in iOS/Apple devices.
+ MJPEG which uses multiple jpeg files one after another to create what is called MOTION JPEG. Whilst larger in size, it is more compatible.

To get the first two video formats working, you need to install the ffmpeg program. Visit their site here to learn how <https://ffmpeg.org/>

Under Linux, Ffmpeg can be installed very easily with this command.

```
sudo apt update && sudo apt install ffmpeg
```

**MJPEG Streaming**

Cameras that have MJPEG abilities and also an API can stream to openHAB with the MJPEG format and Ffmpeg does not need to be installed. The main cameras that can do this are Amcrest, Dahua, Hikvision, Foscam HD and Instar HD. For cameras that do not auto detect the url for mjpeg streams, you will need to enter a working url for ``STREAM_URL_OVERRIDE`` This can be skipped for the already mentioned brands but check for any special setup steps for your brand in this readme. If you can not find STREAM_URL_OVERRIDE, you need to click on the pencil icon in PaperUI to edit the configuration and then scroll to the very bottom of the page and click on the SHOW MORE link.

To request the mjpeg stream from the binding, all you need to do is use this link changing the IP to that of your Openhab server and the SERVER_PORT to match the settings in the bindings setup for that camera. ipcamera.mjpeg is not changed and stays the same for all of your cameras, it is the port that changes between multiple cameras, the rest stays the same. Also see the sitemap examples below.

<http://OpenhabIP:ServerPort/ipcamera.mjpeg>


If your camera can not do MJPEG you can use this method to turn a h.264 stream into MJPEG stream.

<https://community.openhab.org/t/how-to-display-rtsp-streams-from-ip-cameras-in-openhab-and-habpanel-linux-only/69021>

Alternatively you can use 3rd party software running on a server to do the conversion. Converting from h264 to mjpeg takes a lot of CPU power to handle the conversion, so it is better to use HLS format as this will use h264 and not require a conversion that needs CPU grunt. You can run the open source motion software on a raspberry Pi with this project.

<https://github.com/ccrisan/motioneyeos/wiki>


**HLS HTTP Live Streaming**

Cameras with h264 format streams can have this copied into the HLS format which can be used to stream to Chromecasts and also display in browsers that support this format using the webview or Habpanel items. Apple devices have excellent support for HLS due to the standard being invented by Apple. Some browsers like Chrome require a plugin to be installed before being able to display the video.


To use the HLS steaming features, you need to:
1. Set a valid ``SERVER_PORT`` as the default value of -1 will turn the feature off.
2. Add any IPs that need access to the ``IP_WHITELIST`` surrounding each one in brackets (see below example). Internal IPs will trigger a warning in the logs if they are not in the whitelist, however external IPs or localhost will not trigger a warning in the logs as they are completely ignored and the binding will refuse to connect to them. This is a security feature.
3. Ensure ffmpeg is installed.
4. For cameras that do not auto detect the H264 stream which is done for ONVIF cameras, you will need to use the ``FFMPEG_INPUT`` and provide a http or rtsp link. This is used for both the HLS and animated GIF features.
5. For most brands the ``ONVIF_MEDIA_PROFILE`` needs to match the stream number you have setup for h264. This is usually 0 and is the main-stream, the higher numbers are the sub-streams if your camera has any. The DEBUG log output will help guide you with this in the openHAB.log if ONVIF is setup correctly.
6. Consider using a SSD, HDD or a tmpfs (ram drive) if using SD/flash cards as the HLS streams are written to the FFMPEG_OUTPUT folder. Only a small amount of storage is needed.


To create a tmpfs of 20mb at /tmpfs/ run this command to open the file for editing. Recommend using 20Mb per camera that uses this location although it could use less than half that amount if carefully streamlined for less ram.

```
nano /etc/fstab
```

Enter and save this at the bottom of the file using ctrl X when done.

```
tmpfs /tmpfs tmpfs defaults,nosuid,nodev,noatime,size=20m 0 0
```



Example thing file for a Dahua camera that turns off snapshots (not necessary as it can do both) and enables streaming instead....

```
Thing ipcamera:DAHUA:001 [
    IPADDRESS="192.168.1.2",
    PASSWORD="password",
    USERNAME="admin",
    POLL_CAMERA_MS=2000,
    SERVER_PORT=54321,
    IP_WHITELIST="(192.168.1.120)(192.168.1.33)(192.168.1.74)",
    IMAGE_UPDATE_EVENTS=1,
    UPDATE_IMAGE=false,
    FFMPEG_OUTPUT="/tmpfs/camera1/", 
    FFMPEG_INPUT="rtsp://192.168.1.22:554/cam/realmonitor?channel=1&subtype=0"
]


```

Sitemap examples: (Note the IP is for your openHAB server not the camera)

```
Text label="Android Stream" icon="camera"{
    Video url="http://192.168.1.9:54321/ipcamera.mjpeg" encoding="mjpeg"
}

Text label="iOS Stream" icon="camera"{
    Webview url="http://192.168.1.9:54321/ipcamera.m3u8" height=15
}

```

**ffmpeg Special settings**

To get audio working you need to have the camera include audio in the stream and in a format that is supported by Chromecast or your browser, I suggest AAC. Then you need to change the from the first line to the second one.


For cameras with no audio in the stream (default setting)

```
-f lavfi -i aevalsrc=0 -acodec aac -vcodec copy -hls_flags delete_segments
```

For cameras with audio in the stream. Note will break Chromecast if the camera does not send audio.

```bash
-acodec copy -vcodec copy -hls_flags delete_segments
```

Some browsers require larger segment sizes to prevent choppy playback, this can be done with this setting to create 10 second segment files which increases the time before you can get playback working.

```bash
-f lavfi -i aevalsrc=0 -acodec aac -vcodec copy -hls_time 10 -hls_flags delete_segments

```

**Animated GIF feature**

The cameras have a channel called `updateGif` and when this switch is turned 'ON' (either by a rule or manually) the binding will create an animated GIF called ipcamera.gif in the ffmpeg output folder. Once the file is created the switch will turn 'OFF' and this can be used to trigger a rule to send the picture via email, pushover or telegram messages. This feature saves you from using sleep commands in your rules to ensure a file is created as the control only turns off when the file is actually created. The switch can be turned on with a rule triggered by an external zwave PIR sensor or the cameras own motion alarm, the choice and the logic can be created by yourself. The feature has two options called preroll and postroll to be aware of. When preroll is 0 (the default) the binding will use the RTSP stream to fetch the amount of seconds specified in the postroll config to create the GIF from. By changing to a preroll value above 0 the binding will change to using snapshots as the source and this requires the jpeg to be updating. The time between the snapshots is the polling time of the camera (2 seconds by default) and can be raised or lowered to 1 second if you desire. The snapshots are saved to disk and can be used as a feature that is described in the snapshot section above in more detail.

You can request the gif by using this url, or by the path to where the file is stored:

<http://OpenhabIP:ServerPort/ipcamera.gif>


.items

```java
Switch DoorCamCreateGif "Create animated GIF" { channel="ipcamera:DAHUA:DoorCam:updateGif" }

```

.rules

```java
rule "Create front door camera GIF when front doorbell button pushed"
when
    Item FrontDoorbellButton changed to ON
then
    //Start creating the GIF
    DoorCamCreateGif.sendCommand(ON)
    //Cast a doorbell sound using the Chromecast binding.
    KitchenHomeHubPlayURI.sendCommand("http://192.168.1.8:8080/static/doorbell.mp3")
end

rule "Send doorbell GIF via Pushover"
when
    Item DoorCamCreateGif changed to OFF
then
    sendPushoverMessage(pushoverBuilder("Sending GIF from backyard").withApiKey("dsfhghj6546fghfg").withUser("qwerty54657").withDevice("Phone1").withAttachment("/tmpfs/DoorCam/ipcamera.gif"))
end
```

## Batch motion detection rules

In case you have more than one camera to manage, you might want to create a general rule that would react on the events.

Let's say you have a multiple cameras with motion detection, intrusion detection or others.
You can create a Group item called `gCameraEvent` that would combine all these events:

*.items:
```java
Group gCameraEvent

Switch   East_Camera_MotionAlarm                     "Motion alarm"   (gCameraEvent)  {channel="ipcamera:HIKVISION:East:motionAlarm"}
Switch   East_Camera_FieldDetectionAlarm             "Intrusion alarm"   (gCameraEvent)  {channel="ipcamera:HIKVISION:East:fieldDetectionAlarm"}
Switch   East_Camera_UpdateGif                       "Create an animated gif"   (gCameraGif)    {channel="ipcamera:HIKVISION:East:updateGif"}
DateTime East_Camera_LastMotion                      "Last motion [%1$tH:%1$tM %1$tY-%1$tm-%1$td]"
```

Then we can make a rule that would launch GIF recorder for each camera that caught motion alert:

camera.rules:
```java
import org.eclipse.smarthome.model.script.ScriptServiceUtil

rule "Update last motion"
when
    Member of gCameraEvent changed to ON
then
    val camera = triggeringItem.name.split("_Camera_").get(0)
    val time = ScriptServiceUtil.getItemRegistry.getItem(camera + "_Camera_LastMotion") as DateTimeItem
    val gif = ScriptServiceUtil.getItemRegistry.getItem(camera + "_Camera_UpdateGif") as SwitchItem
    postUpdate(time, new DateTimeType())
    sendCommand(gif, ON)
end
```

NOTE: This approach implies that you follow a specific naming convention for your items:
`[Room]_Camera_[Action]` where `Action` is either `MotionAlarm`, `UpdateGif` or `LastMotion`

### Auto renaming archived GIF files

You can also group together all "updateGif" actions by making a `gCameraGif` group and assigning all your cameras there:

```java
Group gCameraGif
Switch   Backyard_Camera_UpdateGif                   "Create an animated gif"               (gCameraGif)    {channel="ipcamera:HIKVISION:Backyard:updateGif"}
Switch   East_Camera_UpdateGif                       "Create an animated gif"               (gCameraGif)    {channel="ipcamera:HIKVISION:East:updateGif"}
Switch   Front_Camera_UpdateGif                   "Create an animated gif"                  (gCameraGif)    {channel="ipcamera:HIKVISION:Front:updateGif"}
Switch   West_Camera_UpdateGif                    "Create an animated gif"                  (gCameraGif)    {channel="ipcamera:HIKVISION:West:updateGif"}
Switch   Garage_Camera_UpdateGif                  "Create an animated gif"                  (gCameraGif)    {channel="ipcamera:HTTPONLY:Garage:updateGif"}
Switch   Driveway_Camera_UpdateGif                   "Create an animated gif"               (gCameraGif)    {channel="ipcamera:HTTPONLY:Driveway:updateGif"}
Switch   LivingRoom_Camera_UpdateGif              "Create an animated gif"                  (gCameraGif)    {channel="ipcamera:HTTPONLY:LivingRoom:updateGif"}
```

By default all GIFs are saved with `ipcamera.gif` name.
Now we can archive the old ones by renaming them with a timestamp:

camera.rules:
```java
var Timer timer = null // top of the file, optionally after imports

rule "Rename GIF Anims when Saved"
    when
        Member of gCameraGif changed to ON
   then
        val String timeNow = String::format( "%1$tY%1$tm%1$td-%1$tT", new java.util.Date ).replace(":", "")
        val camera = triggeringItem.name.split("_Camera_").get(0).toLowerCase + "/"

        // Wait 12 secs for ffmpeg to complete
        if (timer === null) {
            timer = createTimer(now.plusSeconds(12), [ |

                // Same as FFMPEG_OUTPUT parameter in Thing definition
                var filePath = "/etc/openhab2/html/cameras/camera-" + camera
                var oldFileName = (filePath + "ipcamera.gif")

                // Separator between filename and timestamp, e.g. '_', '-', '.', or whatever you prefer
                var fileDateSeparator = "_"

                // Substitute "ipCam2" to your new filename preference
                var newFileName = (filePath + "ipcamera" + fileDateSeparator + timeNow + ".gif")

                // Options are "mv" to rename file or "cp" to copy file
                var methodUsed = "cp"

                executeCommandLine("/bin/" + methodUsed + " " + oldFileName + " "  + newFileName)
                logInfo(oldFileName, "Copied to " + newFileName)
            ])
        } else {
            if(timer !== null) {
                timer.cancel
                timer = null
            }
       }
end
```

Note that the example above also implies that you use the same naming convention (`[Room]_Camera_[Action]`) and the folders where your GIF files are stored are called `/camera-[room]/` where `[room]` is simply `[Room]` but in lowercase.

## Special notes for different brands

**Amcrest**

It is better to setup your AMCREST camera as a DAHUA thing type as the old alarm checking method is used in AMCREST and the newer method is used in DAHUA that is stream based. This means less CPU load on your server and far better response to alarms if you setup as Dahua. Please read the special notes for Dahua as they will apply.

**Dahua**

The camera I have requires the snapshot set to 1 second updates and also the schedule set to record it before the snapshot will respond at 1 second rates. I found that chaning the settings to send the snapshot to a NAS without it having any NAS settings allowed the snapshot to be generated every second. The cameras default settings worked, but it improved when the motion schedule was removed for snapshots.

**Hikvision**

Each alarm you wish to use must have "Notify Surveillance Center" enabled under each alarms settings in the control panel of the camera itself. The CGI/API and also ONVIF are disabled by default on the cameras and also are needed to be enabled and a user for ONVIF created that is the same as what you have given the binding. If your camera does not have PTZ then you can leave ONVIF disabled and just enable the CGI/API that way the camera connects faster.

If you need a channel or control updated in case you have made a change with the cameras app, you can call a refresh on it by using a cron rule.

```java
import org.eclipse.smarthome.core.types.RefreshType

rule "refresh"
when
    Time cron "0 */15 * * * ? *"
then
    //your ITEMS to refresh every 15 minutes here
    Item.sendCommand(RefreshType.REFRESH)
end
```

### HikVision API differences

In case your HikVision NVR does not communicate with the binding, make sure that:

* ISAPI is enabled in the NVR settings
* ONVIF is enabled
* RTSP is enabled

Some versions of these NVRs require setting a different snapshot URL (`SNAPSHOT_URL_OVERRIDE`), as well as `FFMPEG_INPUT`.

```java
Thing ipcamera:HIKVISION:West "West Camera"
[
    IPADDRESS="192.168.0.XX",
    USERNAME="username",
    PASSWORD="password",
    SNAPSHOT_URL_OVERRIDE="/ISAPI/Streaming/channels/401/picture",
    ONVIF_PORT=8000,
    PORT=80,
    NVR_CHANNEL=4,
    SERVER_PORT=54324,
    IP_WHITELIST="DISABLE",
    GIF_PREROLL=0,
    GIF_POSTROLL=6,
    FFMPEG_OUTPUT="/etc/openhab2/html/cameras/camera-west/",
    FFMPEG_INPUT="rtsp://192.168.0.XX:554/ISAPI/Streaming/channels/401"
]
```

Where `401` in the address represents its NVR channel.


**Foscam**

+ If the user/pass is wrong the camera can lockout and refuse to answer the binding requiring a reset of the camera, so be sure the details are correct.

+ To use MJPEG streaming you need to enable one of the streams to use this format. This can be done by entering this into any browser:

```
http://ip:88/cgi-bin/CGIProxy.fcgi?cmd=setSubStreamFormat&format=1&usr=admin&pwd=password
```

+ Some FOSCAM cameras need to have a detection area listed in the URL when you enable the motion alarm. As each model has a different resolution and two different URLs, this makes it difficult to make this automatic so an override feature was added to create your own enable the alarm url. This setting is called ``MOTION_URL_OVERRIDE`` and the steps to using it are:


1. Enable the motion alarm in the web interface of your camera and setup any areas you wish movement to be ignored in ie. Tree branches moving in the wind.
2. Use any web browser to fetch this URL https://x.x.x.x/cgi-bin/CGIProxy.fcgi?cmd=getMotionDetectConfig1&usr=xxxxx&pwd=xxxxx
3. Use the information returned by the above url to create the override settings.

An example for a Foscam C2 is...

```
/cgi-bin/CGIProxy.fcgi?cmd=setMotionDetectConfig1&isEnable=1&snapInterval=1&schedule0=281474976710655&schedule1=281474976710655&schedule2=281474976710655&schedule3=281474976710655&schedule4=281474976710655&schedule5=281474976710655&schedule6=281474976710655&x1=0&y1=0&width1=10000&height1=10000&sensitivity1=1&valid1=1&linkage=6&usr=xxxxx&pwd=xxxxx
```

Another example is:

```
/cgi-bin/CGIProxy.fcgi?cmd=setMotionDetectConfig&isEnable=1&linkage=0001&sensitivity=1&triggerInterval=15&schedule0=281474976710655&schedule1=281474976710655&schedule2=281474976710655&schedule3=281474976710655&schedule4=281474976710655&schedule5=281474976710655&schedule6=281474976710655&area0=1023&area1=1023&area2=1023&area3=1023&area4=1023&area5=1023&area6=1023&area7=1023&area7=1023&area8=1023&area9=1023&usr=username&pwd=password
```


**Instar**

These cameras have the ability to call the openHAB REST API directly when an alarm occurs hence why the binding does not have the alarm switches as the camera can handle this directly. See the openHAB documentation regarding how to use the rest API and if you get this working please send me details how so I can include a more detailed setup guide here.

## Reducing log sizes

There are two log files discussed here, openHAB.log and events.log please take the time to consider both logs if a fast and stable setup is something you care about. On some systems with slow disk access like SD cards, the writing of a log file can greatly impact on performance. We can turn on/up logs to fault find issues, and then disable them to get the performance back when everything is working.


To watch the logs in realtime with Linux based setups you can use this linux command which can be done via SSH with a program called putty from a windows or mac machine.

```

tail -f /var/log/openhab2/openhab.log -f /var/log/openhab2/events.log

```

CTRL+C will close the stream. You can also use SAMBA/network shares to open or copy the file directly, but my favorite way to view the logs is with "Frontail". Frontail is another UI that can be selected like paperUI, and can be installed using the openHABian config tool.


openHAB.log This file displays the information from all bindings and can have the amount of information turned up or down on a per binding basis. The default level is INFO and is the middle level of 5 settings you can use. openHAB documentation goes into this in more detail. Using KARAF console you can use these commands to turn the logging up and down to suit your needs. If you are having issues with the binding not working with your camera, then TRACE will give me everything in DEBUG with the additional reply packets from the camera for me to use for fault finding.


```

log:set WARN org.openhab.binding.ipcamera

log:set INFO org.openhab.binding.ipcamera

log:set DEBUG org.openhab.binding.ipcamera

log:set TRACE org.openhab.binding.ipcamera

```


events.log By default openHAB will log all image updates as an event into a file called events.log, this file can quickly grow if you have multiple cameras all updating pictures every second. To reduce this if you do not want to switch to only updating the image on EVENTS like motion alarms, you then have 2 options. One is to turn off all events, the other is to filter the events before they reach the log file. openHAB does not allow normal filtering at a binding level due to the log being a pure output from the event bus.

To disable the event.log use this command in Karaf.


```

log:set WARN smarthome.event

```

To re-enable use the same command with INFO instead of WARN.

To filter out the events do the following:

```
sudo nano /var/lib/openhab2/etc/org.ops4j.pax.logging.cfg
```

Inside that file paste the following, save and then reboot.

```
############ CUSTOM FILTERS START HERE #################
# event log filter
log4j2.appender.event.filter.myfilter1.type = RegexFilter
log4j2.appender.event.filter.myfilter1.regex = .*changed from raw type.*
log4j2.appender.event.filter.myfilter1.onMatch = DENY
log4j2.appender.event.filter.myfilter1.onMisMatch = ACCEPT
################# END OF FILTERS ######################

```

You can specify the item name in the filter to remove just 1 camera, or you can use the above without the item name to remove all events from images updating which will be for other bindings as well.


## Roadmap for further development

Currently the focus is on stability and creating a good framework that allows multiple brands to be used in RULES in a consistent way. Hopefully the binding is now less work to add a new function to instead of creating stand alone scripts which are not easy for new openHAB users to find, setup or use. By consistent I mean if a camera breaks down and you wish to change brands, your rules with this binding should be easy to adapt to the new brand of camera with minimal changes. Sharing rules with others also becomes far easier if all brands are handled the same way.

If you need a feature added that is in an API and you can not program, please raise an issue ticket here at this Github project with a sample of what a browser shows when you enter in the URL and it is usually very quick to add these features.

If you wish to contribute then please create an issue ticket first to discuss how things will work before doing any coding. This is for multiple reasons due to needing to keep things CONSISTENT between brands and also easy to maintain. This list of areas that could be added are a great place to start helping with this binding if you wish to contribute. Any feedback, push requests and ideas are welcome.


If this binding becomes popular, I can look at extending the framework to support:

+ Auto find and setup cameras across your network.

+ ONVIF alarms (hopefully a Java Onvif library get released that makes this easy, a few are improving at the moment) 

+ PTZ methods for continuous move. Not a priority as the delay between a command sent and the camera moving makes this not desirable over absolute move commands.

+ PTZ preset locations. May be a good idea for cameras that do not work with absolute move.

+ 1 and 2 way audio. Keen to add this at some point for talking with people at my front door.

+ FTP/NAS features to save the images and delete old files for camera that do not have this feature built in. Not a priority for me as all my cameras have these features built in.
