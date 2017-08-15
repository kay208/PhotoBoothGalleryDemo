package com.example.kay.photoboothgallerydemo;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by kewei.xu on 7/7/17.
 */

public class VideoCreator {
    private Handler mCreateHandler;
    private HandlerThread mCreateThread;
    private VideoDecoder mVideoDecode1;
    private VideoDecoder mVideoDecode2;
    private VideoDecoder mVideoDecode3;
    private VideoDecoder mVideoDecode4;
    private VideoEncoder mVideoEncode;
    private OnCreateCompletedCallback mOnCreateCompletedCallback;
    private byte[] mVideoCombinedResult;
    private DecodeResult mDecodeResult = new DecodeResult();
    private ExecutorService mExecutor = Executors.newCachedThreadPool();
    private getDecodeDataCallable mCallable1;
    private getDecodeDataCallable mCallable2;
    private getDecodeDataCallable mCallable3;
    private getDecodeDataCallable mCallable4;
    private byte[] mAudioMixResult;
    private VideoDecoder mBaseVideoDecoder;

    public VideoCreator() {
        mCreateThread = new HandlerThread("photo_booth_video_create_thread");
        mCreateThread.start();
        mCreateHandler = new Handler(mCreateThread.getLooper());
    }

    /**
     * this method should be called at the beginning.
     * @param path1
     * @param path2
     * @param path3
     * @param path4
     */
    public void init(final String path1, final String path2, final String path3, final String path4) {
        mCreateHandler.post(new Runnable() {
            @Override
            public void run() {
                mVideoDecode1 = new VideoDecoder();
                mVideoDecode2 = new VideoDecoder();
                mVideoDecode3 = new VideoDecoder();
                mVideoDecode4 = new VideoDecoder();
                mVideoEncode = new VideoEncoder();
                mVideoDecode1.VideoDecodePrepare(path1);
                mVideoDecode2.VideoDecodePrepare(path2);
                mVideoDecode3.VideoDecodePrepare(path3);
                mVideoDecode4.VideoDecodePrepare(path4);

                getBaseVideoDecoder();
                mVideoEncode.VideoEncodePrepare(mBaseVideoDecoder.getVideoMediaFormat(), mBaseVideoDecoder.getAudioMediaFormat());
                //TODO set the current orientation
                mVideoEncode.setDegrees(90);
                mCallable1 = new getDecodeDataCallable(mVideoDecode1);
                mCallable2 = new getDecodeDataCallable(mVideoDecode2);
                mCallable3 = new getDecodeDataCallable(mVideoDecode3);
                mCallable4 = new getDecodeDataCallable(mVideoDecode4);
            }
        });
    }

    /**
     * start to create the video
     */
    public void create() {
        mCreateHandler.post(createRunnable);
    }

    public void setOnCreateCompletedCallback(OnCreateCompletedCallback onCreateCompletedCallback) {
        this.mOnCreateCompletedCallback = onCreateCompletedCallback;
    }

    interface OnCreateCompletedCallback {
        void onCreateCompleted();
        void onError();
    }

    //get the shortest video
    private void getBaseVideoDecoder() {
        mBaseVideoDecoder = null;
        MediaFormat format1 = mVideoDecode1.getVideoMediaFormat();
        MediaFormat format2 = mVideoDecode2.getVideoMediaFormat();
        MediaFormat format3 = mVideoDecode3.getVideoMediaFormat();
        MediaFormat format4 = mVideoDecode4.getVideoMediaFormat();
        if (format1 == null || format2 == null || format3 == null || format4 == null) {
            return;
        }
        VideoDecoder[] videoDecoders = new VideoDecoder[]{mVideoDecode1, mVideoDecode2, mVideoDecode3, mVideoDecode4};
        Arrays.sort(videoDecoders, new VideoDurationComparator());
        mBaseVideoDecoder = videoDecoders[0];
    }

    private static class VideoDurationComparator implements Comparator<VideoDecoder> {

        @Override
        public int compare(VideoDecoder o1, VideoDecoder o2) {
            return (int) (o1.getVideoMediaFormat().getLong(MediaFormat.KEY_DURATION) - o2.getVideoMediaFormat().getLong(MediaFormat.KEY_DURATION));
        }
    }

