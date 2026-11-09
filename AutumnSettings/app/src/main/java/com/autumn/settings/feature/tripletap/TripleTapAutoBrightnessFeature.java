package com.autumn.settings.feature.tripletap;

import com.autumn.settings.core.Feature;
import com.autumn.settings.hook.BackTapActionHook;
import com.autumn.settings.hook.BackTapMenuHook;
import com.autumn.settings.hook.SecurityCoreBackTapHook;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 在系统「轻敲三下」下拉里增加「开关自动亮度」。
 * 选中后由系统框架在轻敲时切换自动亮度并震动。
 */
public final class TripleTapAutoBrightnessFeature implements Feature {
    public static final String ACTION = "toggle_auto_brightness";
    public static final String SETTING_KEY = "back_triple_tap";
    public static final String OPTION_LABEL = "开关自动亮度";

    @Override
    public String id() {
        return "triple-tap-auto-brightness";
    }

    @Override
    public void onLoad(XC_LoadPackage.LoadPackageParam lpparam) {
        BackTapMenuHook.install(lpparam);
        BackTapActionHook.install(lpparam);
        SecurityCoreBackTapHook.install(lpparam);
    }
}
