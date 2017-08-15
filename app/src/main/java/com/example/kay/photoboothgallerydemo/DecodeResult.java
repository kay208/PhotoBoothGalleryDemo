package com.example.kay.photoboothgallerydemo;

/**
 * Created by kewei.xu on 7/12/17.
 */

public class DecodeResult {
    private byte[] videoData;
    private boolean isVideoToTheEnd;
    private long videoTimeStamp;
    private byte[] audioData;
    private boolean isAudioToTheEnd;
    private long audioTimeStamp;
//    public DecodeResult(byte[] videoData, boolean isVideoToTheEnd, long videoTimeStamp) {
//        this.videoData = videoData;
//        this.isVideoToTheEnd = isVideoToTheEnd;
//        this.videoTimeStamp = videoTimeStamp;
//    }

    public long getVideoTimeStamp() {
        return videoTimeStamp;
    }

    public void setVideoTimeStamp(long videoTimeStamp) {
        this.videoTimeStamp = videoTimeStamp;
    }

    public byte[] getVideoData() {
        return videoData;
    }

    public void setVideoData(byte[] videoData) {
        this.videoData = videoData;
    }

    public boolean isVideoToTheEnd() {
        return isVideoToTheEnd;
    }

    public void setVideoToTheEnd(boolean videoToTheEnd) {
        isVideoToTheEnd = videoToTheEnd;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    public boolean isAudioToTheEnd() {
        return isAudioToTheEnd;
    }

    public void setAudioToTheEnd(boolean audioToTheEnd) {
        isAudioToTheEnd = audioToTheEnd;
    }

    public long getAudioTimeStamp() {
        return audioTimeStamp;
    }

    public void setAudioTimeStamp(long audioTimeStamp) {
        this.audioTimeStamp = audioTimeStamp;
    }
}
