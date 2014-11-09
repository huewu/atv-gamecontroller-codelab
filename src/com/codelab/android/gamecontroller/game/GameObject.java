package com.codelab.android.gamecontroller.game;

import android.graphics.Color;
import android.graphics.Rect;

import java.util.Random;

public abstract class GameObject {

    static protected int sDisplayWidth = 0;
    static protected int sDisplayHeight = 0;

    private static Random sRandom = new Random();

    protected int mPosX;
    protected int mPoxY;
    protected float mRotation = 0.f;

    public static void setDisplay(int displayWidth, int displayHeight) {
        sDisplayWidth = displayWidth;
        sDisplayHeight = displayHeight;
    }

    public abstract int getSize();

    public int getPoxX() {
        return mPosX;
    }

    public int getPosY() {
        return mPoxY;
    }

    public void moveBy(int dx, int dy) {
        mPosX += dx;
        mPoxY += dy;
    }

    public void rotate(float dRotate) {
        mRotation += dRotate;
    }

    public float getMoveRatio() {
        return 10.f;
    }

    public float getRotation() {
        return mRotation;
    }

    /**
     * Player
     */
    public static class Player extends GameObject {

        private static final int[] sColors = new int[]{
                Color.BLUE, Color.LTGRAY, Color.GREEN, Color.YELLOW
        };
        private static int sColorIndex = 0;
        private final Rect mPossibleRangeRect;
        private final Rect mPlayerRect = new Rect();
        private int mScore = 0;
        private int mColorIndex = 0;
        private int mDirection = 0;

        private GameController mController;

        public Player(Rect displayRect, GameController controller) {
            mPossibleRangeRect = displayRect;
            mController = controller;
            mColorIndex = sColorIndex % sColors.length;
            sColorIndex++;
        }

        public int getControllerId() {
            return mController.getDeviceId();
        }        public void moveBy(int dx, int dy) {

            mDirection = dx;

            int tempX = mPosX + dx;
            int tempY = mPoxY + dy;

            final int halfSize = getSize() / 2;

            mPlayerRect.set(tempX - halfSize, tempY - halfSize,
                    tempX + halfSize, tempY + halfSize);

            if (mPossibleRangeRect.contains(mPlayerRect)) {
                mPosX = tempX;
                mPoxY = tempY;
            }
        }

        public String getControllerDescriptor() {
            return mController.getDeviceDescriptor();
        }        @Override
        public int getSize() {
            return sDisplayWidth / 8;
        }

        public void setController(GameController controller) {
            mController = controller;
        }

        public int getColor() {
            return sColors[mColorIndex];
        }

        public int getScore() {
            return mScore;
        }

        public void addScore() {
            mScore += 1;
        }

        public int getDirection() {
            return mDirection;
        }




    }

    /**
     * Candy
     */
    public static class Candy extends GameObject {

        private static final String TAG = "Asteroid";

        private static final int MAX_VELOCITY = 15;
        private static final int[] COLORS = new int[]{
                0xFFF44336, 0xFFFFEBEE, 0xFFFF80AB, 0xFFFF4081, 0xFFF50057, 0xFFC51162,
                0xFFB9F6CA, 0xFF69F0AE, 0xFF00E676, 0xFF00C853, 0xFFF4FF81, 0xFFEEFF41
        };

        private int mVelX;
        private int mVelY;

        private float mAccel;
        private float mAccelSum;

        private int mColor;
        private int mColorSecond;
        private int mColorAccent;

        private Rect mPossibleRect;

        public Candy(int possibleRangeX, int possibleRangeY) {
            mPossibleRect = new Rect(0, -possibleRangeY, possibleRangeX, possibleRangeY);
            reset();
        }

        public void reset() {

            mPosX = sRandom.nextInt(mPossibleRect.width());
            mPoxY = sRandom.nextInt(mPossibleRect.height() / 2) - mPossibleRect.height() / 2;

            mVelX = sRandom.nextInt(5) - 2;
            mVelY = 2 + sRandom.nextInt(6);
            mAccel = (sRandom.nextInt(3) + 1) / 60.f;
            mAccelSum = 0.f;

            mColor = COLORS[sRandom.nextInt(COLORS.length)];
            mColorSecond = COLORS[sRandom.nextInt(COLORS.length)];
            mColorAccent = COLORS[sRandom.nextInt(COLORS.length)];
        }

        public void next() {

            mAccelSum += mAccel;

            if (mAccelSum > 1.f) {

                mVelX = mVelX > 0 ? mVelX + 1 : mVelX - 1;
                mVelY += 1;
                mAccelSum = 0.f;

                mVelY = Math.min(mVelY, MAX_VELOCITY);
            }

            moveBy(mVelX, mVelY);

            //if this asteroid is reached to the end of the screen
            if (!mPossibleRect.contains(mPosX, mPoxY)) {
                reset();
            }
        }

        public int getColor() {
            return mColor;
        }

        public int getSecondColor() {
            return mColorSecond;
        }        @Override
        public int getSize() {
            return sDisplayWidth / 30;
        }

        public int getAccentColor() {
            return mColorAccent;
        }




    }
}
