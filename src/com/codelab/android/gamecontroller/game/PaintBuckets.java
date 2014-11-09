package com.codelab.android.gamecontroller.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.codelab.android.gamecontroller.R;

/**
 * PaintBuckets which contains all the necessary paints using this game.
 */
public class PaintBuckets {

    private static final String TAG = "PaintBuckets";

    private Paint mBigFontPaint;
    private Paint mNormalFontPaint;
    private Paint mMediumFontPaint;

    private Paint mRedFillPaint;
    private Paint mGrayFillPaint;

    private Paint mStrokePaint;

    private Paint mBitmapPaint;
    private Bitmap mLeftAndroid;
    private Bitmap mRightAndroid;
    private Bitmap mCenterAndroid;
    private Bitmap mAndroidArm;
    private Bitmap mBackground;

    private boolean mInitialize = false;

    public void init(Context context, int displayWidth, int displayHeight) {

        if (mInitialize) {
            relese();
        }

        mInitialize = true;

        mRedFillPaint = new Paint();
        mRedFillPaint.setStyle(Paint.Style.FILL);
        mRedFillPaint.setColor(Color.RED);

        mGrayFillPaint = new Paint();
        mGrayFillPaint.setStyle(Paint.Style.FILL);
        mGrayFillPaint.setColor(Color.DKGRAY);

        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(30.f);
        mStrokePaint.setAlpha(200);
        mStrokePaint.setColor(Color.WHITE);

        mBigFontPaint = new Paint();
        mBigFontPaint.setTextSize(displayHeight / 12);
        mBigFontPaint.setColor(Color.WHITE);
        mBigFontPaint.setFakeBoldText(true);

        mMediumFontPaint = new Paint();
        mMediumFontPaint.setTextSize(displayHeight / 16);
        mMediumFontPaint.setColor(Color.WHITE);

        mNormalFontPaint = new Paint();
        mNormalFontPaint.setTextSize(displayHeight / 20);
        mNormalFontPaint.setColor(Color.WHITE);

        mBitmapPaint = new Paint();

        mLeftAndroid
                = BitmapFactory.decodeResource(context.getResources(), R.drawable.left_android);
        mLeftAndroid = Bitmap.createScaledBitmap(mLeftAndroid,
                displayWidth >> 3, displayWidth >> 3, true);

        mRightAndroid
                = BitmapFactory.decodeResource(context.getResources(), R.drawable.right_android);
        mRightAndroid = Bitmap.createScaledBitmap(mRightAndroid,
                displayWidth >> 3, displayWidth >> 3, true);

        mCenterAndroid
                = BitmapFactory.decodeResource(context.getResources(), R.drawable.center_android);
        mCenterAndroid = Bitmap.createScaledBitmap(mCenterAndroid,
                displayWidth >> 3, displayWidth >> 3, true);

        mAndroidArm
                = BitmapFactory.decodeResource(context.getResources(), R.drawable.android_arm);
        mAndroidArm = Bitmap.createScaledBitmap(mAndroidArm,
                displayWidth >> 5, displayWidth >> 3, true);

        mBackground
                = BitmapFactory.decodeResource(context.getResources(), R.drawable.game_background);

        mBackground = scaleCenterCrop(mBackground, displayHeight, displayWidth);

    }

    public void relese() {

        mBackground.recycle();
        mLeftAndroid.recycle();
        mRightAndroid.recycle();
        mCenterAndroid.recycle();
        mAndroidArm.recycle();

        mInitialize = false;
    }

    private Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        final int sourceWidth = source.getWidth();
        final int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        Log.d(TAG, "New Target Size: " + newWidth + ", " + newHeight);
        Log.d(TAG, "Scaled Bitmap Size: " + scaledWidth + ", " + scaledHeight);

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        int left = (int) ((scaledWidth - newWidth) / 2);
        int top = (int) ((scaledHeight - newHeight) / 2);

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        Rect soruceRect = new Rect((int) (left / scale), (int) (top / scale),
                (int) ((left + newWidth) / scale), (int) ((top + newHeight) / scale));
        Rect targetRect = new Rect(0, 0, newWidth, newHeight);

        Log.d(TAG, "Source Rect: " + soruceRect);
        Log.d(TAG, "Dest Rect: " + targetRect);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());

        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, soruceRect, targetRect, mBitmapPaint);

        source.recycle();
        return dest;
    }

    public Paint getBigFontPaint() {
        return mBigFontPaint;
    }

    public Paint getSmallFontPaint() {
        return mNormalFontPaint;
    }

    public Paint getMediumFontPaint() {
        return mMediumFontPaint;
    }

    public Paint getGrayFillPaint() {
        return mGrayFillPaint;
    }

    public Paint getStrokePaint() {
        return mStrokePaint;
    }

    public Paint getRedFillPaint() {
        return mRedFillPaint;
    }

    public Paint getBitmapPaint() {
        return mBitmapPaint;
    }

    public Bitmap getLeftAndroidBitmap() {
        return mLeftAndroid;
    }

    public Bitmap getRightAndroidBitmap() {
        return mRightAndroid;
    }

    public Bitmap getCenterAndroidBitmap() {
        return mCenterAndroid;
    }

    public Bitmap getAndroidArmBitmap() {
        return mAndroidArm;
    }

    public Bitmap getBackgroundBitmap() {
        return mBackground;
    }

}
