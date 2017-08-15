package com.example.kay.photoboothgallerydemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class VideoGalleryActivity extends Activity {

    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private String FilePath;

    private SurfaceView surfaceView2;
    private MediaPlayer mediaPlayer2;
    private String FilePath2;

    private SurfaceView surfaceView3;
    private MediaPlayer mediaPlayer3;
    private String FilePath3;

    private SurfaceView surfaceView4;
    private MediaPlayer mediaPlayer4;
    private String FilePath4;

    private Button mBtnGoToPhoto;
    private Button mBtnDecode;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private VideoCreator videoCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_layout_video);
        mBtnGoToPhoto = (Button) findViewById(R.id.goto_photo_gallery);
        mBtnGoToPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoGalleryActivity.this, MainActivity.class);
                startActivity(intent);
//                mediaPlayer.stop();
//                mediaPlayer.release();
//                mediaPlayer2.stop();
//                mediaPlayer2.release();
//                mediaPlayer3.stop();
//                mediaPlayer3.release();
//                mediaPlayer4.stop();
//                mediaPlayer4.release();
//                videoCreator.close();
                VideoGalleryActivity.this.finish();
            }
        });
        mProgressDialog=new ProgressDialog(VideoGalleryActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("creating");
        mHandler = new Handler(getMainLooper());
        videoCreator = new VideoCreator();
        final String path = "/sdcard/DCIM/Camera/VID_19700124_092012.mp4";
        final String path2 = "/sdcard/DCIM/Camera/VID_19700124_092034.mp4";
        final String path3 = "/sdcard/DCIM/Camera/VID_19700124_092002.mp4";
        final String path4 = "/sdcard/DCIM/Camera/VID_19700124_092024.mp4";
//        final String path4 = "/sdcard/DCIM/Camera/VID_19700124_092941.mp4";
        mBtnDecode = (Button) findViewById(R.id.decode_to_file);
        mBtnDecode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    videoCreator.init(path, path2, path3, path4);
                    videoCreator.setOnCreateCompletedCallback(new VideoCreator.OnCreateCompletedCallback() {
                        @Override
                        public void onCreateCompleted() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.dismiss();
                                    videoCreator.close();
                                }
                            });
                        }

                        @Override
                        public void onError() {
                            mProgressDialog.dismiss();
                            videoCreator.close();
                        }
                    });
                    videoCreator.create();
                    mProgressDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initData1() {
//      FilePath="/sdcard/video/sishui.avi";
        FilePath="/sdcard/DCIM/video/test1.mp4";
        surfaceView = (SurfaceView) findViewById(R.id.sv);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置视频流类型

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                Log.i("sno","start mediaplayer1----------------");
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    mediaPlayer.setDisplay(surfaceView.getHolder());
                    mediaPlayer.setDataSource(FilePath);
                    mediaPlayer.prepareAsync();
                } catch (Exception e) {   ///在这里增加播放失败.
                    mediaPlayer.release();
                    if(mediaPlayer!=null)
                        Log.i("sno","eeeeeeeeeeeeerrormediaPlayer!=null");
                    e.printStackTrace();
                }
            }
        }, 500);
    }

    private void initData2() {
        FilePath2="/sdcard/DCIM/video/test2.mp4";
        surfaceView2 = (SurfaceView) findViewById(R.id.sv2);
        mediaPlayer2 = new MediaPlayer();
        mediaPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置视频流类型

        mediaPlayer2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer2.start();
                Log.i("sno","start mediaPlayer2----------------");
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    mediaPlayer2.setDisplay(surfaceView2.getHolder());
                    mediaPlayer2.setDataSource(FilePath2);
                    mediaPlayer2.prepareAsync();
                } catch (Exception e) {   ///在这里增加播放失败.
                    mediaPlayer2.release();
                    if(mediaPlayer2!=null)
                        Log.i("sno","eeeeeeeeeeeeerrormediaPlayer!=null");
                    e.printStackTrace();
                }
            }
        }, 1000);
    }

    private void initData3() {
        FilePath3="/sdcard/DCIM/video/test1.mp4";
        surfaceView3 = (SurfaceView) findViewById(R.id.sv3);
        mediaPlayer3 = new MediaPlayer();
        mediaPlayer3.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置视频流类型

        mediaPlayer3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer3.start();
                Log.i("sno","start mediaPlayer3----------------");
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    mediaPlayer3.setDisplay(surfaceView3.getHolder());
                    mediaPlayer3.setDataSource(FilePath3);
                    mediaPlayer3.prepareAsync();
                } catch (Exception e) {   ///在这里增加播放失败.
                    mediaPlayer3.release();
                    if(mediaPlayer3!=null)
                        Log.i("sno","eeeeeeeeeeeeerrormediaPlayer!=null");
                    e.printStackTrace();
                }
            }
        }, 1500);
    }

    private void initData4() {
        FilePath4="/sdcard/DCIM/video/test2.mp4";
        surfaceView4 = (SurfaceView) findViewById(R.id.sv4);
        mediaPlayer4 = new MediaPlayer();
        mediaPlayer4.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置视频流类型

        mediaPlayer4.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer4.start();
                Log.i("sno","start mediaPlayer4----------------");
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    mediaPlayer4.setDisplay(surfaceView4.getHolder());
                    mediaPlayer4.setDataSource(FilePath4);
                    mediaPlayer4.prepareAsync();
                } catch (Exception e) {   ///在这里增加播放失败.
                    mediaPlayer4.release();
                    if(mediaPlayer4!=null)
                        Log.i("sno","eeeeeeeeeeeeerrormediaPlayer!=null");
                    e.printStackTrace();
                }
            }
        }, 2000);
    }
}
