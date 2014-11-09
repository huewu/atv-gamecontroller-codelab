package com.codelab.android.gamecontroller.game;

import android.annotation.TargetApi;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.codelab.android.gamecontroller.util.GameControllerUtil;
import com.codelab.android.gamecontroller.util.GameControllerUtil.AxesMapping;
import com.codelab.android.gamecontroller.util.GameControllerUtil.ButtonMapping;


/**
 * A class which can be mapped into one physical game controller.
 */
public class GameController {

    protected final InputDevice mInputDevice;
    protected final boolean[] mButtons = new boolean[ButtonMapping.values().length];
    protected final float[] mAxes = new float[AxesMapping.values().length];

    public GameController(InputDevice device) {
        mInputDevice = device;
    }

    // Given an action int, returns a string description
    public static String actionToString(int action) {
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                return "Down";
            case MotionEvent.ACTION_MOVE:
                return "Move";
            case MotionEvent.ACTION_POINTER_DOWN:
                return "Pointer Down";
            case MotionEvent.ACTION_UP:
                return "Up";
            case MotionEvent.ACTION_POINTER_UP:
                return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE:
                return "Outside";
            case MotionEvent.ACTION_CANCEL:
                return "Cancel";
        }
        return "";
    }

    public int getDeviceId() {
        return mInputDevice.getId();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public String getDeviceDescriptor() {
        return mInputDevice.getDescriptor();
    }

    public void setKeyEvent(KeyEvent ev) {

        // step 3.3: TODO: Using a given keycode in the KeyEvent, get a ButtonMapping index value.
        // You can use the getButtonMappingIndex() helper method in GameControllerUtil.

        // Based on the type of the Action of the KeyEvent,
        // set a mButtons array's value properly.
        // 'true' means a button is pressed and 'false' means a button is released.

        final boolean pressed = (ev.getAction() == KeyEvent.ACTION_DOWN);
        int buttonIndex = GameControllerUtil.getButtonMappingIndex(ev.getKeyCode());

        if (buttonIndex != GameControllerUtil.INVALID_INDEX) {
            mButtons[buttonIndex] = pressed;
        }
    }

    public void setMotionEvent(MotionEvent ev) {

        // step 4.2: TODO: Using a given MotioneEvent, get all the axes values of this input device,
        // and set a mAxes array's value properly.

        // A joystick at rest does not always report an absolute position of (0,0).
        // The getFlat() method is used, to determine the range of values
        // bounding the joystick axis center. For more detailed information, refer to
        // GameControllerUtil.getCenteredAxis()

        for (AxesMapping axesMapping : AxesMapping.values()) {
            mAxes[axesMapping.ordinal()] = GameControllerUtil.getCenteredAxis(ev, mInputDevice,
                    axesMapping.getMotionEvent());
        }
    }

    //button state
    public boolean[] getButtonState() {
        return mButtons;
    }

    public float[] getAxesState() {
        return mAxes;
    }

    /**
     * A class which implement a simple virtual game controoler.
     * <p/>
     * Using motion events, convert motion event's values into proper button and axes state.
     */
    public final static class TouchScreenController extends GameController {

        private static final String TAG = "TouchScreenController";
        private final RectF mLeftPressedRect;
        private final RectF mRightPRessedRect;

        private boolean mIsScreenTouched = false;

        public TouchScreenController(InputDevice device, int displayWidth, int displayHeight) {
            super(device);
            mLeftPressedRect = new RectF(0, 0, displayWidth / 2, displayHeight);
            mRightPRessedRect = new RectF(displayWidth / 2, 0, displayWidth, displayHeight);
        }

        public boolean isScreenTouched() {
            return mIsScreenTouched;
        }

        public void setMotionEvent(MotionEvent ev) {

            final int action = MotionEventCompat.getActionMasked(ev);
            // Get the index of the pointer associated with the action.
            final int index = MotionEventCompat.getActionIndex(ev);

            //get the latest pointer's value.
            final float pointX = MotionEventCompat.getX(ev, index);
            final float pointY = MotionEventCompat.getY(ev, index);

            Log.d(TAG, String.format("The action is %s : [%f, %f]",
                    actionToString(action), pointX, pointY));

            //check
            if (action == MotionEvent.ACTION_DOWN
                    || action == MotionEvent.ACTION_POINTER_DOWN) {
                if (mLeftPressedRect.contains(pointX, pointY)) {
                    mAxes[AxesMapping.AXIS_HAT_X.ordinal()] = -1.f;
                    mButtons[ButtonMapping.BUTTON_A.ordinal()] = true;
                } else if (mRightPRessedRect.contains(pointX, pointY)) {
                    mAxes[AxesMapping.AXIS_HAT_X.ordinal()] = 1.f;
                    mButtons[ButtonMapping.BUTTON_A.ordinal()] = true;
                }

                mIsScreenTouched = true;
            } else if (action == MotionEvent.ACTION_UP) {
                mAxes[AxesMapping.AXIS_HAT_X.ordinal()] = 0.f;
                mButtons[ButtonMapping.BUTTON_A.ordinal()] = false;

                mIsScreenTouched = false;
            }
        }
    }
}
