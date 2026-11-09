package com.autumn.settings.hook;

import android.content.Context;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 在设置首页第一张卡片末尾插入 AutumnSettings。
 * 点进去走设置自己的二级页，打开系统的轻敲设置页。
 */
public final class SettingsHomeHook {
    static final long HEADER_ID = 0xA5700001L;
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final String HEADER_CLASS =
            "com.android.settingslib.miuisettings.preference.PreferenceActivity$Header";
    private static final String BACK_TAP_FRAGMENT = "com.android.settings.BackTapSettingsFragment";

    private SettingsHomeHook() {
    }

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!SETTINGS_PACKAGE.equals(lpparam.packageName)) {
            return;
        }
        HookGuard.run("hook settings home", () -> {
            XposedHelpers.findAndHookMethod(
                    "com.android.settings.BasePreferenceFragment",
                    lpparam.classLoader,
                    "buildHeaders",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookGuard.run("insert home entry", () -> insertEntry(param));
                        }
                    });
            XposedHelpers.findAndHookMethod(
                    "com.android.settings.MiuiSettings",
                    lpparam.classLoader,
                    "onHeaderClick",
                    XposedHelpers.findClass(HEADER_CLASS, lpparam.classLoader),
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            Object header = param.args[0];
                            if (header == null || XposedHelpers.getLongField(header, "id") != HEADER_ID) {
                                return;
                            }
                            HookGuard.run("open back tap page", () ->
                                    openBackTapPage((Context) param.thisObject));
                            param.setResult(null);
                        }
                    });
        });
    }

    private static void openBackTapPage(Context context) throws Throwable {
        ClassLoader classLoader = context.getClassLoader();
        Class<?> launcherClass = XposedHelpers.findClass(
                "com.android.settings.core.SubSettingLauncher", classLoader);
        Object launcher = launcherClass.getConstructor(Context.class).newInstance(context);
        XposedHelpers.callMethod(launcher, "setDestination", BACK_TAP_FRAGMENT);
        XposedHelpers.callMethod(launcher, "setTitleText", "AutumnSettings");
        XposedHelpers.callMethod(launcher, "setSourceMetricsCategory", 0);
        XposedHelpers.callMethod(launcher, "setIsSecondLayerPage", true);
        XposedHelpers.callMethod(launcher, "launch");
    }

    private static void insertEntry(XC_MethodHook.MethodHookParam param) throws Throwable {
        Context context = (Context) XposedHelpers.callMethod(param.thisObject, "getContext");
        if (context == null) {
            return;
        }
        int headersRes = context.getResources().getIdentifier(
                "settings_headers", "xml", SETTINGS_PACKAGE);
        int currentRes = (int) XposedHelpers.callMethod(param.thisObject, "getHeadersResourceId");
        if (headersRes == 0 || currentRes != headersRes) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<Object> headers = (List<Object>) XposedHelpers.getObjectField(param.thisObject, "mHeaders");
        if (headers == null) {
            return;
        }
        for (Object header : headers) {
            if (XposedHelpers.getLongField(header, "id") == HEADER_ID) {
                return;
            }
        }
        int insertAt = 0;
        boolean foundGroup = false;
        for (int i = 0; i < headers.size(); i++) {
            if (XposedHelpers.getIntField(headers.get(i), "groupId") == 1) {
                insertAt = i + 1;
                foundGroup = true;
            }
        }
        if (!foundGroup) {
            insertAt = 0;
        }
        headers.add(insertAt, createHeader(param.thisObject.getClass().getClassLoader()));
        XposedBridge.log("AutumnSettings inserted into the first settings card");
    }

    private static Object createHeader(ClassLoader classLoader) throws Throwable {
        Class<?> headerClass = XposedHelpers.findClass(HEADER_CLASS, classLoader);
        var constructor = headerClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object header = constructor.newInstance();
        XposedHelpers.setLongField(header, "id", HEADER_ID);
        XposedHelpers.setIntField(header, "groupId", 1);
        XposedHelpers.setObjectField(header, "title", "AutumnSettings");
        XposedHelpers.setObjectField(header, "fragment", BACK_TAP_FRAGMENT);
        return header;
    }
}
