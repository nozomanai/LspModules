package com.autumn.settings.util;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.provider.Settings;

import java.lang.reflect.Method;

/**
 * 这些接口在系统进程里存在，但没有出现在公开 SDK 里。
 */
public final class HiddenApis {
    private HiddenApis() {
    }

    public static int currentUserId() {
        try {
            Method method = ActivityManager.class.getMethod("getCurrentUser");
            return (int) method.invoke(null);
        } catch (Throwable throwable) {
            return 0;
        }
    }

    public static int getSystemIntForUser(ContentResolver resolver, String key, int defaultValue, int userId) {
        try {
            Method method = Settings.System.class.getMethod(
                    "getIntForUser",
                    ContentResolver.class,
                    String.class,
                    int.class,
                    int.class);
            return (int) method.invoke(null, resolver, key, defaultValue, userId);
        } catch (Throwable throwable) {
            return Settings.System.getInt(resolver, key, defaultValue);
        }
    }

    public static boolean putSystemIntForUser(ContentResolver resolver, String key, int value, int userId) {
        try {
            Method method = Settings.System.class.getMethod(
                    "putIntForUser",
                    ContentResolver.class,
                    String.class,
                    int.class,
                    int.class);
            return Boolean.TRUE.equals(method.invoke(null, resolver, key, value, userId));
        } catch (Throwable throwable) {
            return Settings.System.putInt(resolver, key, value);
        }
    }

    public static boolean putSystemStringForUser(ContentResolver resolver, String key, String value, int userId) {
        try {
            Method method = Settings.System.class.getMethod(
                    "putStringForUser",
                    ContentResolver.class,
                    String.class,
                    String.class,
                    int.class);
            return Boolean.TRUE.equals(method.invoke(null, resolver, key, value, userId));
        } catch (Throwable throwable) {
            return Settings.System.putString(resolver, key, value);
        }
    }
}
