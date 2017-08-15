package com.example.kay.photoboothgallerydemo;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.Range;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by kewei.xu on 7/10/17.
 */

public class VideoEncoder {
    private static final String VIDEO_MIME = "video/avc";
    private final static String AUDIO_MIME = "audio/mp4a-latm";
    private MediaCodec mVideoCodec;
    private int mVideoTrackIndex = -1;
    private MediaCodec.BufferInfo mVideoBufferInfo;
    private MediaCodec.BufferInfo mAudioBufferInfo;
    private MediaCodec mAudioCodec;
    private int mAudioTrackIndex = -1;
    private MediaMuxer mMuxer;
    private static final String TAG = "VideoEncoder";
    private static final int FRAME_RATE = 30;
    private static final int IFRAME_INTERVAL = 2;
    private int mWidth;
    private int mHeight;
    private int mDegrees;
    private boolean isVideoEncodeDone;
    private boolean isAudioEncodeDone;

    public void VideoEncodePrepare(MediaFormat videoMediaFormat, MediaFormat audioMediaFormat) {

        String outputPath = new File(Environment.getExternalStorageDirectory(),
                "mytest.mp4").toString();

        mWidth = videoMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
        mHeight = videoMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
        createAndStartVideoCodec(videoMediaFormat);
        createAndStartAudioCodec(audioMediaFormat);
        try {
            mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mVideoTrackIndex = -1;
        mAudioTrackIndex = -1;
        isVideoEncodeDone = false;
        isAudioEncodeDone = false;
    }
    
    private void createAndStartVideoCodec(MediaFormat videoMediaFormat) {
        int frameRate = videoMediaFormat.getInteger(videoMediaFormat.KEY_FRAME_RATE);
        int bitRate = 32 * mWidth * mHeight * frameRate / 100;

        VideoInputSpec spec = queryProperInput(mWidth, mHeight, bitRate);

        int supportedWidth = spec.width;
        int supportedHeight = spec.height;
        int supportedFrameRate = spec.frameRate;
        int supportedBitrate = spec.bitRate;
//        String codecName = spec.codecName;

//        MediaCodecInfo codecInfo = selectCodec(VIDEO_MIME);
//        int mColorFormat = selectColorFormat(codecInfo, VIDEO_MIME);

        Log.w(TAG, String.format("Width is %d ,height is %d , frame rate is %d, bitRate is %d", supportedWidth, supportedHeight, supportedFrameRate, supportedBitrate));
        MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME, supportedWidth, supportedHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        format.setInteger(MediaFormat.KEY_BIT_RATE, supportedBitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, supportedFrameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        mVideoBufferInfo = new MediaCodec.BufferInfo();

        try {
            mVideoCodec = MediaCodec.createEncoderByType(VIDEO_MIME);
            mVideoCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mVideoCodec.start();

        } catch (Exception e) {
            throw new RuntimeException("failed init encoder", e);
        }
    }

    private void createAndStartAudioCodec(MediaFormat audioMediaFormat) {
        int sampleRate = audioMediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channelCount = audioMediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int bitRate = audioMediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
        Log.d(TAG, String.format("createAndStartAudioCodec %d, %d, %d", sampleRate, channelCount, bitRate));
        MediaFormat format = MediaFormat.createAudioFormat(AUDIO_MIME, sampleRate, channelCount);
//        format.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        mAudioBufferInfo = new MediaCodec.BufferInfo();

        try {
            mAudioCodec = MediaCodec.createEncoderByType(AUDIO_MIME);
            mAudioCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mAudioCodec.start();

        } catch (Exception e) {
            throw new RuntimeException("failed init encoder", e);
        }
    }

//    private static MediaCodecInfo selectCodec(String mimeType) {
//        int numCodecs = MediaCodecList.getCodecCount();
//        for (int i = 0; i < numCodecs; i++) {
//            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
//
//            if (!codecInfo.isEncoder()) {
//                continue;
//            }
//
//            String[] types = codecInfo.getSupportedTypes();
//            for (int j = 0; j < types.length; j++) {
//                if (types[j].equalsIgnoreCase(mimeType)) {
//                    return codecInfo;
//                }
//            }
//        }
//        return null;
//    }

//    private static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
//        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
//        for (int i = 0; i < capabilities.colorFormats.length; i++) {
//            int colorFormat = capabilities.colorFormats[i];
//            if (isRecognizedFormat(colorFormat)) {
//                return colorFormat;
//            }
//        }
//        return 0;
//    }
//
//    private static boolean isRecognizedFormat(int colorFormat) {
//        switch (colorFormat) {
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
//            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
//                return true;
//            default:
//                return false;
//        }
//    }

    public void close() {
        mVideoCodec.stop();
        mVideoCodec.release();
        mVideoCodec = null;

        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }

    /**
     *
     * @param result
     * @return if the encode is done
     */
    public boolean encodeOneFrame(DecodeResult result) {
        if (!isVideoEncodeDone) {
            offerVideoFrameData(result);
        }
        if (!isAudioEncodeDone) {
            offerAudioFrameData(result);
        }
        return isVideoEncodeDone && isAudioEncodeDone;
    }

    private void offerVideoFrameData(DecodeResult input) throws MediaCodec.CodecException{
        boolean needReadOutputAgain = false;
        try {
            ByteBuffer outputBuffer;
            while (true) {

                if (!needReadOutputAgain) {
                    int inputBufferIndex = mVideoCodec.dequeueInputBuffer(-1);

                    if (inputBufferIndex >= 0) {
                        long ptsUsec = input.getVideoTimeStamp();
                        if (input.isVideoToTheEnd() || input.getVideoData().length == 0) {
                            // Send an empty frame with the end-of-stream flag set.  If we set EOS
                            // on a frame with data, that frame data will be ignored, and the
                            // output will be short one frame.
                            mVideoCodec.queueInputBuffer(inputBufferIndex, 0, 0, ptsUsec,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            needReadOutputAgain = true;
                            Log.d(TAG, "sent video input EOS (with zero-length frame)");
                        } else {
                            ByteBuffer inputBuffer = mVideoCodec.getInputBuffer(inputBufferIndex);
                            inputBuffer.clear();
                            inputBuffer.put(input.getVideoData());
                            mVideoCodec.queueInputBuffer(inputBufferIndex, 0, input.getVideoData().length, ptsUsec, 0);
                            Log.d(TAG, "video encode buffer timestamp:" + ptsUsec);
                        }
                    } else {
                        Log.i(TAG, "video input buffer not read");
                    }
                }

                int outputBufferIndex = mVideoCodec.dequeueOutputBuffer(mVideoBufferInfo, -1);
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    Log.d(TAG, "no output from video encoder available");
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // not expected for an encoder
                    MediaFormat newFormat = mVideoCodec.getOutputFormat();
                    Log.d(TAG, "video encoder output format changed: " + newFormat);

                    // now that we have the Magic Goodies, start the muxer
                    mMuxer.setOrientationHint(mDegrees);
                    mVideoTrackIndex = mMuxer.addTrack(newFormat);
                    synchronized (VideoEncoder.this) {
                        if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1) {
                            mMuxer.start();
                        }
                    }
                } else if (outputBufferIndex < 0) {
                    Log.d(TAG,"unexpected result from video encoder.dequeueOutputBuffer: " + outputBufferIndex);
                } else { // encoderStatus >= 0
                    outputBuffer = mVideoCodec.getOutputBuffer(outputBufferIndex);
                    if (outputBuffer == null) {
                        Log.d(TAG,"video encoderOutputBuffer " + outputBufferIndex + " was null");
                    }

                    if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // Codec config info.  Only expected on first packet.  One way to
                        // handle this is to manually stuff the data into the MediaFormat
                        // and pass that to configure().  We do that here to exercise the API.
//                        MediaFormat format =
//                                MediaFormat.createVideoFormat(VIDEO_MIME, mWidth, mHeight);
//                        format.setByteBuffer("csd-0", outputBuffer);

                        mVideoBufferInfo.size = 0;

                    } else {
                        // Get a decoder input buffer, blocking until it's available.
                        if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            needReadOutputAgain = false;
                            isVideoEncodeDone = true;
                            Log.d(TAG, "video encode to the end");
                        }
                    }

                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.

                    if (mVideoBufferInfo.size != 0) {
                        outputBuffer.position(mVideoBufferInfo.offset);
                        outputBuffer.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);
                        mMuxer.writeSampleData(mVideoTrackIndex, outputBuffer, mVideoBufferInfo);
                    }

                    mVideoCodec.releaseOutputBuffer(outputBufferIndex, false);
                    if (!needReadOutputAgain) {
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void offerAudioFrameData(DecodeResult input) throws MediaCodec.CodecException{
        boolean needReadOutputAgain = false;
        try {
            ByteBuffer outputBuffer;
            while (true) {

                if (!needReadOutputAgain) {
                    int inputBufferIndex = mAudioCodec.dequeueInputBuffer(200);

                    if (inputBufferIndex >= 0) {
                        long ptsUsec = input.getAudioTimeStamp();
                        if (input.isAudioToTheEnd() || input.getAudioData().length == 0) {
                            // Send an empty frame with the end-of-stream flag set.  If we set EOS
                            // on a frame with data, that frame data will be ignored, and the
                            // output will be short one frame.
                            mAudioCodec.queueInputBuffer(inputBufferIndex, 0, 0, ptsUsec,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            needReadOutputAgain = true;
                            Log.d(TAG, "sent audio input EOS (with zero-length frame)");
                        } else {
                            ByteBuffer inputBuffer = mAudioCodec.getInputBuffer(inputBufferIndex);
                            inputBuffer.clear();
                            inputBuffer.put(input.getAudioData());
                            mAudioCodec.queueInputBuffer(inputBufferIndex, 0, input.getAudioData().length, ptsUsec, 0);
                            Log.d(TAG, "audio encode buffer timestamp:" + ptsUsec);
                        }
                    } else {
                        Log.i(TAG, "audio input buffer not read");
                    }
                }

                int outputBufferIndex = mAudioCodec.dequeueOutputBuffer(mAudioBufferInfo, 200);
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    Log.d(TAG, "no output from audio encoder available");
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // not expected for an encoder
                    MediaFormat newFormat = mAudioCodec.getOutputFormat();
                    Log.d(TAG, "audio encoder output format changed: " + newFormat);

                    // now that we have the Magic Goodies, start the muxer
                    synchronized (VideoEncoder.this) {
                        mAudioTrackIndex = mMuxer.addTrack(newFormat);
                        if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1) {
                            mMuxer.start();
                        }
                    }
                } else if (outputBufferIndex < 0) {
                    Log.d(TAG,"unexpected result from audio encoder.dequeueOutputBuffer: " + outputBufferIndex);
                } else { // encoderStatus >= 0
                    outputBuffer = mAudioCodec.getOutputBuffer(outputBufferIndex);
                    if (outputBuffer == null) {
                        Log.d(TAG,"audio encoderOutputBuffer " + outputBufferIndex + " was null");
                    }

                    if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // Codec config info.  Only expected on first packet.  One way to
                        // handle this is to manually stuff the data into the MediaFormat
                        // and pass that to configure().  We do that here to exercise the API.
//                        MediaFormat format =
//                                MediaFormat.createVideoFormat(VIDEO_MIME, mWidth, mHeight);
//                        format.setByteBuffer("csd-0", outputBuffer);

                        mAudioBufferInfo.size = 0;

                    } else {
                        // Get a decoder input buffer, blocking until it's available.
                        if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            needReadOutputAgain = false;
                            isAudioEncodeDone = true;
                            Log.d(TAG, "audio encode to the end");
                        }
                    }

                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.

                    if (mAudioBufferInfo.size != 0) {
                        outputBuffer.position(mAudioBufferInfo.offset);
                        outputBuffer.limit(mAudioBufferInfo.offset + mAudioBufferInfo.size);
                        mMuxer.writeSampleData(mAudioTrackIndex, outputBuffer, mAudioBufferInfo);
                    }

                    mAudioCodec.releaseOutputBuffer(outputBufferIndex, false);
                    if (!needReadOutputAgain) {
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
    }

    public void setDegrees(int degrees) {
        this.mDegrees = degrees;
    }

//    public void setOnEncodeCompletedCallback(OnEncodeCompletedCallback onEncodeCompletedCallback) {
//        this.onEncodeCompletedCallback = onEncodeCompletedCallback;
//    }
//
//    interface OnEncodeCompletedCallback {
//        void onEncodeCompleted();
//    }

    protected class VideoInputSpec{
        public int width;
        public int height;
        public int bitRate;
        public int frameRate;
        public String codecName;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected VideoInputSpec queryProperInput(int width,int height,int bitRate){
        MediaCodecInfo[] infos = new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos();
        String codecName=null;
        int supportedWidth=width;
        int supportedHeight=height;
        int supportedFrameRate=FRAME_RATE;
        int supportedBitrate=bitRate;
        int maxArea=0;
        for(MediaCodecInfo info:infos){
            if(!info.isEncoder()){
                continue;
            }
            boolean isCodecTypeSupported=false;
            String[] supportedTypes=info.getSupportedTypes();
            for(String type:supportedTypes){
                if(type.equals(VIDEO_MIME)){
                    isCodecTypeSupported=true;
                    break;
                }
            }
            if(!isCodecTypeSupported){
                continue;
            }

            Log.w(TAG, "current codec name is  " + info.getName());
            MediaCodecInfo.CodecCapabilities capabilities=info.getCapabilitiesForType(VIDEO_MIME);
            boolean isSurfaceInputSupported=false;
            int[] formats=capabilities.colorFormats;
            for(int format:formats){
                if(format==MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface){
                    isSurfaceInputSupported=true;
                    break;
                }
            }
            if(!isSurfaceInputSupported){
                Log.w(TAG,"Surface input not supported");
                continue;
            }
            Range<Integer> widthRange=capabilities.getVideoCapabilities().getSupportedWidths();
            Range<Integer> heightRange=capabilities.getVideoCapabilities().getSupportedHeights();

            if((widthRange.contains(width)&&heightRange.contains(height))||
                    (widthRange.contains(height)&&heightRange.contains(width))){
                supportedWidth=width;
                supportedHeight=height;
                int upperFrameRate=capabilities.getVideoCapabilities().getSupportedFrameRates().getUpper();
                supportedFrameRate=upperFrameRate>FRAME_RATE?FRAME_RATE:upperFrameRate;
                int upperBitRate=capabilities.getVideoCapabilities().getBitrateRange().getUpper();
                supportedBitrate=upperBitRate>bitRate?bitRate:upperBitRate;
                codecName=info.getName();
                Log.w(TAG,String.format("supported width is %d ,height is %d , frameRate is %d , bitRate is %d",width,height,supportedFrameRate,supportedBitrate));
                Log.w(TAG,"expected spec supported");
                break;
            }else{//Expected width or height is larger than supported ones
                int maxWidth=widthRange.getUpper();
                int maxHeight=heightRange.getUpper();
                int maxLongerSide=Math.max(maxWidth,maxHeight);
                int maxShorterSide=Math.min(maxWidth,maxHeight);
                Log.w(TAG,String.format("looking for spec:%d*%d",maxWidth,maxHeight));

                if(width>=height){
                    maxWidth=maxLongerSide;
                    maxHeight=maxShorterSide;
                }else{
                    maxWidth=maxShorterSide;
                    maxHeight=maxLongerSide;
                }

                int maxScaledWidth=maxHeight*width/height;
                int maxScaledHeight=maxWidth*height/width;
                if(maxScaledWidth<width){
                    maxScaledWidth=maxScaledWidth-maxScaledWidth%8;//8 pixel alignment
                    int area=maxScaledWidth*maxHeight;
                    if(area>=maxArea) {
                        supportedWidth = maxScaledWidth;
                        supportedHeight = maxHeight;
                        maxArea=area;
                        int upperFrameRate=capabilities.getVideoCapabilities().getSupportedFrameRates().getUpper();
                        supportedFrameRate=upperFrameRate>FRAME_RATE?FRAME_RATE:upperFrameRate;
                        int upperBitRate=capabilities.getVideoCapabilities().getBitrateRange().getUpper();
                        supportedBitrate=upperBitRate>bitRate?bitRate:upperBitRate;
                        codecName=info.getName();
                    }
                }else{
                    maxScaledHeight=maxScaledHeight-maxScaledHeight%8;
                    int area=maxScaledHeight*maxWidth;
                    if(area>=maxArea) {
                        supportedWidth = maxWidth;
                        supportedHeight = maxScaledHeight;//8 step alignment
                        maxArea=area;
                        int upperFrameRate=capabilities.getVideoCapabilities().getSupportedFrameRates().getUpper();
                        supportedFrameRate=upperFrameRate>FRAME_RATE?FRAME_RATE:upperFrameRate;
                        int upperBitRate=capabilities.getVideoCapabilities().getBitrateRange().getUpper();
                        supportedBitrate=upperBitRate>bitRate?bitRate:upperBitRate;
                        codecName=info.getName();
                    }
                }
            }

        }

        VideoInputSpec spec=new VideoInputSpec();
        spec.width=supportedWidth;
        spec.height=supportedHeight;
        spec.bitRate=supportedBitrate;
        spec.frameRate=supportedFrameRate;
        spec.codecName=codecName;
        return spec;
    }
}
