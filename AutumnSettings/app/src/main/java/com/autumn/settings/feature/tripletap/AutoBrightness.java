package com.autumn.settings.feature.tripletap;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.VibratorManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.autumn.settings.util.HiddenApis;

/**
 * 当前是自动亮度就关掉，否则打开，然后震一下并提示当前状态。
 */
public final class AutoBrightness {
    private static final String TAG = "AutumnSettings";
    private static final long VIBRATION_MS = 50L;
    private static final String TIP_ON = "自动亮度已开启";
    private static final String TIP_OFF = "自动亮度已关闭";

    private AutoBrightness() {
    }

    public static void toggle(Context context) {
        int userId = HiddenApis.currentUserId();
        int mode = HiddenApis.getSystemIntForUser(
                context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                userId);
        int next = mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                ? Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                : Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        boolean wrote = HiddenApis.putSystemIntForUser(
                context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                next,
                userId);
        Log.i(TAG, "auto brightness " + mode + " -> " + next + ", wrote=" + wrote);
        boolean enabled = next == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        vibrate(context);
        showTip(context, enabled ? TIP_ON : TIP_OFF);
    }

    private static void showTip(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } catch (Throwable throwable) {
                Log.w(TAG, "tip failed", throwable);
            }
        });
    }

    private static void vibrate(Context context) {
        try {
            var vibrator = context.getSystemService(VibratorManager.class).getDefaultVibrator();
            if (vibrator == null || !vibrator.hasVibrator()) {
                return;
            }
            vibrator.vibrate(VibrationEffect.createOneShot(
                    VIBRATION_MS,
                    VibrationEffect.DEFAULT_AMPLITUDE));
        } catch (Throwable throwable) {
            Log.w(TAG, "vibrate failed", throwable);
        }
    }
}
