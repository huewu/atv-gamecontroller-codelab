package com.codelab.android.gamecontroller.game;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.codelab.android.gamecontroller.R;
import com.codelab.android.gamecontroller.util.GameControllerUtil;
import com.codelab.android.gamecontroller.util.GameControllerUtil.AxesMapping;
import com.codelab.android.gamecontroller.util.GameControllerUtil.ButtonMapping;

import java.util.ArrayList;

public class GameLogic {

    private static final String TAG = "GameLogic";
    //number of asteroids
    private static final int NUMBER_OF_INITIAL_ASTEROIDS = 7;
    //game data
    private final ArrayList<GameObject.Candy> mCandyList
            = new ArrayList<GameObject.Candy>(NUMBER_OF_INITIAL_ASTEROIDS);
    //millisecond per frame (30FPS)
    private static final int MILLISEC_PER_FRAME = 1000 / 31;
    private static final int GAME_NEXT_MESSAGE = 10013;
    private final PaintBuckets mPaintBuckets = new PaintBuckets();

    private final ArrayList<GameController> mGameControllerList
            = new ArrayList<GameController>();
    private final ArrayList<GameObject.Player> mPlayerList
            = new ArrayList<GameObject.Player>();

    //re-usable rects
    private final Rect mTextBoundRect = new Rect();
    private final RectF mDrawRect = new RectF();

    //application context
    private final Context mAppContext; //for referencing resources.
    //possible game states
    private final PlayingState mPlayingState = new PlayingState();
    private final PausedState mPausedState = new PausedState();
    private final StoppedState mStoppedState = new StoppedState();
    //instance of the current game state, the default state is StoppedState.
    private GameState mCurrentState;
    //current display width
    private int mDisplayWidth = 0;
    //current display height
    private int mDisplayHeight = 0;
    private HandlerThread mGameThread;
    private Handler mGameHandler;
    //instance of the game change callback listener, which called whenever game is advanced.
    private OnGameChangeListener mGameChangeListener;
    //elapsed time since the previous frame.
    private int mElapsedTime = 0;
    //flag to determine whether the game is initialized or not
    private boolean mGameDataInitialized = false;
    //sound pool
    private SoundPool mSoundPool;
    private int mSoundId;

    public GameLogic(Context context) {
        mAppContext = context.getApplicationContext();
        init();
    }

    /**
     * initialize the game thread and its handler.
     */
    private void init() {

        mGameThread = new HandlerThread("game_thread");
        mGameThread.start();

        mGameHandler = new Handler(mGameThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {

                long currentTime = SystemClock.currentThreadTimeMillis();
                notifyGameChange();
                mElapsedTime =
                        (int) (SystemClock.currentThreadTimeMillis() - currentTime);
                mCurrentState.next();
            }
        };

        requestTransition(mStoppedState);

        mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        mSoundId = mSoundPool.load(mAppContext, R.raw.ringabell, 1); // in 2nd param u have to pass your desire ringtone
    }

    private void notifyGameChange() {
        if (mGameChangeListener != null) {
            mGameChangeListener.onGameChanged(this);
        }
    }

    /**
     * change a current game state as a given gameState parameter
     *
     * @param gameState
     * @return true or false
     */
    private boolean requestTransition(GameState gameState) {

        if (mCurrentState != gameState) {
            mCurrentState = gameState;
            mCurrentState.init();
            mCurrentState.next();
            Log.d(TAG, "Current GameState: " + mCurrentState.getClass().getSimpleName());
            return true;
        } else {
            return false;
        }
    }

    /**
     * request to change current game-state as paused.
     */
    public void requestPause() {
        if (mCurrentState == mPlayingState) {
            requestTransition(mPausedState);
        }
    }

    public void terminate() {
        //release resources....
        if (mSoundPool != null) {
            mSoundPool.release();
        }

        mPaintBuckets.relese();
    }

    public void setCurrentController(InputDevice device) {
        GameController controller = null;

        if (GameControllerUtil.isGamepad(device)) {
            controller = new GameController(device);
        } else if (GameControllerUtil.isTouchScreen(device)) {
            controller = new GameController.TouchScreenController(device,
                    mDisplayWidth, mDisplayHeight);
        }

        if (controller != null) {
            mGameControllerList.clear();
            mGameControllerList.add(controller);

            GameObject.Player player = getCurrentPlayer();
            if (player == null) {
                addPlayer(controller);
            } else {
                player.setController(controller);
            }
        }
    }

