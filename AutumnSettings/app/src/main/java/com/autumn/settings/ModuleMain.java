package com.autumn.settings;

import com.autumn.settings.core.Feature;
import com.autumn.settings.core.FeatureRegistry;
import com.autumn.settings.hook.SettingsHomeHook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class ModuleMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        SettingsHomeHook.install(lpparam);
        for (Feature feature : FeatureRegistry.all()) {
            feature.onLoad(lpparam);
        }
    }
}
