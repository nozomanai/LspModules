package com.autumn.settings.hook;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;

final class HookGuard {
    private static final String TAG = "AutumnSettings";

    private HookGuard() {
    }

    static void run(String where, Action action) {
        try {
            action.run();
        } catch (Throwable throwable) {
            Log.e(TAG, where, throwable);
            XposedBridge.log("AutumnSettings " + where);
            XposedBridge.log(throwable);
        }
    }

    interface Action {
        void run() throws Throwable;
    }
}
