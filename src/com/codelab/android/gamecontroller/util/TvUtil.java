package com.codelab.android.gamecontroller.util;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;

public class TvUtil {

    private static final String TAG = "TvUtil";

    public static boolean isTv(Context context) {
        // step 1.6: TODO: Using UiModeManager, determine whether your app is running on a TV device
        UiModeManager uiModeManager
                = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            return true;
        } else {
            return false;
        }    }
}
