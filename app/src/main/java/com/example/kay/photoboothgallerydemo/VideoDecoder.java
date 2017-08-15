package com.example.kay.photoboothgallerydemo;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;


public class VideoDecoder {
    private static final String TAG = "VideoToFrames";
    private static final boolean VERBOSE = false;
    private final int decodeColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;

    private int mImageWidth = 0;
    private int mImageHeight = 0;

    private MediaExtractor mExtractor = null;

    private MediaCodec mVideoDecoder = null;

    private MediaFormat mVideoMediaFormat;
    private int mVideoTrackIndex;

    private MediaCodec mAudioDecoder = null;
    private MediaFormat mAudioMediaFormat;
    private int mAudioTrackIndex;

    private boolean isVideoDecodeDone;
    private boolean isAudioDecodeDone;
    public MediaFormat VideoDecodePrepare(String videoFilePath) {
        try {
            File videoFile = new File(videoFilePath);
            mExtractor = new MediaExtractor();
            mExtractor.setDataSource(videoFile.toString());
            selectTrack(mExtractor);
            //prepare video codec
            if (mVideoTrackIndex < 0) {
                throw new RuntimeException("No video track found in " + videoFilePath);
            }
            mVideoMediaFormat = mExtractor.getTrackFormat(mVideoTrackIndex);
            String videoMime = mVideoMediaFormat.getString(MediaFormat.KEY_MIME);
            mVideoDecoder = MediaCodec.createDecoderByType(videoMime);
            showSupportedColorFormat(mVideoDecoder.getCodecInfo().getCapabilitiesForType(videoMime));
            if (isColorFormatSupported(decodeColorFormat, mVideoDecoder.getCodecInfo().getCapabilitiesForType(videoMime))) {
                mVideoMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, decodeColorFormat);
                Log.i(TAG, "set decode color format to type " + decodeColorFormat);
            } else {
                Log.i(TAG, "unable to set decode color format, color format type " + decodeColorFormat + " not supported");
            }
            mVideoDecoder.configure(mVideoMediaFormat, null, null, 0);
            mVideoDecoder.start();
            isVideoDecodeDone = false;

            //prepare audio codec
            if (mAudioTrackIndex < 0) {
                throw new RuntimeException("No audio track found in " + videoFilePath);
            }
            mAudioMediaFormat = mExtractor.getTrackFormat(mAudioTrackIndex);
            String audioMime = mAudioMediaFormat.getString(MediaFormat.KEY_MIME);
            mAudioDecoder = MediaCodec.createDecoderByType(audioMime);
            mAudioDecoder.configure(mAudioMediaFormat, null, null, 0);
            mAudioDecoder.start();
            isAudioDecodeDone = false;
        } catch (IOException ioe) {
            throw new RuntimeException("failed init decoder", ioe);
        }

