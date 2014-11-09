package com.codelab.android.gamecontroller.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class GameControllerUtil {

    public static final int INVALID_INDEX = -1;
    private static final String TAG = "GameControllerUtil";

    public static boolean isGamepad(InputDevice device) {

        // step 2.3: TODO check gamepad or joystick capability of a specific InputDevice instance
        //based on the given device, determine whether this device supports gamepad or joystick.
        //you can refer to 'isTouchScreen' method below.

        return false;
    }

    public static boolean isTouchScreen(InputDevice device) {
        return ((device.getSources() & InputDevice.SOURCE_TOUCHSCREEN)
                == InputDevice.SOURCE_TOUCHSCREEN);
    }

    /**
     * get the corresponding index of a ButtonMapping enum list by a given keycode.
     *
     * @param keyCode
     * @return index of enum
     */
    public static int getButtonMappingIndex(int keyCode) {
        for (ButtonMapping buttonMapping : ButtonMapping.values()) {
            if (buttonMapping.getKeycode() == keyCode) {
                return buttonMapping.ordinal();
            }
        }
        return INVALID_INDEX;
    }

    /**
     * get an axis value after calibrating the value based on extent of the center flat position
     *
     * @param event
     * @param device
     * @param axis
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static float getCenteredAxis(MotionEvent event, InputDevice device, int axis) {
        InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of (0,0).
        // Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            float flat = range.getFlat();
            float value = event.getAxisValue(axis);

            // Ignore axis values that are within the 'flat' region of the joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public enum ButtonMapping {
        BUTTON_A(KeyEvent.KEYCODE_BUTTON_A),
        BUTTON_B(KeyEvent.KEYCODE_BUTTON_B),
        BUTTON_X(KeyEvent.KEYCODE_BUTTON_X),
        BUTTON_Y(KeyEvent.KEYCODE_BUTTON_Y),
        BUTTON_L1(KeyEvent.KEYCODE_BUTTON_L1),
        BUTTON_R1(KeyEvent.KEYCODE_BUTTON_R1),
        BUTTON_L2(KeyEvent.KEYCODE_BUTTON_L2),
        BUTTON_R2(KeyEvent.KEYCODE_BUTTON_R2),
        BUTTON_SELECT(KeyEvent.KEYCODE_BUTTON_SELECT),
        BUTTON_START(KeyEvent.KEYCODE_BUTTON_START),
        BUTTON_THUMBL(KeyEvent.KEYCODE_BUTTON_THUMBL),
        BUTTON_THUMBR(KeyEvent.KEYCODE_BUTTON_THUMBR),
        BACK(KeyEvent.KEYCODE_BACK),
        POWER(KeyEvent.KEYCODE_BUTTON_MODE);

        private final int mKeyCode;

        ButtonMapping(int keyCode) {
            mKeyCode = keyCode;
        }

        public int getKeycode() {
            return mKeyCode;
        }
    }

    public enum AxesMapping {
        AXIS_X(MotionEvent.AXIS_X),
        AXIS_Y(MotionEvent.AXIS_Y),
        AXIS_Z(MotionEvent.AXIS_Z),
        AXIS_RZ(MotionEvent.AXIS_RZ),
        AXIS_HAT_X(MotionEvent.AXIS_HAT_X),
        AXIS_HAT_Y(MotionEvent.AXIS_HAT_Y),
        AXIS_LTRIGGER(MotionEvent.AXIS_LTRIGGER),
        AXIS_RTRIGGER(MotionEvent.AXIS_RTRIGGER),
        AXIS_BRAKE(MotionEvent.AXIS_BRAKE),
        AXIS_GAS(MotionEvent.AXIS_GAS);

        private final int mMotionEvent;

        AxesMapping(int motionEvent) {
            mMotionEvent = motionEvent;
        }

        public int getMotionEvent() {
            return mMotionEvent;
        }
    }

}
