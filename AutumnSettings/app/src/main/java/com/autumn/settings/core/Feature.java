package com.autumn.settings.core;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 一项可独立安装的能力。新功能实现这个接口，再登记到 {@link FeatureRegistry}。
 */
public interface Feature {
    String id();

    void onLoad(XC_LoadPackage.LoadPackageParam lpparam);
}
