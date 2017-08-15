package com.example.kay.photoboothgallerydemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private Button mBtnCapture;
    private PhotoBoothGalleryPhoto mPhotoGallery;
    private int index = 1;
    private Button mBtnGoToVideo;
    private Bitmap bitmap1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_layout_photo);
        mBtnCapture = (Button) findViewById(R.id.gallery_photo_capture);
        mBtnGoToVideo = (Button) findViewById(R.id.goto_video_gallery);
        mPhotoGallery = (PhotoBoothGalleryPhoto) findViewById(R.id.gallery_photo_image);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mPhotoGallery.getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = screenHeight - screenWidth;
        mPhotoGallery.setLayoutParams(layoutParams);
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.draw_light);
        bitmap1 = Bitmap.createBitmap(screenWidth, screenWidth, bitmap.getConfig());
        Matrix matrix = new Matrix();
        matrix.postScale((float) screenWidth/bitmap.getWidth(), (float) screenWidth/bitmap.getHeight());
        Canvas canvas = new Canvas(bitmap1);
        canvas.drawBitmap(bitmap, matrix, null);
        canvas = null;
        bitmap.recycle();
        mPhotoGallery.init(screenWidth, screenWidth);
        mBtnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (index) {
                    case 1:
                        mPhotoGallery.updateImage(bitmap1, 0, false, PhotoBoothGalleryPhoto.Location.TOP_LEFT);
                        break;
                    case 2:
                        mPhotoGallery.updateImage(bitmap1, 0, false, PhotoBoothGalleryPhoto.Location.TOP_RIGHT);
                        break;
                    case 3:
                        mPhotoGallery.updateImage(bitmap1, 0, false, PhotoBoothGalleryPhoto.Location.BOTTOM_LEFT);
                        break;
                    case 4:
                        mPhotoGallery.updateImage(bitmap1, 0, false, PhotoBoothGalleryPhoto.Location.BOTTOM_RIGHT);
                        break;
                }
                index++;
            }
        });

        mBtnGoToVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoGalleryActivity.class);
                startActivity(intent);
                bitmap1.recycle();
                MainActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bitmap1.recycle();
    }
}
