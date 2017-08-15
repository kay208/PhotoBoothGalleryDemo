package com.example.kay.photoboothgallerydemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by kewei.xu on 7/5/17.
 */

public class PhotoBoothGalleryPhoto extends ImageView{
    private int mSinglePhotoWidth;
    private int mSinglePhotoHeight;
    private Bitmap mBitmapResult;
    private float mScale;

    enum Location {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public PhotoBoothGalleryPhoto(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    /**
     * this method should be called at the beginning.
     *
     * @param photoWidth the width of the photo
     * @param photoHeight the height of the photo
     */
    public void init(int photoWidth, int photoHeight) {
        int screenHeight = Math.max(getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels);
        mSinglePhotoHeight = (screenHeight - photoHeight) / 2;
        mSinglePhotoWidth = mSinglePhotoHeight;
        mScale = (float) mSinglePhotoHeight / photoHeight;
        mBitmapResult = Bitmap.createBitmap(mSinglePhotoWidth*2, mSinglePhotoHeight*2, Bitmap.Config.ARGB_8888);
    }

    public void updateImage(byte[] data, int orientation, boolean mirror, Location location) {
        updateImage(BitmapFactory.decodeByteArray(data, 0, data.length), orientation, mirror, location);

    }

    public void updateImage(Bitmap bitmapData, int orientation, boolean mirror, Location location) {
        Bitmap bitmapSingle;
        Matrix m = new Matrix();
        if (mirror) {
            // Flip horizontally
            m.setScale(-1f, 1f);
        }
        if (orientation != 0 && orientation != 180) {
            m.postRotate(orientation + 180); // 270 and 90 ,rotate
        } else {
            m.postRotate(orientation);
        }
        m.postScale(mScale, mScale);
        bitmapSingle = Bitmap.createBitmap(mSinglePhotoWidth, mSinglePhotoHeight, Bitmap.Config.ARGB_8888);
        Canvas canvasSingle = new Canvas(bitmapSingle);
        canvasSingle.drawBitmap(bitmapData, m, null);
        Canvas canvasResult = new Canvas(mBitmapResult);
        float x = 0f;
        float y = 0f;
        switch (location) {
            case TOP_LEFT:
                x = 0f;
                y = 0f;
                break;
            case TOP_RIGHT:
                x = mSinglePhotoWidth;
                y = 0f;
                break;
            case BOTTOM_LEFT:
                x = 0f;
                y = mSinglePhotoHeight;
                break;
            case BOTTOM_RIGHT:
                x = mSinglePhotoWidth;
                y = mSinglePhotoHeight;
                break;
        }
        canvasResult.drawBitmap(bitmapSingle, x, y, null);
        canvasResult = null;
        canvasSingle = null;
//        bitmapData.recycle();
        bitmapSingle.recycle();
        setImageBitmap(mBitmapResult);
    }

    public Bitmap getBitmapResult() {
        return mBitmapResult;
    }
}
