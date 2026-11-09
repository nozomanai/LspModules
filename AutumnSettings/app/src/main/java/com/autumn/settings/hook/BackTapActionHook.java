package com.autumn.settings.hook;

import android.content.Context;
import android.os.Bundle;

import com.autumn.settings.feature.tripletap.AutoBrightness;
import com.autumn.settings.feature.tripletap.TripleTapAutoBrightnessFeature;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 系统框架收到轻敲后，遇到「开关自动亮度」就在这里执行。
 */
public final class BackTapActionHook {
    private BackTapActionHook() {
    }

    public static void install(XC_LoadPackage.LoadPackageParam lpparam) {
        String packageName = lpparam.packageName;
        if (!"android".equals(packageName) && !"system".equals(packageName)) {
            return;
        }
        HookGuard.run("hook back tap action", () -> XposedHelpers.findAndHookMethod(
                "com.miui.server.input.util.ShortCutActionsUtils",
                lpparam.classLoader,
                "triggerFunction",
                String.class,
                String.class,
                Bundle.class,
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String function = (String) param.args[0];
                        if (!TripleTapAutoBrightnessFeature.ACTION.equals(function)) {
                            return;
                        }
                        HookGuard.run("toggle auto brightness", () -> {
                            Context context = (Context) XposedHelpers.getObjectField(
                                    param.thisObject, "mContext");
                            AutoBrightness.toggle(context);
                            param.setResult(true);
                        });
                    }
                }));
    }
}