    /**
     * return the first player in the list.
     */
    private GameObject.Player getCurrentPlayer() {
        if (mPlayerList == null || mPlayerList.size() == 0) {
            return null;
        }

        return mPlayerList.get(0);
    }

    /**
     * using a given controller, add a new player into the Game
     *
     * @param controller
     */
    private void addPlayer(GameController controller) {

        final Rect displayRect = new Rect(0, 0, mDisplayWidth, mDisplayHeight);

        GameObject.Player player = new GameObject.Player(displayRect, controller);
        final int playerSize = player.getSize();
        player.moveBy((mDisplayWidth - playerSize) / 2, (mDisplayHeight - playerSize / 2));
        mPlayerList.add(player);
    }

    /**
     * assign a new controller to the GameLogic.
     * the controller must be either a gamepad or a touchscreen.
     * if a new controller is added successfully,
     * the GameLogic would add a new player into the Game
     *
     * @param device a new controller candidate
     * @return number of current controllers in the GameLogic
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public int addController(InputDevice device) {

        GameController controller = null;

        if (GameControllerUtil.isGamepad(device)) {
            controller = new GameController(device);
        } else if (GameControllerUtil.isTouchScreen(device)) {
            controller = new GameController.TouchScreenController(device,
                    mDisplayWidth, mDisplayHeight);
        }

        if (controller == null) {
            return mGameControllerList.size();
        }

        mGameControllerList.add(controller);

        // step 6.1: TODO: Using a device descriptor, check whether this controller is already
        // paired with an existing player or not.
        // If the device descriptor of a certain player is the same as the new device's descriptor,
        // set a new controller as a controller for that player.
        // If not, add a new Player instance to the player list

        final String deviceDesc = device.getDescriptor();
        boolean isNewPlayer = true;

        for (GameObject.Player player : mPlayerList) {
            if (player.getControllerDescriptor().equals(deviceDesc)) {
                //disconnected player is coming back.
                player.setController(controller);
                isNewPlayer = false;
                break;
            }
        }

        if (isNewPlayer) {
            addPlayer(controller);
        }

        return mGameControllerList.size();
    }

    /**
     * if exist, remove a existing controller by a given deviceId
     *
     * @param deviceId
     */
    public int removeController(final int deviceId) {

        // step 5.1: TODO: find the input device by a given deviceId and remove it from the list.
        // if a device is removed, change game state to mPausedState.

        for (GameController gameController : mGameControllerList) {
            if (gameController.getDeviceId() == deviceId) {
                mGameControllerList.remove(gameController);
                requestTransition(mPausedState);
            }
        }

        final int size = mGameControllerList.size();
        Log.d(TAG, "Controller Count: " + size);
        return size;
    }

    public void setGameChangeListener(OnGameChangeListener gameChangeListener) {
        this.mGameChangeListener = gameChangeListener;
    }

    public void setDisplay(int displayWidth, int displayHeight) {
        if (mDisplayHeight != displayHeight || mDisplayWidth != displayWidth) {
            mDisplayWidth = displayWidth;
            mDisplayHeight = displayHeight;

            mPaintBuckets.init(mAppContext, mDisplayWidth, mDisplayHeight);
            GameObject.Player.setDisplay(mDisplayWidth, mDisplayHeight);
            GameObject.Candy.setDisplay(mDisplayWidth, mDisplayHeight);
        }
    }

    public Handler getGameHandler() {
        return mGameHandler;
    }

