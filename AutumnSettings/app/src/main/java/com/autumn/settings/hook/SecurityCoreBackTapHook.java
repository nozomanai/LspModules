package com.autumn.settings.hook;

import com.autumn.settings.feature.tripletap.TripleTapAutoBrightnessFeature;

import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 系统设置里能打开的「背部轻敲」在 com.miui.securitycore。
 * 它按动作表校验当前值，表里没有的就会写回「无」。
 */
public final class SecurityCoreBackTapHook {
    private static final String PACKAGE = "com.miui.securitycore";

    private SecurityCoreBackTapHook() {
    }

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!PACKAGE.equals(lpparam.packageName)) {
            return;
        }
        HookGuard.run("hook securitycore back tap", () -> XposedHelpers.findAndHookMethod(
                "y4.t",
                lpparam.classLoader,
                "h",
                int.class,
                String[].class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        String[] keys = (String[]) param.args[1];
                        if (!containsTripleTap(keys) || !(param.getResult() instanceof Map)) {
                            return;
                        }
                        @SuppressWarnings("unchecked")
                        Map<Object, Object> actions = (Map<Object, Object>) param.getResult();
                        actions.put(
                                TripleTapAutoBrightnessFeature.ACTION,
                                TripleTapAutoBrightnessFeature.OPTION_LABEL);
                    }
                }));
    }

    private static boolean containsTripleTap(String[] keys) {
        if (keys == null) {
            return false;
        }
        for (String key : keys) {
            if (TripleTapAutoBrightnessFeature.SETTING_KEY.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