    private Runnable createRunnable = new Runnable() {
        Future<DecodeResult> future1;
        Future<DecodeResult> future2;
        Future<DecodeResult> future3;
        Future<DecodeResult> future4;
        @Override
        public void run() {
            try {
                future1 = mExecutor.submit(mCallable1);
                future2 = mExecutor.submit(mCallable2);
                future3 = mExecutor.submit(mCallable3);
                future4 = mExecutor.submit(mCallable4);
                doCreate(future1.get(), future2.get(), future3.get(), future4.get());
//                doCreate(future1.get(), future1.get(), future1.get(), future1.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void doCreate(DecodeResult frame1, DecodeResult frame2, DecodeResult frame3, DecodeResult frame4) {
        boolean encodeCompleted;
        if (mVideoCombinedResult == null) {
            mVideoCombinedResult = new byte[frame1.getVideoData().length];
        }
        try {
            if (frame1.isVideoToTheEnd() || frame2.isVideoToTheEnd() || frame3.isVideoToTheEnd() || frame4.isVideoToTheEnd()) {
                mDecodeResult.setVideoData(null);
                mDecodeResult.setVideoToTheEnd(true);
            } else {
                videoDataCombine(frame1, frame2, frame3, frame4, mVideoCombinedResult, mVideoDecode1.getImageWidth(), mVideoDecode1.getImageHeight());
                mDecodeResult.setVideoData(mVideoCombinedResult);
                mDecodeResult.setVideoToTheEnd(false);
            }
            if (frame1.isAudioToTheEnd() || frame2.isAudioToTheEnd() || frame3.isAudioToTheEnd() || frame4.isAudioToTheEnd()) {
                mDecodeResult.setAudioData(null);
                mDecodeResult.setAudioToTheEnd(true);
            } else {
                mAudioMixResult = mixAudio(new DecodeResult[]{frame1, frame2, frame3, frame4});
                mDecodeResult.setAudioData(mAudioMixResult);
                mDecodeResult.setAudioToTheEnd(false);
            }
            mDecodeResult.setVideoTimeStamp(frame1.getVideoTimeStamp());
            mDecodeResult.setAudioTimeStamp(frame1.getAudioTimeStamp());
            encodeCompleted = mVideoEncode.encodeOneFrame(mDecodeResult);
            if (encodeCompleted) {
                if (mOnCreateCompletedCallback != null) {
                    mOnCreateCompletedCallback.onCreateCompleted();
                }
            } else {
                create();
            }
        } catch (MediaCodec.CodecException e) {
            e.printStackTrace();
            if (mOnCreateCompletedCallback != null) {
                mOnCreateCompletedCallback.onError();
            }
        }
    }

    private static class getDecodeDataCallable implements Callable<DecodeResult> {
        private VideoDecoder mVideoDecoder;
        public getDecodeDataCallable(VideoDecoder videoDecoder) {
            mVideoDecoder = videoDecoder;
        }

        @Override
        public DecodeResult call() throws Exception {

            return mVideoDecoder.decodeOneFrame();
        }
    }

    private void videoDataCombine(DecodeResult frame1, DecodeResult frame2, DecodeResult frame3, DecodeResult frame4, byte[] result, int width, int height) {
        //top right in degree 90
        for (int i = 0; i < width/2; i++) {
            for (int j = 0; j < height/2; j++) {
//                result[j * width + i] = frame2.getVideoData()[(j * width + i)*2];
//                if (j % 2 == 0) {
//                    result[width * height + (j / 2 * width + i)] = frame2.getVideoData()[width * height + ((j / 2 * width + i) * 2)];
//                }
                if (i % 2 == 0) {
                    result[j * width + i] = frame2.getVideoData()[(j * width + i) * 2];
                    result[j * width + i + 1] = frame2.getVideoData()[(j * width + i) * 2 + 1];
                }
                if (j % 2 == 0 && i % 2 == 0) {
                    result[width * height + (j / 2 * width + i)] = frame2.getVideoData()[width * height + ((j / 2 * width + i ) * 2)];
                    result[width * height + (j / 2 * width + i) + 1] = frame2.getVideoData()[width * height + ((j / 2 * width + i ) * 2) + 1];
                }
            }
        }
        //top left in degree 90
        for (int i = 0; i < width/2; i++) {
            for (int j = height/2; j < height; j++) {
//                result[j * width + i] = frame1.getVideoData()[((j-height/2) * width + i)*2];
//                if (j % 2 == 0) {
//                    result[width * height + (j / 2 * width + i)] = frame1.getVideoData()[width * height + (((j-height/2) / 2 * width + i) * 2)];
//                }
                if (i % 2 == 0) {
                    result[j * width + i] = frame1.getVideoData()[((j - height / 2) * width + i) * 2];
                    result[j * width + i + 1] = frame1.getVideoData()[((j - height / 2) * width + i) * 2 + 1];
                }
                if (j % 2 == 0 && i % 2 == 0) {
                    result[width * height + (j / 2 * width + i)] = frame1.getVideoData()[width * height + (((j-height/2) / 2 * width + i) * 2)];
                    result[width * height + (j / 2 * width + i) + 1] = frame1.getVideoData()[width * height + (((j-height/2) / 2 * width + i) * 2) + 1];
                }
            }
        }
        //bottom right in degree 90
        for (int i = width/2; i < width; i++) {
            for (int j = 0; j < height/2; j++) {
//                result[j * width + i] = frame4.getVideoData()[(j * width + (i - width/2))*2];
//                if (j % 2 == 0) {
//                    result[width * height + (j / 2 * width + i)] = frame4.getVideoData()[width * height + ((j / 2 * width + (i - width/2)) * 2)];
//                }
                if (i % 2 == 0) {
                    result[j * width + i] = frame4.getVideoData()[(j * width + (i - width/2))*2];
                    result[j * width + i + 1] = frame4.getVideoData()[(j * width + (i - width / 2)) * 2 + 1];
                }
                if (j % 2 == 0 && i % 2 == 0) {
                    result[width * height + (j / 2 * width + i)] = frame4.getVideoData()[width * height + ((j / 2 * width + (i - width/2)) * 2)];
                    result[width * height + (j / 2 * width + i) + 1] = frame4.getVideoData()[width * height + ((j / 2 * width + (i - width/2)) * 2) + 1];
                }
            }
        }
        //bottom left in degree 90
        for (int i = width/2; i < width; i++) {
            for (int j = height/2; j < height; j++) {
//                result[j * width + i] = frame3.getVideoData()[((j-height/2) * width + (i - width/2))*2];
//                if (j % 2 == 0) {
//                    result[width * height + (j / 2 * width + i)] = frame3.getVideoData()[width * height + (((j-height/2) / 2 * width + (i - width/2)) * 2)];
//                }
                if (i % 2 == 0) {
                    result[j * width + i] = frame3.getVideoData()[((j - height / 2) * width + (i - width / 2)) * 2];
                    result[j * width + i + 1] = frame3.getVideoData()[((j - height / 2) * width + (i - width / 2)) * 2 + 1];
                }
                if (j % 2 == 0 && i % 2 == 0) {
                    result[width * height + (j / 2 * width + i)] = frame3.getVideoData()[width * height + (((j-height/2) / 2 * width + (i - width/2)) * 2)];
                    result[width * height + (j / 2 * width + i) + 1] = frame3.getVideoData()[width * height + (((j-height/2) / 2 * width + (i - width/2)) * 2) + 1];
                }
            }
        }
    }

    //average audio mixing algorithm
    private byte[] mixAudio(DecodeResult[] decodeResults) {
        if (decodeResults == null || decodeResults.length == 0)
            return null;

        byte[] realMixAudio = decodeResults[0].getAudioData();

        if(decodeResults.length == 1)
            return realMixAudio;

        for(int rw = 0 ; rw < decodeResults.length ; ++rw){
            if(decodeResults[rw].getAudioData().length != realMixAudio.length){
                Log.e("app", "column of the road of audio + " + rw +" is diffrent.");
                return null;
            }
        }

        int row = decodeResults.length;
        int coloum = realMixAudio.length / 2;
        short[][] sMulRoadAudioes = new short[row][coloum];

        for (int r = 0; r < row; ++r) {
            for (int c = 0; c < coloum; ++c) {
                sMulRoadAudioes[r][c] = (short) ((decodeResults[r].getAudioData()[c * 2] & 0xff) | (decodeResults[r].getAudioData()[c * 2 + 1] & 0xff) << 8);
            }
        }

        short[] sMixAudio = new short[coloum];
        int mixVal;
        int sr = 0;
        for (int sc = 0; sc < coloum; ++sc) {
            mixVal = 0;
            sr = 0;
            for (; sr < row; ++sr) {
                mixVal += sMulRoadAudioes[sr][sc];
            }
            sMixAudio[sc] = (short) (mixVal / row);
        }

        for (sr = 0; sr < coloum; ++sr) {
            realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
            realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
        }
        return realMixAudio;
    }

    public void close() {
        mCreateHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoEncode != null) {
                    mVideoEncode.close();
                }
                if (mVideoDecode1 != null) {
                    mVideoDecode1.close();
                }
                if (mVideoDecode2 != null) {
                    mVideoDecode2.close();
                }
                if (mVideoDecode3 != null) {
                    mVideoDecode3.close();
                }
                if (mVideoDecode4 != null) {
                    mVideoDecode4.close();
                }
            }
        });
    }
}
