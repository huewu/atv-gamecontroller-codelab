/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codelab.android.gamecontroller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Canvas;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.codelab.android.gamecontroller.game.GameLogic;
import com.codelab.android.gamecontroller.util.GameControllerUtil;
import com.codelab.android.gamecontroller.util.TvUtil;

public class FullscreenActivity extends Activity {
    private static final String TAG = "FullscreenActivity";

    /**
     * The instance of the {@link android.view.SurfaceView}, which all the candies will be drawn.
     */
    private SurfaceView mGameView;

    /**
     * The instance of the {@link com.codelab.android.gamecontroller.game.GameLogic},
     * which all the candies will be produced.
     */
    private GameLogic mGameLogic;

    /**
     * The instance of the {@link android.hardware.input.InputManager} system service,
     * which manage various input devices
     */
    private InputManager mInputManager;
    private InputManager.InputDeviceListener mInputDeviceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_fullscreen);

        // step 1.7: TODO: invoke applyOverscanPadding() when the app is running on the TV.
        if (TvUtil.isTv(this)) {
            applyOverscanPadding();
        }

        mGameLogic = new GameLogic(this);
        mGameLogic.setGameChangeListener(new GameLogic.OnGameChangeListener() {
            @Override
            public void onGameChanged(GameLogic logic) {
                Canvas canvas = mGameView.getHolder().lockCanvas();
                if (canvas != null) {
                    logic.draw(canvas);
                    mGameView.getHolder().unlockCanvasAndPost(canvas);
                }
            }
        });

        mGameView = (SurfaceView) findViewById(R.id.gameview);
        mGameView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "Surface created");
                final int width = surfaceHolder.getSurfaceFrame().width();
                final int height = surfaceHolder.getSurfaceFrame().height();
                mGameLogic.setDisplay(width, height);
                checkGameControllers();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                Log.d(TAG, "Surface changed");
                mGameLogic.setDisplay(width, height);
                mGameLogic.requestPause();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.d(TAG, "SurfaceTexture Destroyed");
            }
        });

        // step 2.1: TODO: get the InputManager service and assign it to the mInputManager field.
        mInputManager = (InputManager) getSystemService(INPUT_SERVICE);

        // step 2.2: TODO: implement InputDeviceListener
        // When the onInputDeviceAdded() callback is called,
        // get an InputDevice instance from InputManager,
        // and call the GameLogic.setCurrentController() method.
        // When the onInputDeviceRemoved() callback is called,
        // call the GameLogic.removeController() method,
        // and call the checkGameControllers() method to find alternatives.

        // step 6.2: TODO: In order to support multi controllers,
        // replace setCurrentController() with addController() to handle multiple controllers
        // in your InputDeviceListener implementation.
        mInputDeviceListener = new InputManager.InputDeviceListener() {
            @Override
            public void onInputDeviceAdded(int deviceId) {
                Log.d(TAG, "input device is added");
                InputDevice device = mInputManager.getInputDevice(deviceId);
                mGameLogic.setCurrentController(device);
            }

            @Override
            public void onInputDeviceRemoved(int deviceId) {
                Log.d(TAG, "input device is removed");
                mGameLogic.removeController(deviceId);
                checkGameControllers();
            }

            @Override
            public void onInputDeviceChanged(int deviceId) {
                Log.d(TAG, "input device is changed");
                // IGNORED
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        // step 2.2: TODO register InputDeviceListener
        // To avoid a synchronization problem, register GameHandler as the handler on which
        // the listener should be invoked. You can use GameLogic.getGameHandler() method.
        mInputManager.registerInputDeviceListener(
                mInputDeviceListener, mGameLogic.getGameHandler());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // step 2.2: TODO unregister InputDeviceListener
        mInputManager.unregisterInputDeviceListener(mInputDeviceListener);
    }

    /**
     * release using resoruces.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGameLogic.terminate();
    }

    @Override
    public boolean onKeyDown(final int keyCode, KeyEvent ev) {
        // step 3.1: TODO: handle key down events
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, ev);
        } else {
            return mGameLogic.processKeyEvent(ev);
        }
    }

    @Override
    public boolean onKeyUp(final int keyCode, KeyEvent ev) {
        // step 3.1: TODO: handle key up events
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyUp(keyCode, ev);
        } else {
            return mGameLogic.processKeyEvent(ev);
        }
    }

    @Override
    public void onBackPressed() {
        // step 3.2: TODO: handle BackPressed events. To handle KEYCODE_BACK
        // we prepared the special method, processBackPressed() in GameLogic class.
        // It will handle the game logic regarding KEYCODE_BACK and will return true
        // if the event is successfully handled. If it returns false meaning
        // the game logic didn’t consume the event, you should call super.onBackPressed().
        if (!mGameLogic.processBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return mGameLogic.processMotionEvent(event);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        // step 4.1: TODO: handle generic motion events coming from joysticks.
        return super.onGenericMotionEvent(event);
    }

    /**
     * adding a 10% margin to account for overscan area
     */
    private void applyOverscanPadding() {
        View gameFrame = findViewById(R.id.gameframe);
        final int hPadding
                = getResources().getDimensionPixelOffset(R.dimen.overscan_paddingLeftRight);
        final int vPadding
                = getResources().getDimensionPixelOffset(R.dimen.overscan_paddingTopBottom);
        gameFrame.setPadding(hPadding, vPadding, hPadding, vPadding);
    }

    /**
     * Check for any game controllers that are connected already.
     */
    private void checkGameControllers() {
        Log.d(TAG, "checkGameControllers");

        // step 2.4: TODO: Replace InputDevice.getDeviceIds() with InputManager.getInputDeviceIds()
        // Querying current connected devices and if there is any gamepad capable device,
        // set it as the current controller by calling GameLogic.setCurrentController.

        // step 6.2 TODO: In order to support multi controllers,
        // Replace the setCurrentController() method with the addController() method.

        int[] deviceIds = mInputManager.getInputDeviceIds();

        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            mGameLogic.setCurrentController(dev);
        }
    }

}