        return mVideoMediaFormat;
    }

    public void close() {
        mVideoDecoder.stop();
        mVideoDecoder.release();
        mVideoDecoder = null;

        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
        }
    }

    private void showSupportedColorFormat(MediaCodecInfo.CodecCapabilities caps) {
        System.out.print("supported color format: ");
        for (int c : caps.colorFormats) {
            System.out.print(c + "\t");
        }
        System.out.println();
    }

    private boolean isColorFormatSupported(int colorFormat, MediaCodecInfo.CodecCapabilities caps) {
        for (int c : caps.colorFormats) {
            if (c == colorFormat) {
                return true;
            }
        }
        return false;
    }

    public DecodeResult decodeOneFrame() {
        DecodeResult result = new DecodeResult();
        if (!isVideoDecodeDone) {
            getDecodeNv12Frame(result);
        } else {
            result.setVideoToTheEnd(true);
        }
        if (!isAudioDecodeDone) {
            getDecodeAudioFrame(result);
        } else {
            result.setAudioToTheEnd(true);
        }
        return result;
    }

    private void getDecodeNv12Frame(DecodeResult result) {
        mExtractor.selectTrack(mVideoTrackIndex);
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputBufferId = mVideoDecoder.dequeueInputBuffer(-1);
        if (inputBufferId >= 0) {
            ByteBuffer inputBuffer = mVideoDecoder.getInputBuffer(inputBufferId);
            int sampleSize = mExtractor.readSampleData(inputBuffer, 0); //read data to the inputBuffer, size is sampleSize
            if (sampleSize < 0) {
                mVideoDecoder.queueInputBuffer(inputBufferId, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                long presentationTimeUs = mExtractor.getSampleTime();
                mVideoDecoder.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTimeUs, 0);
                mExtractor.advance();
            }
        }
        int outputBufferId = mVideoDecoder.dequeueOutputBuffer(info, -1);
        if (outputBufferId >= 0) {
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "video decode to the end");
                result.setVideoData(new byte[0]);
                result.setVideoToTheEnd(true);
                isVideoDecodeDone = true;
            }
            boolean doRender = (info.size != 0);
            if (doRender) {
                Image image = mVideoDecoder.getOutputImage(outputBufferId);
                result.setVideoData(getDataFromImage(image));
                image.close();
                mVideoDecoder.releaseOutputBuffer(outputBufferId, false);
            }
            result.setVideoTimeStamp(info.presentationTimeUs);
        }
        if (result.getVideoData() == null) {
            getDecodeNv12Frame(result);
            return;
        }
        mExtractor.unselectTrack(mVideoTrackIndex);
    }

    private void getDecodeAudioFrame(DecodeResult result) {
        mExtractor.selectTrack(mAudioTrackIndex);
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputBufferId = mAudioDecoder.dequeueInputBuffer(200);
        if (inputBufferId >= 0) {
            ByteBuffer inputBuffer = mAudioDecoder.getInputBuffer(inputBufferId);
            int sampleSize = mExtractor.readSampleData(inputBuffer, 0); //read data to the inputBuffer, size is sampleSize
            if (sampleSize < 0) {
                mAudioDecoder.queueInputBuffer(inputBufferId, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                long presentationTimeUs = mExtractor.getSampleTime();
                mAudioDecoder.queueInputBuffer(inputBufferId, 0, sampleSize, presentationTimeUs, 0);
                mExtractor.advance();
            }
        }
        int outputBufferId = mAudioDecoder.dequeueOutputBuffer(info, 200);
        if (outputBufferId >= 0) {
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d(TAG, "audio decode to the end");
                result.setAudioData(new byte[0]);
                result.setAudioToTheEnd(true);
                isAudioDecodeDone = true;
            }
            boolean doRender = (info.size != 0);
            if (doRender) {
                ByteBuffer byteBuffer = mAudioDecoder.getOutputBuffer(outputBufferId);
                byteBuffer.position(info.offset);
                byteBuffer.limit(info.offset + info.size);
                byte[] outputData = new byte[info.size];
                byteBuffer.get(outputData);
                byteBuffer.clear();
                result.setAudioData(outputData);
                mAudioDecoder.releaseOutputBuffer(outputBufferId, false);
            }
            result.setAudioTimeStamp(info.presentationTimeUs);
        }
        if (result.getAudioData() == null) {
            getDecodeAudioFrame(result);
            return;
        }
        mExtractor.unselectTrack(mAudioTrackIndex);
    }

    private void selectTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                mVideoTrackIndex = i;
            }
            if (mime.startsWith("audio/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                mAudioTrackIndex = i;
            }
        }
    }

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

    public byte[] getDataFromImage(Image image) {
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        mImageWidth =width;
        mImageHeight =height;
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            if (VERBOSE) {
                Log.v(TAG, "pixelStride " + pixelStride);
                Log.v(TAG, "rowStride " + rowStride);
                Log.v(TAG, "width " + width);
                Log.v(TAG, "height " + height);
                Log.v(TAG, "buffer size " + buffer.remaining());
            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
            if (VERBOSE) Log.v(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }

    public int getImageWidth() {
        return mImageWidth;
    }

    public int getImageHeight() {
        return mImageHeight;
    }

    public MediaFormat getVideoMediaFormat() {
        return mVideoMediaFormat;
    }

    public MediaFormat getAudioMediaFormat() {
        return mAudioMediaFormat;
    }


    //    private static void dumpFile(String fileName, byte[] data) {
//        FileOutputStream outStream;
//        try {
//            outStream = new FileOutputStream(fileName);
//        } catch (IOException ioe) {
//            throw new RuntimeException("Unable to create output file " + fileName, ioe);
//        }
//        try {
//            outStream.write(data);
//            outStream.close();
//        } catch (IOException ioe) {
//            throw new RuntimeException("failed writing data to file " + fileName, ioe);
//        }
//    }

//    private void compressToJpeg(String fileName, Image image) {
//        FileOutputStream outStream;
//        try {
//            outStream = new FileOutputStream(fileName);
//        } catch (IOException ioe) {
//            throw new RuntimeException("Unable to create output file " + fileName, ioe);
//        }
//        Rect rect = image.getCropRect();
//        YuvImage yuvImage = new YuvImage(getDataFromImage(image, COLOR_FormatNV21), ImageFormat.NV21, rect.width(), rect.height(), null);
//        yuvImage.compressToJpeg(rect, 100, outStream);
//    }

//    public static void convertNV12ToNv21(byte[] data, byte[] dstData, int w, int h) {
//        int size = w * h;
//        // Y
//        System.arraycopy(data, 0, dstData, 0, size);
//
//        for (int i = 0; i < size / 4; i++) {
//            dstData[size + i * 2] = data[size + i * 2 + 1]; //U
//            dstData[size + i * 2 + 1] = data[size + i * 2]; //V
//        }
//    }
}