package com.autumn.settings.hook;

import android.content.Context;
import android.provider.Settings;

import com.autumn.settings.feature.tripletap.TripleTapAutoBrightnessFeature;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 把「开关自动亮度」登记进系统的轻敲动作列表，并让快捷手势页能显示这个名字。
 * 不登记的话，快捷手势页查不到文案，会把它当成「无」。
 */
public final class BackTapMenuHook {
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final String TRIPLE_TAP_KEY = TripleTapAutoBrightnessFeature.SETTING_KEY;

    private BackTapMenuHook() {
    }

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!SETTINGS_PACKAGE.equals(lpparam.packageName)) {
            return;
        }
        HookGuard.run("hook back tap menu", () -> {
            XposedHelpers.findAndHookMethod(
                    "com.android.settings.BackTapSettingsFragment",
                    lpparam.classLoader,
                    "onCreate",
                    android.os.Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookGuard.run("append triple tap option", () -> appendOption(param.thisObject));
                        }
                    });
            XposedHelpers.findAndHookMethod(
                    "com.android.settings.BackTapSettingsFragment",
                    lpparam.classLoader,
                    "updateState",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            HookGuard.run("append triple tap option", () -> appendOption(param.thisObject));
                        }
                    });
            XposedHelpers.findAndHookMethod(
                    "com.android.settings.MiuiShortcut$Key",
                    lpparam.classLoader,
                    "initFunctions",
                    Context.class,
                    java.util.ArrayList.class,
                    String[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            appendKnownAction((String[]) param.args[2], param.args[1],
                                    TripleTapAutoBrightnessFeature.OPTION_LABEL);
                        }
                    });
            XposedHelpers.findAndHookMethod(
                    "com.android.settings.MiuiShortcut$Key",
                    lpparam.classLoader,
                    "initFunctionValues",
                    java.util.ArrayList.class,
                    String[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            appendKnownAction((String[]) param.args[1], param.args[0],
                                    TripleTapAutoBrightnessFeature.ACTION);
                        }
                    });
            XposedHelpers.findAndHookMethod(
                    "com.android.settings.MiuiShortcut$Key",
                    lpparam.classLoader,
                    "getResourceForKey",
                    String.class,
                    Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            if (TripleTapAutoBrightnessFeature.ACTION.equals(param.args[0])) {
                                param.setResult(TripleTapAutoBrightnessFeature.OPTION_LABEL);
                            }
                        }
                    });
            XC_MethodHook refresh = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    HookGuard.run("refresh back tap label", () -> refreshBackTapLabel(param.thisObject));
                }
            };
            XposedHelpers.findAndHookMethod(
                    "com.android.settings.GestureShortcutSettingsFragment",
                    lpparam.classLoader,
                    "onResume",
                    refresh);
            XposedHelpers.findAndHookMethod(
                    "com.android.settings.KeySettings",
                    lpparam.classLoader,
                    "onResume",
                    refresh);
        });
    }

    private static void appendKnownAction(String[] keys, Object rawList, String item) {
        if (!(rawList instanceof List) || keys == null) {
            return;
        }
        boolean backTapPage = false;
        for (String key : keys) {
            if (TRIPLE_TAP_KEY.equals(key)) {
                backTapPage = true;
                break;
            }
        }
        if (!backTapPage) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) rawList;
        if (!list.contains(item)) {
            list.add(item);
        }
    }

    private static void refreshBackTapLabel(Object fragment) {
        Context context = (Context) XposedHelpers.callMethod(fragment, "getContext");
        if (context == null) {
            return;
        }
        String doubleTap = Settings.System.getString(context.getContentResolver(), "back_double_tap");
        String tripleTap = Settings.System.getString(context.getContentResolver(), TRIPLE_TAP_KEY);
        if (!TripleTapAutoBrightnessFeature.ACTION.equals(doubleTap)
                && !TripleTapAutoBrightnessFeature.ACTION.equals(tripleTap)) {
            return;
        }
        Object preference = XposedHelpers.callMethod(fragment, "findPreference", "back_tap");
        if (preference == null) {
            return;
        }
        StringBuilder summary = new StringBuilder();
        appendAction(summary, context, doubleTap);
        appendAction(summary, context, tripleTap);
        if (summary.length() == 0) {
            return;
        }
        XposedHelpers.callMethod(preference, "setValue", summary.toString());
    }

    private static void appendAction(StringBuilder summary, Context context, String action) {
        if (action == null || action.isEmpty() || "none".equals(action) || "key_none".equals(action)) {
            return;
        }
        if (summary.length() > 0) {
            summary.append('/');
        }
        summary.append(actionLabel(context, action));
    }

    private static String actionLabel(Context context, String action) {
        if (action == null || action.isEmpty() || "none".equals(action) || "key_none".equals(action)) {
            return "无";
        }
        if (TripleTapAutoBrightnessFeature.ACTION.equals(action)) {
            return TripleTapAutoBrightnessFeature.OPTION_LABEL;
        }
        try {
            String label = (String) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("com.android.settings.MiuiShortcut$Key", context.getClassLoader()),
                    "getResourceForKey",
                    action,
                    context);
            if (label != null && !label.isEmpty()) {
                return label;
            }
        } catch (Throwable ignored) {
            // Fall back to the raw action id.
        }
        return action;
    }

    private static void appendOption(Object fragment) throws Throwable {
        Object preference = XposedHelpers.callMethod(fragment, "findPreference", TRIPLE_TAP_KEY);
        if (preference == null) {
            XposedBridge.log("AutumnSettings: back_triple_tap preference missing");
            return;
        }
        Method getEntries = findMethod(preference.getClass(), "getEntries");
        Method getEntryValues = findMethod(preference.getClass(), "getEntryValues");
        Method setEntries = findMethod(preference.getClass(), "setEntries", CharSequence[].class);
        Method setEntryValues = findMethod(preference.getClass(), "setEntryValues", CharSequence[].class);
        if (getEntries == null || getEntryValues == null || setEntries == null || setEntryValues == null) {
            XposedBridge.log("AutumnSettings: dropdown methods missing on " + preference.getClass().getName());
            return;
        }
        CharSequence[] entries = (CharSequence[]) getEntries.invoke(preference);
        CharSequence[] values = (CharSequence[]) getEntryValues.invoke(preference);
        if (entries == null || values == null || entries.length != values.length) {
            return;
        }
        for (CharSequence value : values) {
            if (TripleTapAutoBrightnessFeature.ACTION.contentEquals(value)) {
                return;
            }
        }
        int insertAt = values.length;
        for (int i = 0; i < values.length; i++) {
            if ("none".contentEquals(values[i])) {
                insertAt = i;
                break;
            }
        }
        CharSequence[] newEntries = Arrays.copyOf(entries, entries.length + 1);
        CharSequence[] newValues = Arrays.copyOf(values, values.length + 1);
        for (int i = newValues.length - 1; i > insertAt; i--) {
            newEntries[i] = newEntries[i - 1];
            newValues[i] = newValues[i - 1];
        }
        newEntries[insertAt] = TripleTapAutoBrightnessFeature.OPTION_LABEL;
        newValues[insertAt] = TripleTapAutoBrightnessFeature.ACTION;
        setEntries.invoke(preference, (Object) newEntries);
        setEntryValues.invoke(preference, (Object) newValues);
        XposedBridge.log("AutumnSettings: added toggle auto brightness to triple tap");
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... params) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Method method = current.getDeclaredMethod(name, params);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                // Keep walking. DropDownPreference inherits these from ListPreference.
            }
        }
        return null;
    }
}