    public void draw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        mCurrentState.draw(canvas);
    }

    /**
     * propagate a given KeyEvent
     * to the target {@link com.codelab.android.gamecontroller.game.GameController} instance
     * by using a deviceId.
     *
     * @param ev
     * @return true if the event is consumed inside the method
     */
    public boolean processKeyEvent(KeyEvent ev) {

        GameController controller = getGameControllerById(ev.getDeviceId());
        if (controller != null) {
            controller.setKeyEvent(ev);
            return true;
        }
        return true;
    }

    /**
     * get the {@link com.codelab.android.gamecontroller.game.GameController} instance
     * by using a given deviceId
     *
     * @param deviceId
     * @return
     */
    private GameController getGameControllerById(int deviceId) {

        for (GameController gameController : mGameControllerList) {
            if (gameController.getDeviceId() == deviceId) {
                return gameController;
            }
        }

        return null;
    }

    /**
     * propagate a given MotionEvent
     * to the target {@link com.codelab.android.gamecontroller.game.GameController} instance
     * by using a deviceId.
     *
     * @param ev
     * @return true if the event is consumed inside the method
     */
    public boolean processMotionEvent(MotionEvent ev) {

        GameController controller = getGameControllerById(ev.getDeviceId());
        if (controller != null) {
            controller.setMotionEvent(ev);
            return true;
        }
        return false;
    }

    /**
     * handle BackPressed button event based on the current game state.
     *
     * @return true if the event is consumed inside the method
     */
    public boolean processBackPressed() {
        return mCurrentState.onBackPressed();
    }

    /**
     * initialize the game data
     *
     * @param displayWidth
     * @param displayHeight
     */
    private void initGameData(int displayWidth, int displayHeight) {

        if (mGameDataInitialized) {
            return;
        }

        Log.d(TAG, "init GameData");
        mGameDataInitialized = true;

        mDisplayWidth = displayWidth;
        mDisplayHeight = displayHeight;

        mPlayerList.clear();

        for (GameController controller : mGameControllerList) {
            addPlayer(controller);
        }

        mCandyList.clear();
        for (int i = 0; i < NUMBER_OF_INITIAL_ASTEROIDS; ++i) {
            mCandyList.add(new GameObject.Candy(mDisplayWidth, mDisplayHeight));
        }
    }

    private void drawBackground(Canvas canvas) {

        Bitmap bitmap = mPaintBuckets.getBackgroundBitmap();
        canvas.drawBitmap(bitmap, 0, 0, mPaintBuckets.getBitmapPaint());
    }

    private void drawMainMenu(Canvas canvas) {
        final String title = mAppContext.getResources().getString(R.string.title);
        final String desc = mAppContext.getString(R.string.how_to_start_mssage);
        Paint paint;
        int savedColor;

        final int cx = canvas.getWidth() / 2;
        final int cy = canvas.getHeight() / 2;

        //draw title
        paint = mPaintBuckets.getBigFontPaint();
        savedColor = paint.getColor();
        paint.getTextBounds(title, 0, title.length(), mTextBoundRect);
        paint.setColor(Color.YELLOW);
        canvas.drawText(title, cx - mTextBoundRect.centerX(), cy - mTextBoundRect.height(), paint);
        paint.setColor(savedColor);

        //draw description
        paint = mPaintBuckets.getMediumFontPaint();
        savedColor = paint.getColor();
        paint.getTextBounds(desc, 0, desc.length(), mTextBoundRect);
        paint.setColor(Color.LTGRAY);
        canvas.drawText(desc, cx - mTextBoundRect.centerX(), cy + mTextBoundRect.height(), paint);
        paint.setColor(savedColor);
    }

    private void drawUI(Canvas canvas) {

        //draw number of players
        final String playerStr = "Player";
        final Paint paint = mPaintBuckets.getSmallFontPaint();
        paint.getTextBounds(playerStr, 0, playerStr.length(), mTextBoundRect);
        final int textHeight = mTextBoundRect.height();

        int index = 0;
        synchronized (mPlayerList) {
            for (GameObject.Player player : mPlayerList) {
                String formatStr
                        = String.format("Player%d: %d", index + 1, player.getScore());
                canvas.drawText(formatStr,
                        50, textHeight * 1.5f + (textHeight + 20) * index, paint);
                ++index;
            }
        }
    }

    private void drawPausedMenu(Canvas canvas) {

        canvas.save();
        final int cx = canvas.getWidth() / 2;
        final int cy = canvas.getHeight() / 2;
        mDrawRect.set(cx / 2, cy / 2, cx * 1.5f, cy * 1.5f);
        canvas.drawRoundRect(mDrawRect, 15.f, 15.f, mPaintBuckets.getGrayFillPaint());
        final Paint bigFontPaint = mPaintBuckets.getBigFontPaint();
        final Paint medFontPaint = mPaintBuckets.getMediumFontPaint();

        final String pausedStr = "PAUED";
        final String descStr = "A: play, Back: stop";

        bigFontPaint.getTextBounds(pausedStr, 0, pausedStr.length(), mTextBoundRect);
        canvas.drawText(pausedStr, cx - mTextBoundRect.centerX(), cy - mTextBoundRect.centerY(), bigFontPaint);

        final int currentColor = medFontPaint.getColor();
        medFontPaint.setColor(Color.LTGRAY);
        medFontPaint.getTextBounds(descStr, 0, descStr.length(), mTextBoundRect);
        canvas.drawText(descStr, cx - mTextBoundRect.centerX(), mDrawRect.bottom - mTextBoundRect.height(), medFontPaint);
        medFontPaint.setColor(currentColor);

        canvas.restore();
    }

    private void drawGameMap(Canvas canvas) {

        final int w = canvas.getWidth();
        final int h = canvas.getHeight();

        canvas.save();
        canvas.drawRect(0.f, 0.f, w, h, mPaintBuckets.getStrokePaint());
        canvas.restore();
    }

    private void drawCandies(Canvas canvas) {

        canvas.save();
        for (GameObject.Candy candy : mCandyList) {

            final int posX = candy.getPoxX();
            final int posY = candy.getPosY();
            final float size = candy.getSize();

            final Paint paint = mPaintBuckets.getRedFillPaint();
            final int currColor = paint.getColor();

            paint.setColor(candy.getColor());
            canvas.drawCircle(posX, posY, size / 2.f, paint);
            paint.setColor(candy.getSecondColor());
            canvas.drawCircle(posX, posY, size / 3.f, paint);
            paint.setColor(candy.getAccentColor());
            canvas.drawCircle(posX, posY, size / 5.f, paint);

            paint.setColor(currColor);
        }
        canvas.restore();
    }

    private void drawPlayers(Canvas canvas) {

        for (GameObject.Player player : mPlayerList) {
            canvas.save();
            canvas.translate(player.getPoxX(), player.getPosY());

            final float size = player.getSize() / 2.f;
            mDrawRect.set(-size, -size, size, size);

            Bitmap bitmap;
            //draw a body
            if (player.getDirection() < 0) {
                bitmap = mPaintBuckets.getLeftAndroidBitmap();
            } else if (player.getDirection() > 0) {
                bitmap = mPaintBuckets.getRightAndroidBitmap();
            } else {
                bitmap = mPaintBuckets.getCenterAndroidBitmap();
            }

            final Paint paint = mPaintBuckets.getBitmapPaint();
            ColorFilter filter = new LightingColorFilter(player.getColor(), 1);
            paint.setColorFilter(filter);

            canvas.drawBitmap(bitmap, null, mDrawRect, paint);

            //draw two arms
            bitmap = mPaintBuckets.getAndroidArmBitmap();
            mDrawRect.set(-size / 8, -size / 2, size / 8, size / 2);
            canvas.save();
            canvas.translate(-size / 1.6f, -size * 0.2f);
            canvas.rotate(player.getRotation());
            canvas.drawBitmap(bitmap, null, mDrawRect, paint);
            canvas.restore();

            canvas.save();
            canvas.translate(size / 1.6f, -size * 0.2f);
            canvas.rotate(player.getRotation());
            canvas.drawBitmap(bitmap, null, mDrawRect, paint);
            canvas.restore();

            canvas.restore();
            paint.setColorFilter(null);
        }
    }

    private void processPlayersMove() {

        for (GameObject.Player player : mPlayerList) {

            int deviceId = player.getControllerId();
            GameController controller = getGameControllerById(deviceId);

            if (controller == null) {
                continue;
            }

            //read all the axes data from controller
            float[] axes = controller.getAxesState();

            //move player body's position according to the value.
            int dx = (int) (axes[AxesMapping.AXIS_HAT_X.ordinal()] * player.getMoveRatio());
            int dAxisX = (int) (axes[AxesMapping.AXIS_X.ordinal()] * player.getMoveRatio());
            player.moveBy(dx + dAxisX, 0);

            // step 4.3: TODO: read axes values of the right joystick,
            // and rotate player's hand properly.

            // you can read right-joystick axis values using a AXIS_Z enum value.
            // to boost character's movement, multiply getMoveRatio() value.

            float dRAxisX = axes[AxesMapping.AXIS_Z.ordinal()] * player.getMoveRatio();
            player.rotate(dRAxisX);
        }
    }

    private void processCandiesMove() {

        for (GameObject.Candy candy : mCandyList) {
            candy.next();
        }

    }

    private void processPausedMenuEvent() {

        for (GameController controller : mGameControllerList) {
            boolean[] buttons = controller.getButtonState();

            if (buttons[ButtonMapping.BUTTON_A.ordinal()]) {
                requestTransition(mPlayingState);
            } else if (buttons[ButtonMapping.BUTTON_B.ordinal()]) {
                requestTransition(mStoppedState);
            }
        }
    }

    private void processMainMenuEvent() {

        if (mGameControllerList.size() == 0) {
            return;
        }

        /*
        GameController controller = mGameControllerList.get(0);

        if (controller instanceof GameController.TouchScreenController &&
                ((GameController.TouchScreenController)controller).isScreenTouched()) {
            requestTransition(mPlayingState);
        }
        */

        // step 3.4: TODO: re-impelemnt this method.
        // delete existing code snippet above.
        // read the ButtonState of the existing controllers,
        // and if BUTTON_A is pressed, change the game state as Playing.
        //
        // you might need to use getButtonState() and requestTransition() methods.
        // you can also refer to processPausedMenuEvent() method above.

        for (GameController controller : mGameControllerList) {
            boolean[] buttons = controller.getButtonState();

            if (buttons[ButtonMapping.BUTTON_A.ordinal()]) {
                requestTransition(mPlayingState);
            }
        }
    }

    /**
     * check collisions among players and candies.
     */
    private void checkCollisions() {

        Point playerPos = new Point();
        Point candyPos = new Point();

        boolean needReset = false;

        for (GameObject.Candy candy : mCandyList) {
            candyPos.set(candy.getPoxX(), candy.getPosY());

            for (GameObject.Player player : mPlayerList) {
                playerPos.set(player.getPoxX(), player.getPosY());

                double distance = Math.sqrt(Math.pow(playerPos.x - candyPos.x, 2) +
                        Math.pow(playerPos.y - candyPos.y, 2));

                //check distance.
                if (distance < player.getSize() / 2.f) {
                    player.addScore();
                    mSoundPool.play(mSoundId, 1, 1, 0, 0, 1);
                    needReset = true;
                }
            }

            if (needReset) {
                candy.reset();
                needReset = false;
            }
        }
    }

    private void handleGameState() {
        mGameHandler.removeMessages(GAME_NEXT_MESSAGE);
        mGameHandler.sendEmptyMessageDelayed(GAME_NEXT_MESSAGE, MILLISEC_PER_FRAME - mElapsedTime);
    }

    /**
     * OnGameChangeListener, which called whenever game is advanced.
     */
    public static interface OnGameChangeListener {
        void onGameChanged(GameLogic logic);
    }

    /**
     * abstract class in order to encapsulate the logic of each game state.
     */
    private static abstract class GameState {

        public abstract void init();

        public abstract void draw(Canvas canvas);

        public abstract void next();

        public abstract boolean onBackPressed();
    }

    /**
     * game is stopped
     */
    private class StoppedState extends GameState {


        @Override
        public void init() {
            mGameDataInitialized = false;
        }

        @Override
        public void draw(Canvas canvas) {
            //draw world map
            drawGameMap(canvas);
            drawMainMenu(canvas);
        }

        @Override
        public void next() {
            //handle keyEvents.
            processMainMenuEvent();

            //advance to the next frame
            handleGameState();
        }

        @Override
        public boolean onBackPressed() {
            // step 3.5: TODO: when a game is on StoppedState,
            // if a back button is pressed, game should be terminated

            return false; //BackPressed event couldn't be consumed in GameLogic.
            //return true;
        }
    }

    /**
     * game is paused.
     */
    private class PausedState extends GameState {

        @Override
        public void init() {
            //IGNORED
        }

        @Override
        public void draw(Canvas canvas) {
            //draw background
            drawBackground(canvas);
            //draw asteroids
            drawCandies(canvas);
            //draw players
            drawPlayers(canvas);
            //draw world map
            drawGameMap(canvas);
            //drawUI
            drawUI(canvas);
            //drawPauseMenu
            drawPausedMenu(canvas);
        }

        @Override
        public void next() {
            //handle keyEvents.
            processPausedMenuEvent();

            //advance to the next frame
            handleGameState();
        }

        @Override
        public boolean onBackPressed() {
            // step 3.5: TODO: when a game is on PausedState,
            // if a back button is pressed, game should be stopped.

            return requestTransition(mStoppedState);
            //return true;
        }
    }

    /**
     * game is now playing.
     */
    private class PlayingState extends GameState {


        @Override
        public void init() {
            initGameData(mDisplayWidth, mDisplayHeight);
        }

        @Override
        public void draw(Canvas canvas) {
            //draw background
            drawBackground(canvas);
            //draw asteroids
            drawCandies(canvas);
            //draw players
            drawPlayers(canvas);
            //draw world map
            drawGameMap(canvas);
            //drawUI
            drawUI(canvas);
        }

        @Override
        public void next() {
            //handle player movements
            processPlayersMove();

            //handle asteroids movements
            processCandiesMove();

            //check collisions
            checkCollisions();

            //advance to the next frame
            handleGameState();
        }

        @Override
        public boolean onBackPressed() {
            // step 3.5: TODO: when a game is on PlayingState,
            // if a back button is pressed, game should be paused.

            return requestTransition(mPausedState);
            //return true;
        }
    }
}
