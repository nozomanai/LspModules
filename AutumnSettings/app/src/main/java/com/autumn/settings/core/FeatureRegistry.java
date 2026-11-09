package com.autumn.settings.core;

import com.autumn.settings.feature.tripletap.TripleTapAutoBrightnessFeature;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 功能登记处。以后加能力时，在这里多放一个实例，并在对应的系统设置页上接入。
 */
public final class FeatureRegistry {
    private static final List<Feature> FEATURES = Collections.unmodifiableList(Arrays.asList(
            new TripleTapAutoBrightnessFeature()
    ));

    private FeatureRegistry() {
    }

    public static List<Feature> all() {
        return FEATURES;
    }
}
